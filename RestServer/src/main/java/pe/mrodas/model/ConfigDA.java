package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import pe.mrodas.entity.Config;
import pe.mrodas.entity.Environment;
import pe.mrodas.jdbc.SqlInsert;
import pe.mrodas.jdbc.SqlQuery;
import pe.mrodas.jdbc.SqlUpdate;

@UtilityClass
public class ConfigDA {

    private SqlQuery.Save addFields(SqlQuery.Save save, Config config) throws Exception {
        String workingDir = config.getWorkingDir();
        if (workingDir == null || workingDir.trim().isEmpty()) {
            throw new Exception("Working directory can't be NULL or EMPTY");
        }
        Environment e = Environment.nullFieldsToEmpty(config.getEnvironment());
        return save.addField("sysUserName", e.getUsername())
                .addField("sysOsName", e.getOsName())
                .addField("sysHostName", e.getHostName())
                .addField("workingDir", workingDir);
    }

    public void insert(int idUser, Config config) throws Exception {
        ConfigDA.addFields(new SqlInsert("config"), config)
                .addField("idCreator", idUser)
                .execute();
    }

    public void update(int idUser, Config config) throws Exception {
        SqlQuery.Save save = ConfigDA.addFields(new SqlUpdate("config"), config);
        ((SqlUpdate) save).addFilter("idCreator", idUser)
                .execute();
    }
}
