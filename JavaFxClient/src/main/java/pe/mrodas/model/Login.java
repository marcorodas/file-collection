package pe.mrodas.model;

import pe.mrodas.entity.Credential;
import pe.mrodas.entity.Session;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Login {
    @POST("login/auth")
    Call<Session> auth(@Body Credential credential);
}
