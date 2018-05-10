package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
public class FileItem {
    private Integer idFile;
    private String name, md5;
    private List<Tag> tags = new ArrayList<>();
}
