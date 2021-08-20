package com.example.mfscreener.service;

import com.example.mfscreener.model.Scheme;

import java.io.IOException;
import java.net.URISyntaxException;

public interface NavService {
    void loadNavForAllFunds() throws IOException, URISyntaxException;

    Scheme getNav(boolean forceUpdate, Long schemeCode);

    Scheme getNavOnDate(Long schemeCode, String date) throws URISyntaxException;
}
