import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Population {
    private static final Logger LOGGER = Logger.getLogger( Population.class.getName() );
    public static int POPULATION_SIZE = 100;
    public static double SURVIVAL_THRESHOLD = 0.2;
    private List<Chromosome> chromosomes;
    private int generation = 0;

    public Population() {
        chromosomes = IntStream.range(0, 100).boxed()
                .map(i -> new Chromosome())
                .collect(Collectors.toList());
        evaluateFitness();
    }

    public void advance() {
        generation++;
        chromosomes = generateNextPopulation();
        evaluateFitness();
        LOGGER.info(String.format("Generation %d best fitness: %f",
                generation,
                this.getFittest().getFitness()));
        LOGGER.fine(getFittest().getGenes().stream()
                .map(d -> Double.toString(d))
                .collect(Collectors.joining(", ")));
    }

    private List<Chromosome> generateNextPopulation() {
        List<Chromosome> newPop = new ArrayList<>();
        newPop.add(this.getFittest());
        chromosomes.sort(Comparator.reverseOrder());
        chromosomes = chromosomes.subList(0, (int) (chromosomes.size()*SURVIVAL_THRESHOLD));
        newPop.addAll(IntStream.range(0, POPULATION_SIZE-1).boxed()
                .map(i -> getRandom().crossover(getRandom()))
                .collect(Collectors.toList()));
        return newPop;
    }

    private void evaluateFitness() {
        chromosomes.parallelStream().forEach(Chromosome::evaluateFitness);
    }

    private Chromosome getRandom() {
        return chromosomes.get((new Random()).nextInt(chromosomes.size()));
    }

    public Chromosome getFittest() {
        return chromosomes.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    public int getGeneration() {
        return generation;
    }
}
