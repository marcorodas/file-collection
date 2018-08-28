package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.Root;
import pe.mrodas.jdbc.SqlQuery;

import java.sql.Connection;
import java.util.List;

@UtilityClass
public class RootDA {

    public List<Root> getRootList(Connection connection, int idUser) throws Exception {
        return new SqlQuery<>(Root.class, connection, false).setSql(new String[]{
                "SELECT R.idRoot, R.name, R.imageUrl, R.mediaType",
                "   FROM root R",
                "   INNER JOIN user_x_root UR",
                "       ON UR.idRoot = R.idRoot",
                "   WHERE UR.idUser = :idUser",
                "   ORDER BY R.idRoot"
        }).setMapper((mapper, root, rs) -> {
            mapper.map(root::setIdRoot, rs::getInt)
                    .map(root::setName, rs::getString)
                    .map(root::setImageUrl, rs::getString)
                    .map(root::setMediaType, rs::getString);
        }).addParameter("idUser", idUser).executeList();
    }
}
