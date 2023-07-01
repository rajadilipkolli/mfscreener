/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MFSchemeNavEntity that = (MFSchemeNavEntity) o;
        return Objects.equals(getNav(), that.getNav())
                && Objects.equals(
                        getMfSchemeEntity().getSchemeId(),
                        that.getMfSchemeEntity().getSchemeId())
                && Objects.equals(getNavDate(), that.getNavDate());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
