package io.slingr.services.sample.services;


import io.slingr.services.exceptions.ErrorCode;
import io.slingr.services.exceptions.ServiceException;
import io.slingr.services.services.rest.RestClient;
import io.slingr.services.utils.Json;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;

/**
 * Weather service that shows how to build the interaction with an external service
 *
 * Created by lefunes on 11/05/15.
 */
public class WeatherService extends RestClient {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private static final String API_URI = "http://api.openweathermap.org/data/2.5/";

    public WeatherService() throws ServiceException {
        super(API_URI);
        setupDefaultParam("units", "metric");
        setupDefaultParam("APPID", "3d8a556f9df436cc4eca00d7c19b1321");
    }

    public Json getCurrentWeather(String city){
        return getCurrentWeather(Json.parse(String.format("{\"city\":\"%s\"}", city)));
    }

    public Json getCurrentWeather(Json data) {
        if(data == null){
            data = Json.map();
        }

        String city = data.string("city");
        if(StringUtils.isBlank(city)){
            city = "New York, US";
        }
        data.set("city", city.trim());

        WebTarget resource = getApiTarget().path("weather");
        resource = resource.queryParam("q", data.string("city").replace(" ", "%20"));

        int retries = 5;
        Json response = null;

        while(response == null) {
            try {
                final Json restResponse = get(resource);

                if (StringUtils.isNotBlank(restResponse.string("cod")) && StringUtils.isNotBlank(restResponse.string("message"))) {
                    throw ServiceException.permanent(ErrorCode.API, String.format("%s (%s)", restResponse.string("message"), restResponse.string("cod")));
                }

                data.set("city", restResponse.string("name"));
                data.set("country", restResponse.json("sys").string("country"));
                data.set("humidity", restResponse.json("main").decimal("humidity"));
                data.set("pressure", restResponse.json("main").decimal("pressure"));
                data.set("temperature", restResponse.json("main").decimal("temp"));

                Json wt = restResponse.json("weather");
                if (wt != null && wt.isList() && !wt.isEmpty()) {
                    data.set("description", Json.fromObject(wt.toList().get(0)).string("description"));
                } else {
                    data.set("description", "-");
                }

                response = data;
            } catch (ServiceException ex) {
                final int retry = 5-retries;
                logger.warn(String.format("Exception when try to get the weather information [%s] %s", ex.toString(), (retry == 0 ? "" : String.format("retry [%s]", retry))));
                retries--;
                if(!ex.isRetryable() || retries < 0){
                    response = ex.toJson(true);
                } else {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        logger.warn(String.format("Error when waits to retry [%s]", e.getMessage()));
                    }
                }
            } catch (Exception ex) {
                final Json rex = ServiceException.json(ErrorCode.API, ex.getMessage(), ex);
                logger.warn(String.format("Exception when try to get the weather information [%s]", rex.toString()));
                response = rex;
            }
        }
        return response;
    }
}
