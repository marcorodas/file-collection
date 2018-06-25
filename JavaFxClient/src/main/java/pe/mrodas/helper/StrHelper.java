package pe.mrodas.helper;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StrHelper {

    public String charToUpperCase(int position, String input) {
        char[] chars = input.toCharArray();
        chars[position] = Character.toUpperCase(chars[position]);
        return new String(chars);
    }
}
