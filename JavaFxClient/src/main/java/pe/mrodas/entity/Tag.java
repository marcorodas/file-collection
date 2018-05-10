package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class Tag {
    private Integer idTag;
    private String name;
}
