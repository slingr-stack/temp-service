package io.slingr.services.sample;

import io.slingr.services.Service;
import io.slingr.services.framework.annotations.*;
import io.slingr.services.sample.services.WeatherService;
import io.slingr.services.services.AppLogs;
import io.slingr.services.utils.Json;
import io.slingr.services.ws.exchange.FunctionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>Sample endpoint
 *
 * <p>Created by lefunes on 01/12/16.
 */
@SlingrService(name = "temp")
public class SampleEndpoint extends Service {
    private static final Logger logger = LoggerFactory.getLogger(SampleEndpoint.class);

    @ApplicationLogger
    private AppLogs appLogger;

    @ServiceProperty
    private String token;

    @ServiceConfiguration
    private Json configuration;



    private final WeatherService weatherService = new WeatherService();
    private final Random random = new Random();




    @Override
    public void webServicesConfigured() {
        // the web services server is ready at this point.
    }





    @ServiceFunction(name = "randomNumber")
    public Json generateRandomNumber(Json data) {
        if (data == null) {
            data = Json.map();
        }
        appLogger.info("Request to RANDOM NUMBER received", data);

        data.set("token", token);

        // generate random number
        int bound = !data.isEmpty("bound") ? data.integer("bound") : 10000;
        data.set("number", random.nextInt(bound));

        logger.info(String.format("Function RANDOM NUMBER: [%s]", data.toString()));
        return data;
    }

    @ServiceFunction
    public Json ping(FunctionRequest request) {
        final Json data = request.getJsonParams();
        appLogger.info("Request to PING received", data);

        data.set("token", token);
        data.set("ping", "pong");

        //send event
        events().send("pong", data, request.getFunctionId());

        logger.info(String.format("Function PING: [%s]", data.toString()));
        return data;
    }

    @ServiceFunction(name = "routeMessage")
    public Json routeMessage(FunctionRequest request) {
        final Json data = request.getJsonParams();
        appLogger.info("Request to ROUTE MESSAGE received", data);

        data.set("token", token);

        // select a random route
        final String route = String.format("route%s", (random.nextInt(4) + 1));
        data.set("route", route);

        //send event
        events().send(route, data, request.getFunctionId());

        logger.info(String.format("Function ROUTE MESSAGE: [%s]", data.toString()));
        return data;
    }

    @ServiceFunction
    public Json showInformation(FunctionRequest request) {
        final Json data = request.getJsonParams();

        //send app log
        appLogger.info("Request to SHOW INFORMATION received", data);

        //send event
        try {
            Object eventResponse = events().sendSync("requestInformation", data, request.getFunctionId());
            data.set("eventResponse", eventResponse);
            logger.info(String.format("Function SHOW INFORMATION: [%s]", data.toString()));
        } catch (Exception ex) {
            logger.error(String.format("Error when try to process sync event: %s", ex.toString()));
        }

        return data;
    }

    @ServiceFunction(name = "weather")
    public Json weatherRequest(Json data) {
        appLogger.info("Request to WEATHER received", data);

        // call to an external REST API
        data = weatherService.getCurrentWeather(data);

        logger.info(String.format("Function WEATHER: [%s]", data.toString()));
        return data;
    }




}
