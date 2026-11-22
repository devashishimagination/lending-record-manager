package com.devashish.lending.controller;

import com.devashish.lending.model.Account;
import com.devashish.lending.model.LedgerEntry;
import com.devashish.lending.model.Person;
import com.devashish.lending.repository.AccountRepository;
import com.devashish.lending.repository.LedgerEntryRepository;
import com.devashish.lending.repository.PersonRepository;
import com.devashish.lending.service.BalanceService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/people")
public class HistoryController {

    public record AccountHistory(Account account, BigDecimal balance, List<LedgerEntry> entries) { }
    public record PersonHistory(Person person, List<AccountHistory> accounts) { }

    private final PersonRepository people;
    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;
    private final BalanceService balances;

    public HistoryController(PersonRepository people, AccountRepository accounts,
                             LedgerEntryRepository entries, BalanceService balances) {
        this.people = people;
        this.accounts = accounts;
        this.entries = entries;
        this.balances = balances;
    }

    @GetMapping("/{id}/history")
    public PersonHistory history(@PathVariable Long id) {
        Person person = people.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));
        List<AccountHistory> list = accounts.findByPersonId(id).stream()
                .map(a -> new AccountHistory(a, balances.balance(a.getId()),
                        entries.findByAccountIdOrderByEntryDateAsc(a.getId())))
                .toList();
        return new PersonHistory(person, list);
    }
}