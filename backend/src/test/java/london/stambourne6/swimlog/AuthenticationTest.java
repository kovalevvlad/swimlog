package london.stambourne6.swimlog;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;

public class AuthenticationTest extends EndpointTest {

    private static class HttpActionWithName implements HttpAction {

        private final HttpAction action;
        private final String name;

        private HttpActionWithName(HttpAction action, String name) {
            this.action = action;
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public Response perform(Invocation.Builder target) {
            return action.perform(target);
        }
    }

    private static class PathAction {
        private final String path;
        private final HttpActionWithName action;

        public PathAction(String path, HttpActionWithName action) {
            this.path = path;
            this.action = action;
        }

        public String path() {
            return path;
        }

        public HttpAction action() {
            return action;
        }

        @Override
        public String toString() {
            return String.format("%s %s", action.name(), path);
        }
    }

    @Test
    public void testCannotAccessWithoutAuthenticating() {
        final HttpActionWithName get = new HttpActionWithName(b -> b.get(), "GET");
        final HttpActionWithName post = new HttpActionWithName(b -> b.post(Entity.entity(0, MediaType.APPLICATION_JSON_TYPE)), "POST");
        final HttpActionWithName put = new HttpActionWithName(b -> b.put(Entity.entity(0, MediaType.APPLICATION_JSON_TYPE)), "PUT");
        final HttpActionWithName delete = new HttpActionWithName(b -> b.delete(), "DELETE");

        Stream<PathAction> pathsToCheck =
                Stream.of(
                        new PathAction("api/swims", get),
                        new PathAction("api/swims", post),
                        new PathAction("api/swims/123", put),
                        new PathAction("api/swims/123", delete),

                        new PathAction("api/users", get),
                        new PathAction("api/users/123", get),
                        new PathAction("api/users/123", delete),
                        new PathAction("api/users/123", put),
                        new PathAction("api/users/username", get));

        List<PathAction> violations = pathsToCheck.filter(this::canAccessUnauthorised).collect(Collectors.toList());
        if (violations.size() > 0) {
            throw new AssertionError(String.format("The following endpoints were accessible unauthorised: %s", violations));
        }
    }

    private boolean canAccessUnauthorised(PathAction pathAction) {
        Response response = pathAction.action().perform(targetForCredentials("INCORRECT_USERNAME", "INCORRECT_PASSWORD").path(pathAction.path()).request());
        return response.getStatus() != Response.Status.UNAUTHORIZED.getStatusCode();
    }
}
