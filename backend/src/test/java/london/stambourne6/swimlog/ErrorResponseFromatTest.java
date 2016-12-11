package london.stambourne6.swimlog;

import london.stambourne6.swimlog.controller.PlaintextUserCredentials;
import london.stambourne6.swimlog.controller.Error;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNotEquals;

public class ErrorResponseFromatTest extends EndpointTest {

    @Test
    public void testJsonErrorWhenUsernameTooShort() {
        String username = "u";
        String password = RandomStringUtils.randomAlphabetic(7);
        PlaintextUserCredentials newUser = new PlaintextUserCredentials(username, password);
        Response insertedUserResponse = adminTarget().path("api/users").request().post(Entity.entity(newUser, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(MediaType.APPLICATION_JSON_TYPE, insertedUserResponse.getMediaType());
        Error err = insertedUserResponse.readEntity(Error.class);
        assertNotNull(err);
    }

    @Test
    public void testJsonErrorWhenInvalidUsernamePassword() {
        FullUserInfo correctUserInfo = insertRandomUser();
        FullUserInfo invalidUserInfo = new FullUserInfo(correctUserInfo.getId(), correctUserInfo.getUsername(), correctUserInfo.password() + "x", correctUserInfo.getRole());

        URI userInfoUri = UriBuilder.fromUri("api/users/{username}").resolveTemplate("username", invalidUserInfo.getUsername()).build();
        Response response = targetAsUser(invalidUserInfo).path(userInfoUri.toASCIIString()).request().get();
        assertNotEquals(Response.Status.OK, response.getStatus());
        Error err = response.readEntity(Error.class);
        assertNotNull(err);
    }

    @Test
    public void testJsonErrorWhenInvalidParametersSubmitted() {
        Response insertedUserResponse = adminTarget().path("api/users").request().post(Entity.entity("{'username': 'someusername'}", MediaType.APPLICATION_JSON_TYPE));
        assertEquals(MediaType.APPLICATION_JSON_TYPE, insertedUserResponse.getMediaType());
        Error err = insertedUserResponse.readEntity(Error.class);
        assertNotNull(err);
    }
}
