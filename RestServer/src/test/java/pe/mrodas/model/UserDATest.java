package pe.mrodas.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserDATest {

    @Test
    public void createToken() {
        String pass = DigestUtils.sha256Hex("maco");
        System.out.println(pass);
    }
}