package com.devashish.lending.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

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
    @Column(precision = 19, scale = 2)
    private BigDecimal principal;

    @NotNull
    private LocalDate startDate;

    private LocalDate dueDate;                    // optional

    private boolean interestEnabled;

    @Column(precision = 10, scale = 4)
    private BigDecimal monthlyRatePercent;        // percent per period (month or day)

    @Enumerated(EnumType.STRING)
    private InterestMode interestMode;

    private int graceMonths;

    @Enumerated(EnumType.STRING)
    private LateMode lateMode;

    @Column(precision = 10, scale = 4)
    private BigDecimal lateRatePercent;

    @Enumerated(EnumType.STRING)
    private InterestPlan interestPlan = InterestPlan.DECIDE_LATER;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.OPEN;

    private String guarantorName;                 // optional
    private String notes;

    public Account() { }

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

	public InterestMode getInterestMode() {
		return interestMode;
	}

	public void setInterestMode(InterestMode interestMode) {
		this.interestMode = interestMode;
	}

	public int getGraceMonths() {
		return graceMonths;
	}

	public void setGraceMonths(int graceMonths) {
		this.graceMonths = graceMonths;
	}

	public LateMode getLateMode() {
		return lateMode;
	}

	public void setLateMode(LateMode lateMode) {
		this.lateMode = lateMode;
	}

	public BigDecimal getLateRatePercent() {
		return lateRatePercent;
	}

	public void setLateRatePercent(BigDecimal lateRatePercent) {
		this.lateRatePercent = lateRatePercent;
	}

	public InterestPlan getInterestPlan() {
		return interestPlan;
	}

	public void setInterestPlan(InterestPlan interestPlan) {
		this.interestPlan = interestPlan;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
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
}