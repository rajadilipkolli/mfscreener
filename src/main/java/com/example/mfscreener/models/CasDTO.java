/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class CasDTO {

    @JsonProperty("statement_period")
    StatementPeriodDTO statementPeriod;

    @JsonProperty("file_type")
    String fileType;

    @JsonProperty("cas_type")
    String casType;

    @JsonProperty("investor_info")
    InvestorInfoDTO investorInfo;

    @JsonProperty("folios")
    List<FolioDTO> folios;
}
