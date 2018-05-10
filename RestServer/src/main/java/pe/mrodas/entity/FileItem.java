package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Accessors(chain = true)
@Data
public class FileItem {
    private Integer idFile;
    private String name, md5;
    private Date creation;
    private byte[] content;
    private List<Tag> tags = new ArrayList<>();
}
