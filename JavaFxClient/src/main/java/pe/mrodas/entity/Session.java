package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;

@Accessors(chain = true)
@Data
public class Session {
    private String url;
    private Path workingDir;
    private Person person;
}
