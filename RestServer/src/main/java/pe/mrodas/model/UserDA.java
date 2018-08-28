package pe.mrodas.model;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;
import pe.mrodas.entity.*;
import pe.mrodas.jdbc.Adapter;
import pe.mrodas.jdbc.SqlQuery;
import pe.mrodas.jdbc.SqlUpdate;

import javax.ws.rs.NotAuthorizedException;
import java.sql.Connection;
import java.util.List;

@UtilityClass
public class UserDA {
    /**
     * @param token can be null
     * @return userId
     */
    public int validateToken(String token) throws Exception {
        if (token == null) {
            throw new NotAuthorizedException("Invalid 'null' token!");
        }
        int idUser = new SqlQuery<Integer>()
                .setSql("SELECT idUser FROM user WHERE token = :token")
                .addParameter("token", token)
                .execute(rs -> rs.next() ? rs.getInt("idUser") : 0);
        if (idUser > 0) {
            return idUser;
        }
        throw new NotAuthorizedException("Invalid token!");
    }

    private void updateToken(Connection connection, User user) throws Exception {
        Adapter.checkNotNullOrZero(user.getIdUser(), "Update User: id can't be zero or null");
        new SqlUpdate("user")
                .addField("token", user.getToken())
                .addFilter("idUser", user.getIdUser())
                .execute(connection);
    }

    public Session authenticateUser(Credential credential) throws Exception {
        return Adapter.batch(connection -> {
            Session session = UserDA.getUserSession(connection, credential);
            User user = session.getUser();
            Integer idProfile = user.getProfile().getIdProfile();
            if (idProfile == null || idProfile == 0) {
                throw new IllegalArgumentException("Error idProfile can't be zero or null");
            }
            List<Functionality> list = ProfileDA.getFunctionalityList(connection, idProfile);
            user.getProfile().setFunctionalityList(list);
            UserDA.updateToken(connection, user.autoGenToken());
            List<Root> rootList = RootDA.getRootList(connection, user.getIdUser());
            return session.setRootList(rootList);
        });
    }

    private Session getUserSession(Connection connection, Credential credential) throws Exception {
        SqlQuery<Session> query = new SqlQuery<Session>(connection, false).setSql(new String[]{
                "SELECT U.idUser, U.username, P.idProfile, P.name,",
                "   PER.idPerson, PER.firstname, PER.lastname, PER.mail,",
                "   C.workingDir",
                "   FROM user U",
                "   INNER JOIN profile P",
                "       ON P.idProfile = U.idProfile",
                "   INNER JOIN person PER",
                "       ON PER.idPerson = U.idPerson",
                "   LEFT JOIN config C",
                "       ON C.sysHostName = :sysHostName",
                "       AND C.sysOsName = :sysOsName",
                "       AND C.sysUserName = :sysUserName",
                //"       AND C.idCreator = U.idUser",
                "   WHERE U.username = :user AND U.password = :pass"
        }).addParameter("user", credential.getUsername());
        String pass = DigestUtils.sha256Hex(credential.getPassword());
        query.addParameter("pass", pass);
        Environment e = Environment.nullFieldsToEmpty(credential.getEnvironment());
        query.addParameter("sysHostName", e.getHostName())
                .addParameter("sysOsName", e.getOsName())
                .addParameter("sysUserName", e.getUsername());
        Session session = query.execute(rs -> {
            if (!rs.next()) {
                return null;
            }
            int idUser = rs.getInt("idUser");
            if (idUser == 0) {
                return null;
            }
            Profile profile = new Profile()
                    .setIdProfile(rs.getInt("idProfile"))
                    .setName(rs.getString("name"));
            Person person = new Person()
                    .setIdPerson(rs.getInt("idPerson"))
                    .setFirstname(rs.getString("firstname"))
                    .setLastname(rs.getString("lastname"))
                    .setMail(rs.getString("mail"));
            User user = new User()
                    .setIdUser(idUser)
                    .setUsername(rs.getString("username"))
                    .setProfile(profile)
                    .setPerson(person);
            return new Session()
                    .setUser(user)
                    .setWorkingDir(rs.getString("workingDir"));
        });
        if (session == null) {
            throw new NotAuthorizedException("Invalid user!");
        }
        return session;
    }
}
