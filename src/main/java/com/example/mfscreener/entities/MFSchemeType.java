package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mf_scheme_types", uniqueConstraints =
        @UniqueConstraint(columnNames = {"scheme_type", "scheme_category"}))
public class MFSchemeType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_type_id_generator")
    @SequenceGenerator(
            name = "scheme_type_id_generator",
            sequenceName = "scheme_type_id_seq",
            allocationSize = 2)
    @Column(name = "scheme_type_id", nullable = false)
    private Integer schemeTypeId;

    @Column(name = "scheme_type", nullable = false)
    private String schemeType;

    @Column(name = "scheme_category", nullable = false)
    private String schemeCategory;

    @OneToMany(
            mappedBy = "mfSchemeType",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MFScheme> mfSchemes = new ArrayList<>();

    public void addMFScheme(MFScheme mfScheme) {
        mfSchemes.add(mfScheme);
        mfScheme.setMfSchemeType(this);
    }

}