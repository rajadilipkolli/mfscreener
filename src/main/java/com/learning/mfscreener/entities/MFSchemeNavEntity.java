/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Table(
        name = "mf_scheme_nav",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_mf_scheme_nav",
                    columnNames = {"nav", "nav_date", "mf_scheme_id"})
        })
@Entity
@Getter
@Setter
public class MFSchemeNavEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private Double nav;

    @Column(name = "nav_date")
    private LocalDate navDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_id")
    private MFSchemeEntity mfSchemeEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MFSchemeNavEntity)) return false;
        return nav.equals(((MFSchemeNavEntity) o).getNav())
                && navDate.equals(((MFSchemeNavEntity) o).getNavDate())
                && Objects.equals(mfSchemeEntity.getSchemeId(), ((MFSchemeNavEntity) o).mfSchemeEntity.getSchemeId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
