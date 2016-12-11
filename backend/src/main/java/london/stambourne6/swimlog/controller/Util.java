package london.stambourne6.swimlog.controller;

import com.google.common.collect.Sets;
import london.stambourne6.swimlog.model.PasswordManager;
import london.stambourne6.swimlog.model.Database;
import london.stambourne6.swimlog.model.Swim;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class Util {

    public static ResourceConfig resourceConfig(File databaseFile) {
        AbstractBinder dbBinder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new Factory<Database>() {
                    @Override
                    public Database provide() {
                        return new Database(databaseFile, new PasswordManager());
                    }

                    @Override
                    public void dispose(Database database) {}
                }).to(Database.class).in(Singleton.class);
            }
        };

        return new ResourceConfig(
                    UsersEndpointImpl.class,
                    SwimsEndpointImpl.class,
                    AuthenticationFilter.class,
                    AuthorizationFilter.class,
                    JsonParseExceptionMapper.class,
                    CorsFilter.class)
                .register(dbBinder);
    }

    public static Response responseWithStatusAndMessage(Response.Status status, String message, Object... args) {
        return Response.status(status).entity(new Error(String.format(message, args))).build();
    }

    public static <T extends Swim> Set<T> swimsBetween(@NotNull Collection<T> swims, @Null LocalDateParam from, @Null LocalDateParam to) {
        Set<T> swimsSet = Sets.newHashSet(swims);
        if (from != null) {
            swimsSet = swimsSet.stream().filter(j -> !j.getDate().isBefore(from.date())).collect(Collectors.toSet());
        }
        if (to != null) {
            swimsSet = swimsSet.stream().filter(j -> !j.getDate().isAfter(to.date())).collect(Collectors.toSet());
        }
        return swimsSet;
    }
}
