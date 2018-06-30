package pe.mrodas.model;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class RestClient {

    private static String baseUrl;
    private static String token;
    private static boolean tokenIsSet;
    private static Retrofit retrofit;
    private static RestServerException.Handler serverErrorHandler;

    public static void setBaseUrl(String baseUrl) {
        RestClient.baseUrl = baseUrl;
    }

    public static void setToken(String token) {
        tokenIsSet = false;
        RestClient.token = token;
    }

    public static void setServerErrorHandler(RestServerException.Handler serverErrorHandler) {
        RestClient.serverErrorHandler = serverErrorHandler;
    }

    public static Retrofit retrofit() throws Exception {
        if (retrofit == null) {
            if (baseUrl == null) {
                throw new Exception("baseUrl is not set");
            }
            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (!tokenIsSet && token != null) {
            tokenIsSet = true;
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(RestClient::intercept)
                    .build();
            retrofit = retrofit.newBuilder()
                    .client(client).build();
        }
        return retrofit;
    }

    private static okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(newRequest);
    }

    public static <T, R> R execute(Retrofit retro, Class<T> modelClass, Function<T, Call<R>> modelMethod, RestServerException.Handler onServerError) throws Exception {
        T modelObj = retro.create(modelClass);
        Response<R> response = modelMethod.apply(modelObj).execute();
        String modelName = modelClass.toString();
        String url = response.raw().request().url().toString();
        if (response.isSuccessful()) {
            if (response.body() == null) {
                throw new RestServerException(modelName, url, "Null Body Response");
            }
            return response.body();
        }
        ResponseBody body = response.errorBody();
        if (body == null) {
            throw new RestServerException(modelName, url, "Null ErrorBody Response");
        }
        if (onServerError == null) {
            if (serverErrorHandler == null) {
                throw new RestServerException(modelName, url, "Handler Not Set");
            }
            throw serverErrorHandler.buildException(modelName, url, body);
        }
        throw onServerError.buildException(modelName, url, body);
    }

    public static <T, R> R execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, RestServerException.Handler onServerError) throws Exception {
        return RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onServerError);
    }

    public static <T, R> R execute(Class<T> modelClass, Function<T, Call<R>> modelMethod) throws Exception {
        return RestClient.execute(modelClass, modelMethod, null);
    }

    public static <T, R> void execute(Retrofit retro, Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess, RestServerException.Handler onServerError) throws Exception {
        R result = RestClient.execute(retro, modelClass, modelMethod, onServerError);
        onSuccess.accept(result);
    }

    public static <T, R> void execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess, RestServerException.Handler onServerError) throws Exception {
        RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onSuccess, onServerError);
    }
}
