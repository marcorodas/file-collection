package pe.mrodas.model;

import okhttp3.ResponseBody;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RestServerException extends Exception {

    public interface Handler {
        RestServerException buildException(String modelName, String url, ResponseBody body);
    }

    private final String modelName, url;
    private String serverTrace, packageFilter;

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

    public RestServerException setServerTrace(String serverTrace) {
        this.serverTrace = serverTrace;
        return this;
    }

    public void setPackageFilter(String packageFilter) {
        this.packageFilter = packageFilter;
    }

    private Stream<String> getFilteredTrace(String[] traceElements) {
        Stream<String> stream = Arrays.stream(traceElements);
        if (packageFilter == null) {
            return stream;
        }
        return stream.filter(this::filterByPackage);
    }

    private boolean filterByPackage(String input) {
        String trimed = input.trim();
        if (trimed.startsWith("at")) {
            return trimed.startsWith("at " + packageFilter);
        }
        return true;
    }

    @Override
    public String toString() {
        String str = String.format("\tModel: %s\n\tUrl: %s", modelName, url);
        if (serverTrace != null) {
            String trace = this.getFilteredTrace(serverTrace.split("\n"))
                    .map(s -> "\t\t" + s)
                    .collect(Collectors.joining("\n"));
            str += String.format("\n\tServer Trace:\n%s\n", trace);
        }
        return "RestServerException:\n" + str;
    }


}
