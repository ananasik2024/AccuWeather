package study.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.*;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class AccuWeatherMockTests {

    static WireMockServer wm;

    static String load(String resource) {
        try {
            return Files.readString(Path.of("src/test/resources/mocks_accuweather/" + resource));
        } catch (Exception e) {
            throw new RuntimeException("Cannot load fixture: " + resource, e);
        }
    }

    @BeforeAll
    static void startMock() {
        wm = new WireMockServer(0); // свободный порт
        wm.start();
        configureFor("localhost", wm.port());

        // 1) Cities search
        wm.stubFor(get(urlPathEqualTo("/locations/v1/cities/search"))
                .withQueryParam("q", equalTo("Minsk"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("cities_search_minsk.json"))));

        // 2) Cities autocomplete
        wm.stubFor(get(urlPathEqualTo("/locations/v1/cities/autocomplete"))
                .withQueryParam("q", equalTo("Lon"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("cities_autocomplete_lon.json"))));

        // 3) Geoposition search
        wm.stubFor(get(urlPathEqualTo("/locations/v1/cities/geoposition/search"))
                .withQueryParam("q", equalTo("53.9,27.5667"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("geoposition_search_minsk.json"))));

        // 4) Postal codes search
        wm.stubFor(get(urlPathEqualTo("/locations/v1/postalcodes/search"))
                .withQueryParam("q", equalTo("10001"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("postalcodes_search_10001.json"))));

        // 5) Top 50 cities
        wm.stubFor(get(urlPathEqualTo("/locations/v1/topcities/50"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("topcities_50.json"))));

        // 6) Current conditions basic
        wm.stubFor(get(urlPathMatching("/currentconditions/v1/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                        .atPriority(5)
                .willReturn(okJson(load("currentconditions_basic.json"))));

        // 7) Current conditions with details
        wm.stubFor(get(urlPathMatching("/currentconditions/v1/\\d+"))
                .withQueryParam("details", equalTo("true"))
                .withQueryParam("apikey", matching(".*"))
                        .atPriority(1)
                .willReturn(okJson(load("currentconditions_details_true.json"))));

        // 8) Historical current conditions (6h)
        wm.stubFor(get(urlPathMatching("/currentconditions/v1/\\d+/historical/6"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("currentconditions_historical_6.json"))));

        // 9) Historical current conditions (24h)
        wm.stubFor(get(urlPathMatching("/currentconditions/v1/\\d+/historical/24"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("currentconditions_historical_24.json"))));

        // 10) Current conditions for top cities 50
        wm.stubFor(get(urlPathEqualTo("/currentconditions/v1/topcities/50"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("currentconditions_topcities_50.json"))));

        // 11) 1 day daily forecast
        wm.stubFor(get(urlPathMatching("/forecasts/v1/daily/1day/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("language", matching(".*"))
                .withQueryParam("metric", equalTo("true"))
                .willReturn(okJson(load("forecast_daily_1day.json"))));

        // 12) 5 day daily forecast
        wm.stubFor(get(urlPathMatching("/forecasts/v1/daily/5day/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("metric", equalTo("true"))
                .willReturn(okJson(load("forecast_daily_5day.json"))));

        // 13) 12 hour hourly forecast
        wm.stubFor(get(urlPathMatching("/forecasts/v1/hourly/12hour/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("metric", equalTo("true"))
                .willReturn(okJson(load("forecast_hourly_12hour.json"))));

        // 14) 24 hour hourly forecast
        wm.stubFor(get(urlPathMatching("/forecasts/v1/hourly/24hour/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("metric", equalTo("true"))
                .willReturn(okJson(load("forecast_hourly_24hour.json"))));

        // 15) Quarter-day 1 day forecast
        wm.stubFor(get(urlPathMatching("/forecasts/v1/quarterday/1day/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("metric", equalTo("true"))
                .willReturn(okJson(load("forecast_quarterday_1day.json"))));

        // 16) Indices 1 day
        wm.stubFor(get(urlPathMatching("/indices/v1/daily/1day/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("indices_daily_1day.json"))));

        // 17) Indices 5 days
        wm.stubFor(get(urlPathMatching("/indices/v1/daily/5day/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("indices_daily_5day.json"))));

        // 18) Alerts by location (можно сделать 204, если без предупреждений)
        wm.stubFor(get(urlPathMatching("/alerts/v1/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("alerts_by_location.json"))));

        // 19) Forecasts localized (ru-ru)
        wm.stubFor(get(urlPathMatching("/forecasts/v1/daily/1day/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("language", equalTo("ru-ru"))
                .withQueryParam("metric", equalTo("true"))
                .willReturn(okJson(load("forecast_daily_1day_ru.json"))));

        // 20) Current conditions perf (time+headers)
        wm.stubFor(get(urlPathMatching("/currentconditions/v1/\\d+"))
                .withQueryParam("apikey", matching(".*"))
                .willReturn(okJson(load("currentconditions_perf.json"))));
    }

    @AfterAll
    static void stopMock() {
        if (wm != null) wm.stop();
    }

    @BeforeEach
    void setupRestAssured() {
        baseURI = "http://localhost:" + wm.port();
        filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }


    @Test
    @DisplayName("1) Cities search mock works")
    void testCitiesSearchMock() {
        given()
                .queryParam("apikey", "TEST")
                .queryParam("q", "Minsk")
                .when()
                .get("/locations/v1/cities/search")
                .then()
                .statusCode(200);
    }
    @Test @DisplayName("2) Cities autocomplete mock")
    void citiesAutocomplete_mock() {
        given().queryParam("apikey", "TEST").queryParam("q", "Lon")
                .when().get("/locations/v1/cities/autocomplete")
                .then().statusCode(200)
                .body("[0].LocalizedName", is("London"));
    }

    @Test @DisplayName("3) Geoposition search mock")
    void geopositionSearch_mock() {
        given().queryParam("apikey", "TEST").queryParam("q", "53.9,27.5667")
                .when().get("/locations/v1/cities/geoposition/search")
                .then().statusCode(200)
                .body("Key", is("294021"));
    }

    @Test @DisplayName("4) Postal codes search mock")
    void postalCodesSearch_mock() {
        given().queryParam("apikey", "TEST").queryParam("q", "10001")
                .when().get("/locations/v1/postalcodes/search")
                .then().statusCode(200)
                .body("[0].PrimaryPostalCode", is("10001"));
    }

    @Test @DisplayName("5) Top 50 cities mock")
    void top50Cities_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/locations/v1/topcities/50")
                .then().statusCode(200)
                .body("[0].LocalizedName", notNullValue());
    }

    @Test @DisplayName("6) Current conditions (basic) mock")
    void currentConditionsBasic_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/currentconditions/v1/294021")
                .then().statusCode(200)
                .body("[0].WeatherText", is("Cloudy"));
    }

    @Test @DisplayName("7) Current conditions (details=true) mock")
    void currentConditionsDetails_mock() {
        given().queryParam("apikey", "TEST").queryParam("details", "true")
                .when().get("/currentconditions/v1/294021")
                .then().statusCode(200)
                .body("[0].RealFeelTemperature.Metric.Unit", is("C"));
    }

    @Test @DisplayName("8) Historical current conditions (6h) mock")
    void historical6h_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/currentconditions/v1/294021/historical/6")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test @DisplayName("9) Historical current conditions (24h) mock")
    void historical24h_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/currentconditions/v1/294021/historical/24")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test @DisplayName("10) Conditions for top cities (50) mock")
    void conditionsTopCities_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/currentconditions/v1/topcities/50")
                .then().statusCode(200)
                .body("[0].Temperature.Metric.Unit", is("C"));
    }

    @Test @DisplayName("11) Daily forecast 1 day mock")
    void forecast1day_mock() {
        given().queryParam("apikey", "TEST")
                .queryParam("language", "ru-ru")
                .queryParam("metric", "true")
                .when().get("/forecasts/v1/daily/1day/294021")
                .then().statusCode(200)
                .body("DailyForecasts.size()", greaterThanOrEqualTo(1));
    }

    @Test @DisplayName("12) Daily forecast 5 day mock")
    void forecast5day_mock() {
        given().queryParam("apikey", "TEST")
                .queryParam("metric", "true")
                .when().get("/forecasts/v1/daily/5day/294021")
                .then().statusCode(200)
                .body("DailyForecasts.size()", equalTo(5));
    }

    @Test @DisplayName("13) Hourly forecast 12 hour mock")
    void hourly12_mock() {
        given().queryParam("apikey", "TEST")
                .queryParam("metric", "true")
                .when().get("/forecasts/v1/hourly/12hour/294021")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test @DisplayName("14) Hourly forecast 24 hour mock")
    void hourly24_mock() {
        given().queryParam("apikey", "TEST")
                .queryParam("metric", "true")
                .when().get("/forecasts/v1/hourly/24hour/294021")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test @DisplayName("15) Quarter-day forecast 1 day mock")
    void quarterday1_mock() {
        given().queryParam("apikey", "TEST")
                .queryParam("metric", "true")
                .when().get("/forecasts/v1/quarterday/1day/294021")
                .then().statusCode(200)
                .body("size()", equalTo(4));
    }

    @Test @DisplayName("16) Indices 1 day mock")
    void indices1day_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/indices/v1/daily/1day/294021")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test @DisplayName("17) Indices 5 day mock")
    void indices5day_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/indices/v1/daily/5day/294021")
                .then().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test @DisplayName("18) Alerts by location mock")
    void alerts_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/alerts/v1/294021")
                .then().statusCode(200) // если сделаешь 204 в стабе — поменяй на .statusCode(anyOf(is(200), is(204)))
                .body("[0].Severity", notNullValue());
    }

    @Test @DisplayName("19) Daily forecast 1 day (ru-ru) mock")
    void forecast1dayRu_mock() {
        given().queryParam("apikey", "TEST")
                .queryParam("language", "ru-ru")
                .queryParam("metric", "true")
                .when().get("/forecasts/v1/daily/1day/294021")
                .then().statusCode(200)
                .body("Headline.Text", notNullValue());
    }

    @Test @DisplayName("20) Current conditions perf & headers mock")
    void currentConditionsPerf_mock() {
        given().queryParam("apikey", "TEST")
                .when().get("/currentconditions/v1/294021")
                .then().statusCode(200)
                .header("Content-Type", containsString("json"));
    }

}

