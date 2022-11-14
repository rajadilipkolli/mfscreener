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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_folio_details")
public class UserFolioDetailsEntity extends Auditable<String> implements Serializable {

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
