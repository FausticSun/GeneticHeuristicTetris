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
        int[] simulatedTop = Arrays.copyOf(getTop(), COLS);

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

        int landingHeight = height+getpHeight()[nextPiece][orient]/2;


        //for each column in the piece - fill in the appropriate blocks
        for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for(int h = height+getpBottom()[nextPiece][orient][i]; h < height+getpTop()[nextPiece][orient][i]; h++) {
                simulatedField[h][i+slot] = getTurnNumber();
            }
        }

        //adjust top
        for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
            simulatedTop[slot+c]=height+getpTop()[nextPiece][orient][c];
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
                    for(int i = r; i < simulatedTop[c]; i++) {
                        simulatedField[i][c] = simulatedField[i+1][c];
                    }
                    //lower the top
                    simulatedTop[c]--;
                    while(simulatedTop[c]>=1 && simulatedField[simulatedTop[c]-1][c]==0) simulatedTop[c]--;
                }
            }
        }

        List<Integer> features = new ArrayList<>();
        // Original features in the project description
//        // Bias?
//        features.add(1);
//        // Column Heights
//        for (int i: simulatedTop) {
//            features.add(i);
//        }
//        // Difference between adjacent column height
//        for (int i=0; i<simulatedTop.length-1; i++)
//            features.add(Math.abs(simulatedTop[0] - simulatedTop[1]));
//        // Maximum column height
//        int max = 0;
//        for (int i: simulatedTop)
//            max = Math.max(i, max);
//        features.add(max);
//        // Holes
//        int holes = 0;
//        for (int col=0; col<COLS; col++) {
//            for (int row=simulatedTop[col]; row>=0; row--) {
//                if (simulatedField[row][col] == 0) {
//                    holes++;
//                }
//            }
//        }
//        features.add(holes);
//        // Rows cleared
//        features.add(rowsCleared);

        // El-Tetris features
        // http://imake.ninja/el-tetris-an-improvement-on-pierre-dellacheries-algorithm/
        features.add(landingHeight);
        features.add(rowsCleared);
        int rowTransitions = 0;
        for (int row=0; row<ROWS; row++) {
            int prev = simulatedField[row][0];
            for (int col=1; col<COLS; col++) {
                if ((prev == 0 && simulatedField[row][col] != 0) ||
                        (prev != 0 && simulatedField[row][col] == 0)) {
                    rowTransitions++;
                }
                prev = simulatedField[row][col];
            }
        }
        features.add(rowTransitions);
        int colTransitions = 0;
        for (int col=0; col<COLS; col++) {
            int prev = simulatedField[0][col];
            for (int row=1; row<ROWS; row++) {
                if ((prev == 0 && simulatedField[row][col] != 0) ||
                        (prev != 0 && simulatedField[row][col] == 0)) {
                    colTransitions++;
                }
                prev = simulatedField[row][col];
            }
        }
        features.add(colTransitions);
        int holes = 0;
        for (int col=0; col<COLS; col++) {
            for (int row=simulatedTop[col]; row>=0; row--) {
                if (simulatedField[row][col] == 0) {
                    holes++;
                }
            }
        }
        features.add(holes);
        int wellSum = 0;
        boolean prevIsWell, thisIsWell;
        prevIsWell = false;
        for (int row=0; row<ROWS; row++) {
            thisIsWell = simulatedField[row][0] == 0 &&
                    simulatedField[row][1] != 0;
            if (prevIsWell && thisIsWell) {
                wellSum++;
            }
            prevIsWell = thisIsWell;
        }
        for (int col=1; col<COLS-1; col++) {
            prevIsWell = false;
            for (int row=0; row<ROWS; row++) {
                thisIsWell = simulatedField[row][col] == 0 &&
                        simulatedField[row][col-1] != 0 &&
                        simulatedField[row][col+1] != 0;
                if (prevIsWell && thisIsWell) {
                    wellSum++;
                }
                prevIsWell = thisIsWell;
            }
        }
        prevIsWell = false;
        for (int row=0; row<ROWS; row++) {
            thisIsWell = simulatedField[row][COLS-1] == 0 &&
                    simulatedField[row][COLS-2] != 0;
            if (prevIsWell && thisIsWell) {
                wellSum++;
            }
            prevIsWell = thisIsWell;
        }
        features.add(wellSum);

        return IntStream.range(0, Chromosome.CHROMOSOME_SIZE).boxed()
                .mapToDouble(i -> chromosome.getGenes().get(i)*features.get(i))
                .sum();
    }
}
