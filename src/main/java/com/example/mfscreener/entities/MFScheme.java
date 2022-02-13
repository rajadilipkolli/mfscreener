package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "mf_scheme")
@Entity
@Getter
@Setter
public class MFScheme extends Auditable<String> implements Serializable {

  @Id
  @Column(name = "scheme_id", nullable = false)
  private Long schemeId;

  private String payOut;

  @Column(name = "fund_house")
  private String fundHouse;

  @Column(name = "scheme_name", nullable = false)
  private String schemeName;

  @Column(name = "scheme_name_alias")
  private String schemeNameAlias;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mf_scheme_type_id")
  private MFSchemeType mfSchemeType = null;

  @OneToMany(mappedBy = "mfScheme", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MFSchemeNav> mfSchemeNavies = new ArrayList<>();

  public void addSchemeNav(MFSchemeNav mfSchemeNav) {
    mfSchemeNavies.add(mfSchemeNav);
    mfSchemeNav.setMfScheme(this);
  }
}
