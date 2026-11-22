package com.devashish.lending.repository;

import com.devashish.lending.model.LedgerEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByAccountIdOrderByEntryDateAsc(Long accountId);
}