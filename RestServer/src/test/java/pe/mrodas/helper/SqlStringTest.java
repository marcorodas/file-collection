package pe.mrodas.helper;

import org.junit.Test;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

public class SqlStringTest {

    @Test
    public void toStringArray() throws IOException {
        String path = "C:\\Users\\skynet\\.IdeaIC2018.1\\config\\scratches\\file-collection-rest-curl\\query.sql";
        this.sendToClipboard(new SqlString(path).toStrArray());
    }

    @Test
    public void toSql() throws IOException {
        String path = "C:\\Users\\skynet\\.IdeaIC2018.1\\config\\scratches\\file-collection-rest-curl\\query.txt";
        this.sendToClipboard(new SqlString(path).toSql());
    }

    private void sendToClipboard(String string) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(string), null);
        System.out.println(string);
    }
}