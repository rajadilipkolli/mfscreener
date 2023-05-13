/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Table(name = "mf_scheme_nav")
@Entity
@Getter
@Setter
public class MFSchemeNavEntity extends AuditableEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_id_generator")
    @SequenceGenerator(name = "scheme_id_generator", sequenceName = "scheme_id_seq", allocationSize = 100)
    @Column(name = "id", nullable = false)
    private Long id;

    private Double nav;

    private LocalDate navDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_id")
    private MFSchemeEntity mfSchemeEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MFSchemeNavEntity)) return false;
        return nav.equals(((MFSchemeNavEntity) o).getNav()) && navDate.equals(((MFSchemeNavEntity) o).getNavDate());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
