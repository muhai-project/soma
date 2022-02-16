import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.modularity.ModuleExtractor;
import org.semanticweb.owlapi.modularity.locality.LocalityClass;
import org.semanticweb.owlapi.modularity.locality.SyntacticLocalityModuleExtractor;

import java.util.Objects;

public class SomaFoodGenerator {

    private final OWLOntology foodOn;

    private final OWLOntology somaFood;

    private final ModuleExtractor moduleExtractor;

    public SomaFoodGenerator(@NotNull OWLOntology foodOn, @NotNull OWLOntology somaFood) {
        Objects.requireNonNull(foodOn);
        Objects.requireNonNull(somaFood);

        this.foodOn = foodOn;
        this.somaFood = somaFood;

        moduleExtractor =  new SyntacticLocalityModuleExtractor(LocalityClass.STAR, foodOn.axioms());
    }

    public OWLOntology getFoodOn() {
        return foodOn;
    }

    public OWLOntology getSomaFood() {
        return somaFood;
    }
}
