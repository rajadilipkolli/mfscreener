/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.models.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public record CasDTO(
        @JsonProperty("statement_period") StatementPeriodDTO statementPeriod,
        @JsonProperty("file_type") String fileType,
        @JsonProperty("cas_type") String casType,
        @JsonProperty("investor_info") InvestorInfoDTO investorInfo,
        @JsonProperty("folios") List<UserFolioDTO> folios)
        implements Serializable {}
