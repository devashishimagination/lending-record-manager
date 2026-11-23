package com.devashish.lending.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devashish.lending.model.Account;
import com.devashish.lending.model.AccountType;
import com.devashish.lending.model.Person;
import com.devashish.lending.repository.AccountRepository;
import com.devashish.lending.repository.PersonRepository;
import com.devashish.lending.service.BalanceService;

@RestController
@RequestMapping("/api/people")
public class PeopleSummaryController {

    public record Summary(Long id, String name, String phone,
                          BigDecimal toReceive, BigDecimal toPay, BigDecimal kept, int openAccounts) { }

    private final PersonRepository people;
    private final AccountRepository accounts;
    private final BalanceService balances;

    public PeopleSummaryController(PersonRepository people, AccountRepository accounts,
                                   BalanceService balances) {
        this.people = people;
        this.accounts = accounts;
        this.balances = balances;
    }

    @GetMapping("/summary")
    public List<Summary> summary() {
        List<Summary> result = new ArrayList<>();
        for (Person p : people.findAll()) {
            BigDecimal receive = BigDecimal.ZERO;
            BigDecimal pay = BigDecimal.ZERO;
            BigDecimal kept = BigDecimal.ZERO;
            int open = 0;
            for (Account a : accounts.findByPersonId(p.getId())) {
                BigDecimal bal = balances.balance(a.getId());
                if (bal.signum() <= 0) {
                    continue;                                  // settled
                }
                open++;
                if (a.getType() == AccountType.GIVEN) {
                    receive = receive.add(bal);
                } else if (a.getType() == AccountType.TAKEN) {
                    pay = pay.add(bal);
                } else {
                    kept = kept.add(bal);
                }
            }
            result.add(new Summary(p.getId(), p.getName(), p.getPhone(), receive, pay, kept, open));
        }
        return result;
    }
}