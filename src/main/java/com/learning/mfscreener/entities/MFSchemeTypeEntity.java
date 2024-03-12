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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "mf_scheme_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"scheme_type", "scheme_category"}))
public class MFSchemeTypeEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_type_id_generator")
    @SequenceGenerator(name = "scheme_type_id_generator", sequenceName = "scheme_type_id_seq", allocationSize = 2)
    @Column(name = "scheme_type_id", nullable = false)
    private Integer schemeTypeId;

    @Column(name = "scheme_type", nullable = false)
    private String schemeType;

    @Column(name = "scheme_category", nullable = false)
    private String schemeCategory;

    @Version
    private Short version;

    @OneToMany(mappedBy = "mfSchemeTypeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MFSchemeEntity> mfSchemeEntities = new ArrayList<>();

    public void addMFScheme(MFSchemeEntity mfSchemeEntity) {
        mfSchemeEntities.add(mfSchemeEntity);
        mfSchemeEntity.setMfSchemeTypeEntity(this);
    }
}
