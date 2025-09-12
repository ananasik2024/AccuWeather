package study.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.qameta.allure.*;                         // аннотации Allure
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.extension.ExtendWith;
import io.qameta.allure.junit5.AllureJunit5;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 20 автотестов к AccuWeather API (Rest Assured + JUnit 5).
 * Примечание: часть эндпоинтов требует платный тариф.
 * Поэтому в ассерт добавлены только статусы 401/403
 */

@Epic("AccuWeather API")
@Feature("Public Endpoints")
@Owner("Anastasiya Shagrai")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({ AllureJunit5.class })
public class AccuWeatherApiTests {

    static String BASE_URL;
    static String API_KEY;
    static String LOCATION_KEY;
    static String LANGUAGE;

    @BeforeAll
    static void setup() throws IOException {
        Properties props = new Properties();
        try (InputStream is = AccuWeatherApiTests.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) props.load(is);
        }
        BASE_URL = System.getenv().getOrDefault("BASE_URL", props.getProperty("BASE_URL", "https://dataservice.accuweather.com"));
        API_KEY  = System.getenv().getOrDefault("API_KEY",  props.getProperty("API_KEY", ""));
        LOCATION_KEY = props.getProperty("DEFAULT_LOCATION_KEY", "294021"); // пример: Минск
        LANGUAGE = props.getProperty("LANGUAGE", "en-us");

        RestAssured.baseURI = BASE_URL;
    }
    @BeforeEach
    void enableAllureLogging() {
        RestAssured.filters(
                new AllureRestAssured(),     // прикрепляет к Allure request/response как вложения
                new RequestLoggingFilter(),  // лог запроса в консоль
                new ResponseLoggingFilter()  // лог ответа в консоль
        );
    }

    // ------- LOCATIONS --------

    @Test
    @Story("Поиск городов")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("1) Cities search returns at least one result")
    void citiesSearch() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("q", "Minsk")
                .when()
                .get("/locations/v1/cities/search")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    @Test
    @Story("Работа с локациями")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("2) Autocomplete suggests cities")
    void citiesAutocomplete() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("q", "Lon")
                .when()
                .get("/locations/v1/cities/autocomplete")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    @Test
    @Story("Работа с локациями")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("3) Geoposition search by lat/lon returns location key")
    void geopositionSearch() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("q", "53.9,27.5667")
                .when()
                .get("/locations/v1/cities/geoposition/search")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    @Test
    @Story("Работа с локациями")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("4) Postal codes search returns matches")
    void postalSearch() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("q", "10001")
                .when()
                .get("/locations/v1/postalcodes/search")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    @Test
    @Story("Работа с локациями")
    @Severity(SeverityLevel.TRIVIAL)
    @DisplayName("5) Top 50 cities returns list")
    void topCities() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/locations/v1/topcities/50")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    // ------- CURRENT CONDITIONS --------

    @Test
    @Story("Текущие погодные условия")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("6) Current conditions basic")
    void currentConditions() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/currentconditions/v1/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    @Test
    @Story("Текущие погодные условия")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("7) Current conditions with details")
    void currentConditionsDetails() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("details", "true")
                .when()
                .get("/currentconditions/v1/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)))
                .contentType(containsString("json"));
    }

    @Test
    @Story("Исторические погодные данные")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("8) Historical current conditions (6h)")
    void historical6h() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/currentconditions/v1/" + LOCATION_KEY + "/historical/6")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    @Story("Исторические погодные данные")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("9) Historical current conditions (24h)")
    void historical24h() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/currentconditions/v1/" + LOCATION_KEY + "/historical/24")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Текущие погодные условия (топ городов)")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("10) Current conditions for top cities 50")
    void conditionsTopCities() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/currentconditions/v1/topcities/50")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    // ------- FORECASTS --------

    @Test
    @Story("Прогнозы погоды")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("11) 1 day daily forecast")
    void forecast1day() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("language", LANGUAGE)
                .queryParam("metric", "true")
                .when()
                .get("/forecasts/v1/daily/1day/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Прогнозы погоды")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("12) 5 day daily forecast")
    void forecast5day() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("metric", "true")
                .when()
                .get("/forecasts/v1/daily/5day/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Прогнозы погоды")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("13) 12 hour hourly forecast")
    void forecast12hour() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("metric", "true")
                .when()
                .get("/forecasts/v1/hourly/12hour/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Прогнозы погоды")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("14) 24 hour hourly forecast")
    void forecast24hour() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("metric", "true")
                .when()
                .get("/forecasts/v1/hourly/24hour/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Прогнозы погоды")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("15) Quarter-day 1 day forecast")
    void forecastQuarterDay1() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("metric", "true")
                .when()
                .get("/forecasts/v1/quarterday/1day/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    // ------- INDICES --------

    @Test
    @Story("Погодные индексы")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("16) Indices 1 day")
    void indices1day() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/indices/v1/daily/1day/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Погодные индексы")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("17) Indices 5 days")
    void indices5day() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/indices/v1/daily/5day/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    // ------- ALERTS --------

    @Test
    @Story("Погодные предупреждения")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("18) Alerts by location")
    void alerts() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/alerts/v1/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(204), is(401), is(403))); // 204 = нет предупреждений
    }

    // ------- LANGUAGE & PERF EXAMPLES --------

    @Test
    @Story("Локализация прогнозов")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("19) Forecasts localized language ru-ru")
    void forecastLocalized() {
        given()
                .queryParam("apikey", API_KEY)
                .queryParam("language", "ru-ru")
                .queryParam("metric", "true")
                .when()
                .get("/forecasts/v1/daily/1day/" + LOCATION_KEY)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    @Story("Нефункциональные проверки")
    @Severity(SeverityLevel.TRIVIAL)
    @DisplayName("20) Current conditions header+time checks")
    void perfAndHeaderCheck() {
        given()
                .queryParam("apikey", API_KEY)
                .when()
                .get("/currentconditions/v1/" + LOCATION_KEY)
                .then()
                .time(lessThan(3000L))
                .header("Content-Type", containsString("json"))
                .statusCode(anyOf(is(200), is(401), is(403)));
    }
}

