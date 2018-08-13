package pe.mrodas.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import pe.mrodas.entity.Credential;
import pe.mrodas.entity.Session;

public interface LoginModel {
    @POST("login/auth")
    Call<Session> auth(@Body Credential credential);
}
