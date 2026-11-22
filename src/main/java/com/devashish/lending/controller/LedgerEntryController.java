package com.devashish.lending.controller;

import com.devashish.lending.model.LedgerEntry;
import com.devashish.lending.repository.LedgerEntryRepository;
import com.devashish.lending.service.BalanceService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entries")
public class LedgerEntryController {

    private final LedgerEntryRepository repo;
    private final BalanceService balanceService;

    public LedgerEntryController(LedgerEntryRepository repo, BalanceService balanceService) {
        this.repo = repo;
        this.balanceService = balanceService;
    }

    @GetMapping
    public List<LedgerEntry> byAccount(@RequestParam Long accountId) {
        return repo.findByAccountIdOrderByEntryDateAsc(accountId);
    }

    @PostMapping
    public LedgerEntry add(@Valid @RequestBody LedgerEntry entry) {
        return repo.save(entry);
    }

    @GetMapping("/balance")
    public BigDecimal balance(@RequestParam Long accountId) {
        return balanceService.balance(accountId);
    }
}