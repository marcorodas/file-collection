package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.SqlInOperator;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;
import pe.mrodas.jdbc.SqlUpdate;

import java.util.Arrays;
import java.util.List;

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
                "        AND UR.idUser = :idUser"
        }).setMapper((mapper, tag, rs) -> {
            mapper.map(tag::setIdTag, rs::getInt)
                    .map(tag::setName, rs::getString);
        }).addParameter("idRoot", idRoot)
                .addParameter("idUser", idUser)
                .executeList();
    }

    public List<Tag> select(String namePart) throws Exception {
        return new SqlQuery<>(Tag.class).setSql(new String[]{
                "SELECT idTag, name",
                "   FROM tag",
                "   WHERE name LIKE :name_part"
        }).setMapper((mapper, result, rs) -> {
            mapper.map(result::setIdTag, rs::getInt)
                    .map(result::setName, rs::getString);
        }).addParameter("name_part", '%' + namePart + '%')
                .executeList();
    }

    public void save(Tag tag) throws Exception {
        if (tag.getIdTag() == null) {
            new SqlInsert("tag", tag::setIdTag)
                    .addField("name", tag.getName())
                    .execute();
        } else {
            new SqlUpdate("tag")
                    .addFilter("idTag", tag.getIdTag())
                    .addField("name", tag.getName())
                    .execute();
        }
    }

    public void delete(Tag tag) throws Exception {
        new SqlQuery<>()
                .setSql("DELETE FROM tag WHERE name = :name")
                .addParameter("name", tag.getName())
                .execute();
    }

    public void delete(Integer[] ids) throws Exception {
        SqlInOperator inIdTags = new SqlInOperator(Arrays.asList(ids));
        SqlQuery<?> query = new SqlQuery<>().setSql("DELETE FROM tag WHERE idTag IN " + inIdTags);
        inIdTags.getParameters().forEach(query::addParameter);
        query.execute();
    }
}
