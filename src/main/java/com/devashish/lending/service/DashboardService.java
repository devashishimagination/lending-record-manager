package com.devashish.lending.service;

import com.devashish.lending.model.Account;
import com.devashish.lending.model.AccountType;
import com.devashish.lending.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public record Row(Long accountId, Long personId, String personName, String phone,
                      LocalDate startDate, LocalDate dueDate, BigDecimal balance) { }

    public record Dashboard(BigDecimal totalGiven, BigDecimal totalTaken, BigDecimal totalKept,
                            List<Row> overdue, List<Row> toCall) { }

    private final AccountRepository accounts;
    private final BalanceService balances;

    public DashboardService(AccountRepository accounts, BalanceService balances) {
        this.accounts = accounts;
        this.balances = balances;
    }

    public Dashboard build(LocalDate today) {
        BigDecimal given = BigDecimal.ZERO;
        BigDecimal taken = BigDecimal.ZERO;
        BigDecimal kept = BigDecimal.ZERO;
        List<Row> overdue = new ArrayList<>();
        List<Row> toCall = new ArrayList<>();

        for (Account a : accounts.findAll()) {
            BigDecimal bal = balances.balance(a.getId());
            if (bal.signum() <= 0) {
                continue;   // fully settled
            }
            if (a.getType() == AccountType.GIVEN) {
                given = given.add(bal);
            } else if (a.getType() == AccountType.TAKEN) {
                taken = taken.add(bal);
            } else {
                kept = kept.add(bal);
            }

            if (a.getType() == AccountType.GIVEN) {
                Row row = new Row(a.getId(), a.getPerson().getId(), a.getPerson().getName(),
                        a.getPerson().getPhone(), a.getStartDate(), a.getDueDate(), bal);
                if (a.getDueDate() != null && a.getDueDate().isBefore(today)) {
                    overdue.add(row);                         // told date has passed
                } else if (a.getDueDate() == null && a.getStartDate().plusMonths(1).isBefore(today)) {
                    toCall.add(row);                          // no date told, over a month
                }
            }
        }
        return new Dashboard(given, taken, kept, overdue, toCall);
    }
}