package london.stambourne6.swimlog.controller;

import java.nio.charset.Charset;
import java.security.Principal;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import london.stambourne6.swimlog.model.Database;
import london.stambourne6.swimlog.model.User;
import org.glassfish.jersey.internal.util.Base64;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter
{
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private final Database database;

    @Inject
    public AuthenticationFilter(Database database) {
        this.database = database;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        final List<String> authorization = headers.get(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isEmpty()) {
            // No auth headers found
            requestContext.abortWith(Util.responseWithStatusAndMessage(Response.Status.UNAUTHORIZED, "Must provide authorization headers"));
        } else {
            final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
            String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8"));
            final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
            final String username = tokenizer.nextToken();
            final String password = tokenizer.nextToken();

            User user = database.userForCreadentialsIfValid(new PlaintextUserCredentials(username, password));
            if (user == null) {
                // invalid Username or password
                requestContext.abortWith(Util.responseWithStatusAndMessage(Response.Status.UNAUTHORIZED, "Incorrect username/password"));
            }
            else {
                requestContext.setSecurityContext(
                new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> username;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return true;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return null;
                    }
                });
            }
        }
    }
}