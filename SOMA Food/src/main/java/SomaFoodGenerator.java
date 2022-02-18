import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.modularity.ModuleExtractor;
import org.semanticweb.owlapi.modularity.locality.LocalityClass;
import org.semanticweb.owlapi.modularity.locality.SyntacticLocalityModuleExtractor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SomaFoodGenerator {

	private final OWLOntology foodOn;

	private final OWLOntology somaFood;

	private final ModuleExtractor moduleExtractor;

	private final OWLReasoner reasoner;

	public SomaFoodGenerator(@NotNull OWLOntology foodOn, @NotNull OWLOntology somaFood) {
		Objects.requireNonNull(foodOn);
		Objects.requireNonNull(somaFood);

		this.foodOn = foodOn;
		this.somaFood = somaFood;

		moduleExtractor = new SyntacticLocalityModuleExtractor(LocalityClass.STAR, foodOn.axioms());
		reasoner = new ReasonerFactory().createReasoner(foodOn);
	}

	public OWLOntology getFoodOn() {
		return foodOn;
	}

	public OWLOntology getSomaFood() {
		return somaFood;
	}


	public void importEntities(@NotNull List<OWLEntity> toImport, boolean includeSiblings) {
		Objects.requireNonNull(toImport);

		var toImportStream = toImport.stream();

		if (includeSiblings) {
			toImportStream = toImportStream.flatMap(next -> siblings(next));
		}

		var relevantAxiomsStream = moduleExtractor.extract(toImportStream).toList();

		var toMap = relevantAxiomsStream.stream().flatMap(HasSignature::signature).
				filter(BfoDolceMapper::needsMapping).collect(Collectors.toUnmodifiableSet());

		// TODO sort into SOMA FOOD
		throw new NotImplementedException();
	}

	/**
	 * Returns the (inferred) siblings of the given {@link OWLEntity}, including itself.
	 * This method supports {@link OWLClassExpression}s, {@link OWLDataProperty}s, and {@link OWLObjectProperty}s.
	 *
	 * @param entity The {@link OWLEntity} whose siblings should be returned
	 * @return The siblings of the given {@link OWLEntity}, including itself
	 */
	private @NotNull Stream<? extends OWLEntity> siblings(@NotNull final OWLEntity entity) {
		return switch (entity) {
			case OWLClassExpression classExpression -> reasoner.superClasses(classExpression, true).
					flatMap(next -> reasoner.subClasses(next, true));
			case OWLObjectProperty objectProperty -> reasoner.superObjectProperties(objectProperty, true).
					flatMap(next -> (Stream<? extends OWLEntity>) reasoner.subObjectProperties(next, true));
			case OWLDataProperty dataProperty -> reasoner.superDataProperties(dataProperty, true).
					flatMap(next -> reasoner.subDataProperties(next, true));
			default -> throw new IllegalArgumentException("Unexpected value: " + entity);
		};
	}
}
