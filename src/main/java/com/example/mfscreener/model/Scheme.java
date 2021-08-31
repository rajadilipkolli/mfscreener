package com.example.mfscreener.model;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Scheme implements Serializable {

  private static final long serialVersionUID = 585620707939795736L;

  String schemeCode;
  String payout;
  String schemeName;
  String nav;
  String date;
}
