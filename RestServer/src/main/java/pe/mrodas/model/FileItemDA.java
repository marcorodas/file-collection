package pe.mrodas.model;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import lombok.experimental.UtilityClass;

import pe.mrodas.entity.FileItem;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.SqlInOperator;
import pe.mrodas.jdbc.Adapter;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;

@UtilityClass
public class FileItemDA {

    public List<FileItem> select(int idRoot, List<String> md5List) throws Exception {
        SqlInOperator<String> inOperator = md5List.isEmpty()
                ? null : new SqlInOperator<>(md5List);
        SqlQuery<FileItem> query = new SqlQuery<>(FileItem.class).setSql(new String[]{
                "SELECT DISTINCT F.idFile, F.md5, F.extension",
                "   FROM file_item F",
                "   INNER JOIN file_x_tag FT",
                "       ON F.idFile = FT.idFile",
                "   INNER JOIN tag T",
                "       ON T.idTag = FT.idTag",
                "   WHERE T.idRoot = :idRoot",
                "       AND F.expiration IS NULL",
                inOperator == null ? "" : ("AND F.md5 NOT IN " + inOperator)
        });
        query.setMapper((mapper, item, rs) -> {
            mapper.map(item::setIdFile, rs::getInt);
            mapper.map(item::setMd5, rs::getString);
            mapper.map(item::setExtension, rs::getString);
        }).addParameter("idRoot", idRoot);
        if (inOperator != null) {
            inOperator.getParameters().forEach(query::addParameter);
        }
        return query.executeList();
    }

    public FileItem select(int idFile) throws Exception {
        return new SqlQuery<>(FileItem.class).setSql(new String[]{
                "SELECT F.idFile, F.md5, F.extension, FC.content",
                "   FROM file_content FC",
                "   INNER JOIN file_item F",
                "       ON FC.idFile = F.idFile",
                "   WHERE FC.idFile = :idFile",
                "       AND F.expiration IS NULL"
        }).setMapper((mapper, item, rs) -> {
            mapper.map(item::setIdFile, rs::getInt);
            mapper.map(item::setMd5, rs::getString);
            mapper.map(item::setExtension, rs::getString);
            mapper.map(item::setContent, rs::getBytes);
        }).addParameter("idFile", idFile).executeFirst();
    }

    public List<FileItem> select(List<Integer> tagsId) throws Exception {
        SqlQuery<FileItem> query = new SqlQuery<>(FileItem.class);
        SqlInOperator<Integer> inIdTags = new SqlInOperator<>(tagsId);
        query.setSql(new String[]{
                "SELECT F.idFile, F.md5, F.extension",
                "   FROM file_item F",
                "   INNER JOIN file_x_tag FxT",
                "       ON FxT.idFile = F.idFile",
                "   WHERE FxT.idTag IN " + inIdTags,
                "       AND F.expiration IS NULL",
                "   GROUP BY F.idFile",
                "   HAVING COUNT(*) > ", String.valueOf(inIdTags.getParameters().size() - 1)
        });
        inIdTags.getParameters().forEach(query::addParameter);
        return query.setMapper((mapper, result, rs) -> {
            mapper.map(result::setIdFile, rs::getInt);
            mapper.map(result::setMd5, rs::getString);
            mapper.map(result::setExtension, rs::getString);
        }).executeList();
    }

    public List<FileItem> selectUntagged(int idCategory) throws Exception {
        return new SqlQuery<>(FileItem.class).setSql(new String[]{
                "SELECT F.idFile, F.md5, F.extension, FxT.idTag",
                "   FROM file_item F",
                "   INNER JOIN file_x_tag FxT",
                "       ON FxT.idFile = F.idFile",
                "   GROUP BY F.idFile",
                "   HAVING COUNT(*) = 1",
                "       AND FxT.idTag = :idCategory"
        }).setMapper((mapper, result, rs) -> {
            mapper.map(result::setIdFile, rs::getInt);
            mapper.map(result::setMd5, rs::getString);
            mapper.map(result::setExtension, rs::getString);
        }).addParameter("idCategory", idCategory)
                .executeList();
    }

    public static int insert(FileItem item, InputStream inputStream) throws Exception {
        return Adapter.batch(connection -> {
            new SqlInsert("file_item", item::setIdFile)
                    .addField("md5", item.getMd5())
                    .addField("extension", item.getExtension())
                    .execute(connection);
            for (Tag tag : item.getTags()) {
                FileTagDA.insert(connection, item.getIdFile(), tag.getIdTag());
            }
            new SqlInsert("file_content")
                    .addField("idFile", item.getIdFile())
                    .addField("content", inputStream)
                    .execute(connection);
            return item.getIdFile();
        });
    }

    public void delete(String md5) throws Exception {
        Adapter.batch(connection -> {
            int idFile = new SqlQuery<Integer>(connection, false)
                    .setSql("SELECT idFile FROM file_item WHERE md5 = :md5")
                    .addParameter("md5", md5)
                    .execute(rs -> rs.next() ? rs.getInt("idFile") : 0);
            if (idFile > 0) {
                List<String> tables = Arrays.asList("file_x_tag", "file_content", "file_item");
                for (String table : tables) {
                    new SqlQuery<>(connection, false)
                            .setSql(String.format("DELETE FROM %s WHERE idFile = :idFile", table))
                            .addParameter("idFile", idFile)
                            .execute();
                }
            }
        });
    }
}
