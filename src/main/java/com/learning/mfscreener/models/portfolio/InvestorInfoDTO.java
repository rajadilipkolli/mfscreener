/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.models.portfolio;

import java.io.Serializable;

public record InvestorInfoDTO(String email, String name, String mobile, String address) implements Serializable {}
