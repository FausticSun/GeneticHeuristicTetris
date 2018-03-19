import java.util.Arrays;
import java.util.Objects;

public class FieldPieceKey {
    int[][] field;
    int piece;

    public FieldPieceKey(State s) {
        field = new int[State.ROWS][State.COLS];
        for (int row = 0; row< State.ROWS; row++) {
            for (int col = 0; col<State.COLS; col++){
                field[row][col] = s.getField()[row][col] == 0 ? 0 : 1;
            }
        }

        piece = s.getNextPiece();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FieldPieceKey)) return false;
        FieldPieceKey other = (FieldPieceKey) o;
        return Arrays.deepEquals(this.field, other.field) &&
                this.piece == other.piece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, piece);
    }
}
