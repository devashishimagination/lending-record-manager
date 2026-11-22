package com.devashish.lending.controller;

import com.devashish.lending.service.SettlementService;
import org.springframework.web.bind.annotation.*;

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