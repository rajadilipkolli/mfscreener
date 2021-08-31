package com.example.mfscreener.service;

import com.example.mfscreener.model.Scheme;

public interface NavService {

    Scheme getNav(Long schemeCode);

    Scheme getNavOnDate(Long schemeCode, String date);

    void fetchSchemeDetails(Long schemeCode);
}
