package london.stambourne6.swimlog.controller;

import london.stambourne6.swimlog.model.Database;
import london.stambourne6.swimlog.model.Swim;
import london.stambourne6.swimlog.model.UserRole;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class SwimsEndpointImpl implements SwimsEndpoint {

    private final Database database;
    private final ArgumentChecker argsChecker;

    @Context
    SecurityContext securityContext;

    @Inject
    public SwimsEndpointImpl(Database database) {
        this.database = database;
        this.argsChecker = new ArgumentChecker(database);
    }

    @Secured
    @Override
    public Response deleteSwim(int swimId) {
        return argsChecker.produceWithExistingSwim(swimId, swim -> argsChecker.runIfOwnerOrAdminQuerying(swim, () -> database.deleteSwim(swim), securityContext));
    }

    @Secured
    @Override
    public Response updateSwim(int swimId, Swim newSwimState) {
        // Why so complicated you ask? That's because both old/new swims need to be checked to ensure they belong to the user making the change
        return argsChecker.produceWithExistingSwim(
                swimId,
                updatedSwim ->
                        argsChecker.runIfOwnerOrAdminQuerying(
                            updatedSwim,
                            () -> argsChecker.runIfOwnerOrAdminQuerying(
                                            newSwimState,
                                            () -> database.updateSwim(updatedSwim, newSwimState),
                                            securityContext),
                            securityContext));
    }

    @Secured(UserRole.Admin)
    @Override
    public Response listSwims(@Nullable LocalDateParam fromDate, @Nullable LocalDateParam toDate) {
        return Response.ok(Util.swimsBetween(database.swims(), fromDate, toDate)).build();
    }

    @Secured
    @Override
    public Response createSwim(Swim swim) {
        return argsChecker.runWithCheckedSwimState(swim, () -> database.insertSwim(swim));
    }
}
