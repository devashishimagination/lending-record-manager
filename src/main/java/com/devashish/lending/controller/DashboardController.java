package com.devashish.lending.controller;

import com.devashish.lending.service.DashboardService;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardService.Dashboard get() {
        return service.build(LocalDate.now());
    }
}