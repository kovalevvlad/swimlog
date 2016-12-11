package london.stambourne6.swimlog.model;

import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.apache.commons.codec.digest.DigestUtils.sha512;

public class PasswordManager {
    private final SecureRandom random = new SecureRandom();
    private final static int SALT_SIZE_BITS = 512;

    public byte[] randomSalt() {
        int saltSizeBytes = SALT_SIZE_BITS / 8;
        byte bytes[] = new byte[saltSizeBytes];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] hashPassword(@NotNull String password, byte[] salt) {
        try {
            byte[] passwordBytes = password.getBytes("UTF-8");
            return sha512(ArrayUtils.addAll(passwordBytes, salt));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Only support UTF-8 passwords", e);
        }
    }

    public boolean passwordCorrect(String checkedPassword, @NotNull byte[] salt, @NotNull byte[] originalHashedPassword) {
        byte[] providedPasswordHash = hashPassword(checkedPassword, salt);
        return Arrays.equals(originalHashedPassword, providedPasswordHash);
    }
}
