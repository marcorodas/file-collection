package pe.mrodas.model;

import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.ResponseBody;

@Accessors(chain = true)
public class RestServerException extends Exception {

    public interface Handler {
        RestServerException buildException(String modelName, String url, ResponseBody body);
    }

    private final String modelName, url;
    @Setter
    private String serverTrace;

    public RestServerException(String modelName, String url, String serverMsj) {
        super(serverMsj);
        this.modelName = modelName;
        this.url = url;
    }

    public RestServerException(String message, Throwable cause, String modelName, String url) {
        super(message, cause);
        this.modelName = modelName;
        this.url = url;
    }

    @Override
    public String toString() {
        String str = String.format("\tModel: %s\n\tUrl: %s", modelName, url);
        if (serverTrace != null) {
            str = String.format("%s\n\tServer Trace:\n%s\n", str, serverTrace);
        }
        return "RestServerException:\n" + str;
    }

}
