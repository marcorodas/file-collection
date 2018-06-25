package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author skynet
 */
@Accessors(chain = true)
@Data
public class Credential {

    private String username;
    private String password;
}
