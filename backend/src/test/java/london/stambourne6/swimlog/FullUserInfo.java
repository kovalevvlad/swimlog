package london.stambourne6.swimlog;

import london.stambourne6.swimlog.controller.PublicUserInfo;
import london.stambourne6.swimlog.model.UserRole;

public class FullUserInfo extends PublicUserInfo {
    private final String password;

    public FullUserInfo(int id, String username, String password, UserRole role) {
        super(username, role, id);
        this.password = password;
    }

    public String password() {
        return password;
    }
}
