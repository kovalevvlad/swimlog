package london.stambourne6.swimlog.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import london.stambourne6.swimlog.model.UserRole;

public class PublicUserInfo {
    private final String username;
    private final UserRole role;
    private final int id;

    @JsonCreator
    public PublicUserInfo(@JsonProperty("username") String username, @JsonProperty("role") UserRole role, @JsonProperty("id") int id) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicUserInfo)) return false;

        PublicUserInfo that = (PublicUserInfo) o;

        return role == that.role && id != that.id && username.equals(that.username);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username, role, id);
    }
}
