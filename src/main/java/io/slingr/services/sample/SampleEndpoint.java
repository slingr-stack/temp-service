package io.slingr.services.sample;

import io.slingr.endpoints.Endpoint;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.framework.annotations.*;
import io.slingr.services.sample.services.HttpHelper;
import io.slingr.services.sample.services.WeatherService;
import io.slingr.endpoints.services.AppLogs;
import io.slingr.endpoints.services.application.AppUser;
import io.slingr.endpoints.services.datastores.DataStore;
import io.slingr.endpoints.services.datastores.DataStoreResponse;
import io.slingr.endpoints.services.exchange.Parameter;
import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.ws.exchange.FunctionRequest;
import io.slingr.endpoints.ws.exchange.WebServiceRequest;
import io.slingr.endpoints.ws.exchange.WebServiceResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.slingr.services.sample.SampleEndpoint

import java.util.*;

/**
 * <p>Sample endpoint
 *
 * <p>Created by lefunes on 01/12/16.
 */
@SlingrSvc(name = "temp2")
public class SampleEndpoint extends  {
    private static final Logger logger = LoggerFactory.getLogger(SampleEndpoint.class);

    @ApplicationLogger
    private AppLogs appLogger;

    @EndpointProperty
    private String token;

    @EndpointConfiguration
    private Json configuration;

    @EndpointDataStore(name = "test_data_store")
    private DataStore dataStore;

    private final WeatherService weatherService = new WeatherService();
    private final Random random = new Random();

    @Override
    public void endpointConfigured() {
        // the definitions and properties are ready to be used at this point
        this.weatherService.setDebug(properties().isDebug());
    }

    @Override
    public void endpointsServicesConfigured() {
        // the communication with the Endpoints Services app is ready to be used at this point.
    }

    @Override
    public void webServicesConfigured() {
        // the web services server is ready at this point.
    }

    @Override
    public void endpointStarted() {
        // the loggers, endpoint properties, data stores, etc. are initialized at this point. the endpoint is ready to be used

        // Examples:
        //this.appLogger.info("App logger");
        //logger.info(String.format("EndpointProperty [%s]", this.token));
        //logger.info(String.format("Configuration [%s]", this.configuration));
        //logger.info(String.format("Data Store [%s]", this.dataStore.getName()));
        //logger.info(String.format("REST client request [%s]", this.weatherService.getCurrentWeather("Mendoza, AR")));
    }

    @Override
    public void endpointStopped(String cause) {
        logger.info(String.format("Endpoint is stopping - cause [%s]", cause));
    }

    @EndpointFunction(name = "randomNumber")
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

    @EndpointFunction
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

    @EndpointFunction(name = "routeMessage")
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

    @EndpointFunction
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

    @EndpointFunction(name = "weather")
    public Json weatherRequest(Json data) {
        appLogger.info("Request to WEATHER received", data);

        // call to an external REST API
        data = weatherService.getCurrentWeather(data);

        logger.info(String.format("Function WEATHER: [%s]", data.toString()));
        return data;
    }

    @EndpointFunction
    public Json executeScript(FunctionRequest request) {
        appLogger.info("Request to EXECUTE SCRIPT 'actionScript' received");

        final Object response = scripts().execute("actionScript", request.getParams());

        logger.info(String.format("Response to EXECUTE SCRIPT 'actionScript': %s", response != null ? "[" + response.toString() + "]" : "NULL"));
        return response instanceof Json ? (Json) response : Json.map().set("body", response);
    }

