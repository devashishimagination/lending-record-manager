package com.devashish.lending.service;

import com.devashish.lending.model.*;
import com.devashish.lending.repository.AccountRepository;
import com.devashish.lending.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SettlementService {

    public record SettleRequest(LocalDate date, BigDecimal amountPaid, boolean skipInterest,
                                BigDecimal ratePercent, InterestMode interestMode,
                                BigDecimal lateRatePercent, LateMode lateMode) { }

    public record SettleResult(BigDecimal chargeAdded, BigDecimal amountPaid,
                               BigDecimal remainingBalance, AccountStatus status) { }

    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;
    private final InterestService interestService;
    private final BalanceService balanceService;

    public SettlementService(AccountRepository accounts, LedgerEntryRepository entries,
                             InterestService interestService, BalanceService balanceService) {
        this.accounts = accounts;
        this.entries = entries;
        this.interestService = interestService;
        this.balanceService = balanceService;
    }

    public SettleResult settle(Long accountId, SettleRequest req) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        LocalDate date = req.date() != null ? req.date() : LocalDate.now();

        BigDecimal charge = BigDecimal.ZERO;
        if (!req.skipInterest()) {
            if (req.ratePercent() != null) {                    // rate decided now
                account.setInterestEnabled(true);
                account.setMonthlyRatePercent(req.ratePercent());
                account.setInterestMode(req.interestMode());
                account.setLateRatePercent(req.lateRatePercent());
                account.setLateMode(req.lateMode());
                accounts.save(account);
            }
            InterestService.Statement st = interestService.statement(accountId, date, null, null);
            charge = st.interest().add(st.lateCharge());
        }

        if (charge.signum() > 0) {                              // book the agreed charge in the diary
            LedgerEntry e = new LedgerEntry();
            e.setAccount(account);
            e.setType(EntryType.ADDITION);
            e.setAmount(charge);
            e.setEntryDate(date);
            e.setNote("Interest and late charge agreed");
            entries.save(e);
        }
        // the interest is now written in the diary, so stop calculating it again
        account.setInterestEnabled(false);
        account.setInterestPlan(InterestPlan.NONE);
        account.setLateRatePercent(null);

        BigDecimal paid = BigDecimal.ZERO;
        if (req.amountPaid() != null && req.amountPaid().signum() > 0) {
            LedgerEntry p = new LedgerEntry();
            p.setAccount(account);
            p.setType(EntryType.REDUCTION);
            p.setAmount(req.amountPaid());
            p.setEntryDate(date);
            p.setNote("Payment received");
            entries.save(p);
            paid = req.amountPaid();
        }

        BigDecimal remaining = balanceService.balance(accountId);
        account.setStatus(remaining.signum() <= 0 ? AccountStatus.SETTLED : AccountStatus.OPEN);
        accounts.save(account);

        return new SettleResult(charge, paid, remaining, account.getStatus());
    }
}