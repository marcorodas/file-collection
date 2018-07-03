package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class Config {
    private String workingDir;
    private Environment environment;
}
