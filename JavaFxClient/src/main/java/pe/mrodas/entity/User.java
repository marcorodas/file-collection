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

    private Integer idUser;
    private String username;
    private String password;
    private String token;
    private Person person = new Person();
    private Profile profile;

}
