/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_cas_details")
public class UserCASDetailsEntity extends AuditableEntity<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cas_type", nullable = false)
    private CasTypeEnum casTypeEnum;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileTypeEnum fileTypeEnum;

    @OneToOne(mappedBy = "userCasDetailsEntity", cascade = CascadeType.ALL, optional = false)
    private InvestorInfoEntity investorInfoEntity;

    @OneToMany(mappedBy = "userCasDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFolioDetailsEntity> folioEntities = new ArrayList<>();

    public void setInvestorInfoEntity(InvestorInfoEntity investorInfoEntity) {
        if (investorInfoEntity == null) {
            if (this.investorInfoEntity != null) {
                this.investorInfoEntity.setUserCasDetailsEntity(null);
            }
        } else {
            investorInfoEntity.setUserCasDetailsEntity(this);
        }
        this.investorInfoEntity = investorInfoEntity;
    }

    public void addFolioEntity(UserFolioDetailsEntity userFolioDetailsEntity) {
        this.folioEntities.add(userFolioDetailsEntity);
        userFolioDetailsEntity.setUserCasDetailsEntity(this);
    }
}
