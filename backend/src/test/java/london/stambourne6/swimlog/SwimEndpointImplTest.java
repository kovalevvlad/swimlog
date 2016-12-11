package london.stambourne6.swimlog;

import london.stambourne6.swimlog.controller.PublicUserInfo;
import london.stambourne6.swimlog.model.Swim;
import london.stambourne6.swimlog.model.SwimWithId;
import london.stambourne6.swimlog.model.UserRole;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.*;

public class SwimEndpointImplTest extends EndpointTest {

    @Test
    public void testDeleteSwim() {
        PublicUserInfo randomUser = insertRandomUser();
        insertRandomSwim(randomUser);
        List<SwimWithId> swimsForRandomUser = swimsForUser(randomUser);
        assertEquals(1, swimsForRandomUser.size());
        URI deleteUserUri = UriBuilder.fromUri("api/swims/{swimid}").resolveTemplate("swimid", swimsForRandomUser.get(0).getId()).build();
        Response deleteResponse = adminTarget().path(deleteUserUri.toASCIIString()).request().delete();
        throwForNon200(deleteResponse);
        assertEquals(0, swimsForUser(randomUser).size());
    }

    @Test
    public void testUpdateSwim() {
        PublicUserInfo randomUser = insertRandomUser();
        PublicUserInfo randomUser2 = insertRandomUser();
        Swim randomSwim = insertRandomSwim(randomUser);
        CollectionUtils.isEqualCollection(Arrays.asList(randomSwim), swimsForUser(randomUser), swimEquator());
        List<SwimWithId> swimsForRandomUser = swimsForUser(randomUser);
        assertEquals(1, swimsForRandomUser.size());
        URI updateUserUri = UriBuilder.fromUri("api/swims/{swimid}").resolveTemplate("swimid", swimsForRandomUser.get(0).getId()).build();
        Swim newSwimState = randomSwim(randomUser2);
        Response updateResponse = adminTarget().path(updateUserUri.toASCIIString()).request().put(Entity.entity(newSwimState, MediaType.APPLICATION_JSON_TYPE));
        throwForNon200(updateResponse);
        List<SwimWithId> swimsForRandomUser2 = swimsForUser(randomUser2);
        assertEquals(1, swimsForRandomUser2.size());
        assertTrue(swimEquator().equate(newSwimState, swimsForRandomUser2.get(0)));
    }

    @Test
    public void testListSwims() {
        PublicUserInfo userA = insertRandomUser();
        PublicUserInfo userB = insertRandomUser();

        List<Swim> expectedSwims = Arrays.asList(insertRandomSwim(userA), insertRandomSwim(userA), insertRandomSwim(userB), insertRandomSwim(userB));

        Response response = adminTarget().path("api/swims").request().get();
        throwForNon200(response);
        List<SwimWithId> fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(expectedSwims, fetchedSwims);
    }

    @Test
    public void testCreateSwim() {
        PublicUserInfo randomUser = insertRandomUser();
        assertEquals(new ArrayList(), swimsForUser(randomUser));
        Swim randomSwim = insertRandomSwim(randomUser);
        CollectionUtils.isEqualCollection(Arrays.asList(randomSwim), swimsForUser(randomUser), swimEquator());
    }

    @Test
    public void testOnlyAdminsCanListAllSwims() {
        testEndpointOnlyAcceptsFollowingUserRoles("api/swims", Arrays.asList(UserRole.Admin), Invocation.Builder::get);
    }

    @Test
    public void testOnlyAdminsCanDeleteAnySwim() {
        FullUserInfo randomUser = insertRandomUser();
        testEndpointOnlyAcceptsFollowingUserRoles(
                randomSwimUri(randomUser).toASCIIString(),
                Arrays.asList(UserRole.Admin),
                Invocation.Builder::delete);
    }

    @Test
    public void testOnlyAdminsCanUpdateAnySwim() {
        FullUserInfo randomUser = insertRandomUser();
        testEndpointOnlyAcceptsFollowingUserRoles(
                randomSwimUri(randomUser).toASCIIString(),
                Arrays.asList(UserRole.Admin),
                b -> b.put(Entity.entity(randomSwim(randomUser), MediaType.APPLICATION_JSON_TYPE)));
    }

    @Test
    public void testAnyoneCanCreateSwim() {
        testEndpointOnlyAcceptsFollowingUserRoles("api/swims", Arrays.asList(UserRole.Admin, UserRole.User, UserRole.Manager), b -> b.post(Entity.entity(0, MediaType.APPLICATION_JSON_TYPE)));
    }

    @Test
    public void testListSwimsWithDateFilters() {
        PublicUserInfo user = insertRandomUser();
        Swim swim = insertRandomSwim(user);

        Response response = adminTarget().path("api/swims").request().get();
        throwForNon200(response);
        List<SwimWithId> fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(Arrays.asList(swim), fetchedSwims);

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE;

        response = adminTarget().path("api/swims").queryParam("fromDate", swim.getDate().plusDays(1).format(isoFormatter)).request().get();
        throwForNon200(response);
        fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(Arrays.asList(), fetchedSwims);

        response = adminTarget().path("api/swims").queryParam("toDate", swim.getDate().minusDays(1).format(isoFormatter)).request().get();
        throwForNon200(response);
        fetchedSwims = response.readEntity(new GenericType<List<SwimWithId>>() {});
        CollectionUtils.isEqualCollection(Arrays.asList(), fetchedSwims);
    }

    private URI randomSwimUri(FullUserInfo user) {
        insertRandomSwim(user);
        int randomSwimId = swimsForUser(user).get(0).getId();
        return UriBuilder.fromUri("api/swims/{swimid}").resolveTemplate("swimid", randomSwimId).build();
    }
}
