package london.stambourne6.swimlog.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import london.stambourne6.swimlog.model.UserRole;

public class PlaintextUserCredentialsWithRole extends PlaintextUserCredentials {

    private final UserRole role;

    @JsonCreator
    public PlaintextUserCredentialsWithRole(@JsonProperty("username") String username,
                                            @JsonProperty("password") String password,
                                            @JsonProperty("role") UserRole role) {
        super(username, password);
        this.role = role;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaintextUserCredentialsWithRole)) return false;
        if (!super.equals(o)) return false;

        PlaintextUserCredentialsWithRole that = (PlaintextUserCredentialsWithRole) o;

        return role == that.role && getRole() == that.getRole() && getUsername().equals(that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(role, getUsername(), getRole());
    }
}
