package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ErrorMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_id_generator")
  @SequenceGenerator(
      name = "error_id_generator",
      sequenceName = "error_id_seq",
      allocationSize = 100)
  @Column(name = "error_id", nullable = false)
  private Long id;

  private String message;
}
