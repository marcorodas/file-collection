package pe.mrodas.entity;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

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
    private List<Root> rootList = new ArrayList<>();

}
