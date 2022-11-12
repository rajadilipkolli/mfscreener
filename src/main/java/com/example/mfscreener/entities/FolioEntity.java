/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "folio")
public class FolioEntity {

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

    @OneToMany(mappedBy = "folioEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchemeEntity> schemeEntities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "cas_details_entity_id")
    private CASDetailsEntity casDetailsEntity;
}
