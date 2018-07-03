package pe.mrodas.controller;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import pe.mrodas.MainApp;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author skynet
 */
public abstract class TestBase extends ApplicationTest {

    private boolean sessionUnset = false;
    private int duration = 0;

    public abstract void startTest(Stage stage) throws Exception;

    public TestBase() {
    }

    public TestBase(int duration) {
        this(duration, false);
    }

    public TestBase(boolean sessionUnset) {
        this(0, sessionUnset);
    }

    public TestBase(int duration, boolean sessionUnset) {
        this.duration = duration;
        this.sessionUnset = sessionUnset;
    }

    @Test
    public void testShow() throws Exception {
        sleep(duration == 0 ? 30 : duration, TimeUnit.SECONDS);
    }

    @Override
    public void start(Stage stage) throws Exception {
        if (!sessionUnset) {
            String workingDir = "C:\\Users\\skynet\\Desktop\\FilesCollectionTest";
            MainApp.getSession()
                    .setWorkingDir(Paths.get(workingDir));
        }
        this.startTest(stage);
    }

    @After
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

}
