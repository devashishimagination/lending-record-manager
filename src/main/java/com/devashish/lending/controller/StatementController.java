package com.devashish.lending.controller;

import com.devashish.lending.model.InterestMode;
import com.devashish.lending.model.LateMode;
import com.devashish.lending.service.InterestService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statement")
public class StatementController {

    private final InterestService service;

    public StatementController(InterestService service) {
        this.service = service;
    }

    @GetMapping
    public InterestService.Statement get(
            @RequestParam Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(required = false) BigDecimal rate,
            @RequestParam(required = false) BigDecimal lateRate,
            @RequestParam(required = false) InterestMode interestMode,
            @RequestParam(required = false) LateMode lateMode) {
        return service.statement(accountId, asOf != null ? asOf : LocalDate.now(),
                rate, lateRate, interestMode, lateMode);
    }
}