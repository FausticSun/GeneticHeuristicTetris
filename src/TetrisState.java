import javax.swing.plaf.ComponentInputMapUIResource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TetrisState extends State {
    private Chromosome chromosome;

    public TetrisState(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    public int getBestMove() {
        return IntStream.range(0, legalMoves().length).parallel().boxed()
                .max(Comparator.comparing(this::evaluateHeuristic))
                .orElse(0);
    }

    private double evaluateHeuristic(int move) {
        // Create a copy of game field for simulation
        int[][] simulatedField = new int[ROWS][];
        for (int i=0; i<ROWS; i++) {
            simulatedField[i] = Arrays.copyOf(getField()[i], COLS);
        }

        // Convert single int move to orient and slot
        int orient = legalMoves[nextPiece][move][ORIENT];
        int slot = legalMoves[nextPiece][move][SLOT];

        // Simulate the move
        //height if the first column makes contact
        int height = getTop()[slot]-getpBottom()[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for(int c = 1; c < pWidth[nextPiece][orient];c++) {
            height = Math.max(height,getTop()[slot+c]-getpBottom()[nextPiece][orient][c]);
        }

        //check if game ended
        if(height+getpHeight()[nextPiece][orient] >= ROWS) {
            return 0.0;
        }


        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for(int h = height+getpBottom()[nextPiece][orient][i]; h < height+getpTop()[nextPiece][orient][i]; h++) {
                simulatedField[h][i+slot] = getTurnNumber();
            }
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for(int r = height+getpHeight()[nextPiece][orient]-1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for(int c = 0; c < COLS; c++) {
                if(simulatedField[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if(full) {
                rowsCleared++;
                //for each column
                for(int c = 0; c < COLS; c++) {

                    //slide down all bricks
                    for(int i = r; i < getTop()[c]; i++) {
                        simulatedField[i][c] = simulatedField[i+1][c];
                    }
                }
            }
        }

        List<Integer> features = new ArrayList<>();
        // Bias?
        features.add(1);
        // Column Heights
        Integer[] colHeights = new Integer[COLS];
        for (int i=0; i<colHeights.length; i++) {
            colHeights[i] = 0;
        }
        for (int row=0; row<simulatedField.length; row++) {
            for (int col=0; col<simulatedField[row].length; col++) {
                if (simulatedField[row][col] != 0) {
                    colHeights[col] = Math.max(colHeights[col], row);
                }
            }
        }
        features.addAll(Arrays.asList(colHeights));
        // Difference between adjacent column height
        for (int i=0; i<colHeights.length-1; i++)
            features.add(Math.abs(colHeights[0] - colHeights[1]));
        // Maximum column height
        features.add(Collections.max(Arrays.asList(colHeights)));
        // Holes
        int holes = 0;
        for (int col=0; col<COLS; col++) {
            for (int row=colHeights[col]; row>=0; row--) {
                if (simulatedField[row][col] == 0) {
                    holes++;
                }
            }
        }
        features.add(holes);
        // Rows cleared
        features.add(rowsCleared);


        return IntStream.range(0, Chromosome.CHROMOSOME_SIZE).boxed()
                .mapToDouble(i -> chromosome.getGenes().get(i)*features.get(i))
                .sum();
    }
}
