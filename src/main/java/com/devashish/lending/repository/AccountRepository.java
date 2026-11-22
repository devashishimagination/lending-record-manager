package com.devashish.lending.repository;

import com.devashish.lending.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByPersonId(Long personId);
}