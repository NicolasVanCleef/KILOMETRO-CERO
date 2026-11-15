package com.uruguaymnatural.backend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.uruguaymnatural.backend.dto.WeatherDto;

/**
 * Asks a public weather API (Open-Meteo, free and without a key) for the daily forecast of a place.
 *
 * Like the payment client, it never lets an error of the external service escape: if the
 * service fails or takes too long, it answers with an empty list (RNF-14).
 */
@Component
public class WeatherClient {

    /** The part of the service's answer that is used: one value per day in each list. */
    private record Forecast(Daily daily) {
    }

    private record Daily(List<LocalDate> time, List<Integer> weather_code, List<Double> temperature_2m_min,
            List<Double> temperature_2m_max, List<Integer> precipitation_probability_max) {
    }

    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);

    private final RestClient restClient;

    public WeatherClient(@Value("${weather.url}") String baseUrl,
            @Value("${weather.timeout-seconds}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    /** The forecast of each day between the two dates at those coordinates; empty if the service fails. */
    public List<WeatherDto> dailyForecast(double latitude, double longitude, LocalDate from, LocalDate to) {
        try {
            Forecast forecast = restClient.get()
                    .uri(uri -> uri.path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("daily", "weather_code,temperature_2m_min,temperature_2m_max,"
                                    + "precipitation_probability_max")
                            .queryParam("timezone", "America/Montevideo")
                            .queryParam("start_date", from)
                            .queryParam("end_date", to)
                            .build())
                    .retrieve()
                    .body(Forecast.class);
            return toDays(forecast);
        } catch (RuntimeException e) {
            log.warn("El servicio de clima no respondió: {}", e.toString());
            return List.of();
        }
    }

    private static List<WeatherDto> toDays(Forecast forecast) {
        List<WeatherDto> days = new ArrayList<>();
        if (forecast == null || forecast.daily() == null || forecast.daily().time() == null) {
            return days;
        }
        Daily daily = forecast.daily();
        for (int i = 0; i < daily.time().size(); i++) {
            Integer code = daily.weather_code().get(i);
            Double min = daily.temperature_2m_min().get(i);
            Double max = daily.temperature_2m_max().get(i);
            if (code == null || min == null || max == null) {
                continue;   // the service has no forecast for that day yet
            }
            Integer rain = daily.precipitation_probability_max() == null
                    ? null : daily.precipitation_probability_max().get(i);
            days.add(new WeatherDto(daily.time().get(i), describe(code), min, max, rain));
        }
        return days;
    }

    /** The service describes the sky with a number (WMO code); this puts it in words. */
    static String describe(int code) {
        if (code == 0) {
            return "Despejado";
        }
        if (code <= 2) {
            return "Algo nublado";
        }
        if (code == 3) {
            return "Nublado";
        }
        if (code <= 48) {
            return "Niebla";
        }
        if (code <= 57) {
            return "Llovizna";
        }
        if (code <= 67) {
            return "Lluvia";
        }
        if (code <= 77) {
            return "Nieve";
        }
        if (code <= 82) {
            return "Chaparrones";
        }
        if (code <= 86) {
            return "Nieve";
        }
        return "Tormenta";
    }
}
