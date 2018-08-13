package pe.mrodas.model;

import java.util.Arrays;
import java.util.List;

import lombok.experimental.UtilityClass;

import pe.mrodas.entity.Tag;
import pe.mrodas.helper.SqlInOperator;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;

@UtilityClass
public class TagDA {

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
        }).setMapper((mapper, tag, rs) -> {
            mapper.map(tag::setIdTag, rs::getInt);
            mapper.map(tag::setName, rs::getString);
        }).addParameter("idRoot", idRoot)
                .addParameter("idUser", idUser)
                .executeList();
    }

    public Tag save(int idRoot, Tag tag) throws Exception {
        if (tag.getIdTag() == null) {
            new SqlInsert("tag", tag::setIdTag)
                    .addField("idRoot", idRoot)
                    .addField("name", tag.getName())
                    .execute();
        }
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

    public List<Tag> getTags(int idRoot, int idUser, String hint) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT T.idTag, T.name",
                "    FROM tag T",
                "    INNER JOIN user_x_root UR",
                "        ON UR.idRoot = T.idRoot",
                "    WHERE T.idRoot = :idRoot",
                "        AND UR.idUser = :idUser",
                "        AND T.name LIKE :hint",
                "    LIMIT 10"
        }).setMapper((mapper, tag, rs) -> {
            mapper.map(tag::setIdTag, rs::getInt);
            mapper.map(tag::setName, rs::getString);
        }).addParameter("idRoot", idRoot)
                .addParameter("idUser", idUser)
                .addParameter("hint", "%" + hint + "%")
                .executeList();
    }
}
