package com.devashish.lending.controller;

import com.devashish.lending.model.Account;
import com.devashish.lending.repository.AccountRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository repo;

    public AccountController(AccountRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Account> byPerson(@RequestParam Long personId) {
        return repo.findByPersonId(personId);
    }

    @PostMapping
    public Account add(@Valid @RequestBody Account account) {
        return repo.save(account);
    }
}