package london.stambourne6.swimlog.controller;

import com.google.common.base.CharMatcher;
import london.stambourne6.swimlog.model.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static london.stambourne6.swimlog.controller.Util.responseWithStatusAndMessage;

public class ArgumentChecker {

    private final Database database;
    private static final Pattern allowedUsernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");

    public ArgumentChecker(Database database) {
        this.database = database;
    }

    public Response runWithExistingUser(int userId, Consumer<User> runWithUser) {
        User user = database.userForId(userId);
        if (user == null) {
            return Util.responseWithStatusAndMessage(Response.Status.NOT_FOUND, "User with id %d does not exist", userId);
        }
        else {
            runWithUser.accept(user);
            return Response.ok().build();
        }
    }

    public Response produceWithExistingUser(int userId, Function<User, Response> runWithUser) {
        User user = database.userForId(userId);
        if (user == null) {
            return Util.responseWithStatusAndMessage(Response.Status.NOT_FOUND, "User with id %d does not exist", userId);
        }
        else {
            return runWithUser.apply(user);
        }
    }

    public Response produceWithExistingUsername(String username, Function<User, Response> runWithUser) {
        User user = database.userForName(username);
        if (user == null) {
            return Util.responseWithStatusAndMessage(Response.Status.NOT_FOUND, "User with name %d does not exist", username);
        }
        else {
            return runWithUser.apply(user);
        }
    }

    public Response runWithCheckedSwimState(Swim swim, Runnable runnable) {
        return produceWithExistingUser(swim.getUserId(), user -> {
            if (swim.getDistanceKm() < 0) {
                return responseWithStatusAndMessage(Response.Status.BAD_REQUEST, "Distance must be a positive number but was %f", swim.getDistanceKm());
            } else if (swim.getDurationSeconds() < 0) {
                return responseWithStatusAndMessage(Response.Status.BAD_REQUEST, "Duration must be a positive number but was %f", swim.getDurationSeconds());
            } else {
                runnable.run();
                return Response.ok().build();
            }
        });
    }

    public Response produceWithExistingSwim(int swimId, Function<SwimWithId, Response> swimConsumer) {
        SwimWithId swim = database.swimForId(swimId);
        if (swim == null) {
            return Util.responseWithStatusAndMessage(Response.Status.NOT_FOUND, "Swim with id %d does not exist", swimId);
        }
        else {
            return swimConsumer.apply(swim);
        }
    }

    public Response runWithCheckedUserCredentials(PlaintextUserCredentials credentials, Runnable credentialConsumer) {
        if (!allowedUsernamePattern.matcher(credentials.getUsername()).matches()) {
            return Util.responseWithStatusAndMessage(Response.Status.BAD_REQUEST, "Username can only contain alphanumeric characters and underscore (_)");
        }
        else if (!CharMatcher.ascii().matchesAllOf(credentials.getPassword())) {
            return Util.responseWithStatusAndMessage(Response.Status.BAD_REQUEST, "Password can only contain ASCII characters");
        }
        else if (credentials.getUsername() == null || credentials.getUsername().length() < 5) {
            return Util.responseWithStatusAndMessage(Response.Status.BAD_REQUEST, "Username must be at least 5 characters long");
        }
        else if (credentials.getPassword() == null || credentials.getPassword().length() < 7) {
            return Util.responseWithStatusAndMessage(Response.Status.BAD_REQUEST, "Password must be at least 7 characters long");
        }
        else {
            credentialConsumer.run();
            return Response.ok().build();
        }
    }

    public Response runIfOwnerOrAdminQuerying(Swim swim, Runnable swimConsumer, SecurityContext securityContext) {
        User authenticatedUser = database.userForName(securityContext.getUserPrincipal().getName());
        if (authenticatedUser.id() == swim.getUserId() || authenticatedUser.role() == UserRole.Admin) {
            swimConsumer.run();
            return Response.ok().build();
        }
        else {
            return Util.responseWithStatusAndMessage(Response.Status.FORBIDDEN, "Users can only access their own swim records");
        }
    }
}
