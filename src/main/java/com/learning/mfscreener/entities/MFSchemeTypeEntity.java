/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "mf_scheme_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"scheme_type", "scheme_category"}))
public class MFSchemeTypeEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_type_id_generator")
    @SequenceGenerator(name = "scheme_type_id_generator", sequenceName = "scheme_type_id_seq", allocationSize = 2)
    @Column(name = "scheme_type_id", nullable = false)
    private Integer schemeTypeId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Version
    private Short version;

    @OneToMany(mappedBy = "mfSchemeTypeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeEntity> mfSchemeEntities = new ArrayList<>();

    public Integer getSchemeTypeId() {
        return schemeTypeId;
    }

    public MFSchemeTypeEntity setSchemeTypeId(Integer schemeTypeId) {
        this.schemeTypeId = schemeTypeId;
        return this;
    }

    public String getType() {
        return type;
    }

    public MFSchemeTypeEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public MFSchemeTypeEntity setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public MFSchemeTypeEntity setSubCategory(String subCategory) {
        this.subCategory = subCategory;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public MFSchemeTypeEntity setVersion(Short version) {
        this.version = version;
        return this;
    }

    public List<MFSchemeEntity> getMfSchemeEntities() {
        return mfSchemeEntities;
    }

    public MFSchemeTypeEntity setMfSchemeEntities(List<MFSchemeEntity> mfSchemeEntities) {
        this.mfSchemeEntities = mfSchemeEntities;
        return this;
    }

    public void addMFScheme(MFSchemeEntity mfSchemeEntity) {
        mfSchemeEntities.add(mfSchemeEntity);
        mfSchemeEntity.setMfSchemeTypeEntity(this);
    }
}
