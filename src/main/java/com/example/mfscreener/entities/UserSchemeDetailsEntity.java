/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import com.example.mfscreener.repository.util.EntityVisitor;
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
@Table(name = "user_scheme_details")
public class UserSchemeDetailsEntity extends AuditableEntity<String> implements Serializable, Identifiable {

    public static final EntityVisitor<UserSchemeDetailsEntity, UserFolioDetailsEntity> ENTITY_VISITOR =
            new EntityVisitor<>(UserSchemeDetailsEntity.class) {

                public UserFolioDetailsEntity getParent(UserSchemeDetailsEntity visitingObject) {
                    return visitingObject.getUserFolioDetailsEntity();
                }

                public List<UserSchemeDetailsEntity> getChildren(UserFolioDetailsEntity parent) {
                    return parent.getSchemeEntities();
                }

                public void setChildren(UserFolioDetailsEntity parent) {
                    parent.setSchemeEntities(new ArrayList<>());
                }
            };

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
    @JoinColumn(name = "user_folio_id")
    private UserFolioDetailsEntity userFolioDetailsEntity;

    @OneToMany(mappedBy = "userSchemeDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTransactionDetailsEntity> transactionEntities = new ArrayList<>();

    public void addTransactionEntity(UserTransactionDetailsEntity userTransactionDetailsEntity) {
        this.transactionEntities.add(userTransactionDetailsEntity);
        userTransactionDetailsEntity.setUserSchemeDetailsEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserSchemeDetailsEntity userSchemeDetailsEntity = (UserSchemeDetailsEntity) o;
        return id != null && Objects.equals(id, userSchemeDetailsEntity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
