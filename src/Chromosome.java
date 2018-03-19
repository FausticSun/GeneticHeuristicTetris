import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Chromosome implements Comparable<Chromosome> {
    public static final int CHROMOSOME_SIZE = 6;
    public static final double CROSSOVER_RATE = 0.5;
    public static final double AVERAGE_RATE = 0.5;
    public static final double MUTATION_RATE = 0.05;
    public static final int FITNESS_EVALUATIONS = 5;
    public static final double PERTERB_RANGE = 1;
    private List<Double> genes;
    private double fitness = -1;

    public Chromosome() {
        genes = IntStream.range(0, CHROMOSOME_SIZE).boxed()
                .map(i -> (new Random()).nextGaussian()*PERTERB_RANGE)
                .collect(Collectors.toList());
    }

    public Chromosome(Chromosome p1, Chromosome p2) {
        genes = new ArrayList<>();
        double random = Math.random();
        if (random < CROSSOVER_RATE) {
            // Random crossover
            for (int i = 0; i < CHROMOSOME_SIZE; i++) {
                if (Math.random() < 0.5) {
                    genes.add(p1.getGenes().get(i));
                } else {
                    genes.add(p2.getGenes().get(i));
                }
            }
        } else if (random - CROSSOVER_RATE < AVERAGE_RATE) {
            // Average crossover
            genes.addAll(IntStream.range(0, CHROMOSOME_SIZE)
                    .mapToDouble(i -> (p1.getGenes().get(i)+ p2.getGenes().get(i)))
                    .boxed().collect(Collectors.toList()));
        } else {
            genes.addAll(p1.genes);
        }
    }

    public Chromosome crossover(Chromosome other) {
        return (new Chromosome(this, other)).mutate();
    }

    private Chromosome mutate() {
        for (int i=0; i<genes.size(); i++) {
            if (Math.random() < MUTATION_RATE) {
                genes.set(i, genes.get(i) + (new Random()).nextGaussian()*PERTERB_RANGE);
            }
        }
        return this;
    }

    @Override
    public int compareTo(Chromosome o) {
        return Double.compare(this.getFitness(), o.getFitness());
    }

    public double getFitness() {
        return fitness;
    }

    public void evaluateFitness() {
        IntToDoubleFunction fitnessEvaluator = i -> {
            TetrisState s = new TetrisState(this);
            while (!s.hasLost()) {
                s.makeMove(s.getBestMove());
            }
            return s.getRowsCleared();
        };
        fitness = IntStream.range(0, FITNESS_EVALUATIONS).parallel()
                .mapToDouble(fitnessEvaluator)
                .average().orElse(0);
    }

    public List<Double> getGenes() {
        return genes;
    }
}
