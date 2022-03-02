import com.google.mu.util.stream.BiStream;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.util.*;
import java.util.stream.Stream;

public class BfoDolceMapper {

	private final static Map<IRI, Translation> translations;

	private final static Map<IRI, IRI> iriReplacementMap;

	static {
		final var tmpTranslation = new HashMap<IRI, Translation>();

		translations = Collections.unmodifiableMap(tmpTranslation);
		iriReplacementMap = BiStream.from(translations).mapValues(Translation::translated).toMap();
	}

	private BfoDolceMapper() {
	}

	public static boolean needsMapping(@NotNull final OWLEntity toCheck) {
		Objects.requireNonNull(toCheck);
		return translations.containsKey(toCheck);
	}

	public static @NotNull Optional<Translation> translateToDolce(@NotNull final IRI toTranslate) {
		Objects.requireNonNull(toTranslate);
		return Optional.ofNullable(translations.get(toTranslate));
	}

	public static @NotNull Stream<OWLAxiom> translateToDolce(@NotNull final OWLOntologyManager manager, @NotNull final OWLAxiom toTranslate) {
		Objects.requireNonNull(toTranslate);
		Objects.requireNonNull(manager);

		OWLObjectDuplicator objectDuplicator = new OWLObjectDuplicator(manager, iriReplacementMap);

		OWLAxiom translated = objectDuplicator.duplicateObject(toTranslate);

		throw new NotImplementedException();
	}

	private enum MAPPING_RELATION {
		EQUIVALENT, SUBCLASS
	}

	public record Translation(@NotNull IRI translated, @NotNull MAPPING_RELATION relation) {

		public Translation {
			Objects.requireNonNull(translated);
			Objects.requireNonNull(relation);
		}
	}


}
