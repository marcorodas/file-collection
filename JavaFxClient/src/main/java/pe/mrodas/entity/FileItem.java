package pe.mrodas.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class FileItem {
    private Integer idFile;
    private String md5, extension;
    private Date expiration;
    private byte[] content;
    private List<Tag> tags = new ArrayList<>();
}
