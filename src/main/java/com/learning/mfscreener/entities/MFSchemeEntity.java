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
import jakarta.persistence.Version;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Table(name = "mf_scheme")
@Entity
@Getter
@Setter
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

    @Version
    private Short version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_type_id")
    private MFSchemeTypeEntity mfSchemeTypeEntity = null;

    @OneToMany(mappedBy = "mfSchemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeNavEntity> mfSchemeNavEntities = new ArrayList<>();

    public void addSchemeNav(MFSchemeNavEntity mfSchemeNavEntity) {
        mfSchemeNavEntities.add(mfSchemeNavEntity);
        mfSchemeNavEntity.setMfSchemeEntity(this);
    }
}
