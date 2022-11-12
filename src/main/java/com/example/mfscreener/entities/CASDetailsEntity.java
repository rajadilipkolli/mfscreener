/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cas_info")
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

    @OneToOne(mappedBy = "casDetailsEntity", orphanRemoval = true)
    private InvestorInfoEntity investorInfoEntity;

    @OneToMany(mappedBy = "casDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolioEntity> folioEntities = new ArrayList<>();
}
