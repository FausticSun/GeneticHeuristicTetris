import java.util.logging.Logger;

public class Experiment {
    private static final Logger LOGGER = Logger.getLogger(Experiment.class.getName());
    public static final int MAXIMUM_GENERATIONS = 1000;
    public static final int MAXIMUM_FITNESS = 1000000;
    private Population pop;

    public Experiment() {
        LOGGER.info("Initiating experiment");
        pop = new Population();
    }

    public void run(int generations) {
        LOGGER.info(String.format("Starting the experiment at generation %d", pop.getGeneration()));
        if (generations != 0) {
            for (int i=0; i<generations; i++) {
                pop.advance();
            }
        } else {
            while (pop.getGeneration() < MAXIMUM_GENERATIONS &&
                    pop.getFittest().getFitness() < MAXIMUM_FITNESS) {
                pop.advance();
            }
        }
    }

    public Chromosome getFittest() {
        return pop.getFittest();
    }

    public int getGeneration() {
        return pop.getGeneration();
    }
}
