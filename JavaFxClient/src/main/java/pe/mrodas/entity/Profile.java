package pe.mrodas.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 *
 * @author skynet
 */
@Data
public class Profile {

    private Integer idProfile;
    private String name;
    private List<Functionality> functionalityList;

    public Functionality addIdFunctionality(Integer idFunctionality) {
        if (functionalityList == null) {
            functionalityList = new ArrayList<>();
        }
        Functionality functionality = new Functionality();
        functionality.setIdFunctionality(idFunctionality);
        this.functionalityList.add(functionality);
        return functionality;
    }

    public enum Type {
        ADMIN(1), STANDARD(2);
        private int id;

        private Type(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
