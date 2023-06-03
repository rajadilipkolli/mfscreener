/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import com.example.mfscreener.repositoryutil.EntityVisitor;
import com.example.mfscreener.repositoryutil.Identifiable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@Table(name = "user_transaction_details")
public class UserTransactionDetailsEntity extends AuditableEntity<String> implements Serializable, Identifiable {
    public static final EntityVisitor<UserTransactionDetailsEntity, UserSchemeDetailsEntity> ENTITY_VISITOR =
            new EntityVisitor<>(UserTransactionDetailsEntity.class) {

                public UserSchemeDetailsEntity getParent(UserTransactionDetailsEntity visitingObject) {
                    return visitingObject.getUserSchemeDetailsEntity();
                }

                public List<UserTransactionDetailsEntity> getChildren(UserSchemeDetailsEntity parent) {
                    return parent.getTransactionEntities();
                }

                public void setChildren(UserSchemeDetailsEntity parent) {
                    parent.setTransactionEntities(new ArrayList<>());
                }
            };

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private LocalDate transactionDate;
    private String description;
    private Double amount;
    private Double units;
    private Double nav;
    private Double balance;
    private String type;
    private String dividendRate;

    @ManyToOne
    @JoinColumn(name = "user_scheme_detail_id")
    private UserSchemeDetailsEntity userSchemeDetailsEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserTransactionDetailsEntity that = (UserTransactionDetailsEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
