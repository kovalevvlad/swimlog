package london.stambourne6.swimlog;


import london.stambourne6.swimlog.controller.PlaintextUserCredentials;
import london.stambourne6.swimlog.controller.PlaintextUserCredentialsWithRole;
import london.stambourne6.swimlog.controller.PublicUserInfo;
import london.stambourne6.swimlog.model.Swim;
import london.stambourne6.swimlog.model.SwimWithId;
import london.stambourne6.swimlog.model.UserRole;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class UserEndpointImplTest extends EndpointTest {

    @Test
    public void testCreateUserWhenUnauthorised() {
        PlaintextUserCredentials newUser = new PlaintextUserCredentials("vladimir", "password");
        assertFalse(existingUsers().contains(newUser.getUsername()));
        throwForNon200(unauthorizedTarget().path("api/users").request().post(Entity.entity(newUser, MediaType.APPLICATION_JSON_TYPE)));
        assertTrue(existingUsers().contains(newUser.getUsername()));
    }

    @Test
    public void testBadRequestForNonAsciiUsernameAndPassword() {
        PlaintextUserCredentials newUserWithBadPassword = new PlaintextUserCredentials("vladimir", "passwørd");
        Response responseForBadPassword = unauthorizedTarget().path("api/users").request().post(Entity.entity(newUserWithBadPassword, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), responseForBadPassword.getStatus());

        PlaintextUserCredentials newUserWithBadUsername = new PlaintextUserCredentials("vlædimir", "password");
        Response responseForBadUsername = unauthorizedTarget().path("api/users").request().post(Entity.entity(newUserWithBadUsername, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), responseForBadUsername.getStatus());
    }

    @Test
    public void testDeleteUser() {
        PublicUserInfo newUser = insertRandomUser();
        assertTrue(existingUsers().contains(newUser.getUsername()));
        URI deleteUserUri = UriBuilder.fromUri("api/users/{userid}").resolveTemplate("userid", newUser.getId()).build();
        throwForNon200(adminTarget().path(deleteUserUri.toASCIIString()).request().delete());
        assertFalse(existingUsers().contains(newUser.getUsername()));
    }

    @Test
    public void testUpdateUser() {
        PublicUserInfo userInfo = insertRandomUser();
        PlaintextUserCredentialsWithRole newUserInfo =
                new PlaintextUserCredentialsWithRole(
                        RandomStringUtils.randomAlphanumeric(8),
                        RandomStringUtils.randomAlphanumeric(8),
                        userInfo.getRole() == UserRole.User ? UserRole.Manager : UserRole.User);
        assertFalse(userInfo.getUsername().equals(newUserInfo.getUsername()) || userInfo.getRole() == newUserInfo.getRole());
        URI updateUri = UriBuilder.fromUri("api/users/{userid}").resolveTemplate("userid", userInfo.getId()).build();
        throwForNon200(adminTarget().path(updateUri.toASCIIString()).request().put(Entity.entity(newUserInfo, MediaType.APPLICATION_JSON_TYPE)));
        PublicUserInfo info = userInfoForUser(new PlaintextUserCredentials(newUserInfo.getUsername(), newUserInfo.getPassword()));
        assertTrue(newUserInfo.getUsername().equals(info.getUsername()) && newUserInfo.getRole() == info.getRole());
    }

    @Test
    public void testListSwimsForUser() {
        PublicUserInfo randomUser1 = insertRandomUser();
        PublicUserInfo randomUser2 = insertRandomUser();

        Swim swimUser1 = insertRandomSwim(randomUser1);
        List<SwimWithId> user1Swims = swimsForUser(randomUser1);

        Swim swimUser2 = insertRandomSwim(randomUser2);
        List<SwimWithId> user2Swims = swimsForUser(randomUser2);

        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(swimUser1), user1Swims, swimEquator()));
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(swimUser2), user2Swims, swimEquator()));
    }

    @Test
    public void testOnlyAdminsAndManagersCanListUsers() {
        testEndpointOnlyAcceptsFollowingUserRoles("api/users", Arrays.asList(UserRole.Admin, UserRole.Manager), Invocation.Builder::get);
    }

    @Test
    public void testOnlyAdminsAndManagersCanUpdateUsers() {
        testEndpointOnlyAcceptsFollowingUserRoles("api/users/123", Arrays.asList(UserRole.Admin, UserRole.Manager), b -> b.put(Entity.entity("", MediaType.APPLICATION_JSON_TYPE)));
    }

    @Test
    public void testOnlyAdminsAndManagersCanDeleteUsers() {
        testEndpointOnlyAcceptsFollowingUserRoles("api/users/123", Arrays.asList(UserRole.Admin, UserRole.Manager), Invocation.Builder::delete);
    }

    @Test
    public void userCantAccessOtherUsersUserInfo() {
        FullUserInfo user1 = insertRandomUser();
        FullUserInfo user2 = insertRandomUser();

        URI user1UserInfoUri = UriBuilder.fromUri("api/users/{username}").resolveTemplate("username", user1.getUsername()).build();
        Response responseUser1 = targetAsUser(user1).path(user1UserInfoUri.toASCIIString()).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), responseUser1.getStatus());

        Response response = targetAsUser(user2).path(user1UserInfoUri.toASCIIString()).request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void userCantAccessOtherUsersUserSwims() {
        FullUserInfo user1 = insertRandomUser();
        FullUserInfo user2 = insertRandomUser();

        URI user1Swims = UriBuilder.fromUri("api/users/{userid}/swims").resolveTemplate("userid", user1.getId()).build();

        Response responseUser1 = targetAsUser(user1).path(user1Swims.toASCIIString()).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), responseUser1.getStatus());

        Response responseUser2 = targetAsUser(user2).path(user1Swims.toASCIIString()).request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseUser2.getStatus());
    }

    @Test
    public void testListSwimsWithDateFilters() {
        PublicUserInfo user = insertRandomUser();
        Swim swim = insertRandomSwim(user);
        URI userSwimURI = UriBuilder.fromUri("api/users/{userid}/swims").resolveTemplate("userid", user.getId()).build();

        Response response = adminTarget().path(userSwimURI.toASCIIString()).request().get();
        throwForNon200(response);
        List<SwimWithId> fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(Arrays.asList(swim), fetchedSwims);

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE;

        response = adminTarget().path(userSwimURI.toASCIIString()).queryParam("fromDate", swim.getDate().plusDays(1).format(isoFormatter)).request().get();
        throwForNon200(response);
        fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(Arrays.asList(), fetchedSwims);

        response = adminTarget().path(userSwimURI.toASCIIString()).queryParam("toDate", swim.getDate().minusDays(1).format(isoFormatter)).request().get();
        throwForNon200(response);
        fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(Arrays.asList(), fetchedSwims);
    }

    private Set<String> existingUsers() {
        Response response = adminTarget().path("api/users").request().get();
        throwForNon200(response);
        List<PublicUserInfo> usersFetched = response.readEntity(new GenericType<List<PublicUserInfo>>() {});
        return usersFetched.stream().map(PublicUserInfo::getUsername).collect(Collectors.toSet());
    }
}
