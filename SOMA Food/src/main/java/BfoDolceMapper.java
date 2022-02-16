import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Objects;
import java.util.Optional;

public class BfoDolceMapper {

    public BfoDolceMapper() {
    }

    public boolean needsMapping(@NotNull OWLEntity toCheck) {
        Objects.requireNonNull(toCheck);

        throw new NotImplementedException();
    }

    public @NotNull Optional<OWLEntity> mapToDolce(@NotNull OWLEntity toMap) {
        Objects.requireNonNull(toMap);

        throw new NotImplementedException();
    }


}
