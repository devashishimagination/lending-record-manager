package com.devashish.lending.controller;

import jakarta.persistence.EntityManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final EntityManager em;

    public HealthController(EntityManager em) {
        this.em = em;
    }

    @GetMapping("/health")
    public String health() {
        em.createNativeQuery("select 1").getSingleResult();
        return "ok";
    }
}