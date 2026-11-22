package com.devashish.lending.service;

import com.devashish.lending.model.Account;
import com.devashish.lending.model.EntryType;
import com.devashish.lending.model.LedgerEntry;
import com.devashish.lending.repository.AccountRepository;
import com.devashish.lending.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BalanceService {

    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;

    public BalanceService(AccountRepository accounts, LedgerEntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    public BigDecimal balance(Long accountId) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        BigDecimal balance = account.getPrincipal();
        for (LedgerEntry e : entries.findByAccountIdOrderByEntryDateAsc(accountId)) {
            if (e.getType() == EntryType.ADDITION) {
                balance = balance.add(e.getAmount());
            } else {
                balance = balance.subtract(e.getAmount());
            }
        }
        return balance;
    }
}