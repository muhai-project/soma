import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.*;

public class BfoDolceMapper {

    private enum MAPPING_RELATION {
        EQUIVALENT, SUBCLASS
    }

    public record Translation(OWLEntity translated, MAPPING_RELATION relation) {
    }

    private final static Map<OWLEntity, Translation> translations;

    static {
        var tmpTranslation = new HashMap<OWLEntity, Translation>();

        translations = Collections.unmodifiableMap(tmpTranslation);
    }

    private BfoDolceMapper() {
    }

    public static boolean needsMapping(@NotNull OWLEntity toCheck) {
        Objects.requireNonNull(toCheck);
        return translations.containsKey(toCheck);
    }

    public static @NotNull Optional<Translation> translateToDolce(@NotNull OWLEntity toTranslate) {
        Objects.requireNonNull(toTranslate);
        return Optional.ofNullable(translations.get(toTranslate));
    }


}
