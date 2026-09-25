/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "investor_info")
public class InvestorInfoEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "address")
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_cas_details_id")
    private UserCASDetailsEntity userCasDetailsEntity;

    public Long getId() {
        return id;
    }

    public InvestorInfoEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public InvestorInfoEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public InvestorInfoEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public InvestorInfoEntity setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public InvestorInfoEntity setAddress(String address) {
        this.address = address;
        return this;
    }

    public UserCASDetailsEntity getUserCasDetailsEntity() {
        return userCasDetailsEntity;
    }

    public InvestorInfoEntity setUserCasDetailsEntity(UserCASDetailsEntity userCasDetailsEntity) {
        this.userCasDetailsEntity = userCasDetailsEntity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        InvestorInfoEntity that = (InvestorInfoEntity) o;
        return id != null && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
