package london.stambourne6.swimlog.model;

import com.google.common.base.Objects;
import com.sun.istack.internal.NotNull;

import java.util.Arrays;

public class User {
    private final String username;
    private final UserRole role;
    private final byte[] salt;
    private final byte[] hashedPassword;
    private final int id;

    public User(int id, @NotNull String username, @NotNull UserRole role, @NotNull byte[] salt, @NotNull byte[] hashedPassword) {
        this.role = role;
        this.username = username;
        this.salt = Arrays.copyOf(salt, salt.length);
        this.hashedPassword = Arrays.copyOf(hashedPassword, hashedPassword.length);
        this.id = id;
    }

    public int id() {
        return id;
    }

    public String username() {
        return username;
    }

    public UserRole role() {
        return role;
    }

    public byte[] salt() {
        return Arrays.copyOf(salt, salt.length);
    }

    public byte[] hashedPassword() {
        return Arrays.copyOf(hashedPassword, hashedPassword.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return this.id == user.id &&
                this.role() == user.role() &&
                Arrays.equals(this.salt(), user.salt()) &&
                Arrays.equals(this.hashedPassword(), user.hashedPassword()) &&
                this.username().equals(user.username());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id, role(), Arrays.hashCode(salt()), Arrays.hashCode(hashedPassword()), username());
    }
}
