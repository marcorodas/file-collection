package pe.mrodas.entity;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class TagListsToSave {
    private List<Integer> idTagsToAdd, idTagsToDelete;

    public boolean noChanges() {
        return idTagsToAdd.isEmpty() && idTagsToDelete.isEmpty();
    }

    public TagListsToSave setTags(List<Integer> oldList, List<Integer> newList) {
        idTagsToAdd = newList.stream().filter(item -> !oldList.contains(item))
                .collect(Collectors.toList());
        idTagsToDelete = oldList.stream().filter(item -> !newList.contains(item))
                .collect(Collectors.toList());
        return this;
    }
}
