package com.devashish.lending.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    private Person person;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @NotNull
    @Positive
    private BigDecimal principal;

    @NotNull
    private LocalDate startDate;

    private LocalDate dueDate;

    private boolean interestEnabled;
    private BigDecimal monthlyRatePercent;
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public boolean isInterestEnabled() {
		return interestEnabled;
	}

	public void setInterestEnabled(boolean interestEnabled) {
		this.interestEnabled = interestEnabled;
	}

	public BigDecimal getMonthlyRatePercent() {
		return monthlyRatePercent;
	}

	public void setMonthlyRatePercent(BigDecimal monthlyRatePercent) {
		this.monthlyRatePercent = monthlyRatePercent;
	}

	public int getGraceMonths() {
		return graceMonths;
	}

	public void setGraceMonths(int graceMonths) {
		this.graceMonths = graceMonths;
	}

	public String getGuarantorName() {
		return guarantorName;
	}

	public void setGuarantorName(String guarantorName) {
		this.guarantorName = guarantorName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	private int graceMonths;

    private String guarantorName;
    private String notes;

    public Account() { }

    // generate getters and setters (Source > Generate Getters and Setters > Select All)
}