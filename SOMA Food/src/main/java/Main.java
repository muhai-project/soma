import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	public static void main(final String[] cmdlArgs) throws IOException, OWLOntologyCreationException {
		// parse arguments
		final Args args = parseArguments(cmdlArgs);

		// Load both ontologies (from files)
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final OWLOntology foodOn = manager.loadOntology(args.foodOnIri);
		final OWLOntology soma = manager.loadOntologyFromOntologyDocument();

		// Load list of IRIs to import
		final Stream<IRI> irisToImport = irisToImport(args.pathOfIriFile);

		// Import
		// Save SOMA Food
	}

	private static @NotNull Args parseArguments(@NotNull final String[] cmdlArgs) {
		Args args = new Args();
		JCommander.newBuilder()
				.addObject(args)
				.build()
				.parse(cmdlArgs);
		return args;
	}

	private static @NotNull Stream<IRI> irisToImport(@NotNull final Path pathOfIriFile) throws IOException {
		try (Stream<String> lines = Files.lines(pathOfIriFile)) {
			return lines.filter(next -> !next.startsWith("#")).map(IRI::create).collect(Collectors.toUnmodifiableSet()).stream();
		}
	}

	public static class Args {

		@Parameter(names = "-foodOnIRI", description = "IRI from where to load FoodOn (source of import)", converter = IRIConverter.class)
		private IRI foodOnIri = IRI.create("http://purl.obolibrary.org/obo/foodon.owl");

		@Parameter(names = "-includeSiblings", description = "Whether or not to also extract axioms regarding the siblings of specified entities")
		private boolean includeSiblings = false;

		@Parameter(names = "-pathOfIriFile", description = "(Relative or absolute) path to file that contains the Entities to import", converter = PathConverter.class)
		private Path pathOfIriFile = Paths.get("IRIs.txt");

		@Parameter(names = "-pathOfSomaFoodFile", description = "(Relative or absolute) path to .owl file that contains SOMA Food (target of import)", converter = PathConverter.class)
		private Path pathOfSomaFoodFile = Paths.get("SOMA-FOOD.owl");

		public static class IRIConverter implements IStringConverter<IRI> {

			@Override
			public IRI convert(final String toConvert) {
				return IRI.create(toConvert);
			}

		}

		public static class PathConverter implements IStringConverter<Path> {

			@Override
			public Path convert(final String toConvert) {
				return Paths.get(toConvert);
			}

		}

	}
}
