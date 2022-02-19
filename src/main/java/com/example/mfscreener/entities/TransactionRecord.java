package com.example.mfscreener.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class TransactionRecord extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transactionRecord")
    @SequenceGenerator(name = "transactionRecord", sequenceName = "transactionRecordSequence")
    @Column(name = "id", nullable = false)
    private Long id;

    private LocalDate transactionDate;
    private String schemeName;
    private String folioNumber;
    private String transactionType;
    private Float price;
    private Float units;
    private Float balanceUnits;
    private Long schemeId;

}
