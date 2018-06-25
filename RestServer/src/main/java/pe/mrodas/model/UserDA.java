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

    private int getUserId(Credential credential) throws Exception {
        String password = credential.getPassword();
        return new SqlQuery<Integer>()
                .setSql("SELECT idUser FROM user WHERE username = :user AND password = :pass")
                .addParameter("user", credential.getUsername())
                .addParameter("pass", DigestUtils.sha256Hex(password))
                .execute(rs -> rs.next() ? rs.getInt("idUser") : 0);
    }

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

    public User authenticateUser(Credential credential) throws Exception {
        int idUsuario = UserDA.getUserId(credential);
        if (idUsuario == 0) {
            throw new NotAuthorizedException("Invalid user!");
        }
        return Adapter.batch(connection -> {
            User user = UserDA.getUserProfile(connection, idUsuario);
            Integer idProfile = user.getProfile() == null ? null : user.getProfile().getIdProfile();
            if (idProfile == null || idProfile == 0) {
                throw new IllegalArgumentException("Error idProfile can't be zero or null");
            }
            List<Functionality> list = ProfileDA.getFunctionalityList(connection, idProfile);
            user.getProfile().setFunctionalityList(list);
            UserDA.updateToken(connection, user.autoGenToken());
            return user;
        });
    }

    private User getUserProfile(Connection connection, int idUser) throws Exception {
        return new SqlQuery<>(User.class, connection, false).setSql(new String[]{
                "SELECT U.username, P.idProfile, P.name,",
                "   PER.idPerson, PER.firstname, PER.lastname, PER.mail",
                "   FROM user U",
                "   INNER JOIN profile P",
                "       ON P.idProfile = U.idProfile",
                "   INNER JOIN person PER",
                "       ON PER.idPerson = U.idPerson",
                "   WHERE U.idUser = :idUser"
        }).setMapper((mapper, user, rs) -> {
            user.setIdUser(idUser);
            Person person = user.getPerson();
            Profile profile = user.getProfile();
            mapper.map(user::setUsername, rs::getString)
                    .map(profile::setIdProfile, rs::getInt)
                    .map(profile::setName, rs::getString)
                    .map(person::setIdPerson, rs::getInt)
                    .map(person::setFirstname, rs::getString)
                    .map(person::setLastname, rs::getString)
                    .map(person::setMail, rs::getString);
        }).addParameter("idUser", idUser).executeFirst();
    }

}
