package com.devashish.lending.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devashish.lending.service.SettlementService;

@RestController
@RequestMapping("/api/accounts")
public class SettlementController {

    private final SettlementService service;

    public SettlementController(SettlementService service) {
        this.service = service;
    }

    @PostMapping("/{id}/settle")
    public SettlementService.SettleResult settle(@PathVariable Long id,
                                                 @RequestBody SettlementService.SettleRequest req) {
        return service.settle(id, req);
    }
}