package pe.mrodas.model;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@UtilityClass
public class RestClient {

    private String baseUrl;
    private String token;
    private boolean tokenIsSet;
    private Retrofit retrofit;
    private RestServerException.Handler serverErrorHandler;
    private final List<Integer> NULL_BODY_HTTP_CODES = Arrays.asList(
            HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_NO_CONTENT
    );

    public void setBaseUrl(String baseUrl) {
        RestClient.baseUrl = baseUrl;
    }

    public void setToken(String token) {
        tokenIsSet = false;
        RestClient.token = token;
    }

    public void setServerErrorHandler(RestServerException.Handler serverErrorHandler) {
        RestClient.serverErrorHandler = serverErrorHandler;
    }

    public Retrofit retrofit() throws Exception {
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
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .build();
            retrofit = retrofit.newBuilder()
                    .client(client).build();
        }
        return retrofit;
    }

    private okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(newRequest);
    }

    public <T, R> Response<R> execute(Retrofit retro, Class<T> modelClass, Function<T, Call<R>> modelMethod, RestServerException.Handler onServerError) throws Exception {
        T modelObj = retro.create(modelClass);
        Response<R> response = modelMethod.apply(modelObj).execute();
        String modelName = modelClass.toString();
        String url = response.raw().request().url().toString();
        if (response.isSuccessful()) {
            if (response.body() == null && !NULL_BODY_HTTP_CODES.contains(response.raw().code())) {
                throw new RestServerException(modelName, url, "Null Body Response");
            }
            return response;
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

    public <T, R> Response<R> execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, RestServerException.Handler onServerError) throws Exception {
        return RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onServerError);
    }

    public <T, R> Response<R> execute(Class<T> modelClass, Function<T, Call<R>> modelMethod) throws Exception {
        return RestClient.execute(modelClass, modelMethod, null);
    }

    public <T, R> void execute(Retrofit retro, Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<Response<R>> onSuccess, RestServerException.Handler onServerError) throws Exception {
        Response<R> result = RestClient.execute(retro, modelClass, modelMethod, onServerError);
        onSuccess.accept(result);
    }

    public <T, R> void execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<Response<R>> onSuccess, RestServerException.Handler onServerError) throws Exception {
        RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onSuccess, onServerError);
    }

    public MultipartBody.Part createBodyPart(String partName, File file) {
        String fileName = file.getName();
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        return MultipartBody.Part.createFormData(partName, fileName, requestFile);
    }

    public Map<String, RequestBody> createBodyPart(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            String value = e.getValue();
            return RequestBody.create(MultipartBody.FORM, value);
        }));
    }

}
