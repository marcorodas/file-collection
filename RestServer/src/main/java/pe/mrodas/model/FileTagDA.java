package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.Tag;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Stream;

@UtilityClass
class FileTagDA {

    public List<Tag> select(Connection connection, Integer idFile) throws Exception {
        return new SqlQuery<>(Tag.class, connection, false)
                .setSql(new String[]{
                        "SELECT T.idTag, T.name",
                        "   FROM file_x_tag FxT",
                        "   INNER JOIN tag T",
                        "       ON FxT.idTag = T.idTag",
                        "   WHERE FxT.idFile = :idFile"
                }).setMapper((mapper, result, rs) -> {
                    mapper.map(result::setIdTag, rs::getInt);
                    mapper.map(result::setName, rs::getString);
                }).addParameter("idFile", idFile)
                .executeList();
    }

    void insert(Connection connection, Integer idFile, Integer idTag) throws Exception {
        new SqlInsert("file_x_tag")
                .addField("idFile", idFile)
                .addField("idTag", idTag)
                .execute(connection);
    }

    private void delete(Connection connection, Integer idFile, Integer idTag) throws Exception {
        new SqlQuery<>(connection, false)
                .setSql("DELETE FROM file_x_tag WHERE idFile = :idFile AND idTag = :idTag")
                .addParameter("idFile", idFile)
                .addParameter("idTag", idTag)
                .execute();
    }

    private boolean notInStream(Tag testTag, Stream<Tag> tags) {
        Integer idTag = testTag.getIdTag();
        return tags.noneMatch(tag -> tag.getIdTag().equals(idTag));
    }

    void update(Connection connection, Integer idFile, List<Tag> tags) throws Exception {
        Stream<Tag> newItems = tags.stream();
        Stream<Tag> oldItems = new SqlQuery<>(Tag.class, connection, false)
                .setSql("SELECT idTag FROM file_x_tag WHERE idFile = :idFile")
                .setMapper((mapper, result, rs) -> mapper.map(result::setIdTag, rs::getInt))
                .addParameter("idFile", idFile)
                .executeList().stream();
        Stream<Tag> oldItemsToDelete = oldItems.filter(tag -> notInStream(tag, newItems));
        for (Tag tag : oldItemsToDelete.toArray(Tag[]::new)) {
            delete(connection, idFile, tag.getIdTag());
        }
        Stream<Tag> newItemsToInsert = newItems.filter(tag -> notInStream(tag, oldItems));
        for (Tag tag : newItemsToInsert.toArray(Tag[]::new)) {
            insert(connection, idFile, tag.getIdTag());
        }
    }

}
