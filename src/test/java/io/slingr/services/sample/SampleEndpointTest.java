package io.slingr.services.sample;

import io.slingr.endpoints.services.application.AppUser;
import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.Strings;
import io.slingr.endpoints.utils.tests.EndpointTests;
import io.slingr.endpoints.ws.exchange.WebServiceResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>Test over the FreshbooksEndpoint class
 *
 * <p>Created by lefunes on 10/01/15.
 */
public class SampleEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(SampleEndpointTest.class);

    private static EndpointTests test;

    @BeforeClass
    public static void init() throws Exception {
        test = EndpointTests.start(new io.slingr.endpoints.sample.Runner(), "test.properties");
    }

    @Test
    public void functionPing(){
        logger.info("-- testing PING function");
        List<Json> events;
        Json response;

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        response = test.executeFunction("ping", Json.map().set("abc", "123"));
        assertNotNull(response);
        assertEquals("pong", response.string("ping"));
        assertEquals("123", response.string("abc"));
        assertEquals("test-token", response.string("token"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals("pong", events.get(0).string("event"));
        assertEquals("pong", events.get(0).json("data").string("ping"));
        assertEquals("123", events.get(0).json("data").string("abc"));
        assertEquals("test-token", events.get(0).json("data").string("token"));

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        logger.info("-- END");
    }

    @Test
    public void functionRandomNumber(){
        logger.info("-- testing RANDOM NUMBER function");
        List<Json> events;
        Json response;
        Integer number1, number2;

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        response = test.executeFunction("randomNumber", Json.map().set("abc", "123"));
        assertNotNull(response);
        assertEquals("123", response.string("abc"));
        assertEquals("test-token", response.string("token"));

        number1 = response.integer("number");
        assertNotNull(number1);

        response = test.executeFunction("randomNumber", Json.map().set("abc", "456"));
        assertNotNull(response);
        assertEquals("456", response.string("abc"));
        assertEquals("test-token", response.string("token"));

        number2 = response.integer("number");
        assertNotNull(number2);

        assertNotEquals(number1, number2);

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        logger.info("-- END");
    }

    @Test
    public void functionRouteMessage(){
        logger.info("-- testing ROUTE MESSAGE function");
        List<Json> events;
        Json response;
        String route1, route2;

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        response = test.executeFunction("routeMessage", Json.map().set("abc", "123"));
        assertNotNull(response);
        assertEquals("123", response.string("abc"));
        assertEquals("test-token", response.string("token"));

        route1 = response.string("route");
        assertNotNull(route1);

        response = test.executeFunction("routeMessage", Json.map().set("abc", "456"));
        assertNotNull(response);
        assertEquals("456", response.string("abc"));
        assertEquals("test-token", response.string("token"));

        route2 = response.string("route");
        assertNotNull(route2);

        assertNotEquals(route1, route2);

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(2, events.size());

        assertEquals(route1, events.get(0).string("event"));
        assertEquals("123", events.get(0).json("data").string("abc"));
        assertEquals("test-token", events.get(0).json("data").string("token"));
        assertEquals(route1, events.get(0).json("data").string("route"));

        assertEquals(route2, events.get(1).string("event"));
        assertEquals("456", events.get(1).json("data").string("abc"));
        assertEquals("test-token", events.get(1).json("data").string("token"));
        assertEquals(route2, events.get(1).json("data").string("route"));

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        logger.info("-- END");
    }

    @Test
    public void functionShowInformation(){
        logger.info("-- testing SHOW INFORMATION function");
        List<Json> events;
        Json response;

        test.clearEventProcessors();

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        response = test.executeFunction("showInformation", Json.map().set("abc", "123").set("eventResponse", "-"));
        assertNotNull(response);
        assertEquals("123", response.string("abc"));
        assertEquals(Json.map(), response.json("eventResponse"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals("requestInformation", events.get(0).string("event"));
        assertEquals("123", events.get(0).json("data").string("abc"));
        assertEquals("-", events.get(0).json("data").string("eventResponse"));

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        test.registerEventProcessor("requestInformation", message -> Json.map()
                .set("response", "123456")
                .set("event-abc", message.json("data").string("abc"))
                .set("event-res", message.json("data").string("eventResponse"))
        );

        response = test.executeFunction("showInformation", Json.map().set("abc", "456").set("eventResponse", "-"));
        assertNotNull(response);
        assertEquals("456", response.string("abc"));

        response = response.json("eventResponse");
        assertNotNull(response);
        assertEquals("123456", response.string("response"));
        assertEquals("456", response.string("event-abc"));
        assertEquals("-", response.string("event-res"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals("requestInformation", events.get(0).string("event"));
        assertEquals("456", events.get(0).json("data").string("abc"));
        assertEquals("-", events.get(0).json("data").string("eventResponse"));

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        test.removeEventProcessor("requestInformation");

        response = test.executeFunction("showInformation", Json.map().set("abc", "789").set("eventResponse", "-"));
        assertNotNull(response);
        assertEquals("789", response.string("abc"));
        assertEquals(Json.map(), response.json("eventResponse"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals("requestInformation", events.get(0).string("event"));
        assertEquals("789", events.get(0).json("data").string("abc"));
        assertEquals("-", events.get(0).json("data").string("eventResponse"));

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        test.clearEventProcessors();

        logger.info("-- END");
    }

    @Test
    public void functionExecuteScript(){
        logger.info("-- testing EXECUTE SCRIPT function");
        List<Json> events;
        Json response;

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // ------------- NO PROCESSOR
        test.clearScriptProcessors();

        // NO params
        response = test.executeFunction("executeScript");
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // String params
        response = test.executeFunction("executeScript", "an string");
        assertNotNull(response);
        assertTrue(response.isEmpty());

        // JSON MAP params
        response = test.executeFunction("executeScript", Json.map().set("a", "map").set("false", 321));
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // JSON LIST params
        response = test.executeFunction("executeScript", Json.list().push("a").push("list").push(true).push(654));
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // ------------- String PROCESSOR
        test.registerScriptProcessor("actionScript", message -> String.format("ok [%s]", message.object("scriptParameters")));

        // NO params
        response = test.executeFunction("executeScript");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("ok [null]", response.string("body"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // String params
        response = test.executeFunction("executeScript", "an string");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("ok [an string]", response.string("body"));

        // JSON MAP params
        response = test.executeFunction("executeScript", Json.map().set("a", "map").set("false", 321));
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("ok [{a=map, false=321}]", response.string("body"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // JSON LIST params
        response = test.executeFunction("executeScript", Json.list().push("a").push("list").push(true).push(654));
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("ok [[a, list, true, 654]]", response.string("body"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // ------------- JSON MAP PROCESSOR
        test.registerScriptProcessor("actionScript", message -> Json.map().set("rps", message.object("scriptParameters")));

        // NO params
        response = test.executeFunction("executeScript");
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // String params
        response = test.executeFunction("executeScript", "an string");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("an string", response.string("rps"));

        // JSON MAP params
        response = test.executeFunction("executeScript", Json.map().set("a", "map").set("false", 321));
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(Json.map().set("false", 321).set("a", "map"), response.json("rps"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // JSON LIST params
        response = test.executeFunction("executeScript", Json.list().push("a").push("list").push(true).push(654));
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(Json.list().push("a").push("list").push(true).push(654), response.json("rps"));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // ------------- JSON LIST PROCESSOR
        test.registerScriptProcessor("actionScript", message -> Json.list().pushIfNotNull(message.object("scriptParameters")));

        // NO params
        response = test.executeFunction("executeScript");
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // String params
        response = test.executeFunction("executeScript", "an string");
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("an string", response.object(0));

        // JSON MAP params
        response = test.executeFunction("executeScript", Json.map().set("a", "map").set("false", 321));
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(Json.map().set("false", 321).set("a", "map"), response.object(0));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // JSON LIST params
        response = test.executeFunction("executeScript", Json.list().push("a").push("list").push(true).push(654));
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(Json.list().push("a").push("list").push(true).push(654), response.object(0));

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // ------------- NO PROCESSOR
        test.removeScriptProcessor("actionScript");

        // NO params
        response = test.executeFunction("executeScript");
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // String params
        response = test.executeFunction("executeScript", "an string");
        assertNotNull(response);
        assertTrue(response.isEmpty());

        // JSON MAP params
        response = test.executeFunction("executeScript", Json.map().set("a", "map").set("false", 321));
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        // JSON LIST params
        response = test.executeFunction("executeScript", Json.list().push("a").push("list").push(true).push(654));
        assertNotNull(response);
        assertTrue(response.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        test.clearReceivedEvents();
        events = test.getReceivedEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());

        test.clearScriptProcessors();

        logger.info("-- END");
    }

    @Test
    public void wsDataStores(){
        logger.info("-- testing DATA STORES web services");
        List<String> names;
        List<Json> records;
        String id1, id2;

        test.clearDataStores();
        names = test.getDataStoreNames();
        assertNotNull(names);
        assertTrue(names.isEmpty());

        test.executeWebServices(RestMethod.POST, "datastore", Json.map().set("firstName", "aaaa").set("lastName", "1111"));

        names = test.getDataStoreNames();
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("test_data_store", names.get(0));

        records = test.getDataStoreItems("test_data_store");
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals("aaaa", records.get(0).string("firstName"));
        assertEquals("1111", records.get(0).string("lastName"));
        id1 = records.get(0).string("_id");
        assertTrue(StringUtils.isNoneBlank(id1));

        test.executeWebServices(RestMethod.POST, "datastore", Json.map().set("firstName", "bbbb").set("lastName", "2222"));

        names = test.getDataStoreNames();
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("test_data_store", names.get(0));

        records = test.getDataStoreItems("test_data_store");
        assertNotNull(records);
        assertEquals(2, records.size());
        assertEquals("aaaa", records.get(0).string("firstName"));
        assertEquals("1111", records.get(0).string("lastName"));
        assertEquals(id1, records.get(0).string("_id"));
        assertEquals("bbbb", records.get(1).string("firstName"));
        assertEquals("2222", records.get(1).string("lastName"));
        assertTrue(StringUtils.isNoneBlank(records.get(1).string("_id")));
        id2 = records.get(1).string("_id");
        assertTrue(StringUtils.isNoneBlank(id2));
        assertNotEquals(id1, id2);

        test.executeWebServices(RestMethod.POST, String.format("datastore/%s/delete", id1));

        names = test.getDataStoreNames();
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("test_data_store", names.get(0));

        records = test.getDataStoreItems("test_data_store");
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals("bbbb", records.get(0).string("firstName"));
        assertEquals("2222", records.get(0).string("lastName"));
        assertEquals(id2, records.get(0).string("_id"));

        test.clearDataStores();
        names = test.getDataStoreNames();
        assertNotNull(names);
        assertTrue(names.isEmpty());

        logger.info("-- END");
    }

    @Test
    public void wsAppUsers(){
        logger.info("-- testing APP USERS web services");

        final String token = Strings.randomUUIDString();
        assertNotNull(token);

        final String invalidToken = Strings.randomUUIDString();
        assertNotNull(invalidToken);
        assertNotEquals(token, invalidToken);

        final String firstName = Strings.randomAlphabetic(10).toLowerCase();
        assertNotNull(firstName);

        final String lastName = Strings.randomAlphabetic(10).toLowerCase();
        assertNotNull(lastName);

        final String email = String.format("%s@%s.io", Strings.randomAlphabetic(10),Strings.randomAlphabetic(10)).toLowerCase();
        assertNotNull(email);

        final String invalidEmail = String.format("%s@%s.io", Strings.randomAlphabetic(10),Strings.randomAlphabetic(10)).toLowerCase();
        assertNotNull(invalidEmail);
        assertNotEquals(email, invalidEmail);

        WebServiceResponse response;
        List<AppUser> users;
        Json user;

        // no token provided
        response = test.executeWebServices(RestMethod.GET, "users/token");
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains("empty token"));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", ""));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains("empty token"));

        // no email provided
        response = test.executeWebServices(RestMethod.GET, "users/email");
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains("empty email"));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", ""));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains("empty email"));

        // user does not exists
        test.clearAppUsers();
        users = test.getAppUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", token));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", token)));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", email));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", email)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", email));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", email)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", token));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", token)));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", invalidToken));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", invalidToken)));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", invalidEmail));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", invalidEmail)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", invalidEmail));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", invalidEmail)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", invalidToken));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", invalidToken)));

        // create a user
        test.addAppUser(token, test.createAppUser(email, firstName, lastName, true));
        users = test.getAppUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertNotNull(users.get(0));
        assertEquals(email, users.get(0).getEmail());
        assertEquals(firstName, users.get(0).getFirstName());
        assertEquals(lastName, users.get(0).getLastName());
        assertEquals(firstName+" "+lastName, users.get(0).getFullName());
        assertTrue(StringUtils.isNoneBlank(users.get(0).getFullName()));
        assertEquals("ACTIVE", users.get(0).getStatus());
        assertTrue(users.get(0).isActive());
        assertTrue(users.get(0).isDeveloper());

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", token));
        assertNotNull(response);
        assertEquals(200, response.getHttpCode());
        assertTrue(response.getBody() instanceof Json);
        user = (Json) response.getBody();
        assertNotNull(user);
        assertEquals(email, user.string("email"));
        assertEquals(firstName, user.string("firstName"));
        assertEquals(lastName, user.string("lastName"));
        assertEquals(firstName+" "+lastName, user.string("name"));
        assertTrue(user.bool("active"));
        assertTrue(user.bool("developer"));
        assertNull(user.string("id"));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", email));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", email)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", email));
        assertNotNull(response);
        assertEquals(200, response.getHttpCode());
        assertTrue(response.getBody() instanceof Json);
        user = (Json) response.getBody();
        assertNotNull(user);
        assertEquals(email, user.string("email"));
        assertEquals(firstName, user.string("firstName"));
        assertEquals(lastName, user.string("lastName"));
        assertEquals(firstName+" "+lastName, user.string("name"));
        assertTrue(user.bool("active"));
        assertTrue(user.bool("developer"));
        assertNull(user.string("id"));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", token));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", token)));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", invalidToken));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", invalidToken)));

        response = test.executeWebServices(RestMethod.GET, "users/token", null, Json.map().set("token", invalidEmail));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with token [%s]", invalidEmail)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", invalidEmail));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", invalidEmail)));

        response = test.executeWebServices(RestMethod.GET, "users/email", null, Json.map().set("email", invalidToken));
        assertNotNull(response);
        assertEquals(404, response.getHttpCode());
        assertTrue(response.getBody().toString().contains(String.format("User not found with email [%s]", invalidToken)));

        // clean users
        test.clearAppUsers();
        users = test.getAppUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());

        logger.info("-- END");
    }
}
