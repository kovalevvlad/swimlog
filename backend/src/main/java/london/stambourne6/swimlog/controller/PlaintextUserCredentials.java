package london.stambourne6.swimlog.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class PlaintextUserCredentials {
    private final String username;
    private final String password;

    @JsonCreator
    public PlaintextUserCredentials(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaintextUserCredentials)) return false;

        PlaintextUserCredentials that = (PlaintextUserCredentials) o;

        return password.equals(that.password) && username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username, password);
    }
}
