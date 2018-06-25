package pe.mrodas.model;

import pe.mrodas.entity.Credential;
import pe.mrodas.entity.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Login {
    @POST("login/auth")
    Call<User> auth(@Body Credential credential);
}
