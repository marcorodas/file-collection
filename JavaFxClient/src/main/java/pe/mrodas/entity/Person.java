package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author skynet
 */
@Accessors(chain = true)
@Data
public class Person {

    private Integer idPerson;
    private String firstname;
    private String lastname;
    private String identityCode;
    private String address;
    private String phone;
    private String cellphone;
    private String mail;

    public String getFullName() {
        return String.format("%s %s", firstname, lastname)
                .replace("null", "").trim();
    }
}
