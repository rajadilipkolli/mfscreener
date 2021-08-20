package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "mf_scheme")
@Entity
@Getter
@Setter
public class MFScheme {

    @Id
    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    @Column(name = "fund_house")
    private String fundHouse;

    @Column(name = "scheme_name", nullable = false)
    private String schemeName;

    private String payOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mf_scheme_type_id")
    private MFSchemeType mfSchemeType = null;

    @OneToMany(
            mappedBy = "mfScheme",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MFSchemeNav> mfSchemeNavies = new ArrayList<>();

    public void addSchemeNav(MFSchemeNav mfSchemeNav) {
        mfSchemeNavies.add(mfSchemeNav);
        mfSchemeNav.setMfScheme(this);
    }

}