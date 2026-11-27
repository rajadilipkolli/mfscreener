package com.learning.mfscreener.common;

import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.learning.mfscreener.utils.AppConstants;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@TestConfiguration(proxyBeanMethods = false)
class MarketDataFixtures {

    private static final DateTimeFormatter MFAPI_DATE = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    private static final URI MFAPI_BASE = URI.create(AppConstants.MFAPI_WEBSITE_BASE_URL);

    @Bean
    @Primary
    RestClient fixtureRestClient(RestClient.Builder builder) {
        // Bind before the application runner starts loading the scheme catalog.
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        server.expect(manyTimes(), requestTo(AppConstants.AMFI_WEBSITE_LINK))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(new ClassPathResource("market-data/catalog.txt"), MediaType.TEXT_PLAIN));
        server.expect(manyTimes(), request -> {
                    URI uri = request.getURI();
                    if (!"https".equals(uri.getScheme())
                            || !"portal.amfiindia.com".equals(uri.getHost())
                            || !"/DownloadNAVHistoryReport_Po.aspx".equals(uri.getPath())) {
                        throw new AssertionError("Expected the AMFI historical NAV endpoint: " + uri);
                    }
                })
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(new ClassPathResource("market-data/historical.txt"), MediaType.TEXT_PLAIN));
        server.expect(manyTimes(), request -> {
                    URI uri = MFAPI_BASE.relativize(request.getURI());
                    if (uri.isAbsolute() || !uri.getPath().matches("[0-9]+")) {
                        throw new AssertionError("Expected a numeric MFAPI scheme code: " + request.getURI());
                    }
                })
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> withSuccess(
                                navResponse(
                                        MFAPI_BASE.relativize(request.getURI()).getPath()),
                                MediaType.APPLICATION_JSON)
                        .createResponse(request));
        return builder.build();
    }

    private static String navResponse(String schemeCode) {
        String nav =
                switch (schemeCode) {
                    case "120186" -> "63.0400";
                    case "120503" -> "97.3520";
                    case "120594" -> "190.1100";
                    case "120684" -> "58.1998";
                    case "118272" -> "300.0000";
                    // No recent NAV: exercise AMFI fallback or the controller's 404.
                    case "119404", "119578", "141565", "144610", "151113", "159999" -> null;
                    default -> throw new AssertionError("No MFAPI fixture for scheme " + schemeCode);
                };
        Stream<String> rows = Stream.empty();
        if (nav != null) {
            // Recent dates cover both explicit and default dates without relying on live market data.
            LocalDate today = LocalDate.now();
            rows = today.minusDays(7).datesUntil(today.plusDays(1)).map(date -> navRow(date, nav));
        }
        Stream<String> historicalRows =
                switch (schemeCode) {
                    // AMFI matches the old scheme code by ISIN; MFAPI supplies its historical NAV.
                    case "119404" -> Stream.of(navRow(LocalDate.of(2022, 10, 20), "63.1620"));
                    case "119578" ->
                        Stream.of(
                                navRow(AppConstants.GRAND_FATHERED_DATE, "167.0000"),
                                navRow(LocalDate.of(2018, 12, 20), "176.6091"));
                    case "120503" -> Stream.of(navRow(LocalDate.of(2022, 12, 20), "73.6085"));
                    case "120684" -> Stream.of(navRow(LocalDate.of(2024, 4, 15), "58.2000"));
                    default -> Stream.empty();
                };
        return Stream.concat(rows, historicalRows).collect(Collectors.joining(",", "{\"data\":[", "]}"));
    }

    private static String navRow(LocalDate date, String nav) {
        return "{\"date\":\"%s\",\"nav\":\"%s\"}".formatted(date.format(MFAPI_DATE), nav);
    }
}
