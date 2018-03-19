import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Experiment ex = new Experiment();
        TetrisState s;
        TFrame demo;

        while (ex.getGeneration() < Experiment.MAXIMUM_GENERATIONS) {
            ex.run(1);

            LOGGER.info(String.format("Demoing fittest of Generation %d", ex.getGeneration()));
            s = new TetrisState(ex.getFittest());
            demo = new TFrame(s);

            while (!s.hasLost()) {
                s.makeMove();
                s.draw();
                s.drawNext(0, 0);
            }
            demo.dispose();
            LOGGER.info(String.format("%d moves made with %d rows cleared", s.getTurnNumber(), s.getRowsCleared()));
        }
    }
}
