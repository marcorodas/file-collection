package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
public class Session {
    private String workingDir;
    private User user;
    private List<Root> rootList = new ArrayList<>();
}
