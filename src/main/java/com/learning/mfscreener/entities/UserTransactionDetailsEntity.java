/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "user_transaction_details")
public class UserTransactionDetailsEntity extends AuditableEntity<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private LocalDate transactionDate;
    private String description;
    private Double amount;
    private Double units;
    private Double nav;
    private Double balance;
    private String type;
    private String dividendRate;

    @ManyToOne
    @JoinColumn(name = "user_scheme_detail_id")
    private UserSchemeDetailsEntity userSchemeDetailsEntity;

    public Long getId() {
        return id;
    }

    public UserTransactionDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public UserTransactionDetailsEntity setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UserTransactionDetailsEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public UserTransactionDetailsEntity setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Double getUnits() {
        return units;
    }

    public UserTransactionDetailsEntity setUnits(Double units) {
        this.units = units;
        return this;
    }

    public Double getNav() {
        return nav;
    }

    public UserTransactionDetailsEntity setNav(Double nav) {
        this.nav = nav;
        return this;
    }

    public Double getBalance() {
        return balance;
    }

    public UserTransactionDetailsEntity setBalance(Double balance) {
        this.balance = balance;
        return this;
    }

    public String getType() {
        return type;
    }

    public UserTransactionDetailsEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getDividendRate() {
        return dividendRate;
    }

    public UserTransactionDetailsEntity setDividendRate(String dividendRate) {
        this.dividendRate = dividendRate;
        return this;
    }

    public UserSchemeDetailsEntity getUserSchemeDetailsEntity() {
        return userSchemeDetailsEntity;
    }

    public UserTransactionDetailsEntity setUserSchemeDetailsEntity(UserSchemeDetailsEntity userSchemeDetailsEntity) {
        this.userSchemeDetailsEntity = userSchemeDetailsEntity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserTransactionDetailsEntity that = (UserTransactionDetailsEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
