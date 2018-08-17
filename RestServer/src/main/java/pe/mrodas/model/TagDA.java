package pe.mrodas.model;

import javax.ws.rs.NotFoundException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import pe.mrodas.entity.Tag;
import pe.mrodas.entity.TagListsToSave;
import pe.mrodas.helper.SqlInOperator;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;

@UtilityClass
public class TagDA {

    private final SqlQuery.MapperConfig<Tag> TAG_MAPPER = (mapper, tag, rs)
            -> mapper.map(tag::setIdTag, rs::getInt)
            .map(tag::setName, rs::getString);

    public List<Tag> getCategories(int idRoot, int idUser) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT T.idTag, T.name",
                "    FROM tag T",
                "    INNER JOIN category C",
                "        ON C.idTag = T.idTag",
                "        AND C.idRoot = T.idRoot",
                "    INNER JOIN user_x_root UR",
                "        ON UR.idRoot = T.idRoot",
                "    WHERE T.idRoot = :idRoot",
                "        AND UR.idUser = :idUser",
                "    ORDER BY C.order"
        }).setMapper(TAG_MAPPER)
                .addParameter("idRoot", idRoot)
                .addParameter("idUser", idUser)
                .executeList();
    }

    public Tag insert(int idRoot, Tag tag) throws Exception {
        new SqlInsert("tag", tag::setIdTag)
                .addField("idRoot", idRoot)
                .addField("name", tag.getName())
                .execute();
        return tag;
    }

    public void delete(Tag tag) throws Exception {
        new SqlQuery<>()
                .setSql("DELETE FROM tag WHERE name = :name")
                .addParameter("name", tag.getName())
                .execute();
    }

    public void delete(Integer[] ids) throws Exception {
        SqlInOperator<Integer> inIdTags = new SqlInOperator<>(Arrays.asList(ids));
        SqlQuery<?> query = new SqlQuery<>().setSql("DELETE FROM tag WHERE idTag IN " + inIdTags);
        inIdTags.getParameters().forEach(query::addParameter);
        query.execute();
    }

    public Tag getTag(int idRoot, String name) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT T.idTag, T.name",
                "    FROM tag T",
                "    WHERE T.idRoot = :idRoot",
                "        AND T.name = :name"
        }).setMapper(TAG_MAPPER)
                .addParameter("idRoot", idRoot)
                .addParameter("name", name)
                .executeFirst();
    }

    public List<Tag> getTags(int idRoot, int idUser, String hint) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT T.idTag, T.name",
                "    FROM tag T",
                "    INNER JOIN user_x_root UR",
                "        ON UR.idRoot = T.idRoot",
                "    WHERE T.idRoot = :idRoot",
                "        AND UR.idUser = :idUser",
                "        AND UCASE(T.name) LIKE :hint",
                "    LIMIT 10"
        }).setMapper(TAG_MAPPER)
                .addParameter("idRoot", idRoot)
                .addParameter("idUser", idUser)
                .addParameter("hint", "%" + hint.toUpperCase() + "%")
                .executeList();
    }

    public List<Tag> getTags(int idRoot, String hint, boolean includeCategories) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT T.idTag, T.name",
                "    FROM tag T",
                "    WHERE T.idRoot = :idRoot",
                "        AND UCASE(T.name) LIKE :hint",
                includeCategories ? ""
                        : "AND T.idTag NOT IN (SELECT idTag FROM category)",
                "    LIMIT 10"
        }).setMapper(TAG_MAPPER)
                .addParameter("idRoot", idRoot)
                .addParameter("hint", "%" + hint.toUpperCase() + "%")
                .executeList();
    }

    public List<Tag> getFileTags(String md5) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT T.idTag, T.name",
                "    FROM tag T",
                "    INNER JOIN file_x_tag FxT",
                "       ON FxT.idTag = T.idTag",
                "    INNER JOIN file_item F",
                "       ON FxT.idFile = F.idFile",
                "    WHERE F.md5 = :md5"
        }).setMapper(TAG_MAPPER)
                .addParameter("md5", md5)
                .executeList();
    }

    public void save(Connection conn, String md5, TagListsToSave tagListsToSave) throws Exception {
        Integer idFile = new SqlQuery<Integer>(conn, false).setSql(new String[]{
                "SELECT idFile FROM file_item WHERE md5 = :md5"
        }).addParameter("md5", md5).execute(rs -> rs.next() ? rs.getInt("idFile") : null);
        if (idFile == null) {
            throw new NotFoundException("Unable to find idFile to save tags!");
        }
        TagDA.insertTagListToFile(conn, idFile, tagListsToSave.getIdTagsToAdd());
        TagDA.deleteTagListFromFile(conn, idFile, tagListsToSave.getIdTagsToDelete());
    }

    private void deleteTagListFromFile(Connection conn, Integer idFile, List<Integer> idTagsToDelete) throws Exception {
        if (idTagsToDelete.isEmpty()) {
            return;
        }
        SqlInOperator<Integer> inOperator = new SqlInOperator<>(idTagsToDelete);
        SqlQuery<?> query = new SqlQuery<>(conn, false).setSql(new String[]{
                "DELETE FROM file_x_tag",
                "   WHERE idFile = :idFile AND idTag IN", inOperator.toString()
        }).addParameter("idFile", idFile);
        inOperator.getParameters().forEach(query::addParameter);
        query.execute();
    }

    private void insertTagListToFile(Connection conn, int idFile, List<Integer> idTagsToAdd) throws Exception {
        if (idTagsToAdd.isEmpty()) {
            return;
        }
        SqlInOperator<Integer> inOperator = new SqlInOperator<>(idTagsToAdd);
        String nameFields = inOperator.toString();
        String[] nameFieldArray = nameFields.substring(1, nameFields.length() - 1).split(",");
        SqlQuery<?> query = new SqlQuery<>(conn, false).setSql(new String[]{
                "INSERT INTO file_x_tag(idFile, idTag)",
                "   VALUES", Arrays.stream(nameFieldArray)
                .map(field -> String.format("(:idFile, %s)", field))
                .collect(Collectors.joining(","))
        }).addParameter("idFile", idFile);
        inOperator.getParameters().forEach(query::addParameter);
        query.execute();
    }
}
