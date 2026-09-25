package com.learning.mfscreener.models.response;

import java.io.Serializable;

public record XIRRResponse(String folio, Long amfiId, String scheme, double xirr) implements Serializable {}
