package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author skynet
 */
@Accessors(chain = true)
@Data
public class Functionality {

    private Integer idFunctionality;
    private String name;
}
