package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.FileItem;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.SqlInOperator;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class FileItemDA {

    public List<FileItem> retrieveContent(List<Integer> idFiles) throws Exception {
        if (!idFiles.isEmpty()) {
            SqlInOperator inIdFiles = new SqlInOperator(idFiles);
            SqlQuery<FileItem> query = new SqlQuery<>(FileItem.class).setSql(new String[]{
                    "SELECT idFile, md5, creation, content",
                    "   FROM file_item",
                    "   WHERE idFile IN " + inIdFiles
            }).setMapper((mapper, result, rs) -> {
                mapper.map(result::setIdFile, rs::getInt);
                mapper.map(result::setMd5, rs::getString);
                mapper.map(result::setCreation, rs::getDate);
                mapper.map(result::setContent, rs::getBytes);
            });
            inIdFiles.getParameters().forEach(query::addParameter);
            return query.executeList();
        }
        return new ArrayList<>();
    }

    public FileItem select(Connection connection, Integer idFile) throws Exception {
        List<Tag> tags = FileTagDA.select(connection, idFile);
        return new SqlQuery<>(FileItem.class, connection, false).setSql(new String[]{
                "SELECT idFile, md5",
                "   FROM file_item",
                "   WHERE idFile = :idFile"
        }).setMapper((mapper, result, rs) -> {
            mapper.map(result::setIdFile, rs::getInt);
            mapper.map(result::setMd5, rs::getString);
        }).addParameter("idFile", idFile).executeFirst().setTags(tags);
    }

    public List<FileItem> select(List<Tag> tags) throws Exception {
        SqlQuery<FileItem> query = new SqlQuery<>(FileItem.class);
        if (tags.isEmpty()) {
            query.setSql("SELECT idFile, md5 FROM file ORDER BY creation DESC");
        } else {
            SqlInOperator inIdTags = new SqlInOperator(tags.stream().map(Tag::getIdTag));
            query.setSql(new String[]{
                    "SELECT F.idFile, F.md5",
                    "   FROM file_item F",
                    "   INNER JOIN file_x_tag FxT",
                    "       ON FxT.idFile = F.idFile",
                    "   WHERE FxT.idTag IN " + inIdTags,
                    "   ORDER BY F.creation DESC"
            });
            inIdTags.getParameters().forEach(query::addParameter);
        }
        return query.setMapper((mapper, result, rs) -> {
            mapper.map(result::setIdFile, rs::getInt);
            mapper.map(result::setMd5, rs::getString);
        }).executeList();
    }

    private void insert(Connection connection, FileItem item) throws Exception {
        new SqlInsert("file", item::setIdFile)
                .addField("md5", item.getMd5())
                .addField("creation", item.getCreation())
                .addField("content", item.getContent())
                .execute(connection);
        for (Tag tag : item.getTags()) {
            FileTagDA.insert(connection, item.getIdFile(), tag.getIdTag());
        }
    }

    private void save(Connection connection, FileItem item) throws Exception {
        if (item.getIdFile() == null) {
            insert(connection, item);
        } else {
            FileTagDA.update(connection, item.getIdFile(), item.getTags());
        }
    }

}
