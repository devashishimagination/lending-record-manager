package com.devashish.lending.service;

import com.devashish.lending.model.*;
import com.devashish.lending.repository.AccountRepository;
import com.devashish.lending.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InterestService {

    public record Statement(Long accountId, LocalDate asOf, BigDecimal principalBalance,
                            BigDecimal interest, BigDecimal lateCharge, BigDecimal totalDue) { }

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;

    public InterestService(AccountRepository accounts, LedgerEntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    public Statement statement(Long accountId, LocalDate asOf, BigDecimal rate, BigDecimal lateRate) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        List<LedgerEntry> list = entries.findByAccountIdOrderByEntryDateAsc(accountId);

        BigDecimal balance = balanceOn(account, list, asOf);
        BigDecimal interest = interest(account, list, asOf, rate);
        BigDecimal late = lateCharge(account, balance, asOf, lateRate);
        return new Statement(accountId, asOf, balance, interest, late, balance.add(interest).add(late));
    }

    private BigDecimal balanceOn(Account account, List<LedgerEntry> list, LocalDate date) {
        BigDecimal balance = account.getPrincipal();
        for (LedgerEntry e : list) {
            if (e.getEntryDate().isAfter(date)) {
                continue;
            }
            balance = (e.getType() == EntryType.ADDITION)
                    ? balance.add(e.getAmount()) : balance.subtract(e.getAmount());
        }
        return balance;
    }

    private BigDecimal interest(Account account, List<LedgerEntry> list,
                                LocalDate asOf, BigDecimal rateOverride) {
        BigDecimal rate = rateOverride != null
                ? rateOverride
                : (account.isInterestEnabled() ? account.getMonthlyRatePercent() : null);
        if (rate == null) {
            return BigDecimal.ZERO;
        }
        InterestMode mode = account.getInterestMode() != null
                ? account.getInterestMode() : InterestMode.MONTHLY;
        LocalDate start = account.getStartDate();
        long periods = (mode == InterestMode.DAILY)
                ? ChronoUnit.DAYS.between(start, asOf)
                : ChronoUnit.MONTHS.between(start, asOf);

        BigDecimal total = BigDecimal.ZERO;
        for (int p = 0; p < periods; p++) {
            LocalDate periodStart = (mode == InterestMode.DAILY) ? start.plusDays(p) : start.plusMonths(p);
            total = total.add(balanceOn(account, list, periodStart).multiply(rate).divide(HUNDRED));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal lateCharge(Account account, BigDecimal balance,
                                  LocalDate asOf, BigDecimal lateRateOverride) {
        BigDecimal lateRate = lateRateOverride != null ? lateRateOverride : account.getLateRatePercent();
        if (account.getDueDate() == null || lateRate == null) {
            return BigDecimal.ZERO;
        }
        LateMode mode = account.getLateMode() != null ? account.getLateMode() : LateMode.MONTHLY;
        LocalDate lateStart = account.getDueDate().plusMonths(account.getGraceMonths());
        if (!asOf.isAfter(lateStart)) {
            return BigDecimal.ZERO;
        }
        long periods = (mode == LateMode.DAILY)
                ? ChronoUnit.DAYS.between(lateStart, asOf)
                : ChronoUnit.MONTHS.between(lateStart, asOf);

        return balance.multiply(lateRate).multiply(BigDecimal.valueOf(periods))
                .divide(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
}