package pe.mrodas.model;

import org.junit.Test;
import pe.mrodas.entity.Tag;

import java.util.Arrays;
import java.util.List;

public class TagDATest extends BaseDATest {

    @Test
    public void testSelect() throws Exception {
        List<Tag> tags = Arrays.asList(
                new Tag().setName("conifero"),
                new Tag().setName("conillo")
        );
        for (Tag tag : tags) {
            TagDA.save(idRoot, tag);
        }
        TagDA.select("coni").forEach(System.out::println);
        for (Tag tag : tags) {
            TagDA.delete(tag);
        }
    }

    @Test
    public void testSave() throws Exception {
        Tag tag = new Tag().setName("eribola");
        TagDA.save(idRoot, tag);
        TagDA.save(idRoot, tag.setName("ericota"));
        TagDA.select("eri").forEach(System.out::println);
        TagDA.delete(tag);
    }
}