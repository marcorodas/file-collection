package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.Tag;
import pe.mrodas.jdbc.SqlInOperator;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;
import pe.mrodas.jdbc.SqlUpdate;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class TagDA {

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
