package pe.mrodas.helper;

import org.junit.Test;

public class FileHelperTest {

    @Test
    public void getNameParts() {
        String fileName = "C:\\Users\\skynet\\Desktop\\FilesCollectionTest\\Antifujimorismo\\stage\\0EDF37A157629129BD3D7DB85791B35D.png";
        System.out.println(FileHelper.getName(fileName));
        System.out.println(FileHelper.getExtension(fileName));
    }
}