package pe.mrodas.model;

import java.sql.Connection;
import java.util.List;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.Functionality;
import pe.mrodas.jdbc.SqlQuery;

/**
 * @author skynet
 */
@UtilityClass
public class ProfileDA {

    public List<Functionality> getFunctionalityList(Connection connection, Integer idProfile) throws Exception {
        return new SqlQuery<>(Functionality.class, connection, false).setSql(new String[]{
                "SELECT F.idFunctionality, F.name",
                "   FROM functionality F",
                "   INNER JOIN functionality_x_profile FP",
                "       ON F.idFunctionality = FP.idFunctionality",
                "   WHERE FP.idProfile = :idProfile"
        }).setMapper((mapper, funcionalidad, rs) -> {
            mapper.map(funcionalidad::setIdFunctionality, rs::getInt)
                    .map(funcionalidad::setName, rs::getString);
        }).addParameter("idProfile", idProfile).executeList();
    }
}
