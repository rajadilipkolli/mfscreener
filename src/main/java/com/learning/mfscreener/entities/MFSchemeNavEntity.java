/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.LocalDate;
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

    private Float nav;

    @Column(name = "nav_date")
    private LocalDate navDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_id")
    private MFSchemeEntity mfSchemeEntity;

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MFSchemeNavEntity other)) {
            return false;
        } else {
            if (!other.canEqual(this)) {
                return false;
            } else if (Double.compare(this.getNav(), other.getNav()) != 0) {
                return false;
            } else {
                Object this$navDate = this.getNavDate();
                Object other$navDate = other.getNavDate();
                if (this$navDate == null) {
                    if (other$navDate != null) {
                        return false;
                    }
                } else if (!this$navDate.equals(other$navDate)) {
                    return false;
                }
                MFSchemeEntity this$mfSchemeEntity = this.getMfSchemeEntity();
                MFSchemeEntity other$mfSchemeEntity = other.getMfSchemeEntity();
                if (this$mfSchemeEntity == null) {
                    return other$mfSchemeEntity == null;
                } else {
                    return this$mfSchemeEntity.getSchemeId().equals(other$mfSchemeEntity.getSchemeId());
                }
            }
        }
    }

    private boolean canEqual(final Object other) {
        return other instanceof MFSchemeNavEntity;
    }

    @Override
    public final int hashCode() {
        int result = 1;
        long $nav = Double.doubleToLongBits(this.getNav());
        result = result * 59 + (int) ($nav >>> 32 ^ $nav);
        Object $navDate = this.getNavDate();
        result = result * 59 + ($navDate == null ? 43 : $navDate.hashCode());
        MFSchemeEntity $mfSchemeEntity = this.getMfSchemeEntity();
        result = result * 59
                + ($mfSchemeEntity == null ? 43 : $mfSchemeEntity.getSchemeId().hashCode());
        return result;
    }
}
