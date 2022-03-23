import org.jetbrains.annotations.NotNull;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.modularity.ModuleExtractor;
import org.semanticweb.owlapi.modularity.locality.LocalityClass;
import org.semanticweb.owlapi.modularity.locality.SyntacticLocalityModuleExtractor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SomaFoodGenerator {

	private final OWLOntology foodOn;

	private final OWLOntology somaFood;

	private final ModuleExtractor moduleExtractor;

	private final OWLReasoner reasoner;

	public SomaFoodGenerator(@NotNull final OWLOntology foodOn, @NotNull final OWLOntology somaFood) {
		this(foodOn, somaFood, new ReasonerFactory());
	}

	public SomaFoodGenerator(@NotNull final OWLOntology foodOn, @NotNull final OWLOntology somaFood, @NotNull final OWLReasonerFactory reasonerFactory) {
		Objects.requireNonNull(foodOn);
		Objects.requireNonNull(somaFood);
		Objects.requireNonNull(reasonerFactory);

		this.foodOn = foodOn;
		this.somaFood = somaFood;

		moduleExtractor = new SyntacticLocalityModuleExtractor(LocalityClass.STAR, foodOn.axioms(Imports.INCLUDED));
		reasoner = reasonerFactory.createReasoner(foodOn);
	}

	public OWLOntology getFoodOn() {
		return foodOn;
	}

	public OWLOntology getSomaFood() {
		return somaFood;
	}


	public void importEntities(@NotNull final Stream<IRI> toImport, final boolean includeSiblings) {
		Objects.requireNonNull(toImport);

		var toImportEvtSiblings = toImport.flatMap(this::toEntity);

		if (includeSiblings) {
			toImportEvtSiblings = toImportEvtSiblings.parallel().flatMap(this::siblings);
		}

		final var relevantAxioms = moduleExtractor.extract(toImportEvtSiblings).toList();

		// final var toMap = relevantAxioms.stream().flatMap(HasSignature::signature).
		//		filter(BfoDolceMapper::needsMapping).collect(Collectors.toUnmodifiableSet());

		// TODO sort into SOMA FOOD (Mapping from Bfo to Dul)

		somaFood.addAxioms(relevantAxioms);

		somaFood.signature(Imports.EXCLUDED).map(OWLEntity::getIRI).collect(Collectors.toUnmodifiableSet())
		        .forEach(next -> {
			        somaFood.addAxioms(foodOn.annotationAssertionAxioms(next, Imports.INCLUDED));
		        });
	}

	private @NotNull Stream<OWLEntity> toEntity(@NotNull final IRI iri) {
		var entities = foodOn.getEntitiesInSignature(iri, Imports.INCLUDED);
		if (entities.isEmpty()) {
			throw new IllegalArgumentException("FoodOn does not contain an entity with the IRI '" + iri + "'");
		}
		return entities.stream();
	}

	/**
	 * Returns the (inferred) siblings of the given {@link OWLEntity}, including itself.
	 * This method supports {@link OWLClassExpression}s, {@link OWLDataProperty}s, and {@link OWLObjectProperty}s.
	 *
	 * @param entity The {@link OWLEntity} whose siblings should be returned
	 * @return The siblings of the given {@link OWLEntity}, including itself
	 */
	private @NotNull Stream<? extends OWLEntity> siblings(@NotNull final OWLEntity entity) {
		//return switch (entity) {
		//	case OWLClassExpression classExpression -> reasoner.superClasses(classExpression, true)
		//	                                                   .flatMap(next -> reasoner.subClasses(next, true));
		//	case OWLObjectProperty objectProperty -> reasoner.superObjectProperties(objectProperty, true)
		//	                                                 .flatMap(next -> reasoner.subObjectProperties(next, true))
		//	                                                 .map(OWLObjectPropertyExpression::getNamedProperty);
		//	case OWLDataProperty dataProperty -> reasoner.superDataProperties(dataProperty, true)
		//	                                             .flatMap(next -> reasoner.subDataProperties(next, true));
		//	default -> throw new IllegalArgumentException("Unexpected value: " + entity);
		//};

		if (entity instanceof OWLClassExpression) {
			return reasoner.superClasses((OWLClassExpression) entity, true)
			               .flatMap(next -> reasoner.subClasses(next, true));
		}
		if (entity instanceof OWLObjectProperty) {
			return reasoner.superObjectProperties((OWLObjectPropertyExpression) entity, true)
			               .flatMap(next -> reasoner.subObjectProperties(next, true))
			               .map(OWLObjectPropertyExpression::getNamedProperty);
		}
		if (entity instanceof OWLDataProperty) {
			return reasoner.superDataProperties((OWLDataProperty) entity, true)
			               .flatMap(next -> reasoner.subDataProperties(next, true));
		}
		throw new IllegalArgumentException("Unexpected value: " + entity);
	}
}
