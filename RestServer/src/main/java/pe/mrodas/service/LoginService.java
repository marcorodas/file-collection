package pe.mrodas.service;

import pe.mrodas.entity.Credential;
import pe.mrodas.entity.Session;
import pe.mrodas.entity.User;
import pe.mrodas.model.UserDA;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("login")
@Produces("application/json")
public class LoginService {

    //https://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey
    @POST
    @Path("auth")
    public Session authenticateUser(Credential credential) throws Exception {
        return UserDA.authenticateUser(credential);
    }
}