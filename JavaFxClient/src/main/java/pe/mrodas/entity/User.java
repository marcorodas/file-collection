package pe.mrodas.entity;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author skynet
 */
@Accessors(chain = true)
@Data
public class User {

    public enum Profile {
        ADMIN(1), STANDARD(2);
        @Getter
        private final int id;

        Profile(int id) {
            this.id = id;
        }
    }

    private Integer idUser;
    private String username;
    private String password;
    private String salt;
    private Person person = new Person();
    private Profile profile;

    public User setIdProfile(int idProfile) {
        for (Profile profile : Profile.values()) {
            if (profile.getId() == idProfile) {
                this.profile = profile;
            }
        }
        return this;
    }
}
