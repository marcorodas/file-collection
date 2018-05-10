package pe.mrodas.model;

import org.junit.Before;
import pe.mrodas.jdbc.DBLayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class BaseDATest {
    private static final String DB_PROPERTIES = "db.properties";

    private static void copyToTest(String fileName) throws IOException {
        String workingDir = BaseDATest.class.getResource("/").getPath();
        Path dest = new File(workingDir).toPath();
        Path src = Paths.get(dest.getParent().getParent().toString(), "src/main/webapp/WEB-INF/classes");
        dest = Paths.get(dest.toString(), fileName);
        src = Paths.get(src.toString(), fileName);
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    @Before
    public void init() throws IOException {
        BaseDATest.copyToTest(DB_PROPERTIES);
        DBLayer.Connector.configureWithPropFile(DB_PROPERTIES);
    }
}
