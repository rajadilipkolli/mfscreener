package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
public class ErrorMessage extends Auditable<String> implements Serializable {

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
