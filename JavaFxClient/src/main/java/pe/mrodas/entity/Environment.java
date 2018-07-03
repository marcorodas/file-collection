package pe.mrodas.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Accessors(chain = true)
@Data
public class Environment {
    private String username, osName, hostName;

    public static Environment get() {
        Environment environment = new Environment()
                .setUsername(System.getProperty("user.name"))
                .setOsName(System.getProperty("os.name"));
        try {
            return environment.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex) {
            return environment.setHostName("Unknown");
        }
    }
}
