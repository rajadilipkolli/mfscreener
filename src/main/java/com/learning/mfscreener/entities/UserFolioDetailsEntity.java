/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(
        name = "user_folio_details",
        indexes = {@Index(name = "user_details_idx_pan_id", columnList = "id, pan")})
public class UserFolioDetailsEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "folio", nullable = false)
    private String folio;

    @Column(name = "amc", nullable = false)
    private String amc;

    @Column(name = "pan", nullable = false)
    private String pan;

    @Column(name = "kyc")
    private String kyc;

    @Column(name = "pan_kyc")
    private String panKyc;

    @OneToMany(mappedBy = "userFolioDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSchemeDetailsEntity> schemeEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_cas_details_id", nullable = false)
    private UserCASDetailsEntity userCasDetailsEntity;

    public Long getId() {
        return id;
    }

    public UserFolioDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFolio() {
        return folio;
    }

    public UserFolioDetailsEntity setFolio(String folio) {
        this.folio = folio;
        return this;
    }

    public String getAmc() {
        return amc;
    }

    public UserFolioDetailsEntity setAmc(String amc) {
        this.amc = amc;
        return this;
    }

    public String getPan() {
        return pan;
    }

    public UserFolioDetailsEntity setPan(String pan) {
        this.pan = pan;
        return this;
    }

    public String getKyc() {
        return kyc;
    }

    public UserFolioDetailsEntity setKyc(String kyc) {
        this.kyc = kyc;
        return this;
    }

    public String getPanKyc() {
        return panKyc;
    }

    public UserFolioDetailsEntity setPanKyc(String panKyc) {
        this.panKyc = panKyc;
        return this;
    }

    public List<UserSchemeDetailsEntity> getSchemeEntities() {
        return schemeEntities;
    }

    public UserFolioDetailsEntity setSchemeEntities(List<UserSchemeDetailsEntity> schemeEntities) {
        this.schemeEntities = schemeEntities;
        return this;
    }

    public UserCASDetailsEntity getUserCasDetailsEntity() {
        return userCasDetailsEntity;
    }

    public UserFolioDetailsEntity setUserCasDetailsEntity(UserCASDetailsEntity userCasDetailsEntity) {
        this.userCasDetailsEntity = userCasDetailsEntity;
        return this;
    }

    public void addSchemeEntity(UserSchemeDetailsEntity userSchemeDetailsEntity) {
        this.schemeEntities.add(userSchemeDetailsEntity);
        userSchemeDetailsEntity.setUserFolioDetailsEntity(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserFolioDetailsEntity that = (UserFolioDetailsEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
