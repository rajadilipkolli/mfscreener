/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@Table(name = "scheme_info")
public class SchemeEntity extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "scheme")
    private String scheme;

    @Column(name = "isin")
    private String isin;

    private String advisor;

    private String rtaCode;

    private String rta;

    private String type;

    private Long amfi;

    @Column(name = "open")
    private String myopen;

    private String close;

    @Column(name = "close_calculated")
    private String closeCalculated;

    @ManyToOne
    @JoinColumn(name = "folio_id")
    private FolioEntity folioEntity;

    @OneToMany(mappedBy = "schemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactionEntities = new ArrayList<>();

    public void addTransactionEntity(TransactionEntity transactionEntity) {
        this.transactionEntities.add(transactionEntity);
        transactionEntity.setSchemeEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SchemeEntity schemeEntity = (SchemeEntity) o;
        return id != null && Objects.equals(id, schemeEntity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
