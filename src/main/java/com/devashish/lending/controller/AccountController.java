package com.devashish.lending.controller;

import com.devashish.lending.model.Account;
import com.devashish.lending.repository.AccountRepository;
import com.devashish.lending.repository.LedgerEntryRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository repo;
    private final LedgerEntryRepository entries;

    public AccountController(AccountRepository repo, LedgerEntryRepository entries) {
        this.repo = repo;
        this.entries = entries;
    }

    @GetMapping
    public List<Account> byPerson(@RequestParam Long personId) {
        return repo.findByPersonId(personId);
    }

    @PostMapping
    public Account add(@Valid @RequestBody Account account) {
        return repo.save(account);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        entries.deleteAll(entries.findByAccountIdOrderByEntryDateAsc(id));  // diary lines first
        repo.deleteById(id);
    }
}