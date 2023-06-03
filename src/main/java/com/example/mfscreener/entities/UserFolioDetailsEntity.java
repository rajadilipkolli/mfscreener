/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import com.example.mfscreener.repositoryutil.EntityVisitor;
import com.example.mfscreener.repositoryutil.Identifiable;
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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_folio_details")
public class UserFolioDetailsEntity extends AuditableEntity<String> implements Serializable, Identifiable {

    public static final EntityVisitor<UserFolioDetailsEntity, UserCASDetailsEntity> ENTITY_VISITOR =
            new EntityVisitor<>(UserFolioDetailsEntity.class) {

                public UserCASDetailsEntity getParent(UserFolioDetailsEntity visitingObject) {
                    return visitingObject.getUserCasDetailsEntity();
                }

                public List<UserFolioDetailsEntity> getChildren(UserCASDetailsEntity parent) {
                    return parent.getFolioEntities();
                }

                public void setChildren(UserCASDetailsEntity parent) {
                    parent.setFolioEntities(new ArrayList<>());
                }
            };

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "folio")
    private String folio;

    @Column(name = "amc")
    private String amc;

    @Column(name = "pan")
    private String pan;

    @Column(name = "kyc")
    private String kyc;

    @Column(name = "pan_kyc")
    private String panKyc;

    @OneToMany(mappedBy = "userFolioDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSchemeDetailsEntity> schemeEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_cas_details_id")
    private UserCASDetailsEntity userCasDetailsEntity;

    public void addSchemeEntity(UserSchemeDetailsEntity userSchemeDetailsEntity) {
        this.schemeEntities.add(userSchemeDetailsEntity);
        userSchemeDetailsEntity.setUserFolioDetailsEntity(this);
    }
}
