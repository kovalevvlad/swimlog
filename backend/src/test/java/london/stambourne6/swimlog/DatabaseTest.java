package london.stambourne6.swimlog;

import com.google.common.collect.Sets;
import london.stambourne6.swimlog.controller.PlaintextUserCredentials;
import london.stambourne6.swimlog.controller.PlaintextUserCredentialsWithRole;
import london.stambourne6.swimlog.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class DatabaseTest {

    private Database database = null;
    private PasswordManager passwordManager = new PasswordManager();
    private final Random random = new Random();

    @Before
    public void setUp() throws IOException {
        File tempDataFile = File.createTempFile("swimlog_database_test_", ".mv.db");
        tempDataFile.deleteOnExit();
        this.database = new Database(tempDataFile, passwordManager);
    }

    @Test
    public void testInsertingAndFetchingUsers() {
        String name = "vlad";
        String password = "test_password";
        UserRole role = UserRole.Manager;

        PlaintextUserCredentials newUser = new PlaintextUserCredentials(name, password);
        User insertedUser = database.insertUser(newUser, role);
        User fetchedUser = database.userForId(insertedUser.id());
        User fetchedByNameUser = database.userForName(insertedUser.username());

        assertEquals(insertedUser.id(), fetchedUser.id());
        assertEquals(insertedUser.id(), fetchedByNameUser.id());

        assertEquals(newUser.getUsername(), insertedUser.username());
        assertEquals(insertedUser.username(), fetchedUser.username());
        assertEquals(insertedUser.username(), fetchedByNameUser.username());

        assertEquals(role, insertedUser.role());
        assertEquals(insertedUser.role(), fetchedUser.role());
        assertEquals(insertedUser.role(), fetchedByNameUser.role());

        byte[] expectedHashedPassword = passwordManager.hashPassword(password, insertedUser.salt());
        assertArrayEquals(expectedHashedPassword, insertedUser.hashedPassword());
        assertArrayEquals(insertedUser.hashedPassword(), fetchedUser.hashedPassword());
        assertArrayEquals(insertedUser.hashedPassword(), fetchedByNameUser.hashedPassword());
    }

    @Test
    public void testInsertingAndFetchingSwims() {
        User insertedUser = insertRandomUser();

        LocalDate date = LocalDate.of(2000, 1, 1);
        double distance = 101.11;
        double duration = 99.5;
        Swim newSwim = new Swim(date, distance, insertedUser.id(), duration);
        SwimWithId insertedSwim = database.insertSwim(newSwim);
        SwimWithId fetchedSwim = database.swimForId(insertedSwim.getId());

        assertEquals(insertedSwim.getId(), fetchedSwim.getId());

        assertEquals(newSwim.getUserId(), insertedSwim.getUserId());
        assertEquals(insertedSwim.getUserId(), fetchedSwim.getUserId());

        assertEquals(newSwim.getDurationSeconds(), insertedSwim.getDurationSeconds(), 0.001);
        assertEquals(insertedSwim.getDurationSeconds(), fetchedSwim.getDurationSeconds(), 0.001);

        assertEquals(newSwim.getDistanceKm(), insertedSwim.getDistanceKm(), 0.001);
        assertEquals(insertedSwim.getDistanceKm(), fetchedSwim.getDistanceKm(), 0.001);

        assertEquals(newSwim.getDate(), insertedSwim.getDate());
        assertEquals(insertedSwim.getDate(), fetchedSwim.getDate());
    }

    @Test
    public void testDeleteUser() {
        User insertedUser = insertRandomUser();
        User fetchedUser = database.userForId(insertedUser.id());
        assertNotNull(fetchedUser);

        database.deleteUser(fetchedUser);
        fetchedUser = database.userForId(insertedUser.id());
        assertNull(fetchedUser);
    }

    @Test
    public void testDeleteSwim() {
        User insertedUser = insertRandomUser();

        LocalDate date = LocalDate.of(2000, 1, 1);
        double distance = 101.11;
        double duration = 99.5;
        Swim newSwim = new Swim(date, distance, insertedUser.id(), duration);
        SwimWithId insertedSwim = database.insertSwim(newSwim);
        SwimWithId fetchedSwim = database.swimForId(insertedSwim.getId());
        assertNotNull(fetchedSwim);

        database.deleteSwim(fetchedSwim);
        fetchedSwim = database.swimForId(fetchedSwim.getId());
        assertNull(fetchedSwim);
    }

    @Test
    public void testUpdateUser() {
        User insertedUser = insertRandomUser();

        String newName = "vladimir";
        String newPassword = "test_password123";
        UserRole newRole = UserRole.Manager;
        database.updateUser(insertedUser, new PlaintextUserCredentialsWithRole(newName, newPassword, newRole));

        User updatedUser = database.userForId(insertedUser.id());
        assertNotNull(updatedUser);
        assertEquals(newName, updatedUser.username());
        assertEquals(newRole, updatedUser.role());
        assertArrayEquals(passwordManager.hashPassword(newPassword, updatedUser.salt()), updatedUser.hashedPassword());
    }

    @Test
    public void testUpdateSwim() {
        User insertedUser = insertRandomUser();

        LocalDate date = LocalDate.of(2000, 1, 1);
        double distance = 101.11;
        double duration = 99.5;
        Swim newSwim = new Swim(date, distance, insertedUser.id(), duration);
        SwimWithId insertedSwim = database.insertSwim(newSwim);

        User newInsertedUser = insertRandomUser();

        LocalDate newDate = LocalDate.of(2010, 1, 1);
        double newDistance = 999.9;
        double newDuration = 53.11;
        int newUserId = newInsertedUser.id();
        database.updateSwim(insertedSwim, new Swim(newDate, newDistance, newUserId, newDuration));

        SwimWithId updatedSwim = database.swimForId(insertedSwim.getId());
        assertNotNull(updatedSwim);
        assertEquals(newDate, updatedSwim.getDate());
        assertEquals(newDistance, updatedSwim.getDistanceKm(), 0.0001);
        assertEquals(newDuration, updatedSwim.getDurationSeconds(), 0.0001);
        assertEquals(newUserId, updatedSwim.getUserId());
    }

    @Test
    public void testListUsers() {
        Collection<User> existingUsers = database.users();
        for(User existingUser : existingUsers) {
            database.deleteUser(existingUser);
        }

        Set<User> expectedUsers = Sets.newHashSet(insertRandomUser(), insertRandomUser());
        Set<User> actualUsers = database.users();
        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    public void testListSwims() {
        User insertedUser = insertRandomUser();

        LocalDate dateA = LocalDate.of(2000, 1, 1);
        double distanceA = 1000.0;
        double durationA = 555.5;

        LocalDate dateB = LocalDate.of(2016, 1, 1);
        double distanceB = 2000.0;
        double durationB = 666.6;

        Collection<SwimWithId> existingSwims = database.swims();
        for(SwimWithId existingSwim : existingSwims) {
            database.deleteSwim(existingSwim);
        }

        SwimWithId insertedA = database.insertSwim(new Swim(dateA, distanceA, insertedUser.id(), durationA));
        SwimWithId insertedB = database.insertSwim(new Swim(dateB, distanceB, insertedUser.id(), durationB));
        Set<SwimWithId> expectedSwims = Sets.newHashSet(insertedA, insertedB);
        Set<SwimWithId> actualSwims = database.swims();
        assertEquals(expectedSwims, actualSwims);
    }

    @Test
    public void testInsertingIntoDbCreatesDbFile() throws IOException {
        File tempDataFile = File.createTempFile("swimlog_database_test_", ".mv.db");
        tempDataFile.deleteOnExit();
        Database database = new Database(tempDataFile, passwordManager);
        database.insertUser(new PlaintextUserCredentials("username", "passwordz"), UserRole.User);
        assertTrue(tempDataFile.length() > 0);
    }

    private User insertRandomUser() {
        String username = RandomStringUtils.randomAlphanumeric(7);
        String password = RandomStringUtils.randomAlphanumeric(7);
        UserRole role = UserRole.values()[random.nextInt(UserRole.values().length)];
        return database.insertUser(new PlaintextUserCredentials(username, password), role);
    }
}
