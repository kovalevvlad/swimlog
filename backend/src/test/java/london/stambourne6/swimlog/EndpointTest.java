package london.stambourne6.swimlog;

import london.stambourne6.swimlog.controller.PlaintextUserCredentials;
import london.stambourne6.swimlog.controller.PlaintextUserCredentialsWithRole;
import london.stambourne6.swimlog.controller.PublicUserInfo;
import london.stambourne6.swimlog.controller.Util;
import london.stambourne6.swimlog.model.Database;
import london.stambourne6.swimlog.model.Swim;
import london.stambourne6.swimlog.model.SwimWithId;
import london.stambourne6.swimlog.model.UserRole;
import org.apache.commons.collections4.Equator;
import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

public class EndpointTest extends JerseyTest {

    protected interface HttpAction {
        Response perform(Invocation.Builder target);
    }

    private final Random random = new Random();

    @Override
    protected Application configure() {
        try {
            File tempFile = File.createTempFile("UserEndpointImpl_test", ".mv.db");
            tempFile.deleteOnExit();
            return Util.resourceConfig(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a temp file for backing a db", e);
        }
    }

    public static void throwForNon200(Response response) {
        if (response.getStatus() != 200) {
            throw new RuntimeException(
                    String.format("Server returned status %d. Message: %s.",
                            response.getStatus(),
                            response.readEntity(String.class)));
        }
    }

    public FullUserInfo insertRandomUser() {
        String username = RandomStringUtils.randomAlphanumeric(7);
        String password = RandomStringUtils.randomAlphabetic(7);
        PlaintextUserCredentials newUser = new PlaintextUserCredentials(username, password);
        Response insertedUserResponse = adminTarget().path("api/users").request().post(Entity.entity(newUser, MediaType.APPLICATION_JSON_TYPE));
        throwForNon200(insertedUserResponse);

        URI userInfoUri = UriBuilder.fromUri("api/users/{username}").resolveTemplate("username", newUser.getUsername()).build();
        Response response = adminTarget().path(userInfoUri.toASCIIString()).request().get();
        throwForNon200(response);
        PublicUserInfo newUserInfo = response.readEntity(PublicUserInfo.class);
        return new FullUserInfo(newUserInfo.getId(), username, password, newUserInfo.getRole());
    }

    public PublicUserInfo userInfoForUser(PlaintextUserCredentials newUser) {
        URI userInfoUri = UriBuilder.fromUri("api/users/{username}").resolveTemplate("username", newUser.getUsername()).build();
        Response response = adminTarget().path(userInfoUri.toASCIIString()).request().get();
        throwForNon200(response);
        return response.readEntity(PublicUserInfo.class);
    }

    public Swim insertRandomSwim(PublicUserInfo user) {
        Swim randomSwim = randomSwim(user);
        throwForNon200(adminTarget().path("api/swims").request().post(Entity.entity(randomSwim, MediaType.APPLICATION_JSON_TYPE)));
        return randomSwim;
    }

    public Swim randomSwim(PublicUserInfo user) {
        LocalDate randomDate = LocalDate.of(2010 + random.nextInt(10), 1 + random.nextInt(12), 1 + random.nextInt(28));
        return new Swim(randomDate, random.nextDouble() * 100, user.getId(), random.nextDouble() * 10000);
    }

    public List<SwimWithId> swimsForUser(PublicUserInfo user) {
        URI userSwimsUri = UriBuilder.fromUri("api/users/{userid}/swims").resolveTemplate("userid", user.getId()).build();
        Response response = adminTarget().path(userSwimsUri.toASCIIString()).request().get();
        throwForNon200(response);
        return response.readEntity(new GenericType<List<SwimWithId>>() {});
    }

    public Equator<Swim> swimEquator() {
        return new Equator<Swim>() {
            @Override
            public boolean equate(Swim a, Swim b) {
                if ((a == null) != (b == null)) {
                    return false;
                } else if (a == b) {
                    return true;
                } else {
                    return a.getDate().equals(b.getDate()) &&
                            Math.abs(a.getDistanceKm() - b.getDistanceKm()) < 0.0001 &&
                            Math.abs(a.getDurationSeconds() - b.getDurationSeconds()) < 0.0001 &&
                            a.getUserId() == b.getUserId();
                }
            }

            @Override
            public int hash(Swim swim) {
                return Objects.hash(swim.getDate(), swim.getDistanceKm(), swim.getDurationSeconds(), swim.getUserId());
            }
        };
    }

    public WebTarget targetForCredentials(String username, String password) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(username, password);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(feature) ;

        return ClientBuilder.newClient(clientConfig).target(getBaseUri());
    }
    
    public WebTarget adminTarget() {
        return targetForCredentials(Database.DEFAULT_USERNAME, Database.DEFAULT_PASSWORD);
    }

    public WebTarget unauthorizedTarget() {
        return client().target(getBaseUri());
    }

    public WebTarget targetAsUser(FullUserInfo userInfo) {
        return targetForCredentials(userInfo.getUsername(), userInfo.password());
    }

    public void testEndpointOnlyAcceptsFollowingUserRoles(String relativeUrl, Collection<UserRole> roles, HttpAction action) {
        HashMap<UserRole, Response.StatusType> badResponses = new HashMap<>();

        for (UserRole testedRole : UserRole.values()) {
            FullUserInfo userWithRole = insertRandomUserWithRole(testedRole);
            Response responseWithRole = action.perform(targetForCredentials(userWithRole.getUsername(), userWithRole.password()).path(relativeUrl).request());
            boolean isUnauthorised = responseWithRole.getStatus() == Response.Status.FORBIDDEN.getStatusCode();
            if (roles.contains(testedRole) && isUnauthorised) {
                 badResponses.put(testedRole, Response.Status.FORBIDDEN);
            }
            else if (!roles.contains(testedRole) && !isUnauthorised) {
                badResponses.put(testedRole, responseWithRole.getStatusInfo());
            }
        }

        if (badResponses.size() > 0) {
            throw new AssertionError(
                    String.format(
                            "Expected %s roles to be ok and the others to be forbidden, but got these violations: %s",
                            roles,
                            badResponses));
        }
    }

    private FullUserInfo insertRandomUserWithRole(UserRole role) {
        FullUserInfo randomUser = insertRandomUser();
        URI updateUri = UriBuilder.fromUri("api/users/{userid}").resolveTemplate("userid", randomUser.getId()).build();
        throwForNon200(
                adminTarget()
                        .path(updateUri.toASCIIString())
                        .request()
                        .put(Entity.entity(
                                new PlaintextUserCredentialsWithRole(randomUser.getUsername(), randomUser.password(), role),
                                MediaType.APPLICATION_JSON_TYPE)));
        return new FullUserInfo(randomUser.getId(), randomUser.getUsername(), randomUser.password(), role);
    }
}
