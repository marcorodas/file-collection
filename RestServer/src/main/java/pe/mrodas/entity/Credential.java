package pe.mrodas.entity;

import lombok.Data;

/**
 *
 * @author skynet
 */
@Data
public class Credential {

    private String username;
    private String password;
    private Environment environment;
}
