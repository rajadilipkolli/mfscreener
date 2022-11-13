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
@Table(name = "cas_details")
public class CASDetailsEntity extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cas_type", nullable = false)
    private CasType casType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @OneToOne(mappedBy = "casDetailsEntity", cascade = CascadeType.ALL, optional = false)
    private InvestorInfoEntity investorInfoEntity;

    @OneToMany(mappedBy = "casDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolioEntity> folioEntities = new ArrayList<>();

    public void setInvestorInfoEntity(InvestorInfoEntity investorInfoEntity) {
        if (investorInfoEntity == null) {
            if (this.investorInfoEntity != null) {
                this.investorInfoEntity.setCasDetailsEntity(null);
            }
        } else {
            investorInfoEntity.setCasDetailsEntity(this);
        }
        this.investorInfoEntity = investorInfoEntity;
    }

    public void addFolioEntity(FolioEntity folioEntity) {
        this.folioEntities.add(folioEntity);
        folioEntity.setCasDetailsEntity(this);
    }
}
