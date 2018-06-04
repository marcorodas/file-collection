package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.security.SecureRandom;

/**
 * @author skynet
 */
@Accessors(chain = true)
@Data
public class User {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Integer idUser;
    private String username;
    private String password;
    private String token;
    private Person person = new Person();
    private Profile profile = new Profile();

    public User autoGenToken() {
        long longToken = Math.abs(RANDOM.nextLong());
        token = Long.toString(longToken, 16);
        return this;
    }
}
