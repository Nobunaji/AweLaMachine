package awele.bot.competitor;

import awele.core.Board;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple transposition table for storing board evaluations.
 * Key: board.toString()
 * Stored: score (double), best move (int), alpha (double), beta (double)
 */
public class TranspositionTable {

  /** Entry stored in the transposition table */
  public static class Entry {
    public final double score;
    public final int currentPlayer;
    public final int depth;
    public final boolean maximizing;
    public final double alpha;
    public final double beta;

    public Entry(double score, int currentPlayer, int depth, boolean maximizing, double alpha, double beta) {
      this.score = score;
      this.currentPlayer = currentPlayer;
      this.depth = depth;
      this.maximizing = maximizing;
      this.alpha = alpha;
      this.beta = beta;
    }

    @Override
    public String toString() {
      return "Entry(score=" + score + ", currentPlayer=" + currentPlayer + ", depth=" + depth + ", maximizing=" + maximizing + ", alpha=" + alpha + ", beta=" + beta + ")";
    }
  }

  private final Map<String, Entry> table;

  public TranspositionTable() {
    this.table = new HashMap<>();
  }

  /** Clear the table */
  public void clear() {
    table.clear();
  }

  /** Return number of stored entries */
  public int size() {
    return table.size();
  }

  /** Check whether a board is present in the table */
  public boolean contains(Board board) {
    if (board == null) return false;
    return table.containsKey(board.toString());
  }

  /** Retrieve the entry for the given board, or null if none */
  public Entry get(Board board) {
    if (board == null) return null;
    return table.get(board.toString());
  }

  /** Put an entry into the table using board.toString() as key */
  public void put(Board board, double score, int currentPlayer, int depth, boolean maximizing, double alpha, double beta) {
    if (board == null) return;
    table.put(board.toString(), new Entry(score, currentPlayer, depth, maximizing, alpha, beta));
  }

  /** Convenience getters */
  public Double getScore(Board board) {
    Entry e = get(board);
    return e == null ? null : e.score;
  }

  public int getCurrentPlayer(Board board) {
    Entry e = get(board);
    return e == null ? -1 : e.currentPlayer;
  }

  public Integer getDepth(Board board) {
    Entry e = get(board);
    return e == null ? null : e.depth;
  }

  public Boolean isMaximizing(Board board) {
    Entry e = get(board);
    return e == null ? null : e.maximizing;
  }

  public Double getAlpha(Board board) {
    Entry e = get(board);
    return e == null ? null : e.alpha;
  }

  public Double getBeta(Board board) {
    Entry e = get(board);
    return e == null ? null : e.beta;
  }
}
