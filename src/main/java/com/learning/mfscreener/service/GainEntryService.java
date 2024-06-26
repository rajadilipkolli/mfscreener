package com.learning.mfscreener.service;

import com.learning.mfscreener.models.portfolio.Fund;
import com.learning.mfscreener.models.portfolio.FundType;
import com.learning.mfscreener.models.portfolio.GainType;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GainEntryService {

    private final Map<String, BigDecimal> CII = loadCostInflationIndexData();
    private final NavService navService;

    private LocalDate cutoffDate;
    private LocalDate sellCutoffDate;
    private String finYear;
    private Fund fund;
    private FundType fundType;
    private LocalDate purchaseDate;
    private BigDecimal purchaseNav;
    private BigDecimal purchaseValue;
    private BigDecimal stampDuty;
    private LocalDate saleDate;
    private BigDecimal saleNav;
    private BigDecimal saleValue;
    private BigDecimal stt;
    private BigDecimal units;
    private String cachedIsin;
    private BigDecimal cachedNav;

    public GainEntryService init(
            String finYear,
            Fund fund,
            FundType fundType,
            LocalDate purchaseDate,
            BigDecimal purchaseNav,
            BigDecimal purchaseValue,
            BigDecimal stampDuty,
            LocalDate saleDate,
            BigDecimal saleNav,
            BigDecimal saleValue,
            BigDecimal stt,
            BigDecimal units) {
        this.finYear = finYear;
        this.fund = fund;
        this.fundType = fundType;
        this.purchaseDate = purchaseDate;
        this.purchaseNav = purchaseNav;
        this.purchaseValue = purchaseValue;
        this.stampDuty = stampDuty;
        this.saleDate = saleDate;
        this.saleNav = saleNav;
        this.saleValue = saleValue;
        this.stt = stt;
        this.units = units;
        this.cutoffDate = LocalDate.of(2018, 1, 31);
        this.sellCutoffDate = LocalDate.of(2018, 4, 1);
        updateNav(fund.isin());
        return this;
    }

    public GainEntryService(NavService navService) {
        this.navService = navService;
    }

    public void updateNav(String isin) {
        this.cachedIsin = isin;
        this.cachedNav = navService.getNavByISINOnDate(isin, LocalDate.of(2018, 1, 31));
    }

    public String getFinYear() {
        return finYear;
    }

    public GainEntryService setFinYear(String finYear) {
        this.finYear = finYear;
        return this;
    }

    public Fund getFund() {
        return fund;
    }

    public GainEntryService setFund(Fund fund) {
        this.fund = fund;
        return this;
    }

    public FundType getFundType() {
        return fundType;
    }

    public GainEntryService setFundType(FundType fundType) {
        this.fundType = fundType;
        return this;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public GainEntryService setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
        return this;
    }

    public BigDecimal getPurchaseNav() {
        return purchaseNav;
    }

    public GainEntryService setPurchaseNav(BigDecimal purchaseNav) {
        this.purchaseNav = purchaseNav;
        return this;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public GainEntryService setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue = purchaseValue;
        return this;
    }

    public BigDecimal getStampDuty() {
        return stampDuty;
    }

    public GainEntryService setStampDuty(BigDecimal stampDuty) {
        this.stampDuty = stampDuty;
        return this;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public GainEntryService setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
        return this;
    }

    public BigDecimal getSaleNav() {
        return saleNav;
    }

    public GainEntryService setSaleNav(BigDecimal saleNav) {
        this.saleNav = saleNav;
        return this;
    }

    public BigDecimal getSaleValue() {
        return saleValue;
    }

    public GainEntryService setSaleValue(BigDecimal saleValue) {
        this.saleValue = saleValue;
        return this;
    }

    public BigDecimal getStt() {
        return stt;
    }

    public GainEntryService setStt(BigDecimal stt) {
        this.stt = stt;
        return this;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public GainEntryService setUnits(BigDecimal units) {
        this.units = units;
        return this;
    }

    public LocalDate getCutoffDate() {
        return cutoffDate;
    }

    public LocalDate getSellCutoffDate() {
        return sellCutoffDate;
    }

    public BigDecimal getLtcgTaxable() {
        if (this.getGainType() == GainType.LTCG) {
            return this.saleValue.subtract(this.getCoa()).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getCoa() {
        if (this.getFundType() == FundType.DEBT) {
            return this.purchaseValue.multiply(this.getIndexRatio()).setScale(2, RoundingMode.HALF_UP);
        }
        if (this.purchaseDate.isBefore(this.getCutoffDate())) {
            if (this.saleDate.isBefore(this.getSellCutoffDate())) {
                return this.saleValue;
            }
            return this.purchaseValue.max(this.getFmv().min(this.saleValue));
        }
        return this.purchaseValue;
    }

    public BigDecimal getFmv() {
        BigDecimal fmvNav = this.getFmvNav();
        if (fmvNav == null) {
            return this.purchaseValue;
        }
        return fmvNav.multiply(this.getUnits());
    }

    public BigDecimal getFmvNav() {
        if (!this.fund.isin().equals(this.cachedIsin)) {
            this.updateNav(this.fund.isin());
        }
        return this.cachedNav;
    }

    public BigDecimal getIndexRatio() {
        return CII.get(LocalDateUtility.getFinYear(this.saleDate))
                .divide(CII.get(LocalDateUtility.getFinYear(this.purchaseDate)), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getLtcg() {
        if (this.getGainType() == GainType.LTCG) {
            return getGain();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getStcg() {
        if (this.getGainType() == GainType.STCG) {
            return getGain();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getGain() {
        return this.saleValue.subtract(this.purchaseValue).setScale(2, RoundingMode.HALF_UP);
    }

    public GainType getGainType() {
        Map<String, LocalDate> ltcg = new HashMap<>();
        ltcg.put(FundType.EQUITY.name(), this.purchaseDate.plusYears(1));
        ltcg.put(FundType.DEBT.name(), this.purchaseDate.plusYears(3));

        return this.saleDate.isAfter(ltcg.get(this.fundType.name())) ? GainType.LTCG : GainType.STCG;
    }

    public Map<String, BigDecimal> loadCostInflationIndexData() {
        Map<String, BigDecimal> ciiDataMap = new HashMap<>();
        ciiDataMap.put("FY2001-02", BigDecimal.valueOf(100));
        ciiDataMap.put("FY2002-03", BigDecimal.valueOf(105));
        ciiDataMap.put("FY2003-04", BigDecimal.valueOf(109));
        ciiDataMap.put("FY2004-05", BigDecimal.valueOf(113));
        ciiDataMap.put("FY2005-06", BigDecimal.valueOf(117));
        ciiDataMap.put("FY2006-07", BigDecimal.valueOf(122));
        ciiDataMap.put("FY2007-08", BigDecimal.valueOf(129));
        ciiDataMap.put("FY2008-09", BigDecimal.valueOf(137));
        ciiDataMap.put("FY2009-10", BigDecimal.valueOf(148));
        ciiDataMap.put("FY2010-11", BigDecimal.valueOf(167));
        ciiDataMap.put("FY2011-12", BigDecimal.valueOf(184));
        ciiDataMap.put("FY2012-13", BigDecimal.valueOf(200));
        ciiDataMap.put("FY2013-14", BigDecimal.valueOf(220));
        ciiDataMap.put("FY2014-15", BigDecimal.valueOf(240));
        ciiDataMap.put("FY2015-16", BigDecimal.valueOf(254));
        ciiDataMap.put("FY2016-17", BigDecimal.valueOf(264));
        ciiDataMap.put("FY2017-18", BigDecimal.valueOf(272));
        ciiDataMap.put("FY2018-19", BigDecimal.valueOf(280));
        ciiDataMap.put("FY2019-20", BigDecimal.valueOf(289));
        ciiDataMap.put("FY2020-21", BigDecimal.valueOf(301));
        ciiDataMap.put("FY2021-22", BigDecimal.valueOf(317));
        ciiDataMap.put("FY2022-23", BigDecimal.valueOf(331));
        ciiDataMap.put("FY2023-24", BigDecimal.valueOf(348));
        // TODO to be updated when notified
        ciiDataMap.put("FY2024-25", BigDecimal.valueOf(348));
        return ciiDataMap;
    }
}
