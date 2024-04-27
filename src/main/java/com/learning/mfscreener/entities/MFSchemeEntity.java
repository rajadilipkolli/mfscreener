/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.DynamicUpdate;

@Table(name = "mf_scheme")
@Entity
@DynamicUpdate
public class MFSchemeEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    private String payOut;

    @Column(name = "fund_house")
    private String fundHouse;

    @Column(name = "scheme_name", nullable = false)
    private String schemeName;

    @Column(name = "scheme_name_alias")
    private String schemeNameAlias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_type_id")
    private MFSchemeTypeEntity mfSchemeTypeEntity = null;

    @OneToMany(mappedBy = "mfSchemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeNavEntity> mfSchemeNavEntities = new ArrayList<>();

    public Long getSchemeId() {
        return schemeId;
    }

    public MFSchemeEntity setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
        return this;
    }

    public String getPayOut() {
        return payOut;
    }

    public MFSchemeEntity setPayOut(String payOut) {
        this.payOut = payOut;
        return this;
    }

    public String getFundHouse() {
        return fundHouse;
    }

    public MFSchemeEntity setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
        return this;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MFSchemeEntity setSchemeName(String schemeName) {
        this.schemeName = schemeName;
        return this;
    }

    public String getSchemeNameAlias() {
        return schemeNameAlias;
    }

    public MFSchemeEntity setSchemeNameAlias(String schemeNameAlias) {
        this.schemeNameAlias = schemeNameAlias;
        return this;
    }

    public MFSchemeTypeEntity getMfSchemeTypeEntity() {
        return mfSchemeTypeEntity;
    }

    public MFSchemeEntity setMfSchemeTypeEntity(MFSchemeTypeEntity mfSchemeTypeEntity) {
        this.mfSchemeTypeEntity = mfSchemeTypeEntity;
        return this;
    }

    public List<MFSchemeNavEntity> getMfSchemeNavEntities() {
        return mfSchemeNavEntities;
    }

    public MFSchemeEntity setMfSchemeNavEntities(List<MFSchemeNavEntity> mfSchemeNavEntities) {
        this.mfSchemeNavEntities = mfSchemeNavEntities;
        return this;
    }

    public MFSchemeEntity addSchemeNav(MFSchemeNavEntity mfSchemeNavEntity) {
        mfSchemeNavEntities.add(mfSchemeNavEntity);
        mfSchemeNavEntity.setMfSchemeEntity(this);
        return this;
    }
}
