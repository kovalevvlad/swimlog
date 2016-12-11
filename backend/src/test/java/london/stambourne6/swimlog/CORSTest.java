package london.stambourne6.swimlog;

import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;

public class CORSTest extends EndpointTest {

    @Test
    public void testCorsHeadersArePresent() {
        Response response = adminTarget().path("api/users").request().options();
        throwForNon200(response);
        MultivaluedMap<String, Object> headers = response.getHeaders();
        assertTrue(headers.containsKey("Access-Control-Allow-Origin"));
        assertTrue(headers.containsKey("Access-Control-Allow-Headers"));
        assertTrue(headers.containsKey("Access-Control-Allow-Credentials"));
        assertTrue(headers.containsKey("Access-Control-Allow-Methods"));
    }
}
