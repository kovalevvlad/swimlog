package london.stambourne6.swimlog.model;

import com.google.common.base.Preconditions;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import london.stambourne6.swimlog.controller.PlaintextUserCredentials;
import london.stambourne6.swimlog.controller.PlaintextUserCredentialsWithRole;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class Database {

    public final static String DEFAULT_USERNAME = "admin";
    public final static String DEFAULT_PASSWORD = "changeme";

    private final File dataFile;
    private PasswordManager pm;

    public Database(@NotNull File dataFile, @NotNull PasswordManager pm) {
        Preconditions.checkArgument(dataFile.getName().endsWith(".mv.db"), "Database filename must end with .mv.db");
        this.pm = pm;
        try {
            Class.forName("org.h2.Driver");
            this.dataFile = dataFile;
            if (dataFile.length() == 0) {
                try (Connection con = newConnection()) {
                    // Install schema
                    String initQuery = IOUtils.toString(
                            this.getClass().getClassLoader().getResourceAsStream("initialise_db.sql"),
                            Charset.forName("UTF-8"));
                    try (Statement st = con.createStatement()) {
                        st.execute(initQuery);
                    }

                    // Install default admin user
                    insertUser(new PlaintextUserCredentials(DEFAULT_USERNAME, DEFAULT_PASSWORD), UserRole.Admin);
                }
            }
        } catch(ClassNotFoundException e){
            throw new RuntimeException("Unable to load H2 drivers", e);
        } catch (SQLException e) {
            throw new RuntimeException("Encountered issues when setting up DB schema", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load schema SQL file", e);
        }
    }

    public @NotNull User insertUser(@NotNull PlaintextUserCredentials user, UserRole role) {
        byte[] salt = pm.randomSalt();
        byte[] hashedPassword = pm.hashPassword(user.getPassword(), salt);
        return runWithPreparedStatement(
                "INSERT INTO \"user\" (name, role_id, salt, hashed_password)\n" +
                "SELECT ?, id, ?, ? FROM \"role\" WHERE name = ?",
                st -> {
                    st.setString(1, user.getUsername());
                    st.setBytes(2, salt);
                    st.setBytes(3, hashedPassword);
                    st.setString(4, role.name());
                    st.executeUpdate();
                    try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                        Preconditions.checkState(generatedKeys.next());
                        int userId = generatedKeys.getInt(1);
                        return new User(userId, user.getUsername(), role, salt, hashedPassword);
                    }
                });
    }

    public void deleteUser(@NotNull User user) {
        runWithPreparedStatement(
                "DELETE FROM swim WHERE user_id = ?",
                st -> {
                    st.setInt(1, user.id());
                    st.execute();
                });

        runWithPreparedStatement(
                "DELETE FROM \"user\" WHERE id = ?",
                st -> {
                    st.setInt(1, user.id());
                    st.execute();
                });
    }

    public @Nullable User userForId(int userId) {
        return runWithPreparedStatement(
            "SELECT u.name, r.name, u.salt, u.hashed_password FROM \"user\" AS u\n" +
            "JOIN \"role\" AS r ON r.id = u.role_id WHERE u.id = ?",
            st -> {
                st.setInt(1, userId);
                try (ResultSet rs = st.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    } else {
                        User fetchedUser = new User(userId, rs.getString(1), UserRole.valueOf(rs.getString(2)), rs.getBytes(3), rs.getBytes(4));
                        Preconditions.checkState(!rs.next(), String.format("Got multiple users for userId = %d", userId));
                        return fetchedUser;
                    }
                }
            });
    }

    public @Nullable User userForName(@NotNull String username) {
        return runWithPreparedStatement(
                "SELECT u.id, r.name, u.salt, u.hashed_password FROM \"user\" AS u\n" +
                        "JOIN \"role\" AS r ON r.id = u.role_id WHERE u.name = ?",
                st -> {
                    st.setString(1, username);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) {
                            return null;
                        } else {
                            User fetchedUser = new User(rs.getInt(1), username, UserRole.valueOf(rs.getString(2)), rs.getBytes(3), rs.getBytes(4));
                            Preconditions.checkState(!rs.next(), String.format("Got multiple users for name = %s", username));
                            return fetchedUser;
                        }
                    }
                });
    }

    private @Nullable User userForUsername(@NotNull String username) {
        Preconditions.checkArgument(username != null, "username must not be null");
        return runWithPreparedStatement(
                "SELECT u.id, r.name, u.salt, u.hashed_password FROM \"user\" AS u\n" +
                        "JOIN \"role\" AS r ON r.id = u.role_id WHERE u.name = ?",
                st -> {
                    st.setString(1, username);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) {
                            return null;
                        } else {
                            User fetchedUser = new User(rs.getInt(1), username, UserRole.valueOf(rs.getString(2)), rs.getBytes(3), rs.getBytes(4));
                            Preconditions.checkState(!rs.next(), String.format("Got multiple users for username = %s", username));
                            return fetchedUser;
                        }
                    }
                });
    }

    public void updateUser(@NotNull User toUpdate, @NotNull PlaintextUserCredentialsWithRole update) {
        runWithPreparedStatement(
                        "UPDATE \"user\" u\n" +
                        "SET name = ?, role_id = (SELECT id FROM \"role\" AS r WHERE r.name = ?), salt = ?, hashed_password = ?\n" +
                        "WHERE u.id = ?",
                        st -> {
                            byte[] salt = pm.randomSalt();
                            byte[] hashedPassword = pm.hashPassword(update.getPassword(), salt);
                            st.setString(1, update.getUsername());
                            st.setString(2, update.getRole().name());
                            st.setBytes(3, salt);
                            st.setBytes(4, hashedPassword);
                            st.setInt(5, toUpdate.id());
                            st.execute();
                        });
    }

    public @NotNull Set<User> users() {
        String query = "SELECT u.id, r.name, u.name, u.salt, u.hashed_password FROM \"user\" AS u JOIN \"role\" AS r on r.id = u.role_id";
        return runWithPreparedStatement(query, st -> {
            Set<User> result = new HashSet<>();
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    UserRole role = UserRole.valueOf(rs.getString(2));
                    String name = rs.getString(3);
                    byte[] salt = rs.getBytes(4);
                    byte[] hashedPassword = rs.getBytes(5);
                    result.add(new User(id, name, role, salt, hashedPassword));
                }
            }
            return result;
        });
    }

    public @NotNull
    SwimWithId insertSwim(@NotNull Swim swim) {
        return runWithPreparedStatement(
                "INSERT INTO swim (user_id, \"date\", distance_km, duration_seconds) VALUES (?, ?, ?, ?)",
                st -> {
                    st.setInt(1, swim.getUserId());
                    st.setDate(2, Date.valueOf(swim.getDate()));
                    st.setDouble(3, swim.getDistanceKm());
                    st.setDouble(4, swim.getDurationSeconds());
                    st.execute();
                    try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                        Preconditions.checkState(generatedKeys.next());
                        return SwimWithId.fromSwimState(generatedKeys.getInt(1), swim);
                    }
                });
    }

    public void deleteSwim(@NotNull SwimWithId swim) {
        runWithPreparedStatement(
                "DELETE FROM swim WHERE id = ?;",
                st -> {
                    st.setInt(1, swim.getId());
                    st.execute();
                });
    }

    public void updateSwim(@NotNull SwimWithId toUpdate, @NotNull Swim update) {
        runWithPreparedStatement(
                "UPDATE swim\n" +
                "SET user_id = ?, \"date\" = ?, distance_km = ?, duration_seconds = ?\n" +
                "WHERE id = ?",
                st -> {
                    st.setInt(1, update.getUserId());
                    st.setDate(2, Date.valueOf(update.getDate()));
                    st.setDouble(3, update.getDistanceKm());
                    st.setDouble(4, update.getDurationSeconds());
                    st.setInt(5, toUpdate.getId());
                    st.execute();
                });
    }

    public @Nullable
    SwimWithId swimForId(int swimId) {
        return runWithPreparedStatement("SELECT \"date\", duration_seconds, distance_km, user_id FROM swim WHERE id = ?", st -> {
            st.setInt(1, swimId);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return null;
                } else {
                    SwimWithId fetchedSwim = new SwimWithId(swimId, rs.getDate(1).toLocalDate(), rs.getDouble(3), rs.getDouble(2), rs.getInt(4));
                    Preconditions.checkState(!rs.next(), String.format("Got multiple users for userId = %d", swimId));
                    return fetchedSwim;
                }
            }
        });
    }

    public @NotNull Set<SwimWithId> swims() {
        return swimsWithFilter("", st -> {});
    }

    public @NotNull Set<SwimWithId> swimsForUser(User user) {
        return swimsWithFilter(" WHERE user_id = ?", st -> st.setInt(1, user.id()));
    }

    public @Nullable User userForCreadentialsIfValid(PlaintextUserCredentials userCredentials) {
        User user = userForUsername(userCredentials.getUsername());
        boolean credentialsValid = user != null && Arrays.equals(user.hashedPassword(), pm.hashPassword(userCredentials.getPassword(), user.salt()));
        return credentialsValid ? user : null;
    }

    private Set<SwimWithId> swimsWithFilter(String sqlFilter, PreparedStatementConsumer statementInitializer) {
        String query = "SELECT user_id, id, \"date\", duration_seconds, distance_km FROM swim";
        return runWithPreparedStatement(query + sqlFilter, st -> {
            statementInitializer.run(st);
            Set<SwimWithId> result = new HashSet<>();
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt(1);
                    int id = rs.getInt(2);
                    LocalDate date = rs.getDate(3).toLocalDate();
                    double duration = rs.getDouble(4);
                    double distance = rs.getDouble(5);
                    result.add(new SwimWithId(id, date, distance, duration, userId));
                }
            }
            return result;
        });
    }

    private Connection newConnection() throws SQLException {
        String absolutePath = dataFile.getAbsolutePath();
        String connectionString = String.format("jdbc:h2:%s", absolutePath.substring(0, absolutePath.length() - ".mv.db".length()));
        Connection con = DriverManager.getConnection(connectionString, "sa", "");
        con.setAutoCommit(true);
        return con;
    }

    private interface PreparedStatementConsumer { void run(PreparedStatement st) throws SQLException; }
    private interface ProducingPreparedStatementConsumer<T> { T run(PreparedStatement st) throws SQLException; }

    private <T> T runWithPreparedStatement(String queryToPrepare, ProducingPreparedStatementConsumer<T> func) {
        return runWithSqlErrors(() -> {
            try (Connection con = newConnection()) {
                try (PreparedStatement st = con.prepareStatement(queryToPrepare)) {
                    return func.run(st);
                }
            }
        });
    }
    private void runWithPreparedStatement(String queryToPrepare, PreparedStatementConsumer func) {
        runWithSqlErrors(() -> {
            try (Connection con = newConnection()) {
                try (PreparedStatement st = con.prepareStatement(queryToPrepare)) {
                    func.run(st);
                }
            }
        });
    }

    private interface ProducerThrowingSqlExceptions<T> { T run() throws SQLException; }
    private interface RunnableThrowingSqlExceptions { void run() throws SQLException; }

    private static <T> T runWithSqlErrors(ProducerThrowingSqlExceptions<T> r) {
        try {
            return r.run();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private static void runWithSqlErrors(RunnableThrowingSqlExceptions r) {
        try {
            r.run();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}