    @EndpointWebService(methods = {RestMethod.GET, RestMethod.POST})
    private WebServiceResponse mainPage(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        final Map<String, Object> properties = new HashMap<>();
        properties.put("Endpoint Name", properties().getEndpointName());
        properties.put("Application Name", properties().getApplicationName());
        properties.put("Environment", properties().getEnvironment());
        properties.put("Local Deployment", properties().isLocalDeployment());
        properties.put("Web Services URI", properties().getWebServicesUri());
        properties.put("Using proxy?", properties().isUsingProxy() ? "YES" : "NO");
        properties.put("Custom Domain", properties().getCustomDomain());
        properties.put("Base Domain", properties().getBaseDomain());
        properties.put("Per User Endpoint", definitions().isPerUserEndpoint());
        properties.put("Data Stores", Arrays.toString(definitions().getDataStoresNames().toArray()));
        HttpHelper.addPanel(sb, String.format("Endpoint <b>%s-%s-%s</b>", properties().getApplicationName(), properties().getEnvironment(), properties().getEndpointName()), properties);

        // send event if the web service request is a POST
        if (RestMethod.POST.equals(request.getMethod())) {
            final String event = request.getParameter("event");
            if (StringUtils.isNotBlank(event)) {
                if (event.equals("inboundEvent")) {
                    events().send("inboundEvent", Json.map().set("token", token).set("number", random.nextInt(10000)));
                    HttpHelper.addAlert(sb, "success", "Inbound event will be sent");
                } else if (event.equals("requestInformation")) {
                    try {
                        final Object appResponse = events().sendSync("requestInformation", Json.map().set("value", random.nextInt(10000)));
                        HttpHelper.addAlert(sb, "success", String.format("Request information event sent!<br><pre>%s</pre>", appResponse));
                    } catch (Exception ex) {
                        HttpHelper.addAlert(sb, "danger", String.format("<b>Error: </b><br />%s", ex.toString()));
                    }
                } else {
                    HttpHelper.addAlert(sb, "danger", String.format("<b>Invalid event: </b>%s", event));
                }
            } else {
                HttpHelper.addAlert(sb, "danger", "<b>Empty event</b>");
            }
        }

        // show button to send events
        final String jsFunction = "hideForms";
        final String lsId = "ls-events";
        final String f1Id = "fe1";
        final String f2Id = "fe2";
        HttpHelper.addLoadingSpinner(lsId, true, sb);
        HttpHelper.addEventForm(f1Id, properties().getWebServicesUri() + "?event=inboundEvent", "Send Inbound Event", jsFunction, sb);
        HttpHelper.addEventForm(f2Id, properties().getWebServicesUri() + "?event=requestInformation", "Send Request Information Event", jsFunction, sb);


        final StringBuilder sb2 = new StringBuilder();
        sb2.append(String.format("\t\t\tfunction %s(){\n", jsFunction));
        sb2.append(String.format("\t\t\t\tdocument.getElementById(\"%s\").className = \"loader\";\n", lsId));
        sb2.append(String.format("\t\t\t\tdocument.getElementById(\"%s\").className += \" hidden\";\n", f1Id));
        sb2.append(String.format("\t\t\t\tdocument.getElementById(\"%s\").className += \" hidden\";\n", f2Id));
        sb2.append("\t\t\t\treturn true;\n");
        sb2.append("\t\t\t}\n");

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, sb2.toString(), HttpHelper.Menu.HOME), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "favicon.ico", methods = RestMethod.GET)
    public WebServiceResponse getFavicon(WebServiceRequest request) throws Exception {
        return new WebServiceResponse(HttpHelper.getFavicon(), "image/x-icon");
    }

    @EndpointWebService(path = "response", methods = RestMethod.GET)
    public WebServiceResponse getResponse() throws Exception {

        final int value = random.nextInt(10000);
        appLogger.info(String.format("Request value [%s]", value));

        final Object appResponse = events().sendSync("requestInformation", Json.map().set("value", value));
        appLogger.info(String.format("Request response from server [%s]", appResponse));

        final WebServiceResponse response = new WebServiceResponse(Json.map()
                .set("value", value)
                .set("appResponse", appResponse)
        );
        response.setHttpCode(202); // HTTP code: 202 Accepted
        response.setHeader(Parameter.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        response.setHeader("value", value);

        logger.info(String.format("Response from server: [%s]", response.getBody().toString()));
        return response;
    }

    @EndpointWebService(path = "weather", methods = RestMethod.GET)
    public WebServiceResponse weatherPage(WebServiceRequest request) {
        String city = request.getParameter("city");
        if (StringUtils.isBlank(city)) {
            city = "";
        }

        final StringBuilder sbPanel = new StringBuilder();
        HttpHelper.addWeatherForm(properties().getWebServicesUri(), sbPanel, city);

        final StringBuilder sb = new StringBuilder();
        HttpHelper.addPanel(sb, "Weather", sbPanel);

        if (StringUtils.isNotBlank(city)) {
            final Json cWeather = weatherService.getCurrentWeather(city);

            final Map<String, Object> properties = new HashMap<>();
            properties.put("City", cWeather.string("city"));
            properties.put("Country", cWeather.string("country"));
            properties.put("Humidity", cWeather.string("humidity"));
            properties.put("Pressure", cWeather.string("pressure"));
            properties.put("Temperature", cWeather.string("temperature"));
            properties.put("Description", cWeather.string("description"));
            HttpHelper.addPanel(sb, String.format("City <b>%s</b>", city), properties);
        }

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.WEATHER), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore", methods = {RestMethod.GET, RestMethod.POST})
    public WebServiceResponse getRecordsList(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        final Json body = request.getJsonBody();

        // save record if the web service request is a POST
        if (RestMethod.POST.equals(request.getMethod())) {
            try {

                Json response = dataStore.save(Json.map()
                        .set("firstName", body.string("firstName"))
                        .set("lastName", body.string("lastName"))
                );
                HttpHelper.addAlert(sb, "success", String.format("Record saved: %s", response.string(Parameter.DATA_STORE_ID)));
            } catch (Exception ex) {
                HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to save record: </b><br />%s", ex.getMessage()));
            }
        }

        final String currentOffset = request.getParameter("offset");
        final int size = 5;

        try {
            final DataStoreResponse dsResponse = dataStore.find(null, currentOffset, size);
            String nextOffset = dsResponse.getOffset();

            HttpHelper.addRecordsTable(properties().getWebServicesUri(), sb,
                    String.format("All records, total [%s], size [%s], from [%s] to [%s]", dsResponse.getTotal(), size,
                            StringUtils.isNotBlank(currentOffset) ? currentOffset : "-",
                            StringUtils.isNotBlank(nextOffset) ? nextOffset : "-"),
                    dsResponse.getItems(),
                    String.format("<div><a href=\"/datastore\">&lt; ini</a> %s", StringUtils.isNotBlank(nextOffset) ? String.format(" - <a href=\"/datastore?offset=%s\">next &gt;</a></div>", nextOffset) : ""));

            final StringBuilder sbPanel = new StringBuilder();
            HttpHelper.addNewRecordForm(properties().getWebServicesUri(), sbPanel);
            HttpHelper.addPanel(sb, "New Record", sbPanel);

            final StringBuilder sbPanel2 = new StringBuilder();
            HttpHelper.addRemoveAllRecordsButton(properties().getWebServicesUri(), sbPanel2);
            HttpHelper.addCountAllRecordsButton(properties().getWebServicesUri(), sbPanel2);
            HttpHelper.addPanel(sb, "Actions", sbPanel2);
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to get records: </b><br />%s", ex.toString()));
        }

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore/{recordId}", methods = {RestMethod.GET, RestMethod.POST})
    public WebServiceResponse getRecord(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        final String recordId = request.getPathVariable("recordId");

        // update record if the web service request is a POST
        if (RestMethod.POST.equals(request.getMethod())) {
            try {
                final Json body = request.getJsonBody();
                Json response = dataStore.update(recordId, Json.map()
                        .set("_id", body.string("recordId"))
                        .set("firstName", body.string("firstName"))
                        .set("lastName", body.string("lastName"))
                );
                HttpHelper.addAlert(sb, "success", String.format("Record updated: %s", response.string(Parameter.DATA_STORE_ID)));
            } catch (Exception ex) {
                HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to update record: </b><br />%s", ex.getMessage()));
            }
        }

        try {
            final Json record = dataStore.findById(recordId);
            if (record != null) {
                final StringBuilder sbPanel = new StringBuilder();
                HttpHelper.addUpdateRecordForm(properties().getWebServicesUri(), sbPanel, record);
                HttpHelper.addPanel(sb, "Update Record: " + recordId, sbPanel);
                HttpHelper.addRemoveRecordButton(properties().getWebServicesUri(), sb, recordId);
            } else {
                HttpHelper.addAlert(sb, "danger", String.format("<b>Record [%s] does not exists. </b>", recordId));
            }
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to get record [%s]: </b><br />%s", recordId, ex.toString()));
        }

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore/{recordId}/delete", methods = RestMethod.POST)
    public WebServiceResponse deleteRecord(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        try {
            final String recordId = request.getPathVariable("recordId");
            dataStore.removeById(recordId);
            HttpHelper.addAlert(sb, "success", String.format("<b>Record deleted: </b>%s", recordId));
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to delete record: </b><br />%s", ex.getMessage()));
        }

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore/delete", methods = RestMethod.POST)
    public WebServiceResponse deleteAllRecord(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        try {
            dataStore.remove();
            HttpHelper.addAlert(sb, "success", "<b>All records deleted!</b>");
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to delete all records: </b><br />%s", ex.getMessage()));
        }

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore/count", methods = RestMethod.GET)
    public WebServiceResponse countAllRecord(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        try {
            HttpHelper.addAlert(sb, "success", String.format("<b>Count all records: %s</b>", dataStore.count()));
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to count all record: </b><br />%s", ex.getMessage()));
        }

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore/find/{search}", methods = RestMethod.GET)
    public WebServiceResponse findRecords(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        final String search = request.getPathVariable("search");
        try {
            final DataStoreResponse response = dataStore.find(Json.map().set("firstName", search));
            if (response != null) {
                HttpHelper.addRecordsTable(properties().getWebServicesUri(), sb,
                        String.format("<b>Find by first name [%s]</b> - Found records: %s", search, response.getTotal()),
                        response.getItems(), null);
            } else {
                HttpHelper.addAlert(sb, "danger", String.format("<b>Empty response when find records with first name [%s]</b>", search));
            }
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to find records with first name [%s]</b><br />%s", search, ex.getMessage()));
        }

        final StringBuilder actionPanel = new StringBuilder();
        HttpHelper.addFindAllRecordButton(properties().getWebServicesUri(), actionPanel);
        HttpHelper.addFindRecordsButton(properties().getWebServicesUri(), actionPanel, search);
        HttpHelper.addFindRecordButton(properties().getWebServicesUri(), actionPanel, search);
        HttpHelper.addPanel(sb, "Actions", actionPanel);

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "datastore/findone/{search}", methods = RestMethod.GET)
    public WebServiceResponse findOneRecord(WebServiceRequest request) {
        final StringBuilder sb = new StringBuilder();

        final String search = request.getPathVariable("search");
        try {
            final Json response = dataStore.findOne(Json.map().set("firstName", search));
            if (response != null) {
                HttpHelper.addRecordsTable(properties().getWebServicesUri(), sb,
                        String.format("<b>Find one record by first name [%s]</b> - Found: %s", search, response),
                        Collections.singletonList(response), null);
            } else {
                HttpHelper.addAlert(sb, "danger", String.format("<b>Empty response when find one record with first name [%s]</b>", search));
            }
        } catch (Exception ex) {
            HttpHelper.addAlert(sb, "danger", String.format("<b>Exception when try to find one record with first name [%s]</b><br />%s", search, ex.getMessage()));
        }

        final StringBuilder actionPanel = new StringBuilder();
        HttpHelper.addFindAllRecordButton(properties().getWebServicesUri(), actionPanel);
        HttpHelper.addFindRecordsButton(properties().getWebServicesUri(), actionPanel, search);
        HttpHelper.addFindRecordButton(properties().getWebServicesUri(), actionPanel, search);
        HttpHelper.addPanel(sb, "Actions", actionPanel);

        return new WebServiceResponse(HttpHelper.formatPage(properties().getWebServicesUri(), sb, HttpHelper.Menu.DATA_STORE), ContentType.TEXT_HTML.toString());
    }

    @EndpointWebService(path = "users/token", methods = RestMethod.GET)
    public WebServiceResponse checkUserByToken(WebServiceRequest request) {
        try {
            String token = request.getHeader("token");
            if (StringUtils.isBlank(token)) {
                token = request.getParameter("token");
            }

            final AppUser user = appUsers().findByToken(token);
            return new WebServiceResponse(Json.map()
                    .set("email", user.getEmail())
                    .set("lastName", user.getLastName())
                    .set("firstName", user.getFirstName())
                    .set("name", user.getFullName())
                    .set("developer", user.isDeveloper())
                    .set("active", user.isActive())
            );
        } catch (EndpointException ex) {
            return new WebServiceResponse(404, ex.toJson());
        } catch (Exception ex) {
            return new WebServiceResponse(404, ex.toString());
        }
    }

    @EndpointWebService(path = "users/email", methods = RestMethod.GET)
    public WebServiceResponse checkUserByEmail(WebServiceRequest request) {
        try {
            String email = request.getHeader("email");
            if (StringUtils.isBlank(email)) {
                email = request.getParameter("email");
            }

            final AppUser user = appUsers().findByEmail(email);
            return new WebServiceResponse(Json.map()
                    .set("email", user.getEmail())
                    .set("lastName", user.getLastName())
                    .set("firstName", user.getFirstName())
                    .set("name", user.getFullName())
                    .set("developer", user.isDeveloper())
                    .set("active", user.isActive())
            );
        } catch (EndpointException ex) {
            return new WebServiceResponse(404, ex.toJson());
        } catch (Exception ex) {
            return new WebServiceResponse(404, ex.toString());
        }
    }

    @EndpointFunction(name = "error")
    public Json errors(FunctionRequest request) {
        appLogger.warn("Request to ERROR received");

        throw new IllegalArgumentException(convertMessage("Invalid argument", request));
    }

    @EndpointFunction(name = "recoverableError")
    public Json recoverableError(FunctionRequest request) {
        appLogger.warn("Request to RECOVERABLE ERROR received");

        throw EndpointException.retryable(ErrorCode.API, convertMessage("Recoverable error", request), Json.map().set("info", "test value 1"), new IllegalArgumentException("Invalid argument"));
    }

    @EndpointFunction(name = "irrecoverableError")
    public Json irrecoverableError(FunctionRequest request) {
        appLogger.warn("Request to IRRECOVERABLE ERROR received");

        throw EndpointException.permanent(ErrorCode.CONVERSION, convertMessage("Irrecoverable error", request), Json.map().set("info", "test value 2"), new IllegalArgumentException("Invalid argument"));
    }

    private String convertMessage(String message, FunctionRequest request) {
        String response;
        if (request.isRedelivered()) {
            response = String.format("%s: redelivered [true][%s/%s]", message, request.getRedeliveredCounter(), request.getRedeliveredMaxCounter());
        } else {
            response = String.format("%s: redelivered [false]", message);
        }

        logger.info(response);
        return response;
    }
}
