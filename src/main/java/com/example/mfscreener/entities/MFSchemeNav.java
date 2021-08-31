package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "mf_scheme_nav")
@Entity
@Getter
@Setter
public class MFSchemeNav {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheme_id_generator")
  @SequenceGenerator(
      name = "scheme_id_generator",
      sequenceName = "scheme_id_seq",
      allocationSize = 100)
  @Column(name = "id", nullable = false)
  private Long id;

  private Double nav;

  private LocalDate navDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mf_scheme_id")
  private MFScheme mfScheme;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MFSchemeNav)) return false;
    return nav.equals(((MFSchemeNav) o).getNav()) && navDate.equals(((MFSchemeNav) o).getNavDate());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
