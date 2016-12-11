package london.stambourne6.swimlog.controller;

import london.stambourne6.swimlog.model.User;
import london.stambourne6.swimlog.model.Database;
import london.stambourne6.swimlog.model.UserRole;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private final Database database;

    @Inject
    public AuthorizationFilter(Database database) {
        this.database = database;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Class<?> resourceClass = resourceInfo.getResourceClass();
        List<UserRole> classRoles = extractRoles(resourceClass);

        Method resourceMethod = resourceInfo.getResourceMethod();
        List<UserRole> methodRoles = extractRoles(resourceMethod);

        User authenticatedUser = database.userForName(requestContext.getSecurityContext().getUserPrincipal().getName());

        if ((!methodRoles.isEmpty() && !methodRoles.contains(authenticatedUser.role())) ||
             (methodRoles.isEmpty() && !classRoles.contains(authenticatedUser.role()))) {
            requestContext.abortWith(
                    Util.responseWithStatusAndMessage(
                            Response.Status.FORBIDDEN,
                            "You are not authorized to access this resource"));
        }
    }

    private List<UserRole> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<>();
            } else {
                UserRole[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }
}
