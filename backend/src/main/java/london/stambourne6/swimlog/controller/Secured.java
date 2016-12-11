package london.stambourne6.swimlog.controller;

import london.stambourne6.swimlog.model.UserRole;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Secured {
    UserRole[] value() default {UserRole.User, UserRole.Manager, UserRole.Admin};
}
