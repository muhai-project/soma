import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	private final Args args;

	private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	public Main(Args args) {
		this.args = args;
	}

	public static void main(final String[] cmdlArgs) throws IOException, OWLOntologyCreationException,
	                                                        OWLOntologyStorageException {
		// parse arguments
		final Args args = new Args();
		JCommander.newBuilder().addObject(args).build().parse(cmdlArgs);

		// run
		Main main = new Main(args);
		main.run();
		System.exit(0);
	}

	private static void saveOntology(final OWLOntology toSave, final Path pathWhereToSave) throws IOException,
	                                                                                              OWLOntologyStorageException {
		File fileWhereToSave = pathWhereToSave.toAbsolutePath().toFile();
		fileWhereToSave.getParentFile().mkdirs();

		if (!fileWhereToSave.exists()) {
			fileWhereToSave.createNewFile();
			// TODO Print outs
			// TODO update owl api (remove new in line 1108 in SyntacticLocalityEvaluator)
		}
		try (OutputStream outputStream = new FileOutputStream(fileWhereToSave, false)) {
			toSave.saveOntology(outputStream);
		}
	}

	private OWLOntology createSomaFood() throws OWLOntologyCreationException {
		// create
		OWLOntology somaFood = manager.createOntology(args.somaFoodIri);

		// add import of SOMA
		OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory().getOWLImportsDeclaration(args.somaIri);
		manager.applyChange(new AddImport(somaFood, importDeclaration));

		return somaFood;
	}

	private @NotNull Stream<IRI> irisToImport() throws IOException {
		try (Stream<String> lines = Files.lines(args.pathOfIriFile)) {
			return lines.filter(next -> !next.startsWith("#") && !next.isBlank()).map(IRI::create)
			            .collect(Collectors.toUnmodifiableSet()).stream();
		}
	}

	public void run() throws IOException, OWLOntologyStorageException, OWLOntologyCreationException {
		// Load list of IRIs to import
		final Stream<IRI> irisToImport;
		try {
			irisToImport = irisToImport();
		} catch (NoSuchFileException exception) {
			System.err.println(
					"The file of IRIs to import does not exist under '" + args.pathOfIriFile.toAbsolutePath() + "'.\n" + "You can specify the IRI file path using '--pathOfIriFile'.");
			return;
		}

		// Load FoodOn (from the internet)
		final OWLOntology foodOn = manager.loadOntology(args.foodOnIri);

		// Create somaFood
		final OWLOntology somaFood = createSomaFood();

		// Import
		SomaFoodGenerator generator = new SomaFoodGenerator(foodOn, somaFood);
		generator.importEntities(irisToImport, args.includeSiblings);

		// Save SOMA Food
		try {
			saveOntology(somaFood, args.pathOfSomaFoodFile);
		} catch (FileNotFoundException exception) {
			System.err.println(
					"The ontology could not be saved under '" + args.pathOfSomaFoodFile.toAbsolutePath() + "'.\nYou can specify the path where to save SOMA Food using '--pathOfSomaFoodFile'.");
		}
	}

	public static class Args {

		@Parameter(names = "-somaFoodIri", description = "IRI of the new SOMA FOOD ontology")
		public IRI somaFoodIri = IRI.create("http://www.ease-crc.org/ont/SOMA-FOOD.owl");

		@Parameter(names = "-somaIri", description = "IRI of the SOMA ontology")
		public IRI somaIri = IRI.create("http://www.ease-crc.org/ont/SOMA-FOOD.owl");

		@Parameter(names = "-foodOnIri", description = "IRI from where to load FoodOn (source of import)", converter = IRIConverter.class)
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
