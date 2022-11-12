/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@Table(name = "investor_info")
public class InvestorInfoEntity extends Auditable<String> implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "address")
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "cas_details_entity_id")
    private CASDetailsEntity casDetailsEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        InvestorInfoEntity that = (InvestorInfoEntity) o;
        return id != null && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
