package london.stambourne6.swimlog.controller;

import com.sun.istack.internal.Nullable;
import london.stambourne6.swimlog.model.User;
import london.stambourne6.swimlog.model.Database;
import london.stambourne6.swimlog.model.UserRole;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;


public class UsersEndpointImpl implements UsersEndpoint {

    private final Database database;
    private final ArgumentChecker argsChecker;

    @Context
    SecurityContext securityContext;

    @Inject
    public UsersEndpointImpl(Database database) {
        this.database = database;
        this.argsChecker = new ArgumentChecker(database);
    }

    @Secured({UserRole.Admin, UserRole.Manager})
    @Override
    public Response deleteUser(int userId) {
        return argsChecker.runWithExistingUser(userId, database::deleteUser);
    }

    @Secured({UserRole.Admin, UserRole.Manager})
    @Override
    public Response updateUser(int userId, PlaintextUserCredentialsWithRole newUserCredentials) {
        return argsChecker.runWithExistingUser(
                userId,
                user -> argsChecker.runWithCheckedUserCredentials(newUserCredentials, () -> database.updateUser(user, newUserCredentials)));
    }

    @Secured({UserRole.Admin, UserRole.Manager})
    @Override
    public Response listUsers() {
        List<PublicUserInfo> users = database.users().stream().map(u -> new PublicUserInfo(u.username(), u.role(), u.id())).collect(Collectors.toList());
        return Response.ok(users).build();
    }

    @Override
    public Response createUser(PlaintextUserCredentials credentials) {
        return argsChecker.runWithCheckedUserCredentials(credentials, () -> database.insertUser(credentials, UserRole.User));
    }

    @Secured
    @Override
    public Response listSwimsForUser(int userId, @Nullable LocalDateParam fromDate, @Nullable LocalDateParam toDate) {
        String activeUserName = securityContext.getUserPrincipal().getName();
        User activeUser = database.userForName(activeUserName);

        if (activeUser.role() == UserRole.User && activeUser.id() != userId) {
            return Util.responseWithStatusAndMessage(Response.Status.UNAUTHORIZED, "Users can only list their own swims");
        }
        else {
            return argsChecker.produceWithExistingUser(
                    userId,
                    user -> Response.ok(Util.swimsBetween(database.swimsForUser(user), fromDate, toDate)).build());
        }
    }

    @Secured
    @Override
    public Response userInfo(String userName) {
        String activeUserName = securityContext.getUserPrincipal().getName();
        User activeUser = database.userForName(activeUserName);

        if (activeUser.role() == UserRole.User && !activeUser.username().equals(userName)) {
            return Util.responseWithStatusAndMessage(Response.Status.UNAUTHORIZED, "Users can only access their own user information");
        }
        else {
            return argsChecker.produceWithExistingUsername(
                    userName,
                    user -> Response.ok(new PublicUserInfo(user.username(), user.role(), user.id())).build());
        }
    }
}