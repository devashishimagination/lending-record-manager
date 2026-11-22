package com.devashish.lending.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devashish.lending.model.Person;
import com.devashish.lending.repository.PersonRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonRepository repo;

    public PersonController(PersonRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Person> all() {
        return repo.findAll();
    }

    @PostMapping
    public Person add(@Valid @RequestBody Person person) {
        return repo.save(person);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}