/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(
        name = "user_scheme_details",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_userschemedetailsentity",
                    columnNames = {"isin", "user_folio_id"})
        })
public class UserSchemeDetailsEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "scheme")
    private String scheme;

    @Column(name = "isin")
    private String isin;

    private String advisor;

    private String rtaCode;

    private String rta;

    private String type;

    private Long amfi;

    @Column(name = "open")
    private String myopen;

    private String close;

    @Column(name = "close_calculated")
    private String closeCalculated;

    @Version
    private Short version;

    @ManyToOne
    @JoinColumn(name = "user_folio_id")
    private UserFolioDetailsEntity userFolioDetailsEntity;

    @OneToMany(mappedBy = "userSchemeDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTransactionDetailsEntity> transactionEntities = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public UserSchemeDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public UserSchemeDetailsEntity setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getIsin() {
        return isin;
    }

    public UserSchemeDetailsEntity setIsin(String isin) {
        this.isin = isin;
        return this;
    }

    public String getAdvisor() {
        return advisor;
    }

    public UserSchemeDetailsEntity setAdvisor(String advisor) {
        this.advisor = advisor;
        return this;
    }

    public String getRtaCode() {
        return rtaCode;
    }

    public UserSchemeDetailsEntity setRtaCode(String rtaCode) {
        this.rtaCode = rtaCode;
        return this;
    }

    public String getRta() {
        return rta;
    }

    public UserSchemeDetailsEntity setRta(String rta) {
        this.rta = rta;
        return this;
    }

    public String getType() {
        return type;
    }

    public UserSchemeDetailsEntity setType(String type) {
        this.type = type;
        return this;
    }

    public Long getAmfi() {
        return amfi;
    }

    public UserSchemeDetailsEntity setAmfi(Long amfi) {
        this.amfi = amfi;
        return this;
    }

    public String getMyopen() {
        return myopen;
    }

    public UserSchemeDetailsEntity setMyopen(String myopen) {
        this.myopen = myopen;
        return this;
    }

    public String getClose() {
        return close;
    }

    public UserSchemeDetailsEntity setClose(String close) {
        this.close = close;
        return this;
    }

    public String getCloseCalculated() {
        return closeCalculated;
    }

    public UserSchemeDetailsEntity setCloseCalculated(String closeCalculated) {
        this.closeCalculated = closeCalculated;
        return this;
    }

    public UserFolioDetailsEntity getUserFolioDetailsEntity() {
        return userFolioDetailsEntity;
    }

    public UserSchemeDetailsEntity setUserFolioDetailsEntity(UserFolioDetailsEntity userFolioDetailsEntity) {
        this.userFolioDetailsEntity = userFolioDetailsEntity;
        return this;
    }

    public List<UserTransactionDetailsEntity> getTransactionEntities() {
        return transactionEntities;
    }

    public UserSchemeDetailsEntity setTransactionEntities(List<UserTransactionDetailsEntity> transactionEntities) {
        this.transactionEntities = transactionEntities;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public UserSchemeDetailsEntity setVersion(Short version) {
        this.version = version;
        return this;
    }

    public void addTransactionEntity(UserTransactionDetailsEntity userTransactionDetailsEntity) {
        this.transactionEntities.add(userTransactionDetailsEntity);
        userTransactionDetailsEntity.setUserSchemeDetailsEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserSchemeDetailsEntity userSchemeDetailsEntity = (UserSchemeDetailsEntity) o;
        return isin != null
                && Objects.equals(isin, userSchemeDetailsEntity.isin)
                && Objects.equals(
                        userFolioDetailsEntity.getId(),
                        userSchemeDetailsEntity.getUserFolioDetailsEntity().getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
