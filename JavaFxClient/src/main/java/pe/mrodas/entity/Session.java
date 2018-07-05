package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Accessors(chain = true)
@Data
public class Session {
    private String workingDir;
    private User user;
    private List<Root> rootList = new ArrayList<>();
}
