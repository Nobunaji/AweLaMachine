package awele.bot.competitor;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NegaMax extends CompetitorBot {
  private static final int MAX_DEPTH = 9;
  private double[] decision;

  private HashMap<String, Double> evalCache;

  private static double LOW_FACTOR = 0.05;
  private static double MEDIUM_FACTOR = 0.1;
  private static double HIGH_FACTOR = 0.2;
  private static double EXTREME_FACTOR = 0.5;

  /**
   * @brief Constructeur
   * @throws InvalidBotException
   */
  public NegaMax() throws InvalidBotException {
    this.setBotName("NegaMax");
    this.addAuthor("Nicolas BRION");
  }

  @Override
  public void initialize() { this.evalCache = new HashMap<>(); }

  @Override
  public void finish() { 
    if (this.evalCache != null) this.evalCache.clear(); 
  } 

  /**
   * Get the bot's decision using NegaMax algorithm with alpha-beta pruning
   * @param board Current board state
   * @return Decision array
  **/
  private static final int DECISION_TIME_MS = 100; // ms

  @Override
  public double[] getDecision(Board board) {

    double alpha = Double.NEGATIVE_INFINITY;
    double beta = Double.POSITIVE_INFINITY;

    this.decision = new double[Board.NB_HOLES];

    boolean[] validMoves = board.validMoves(board.getCurrentPlayer());

    for (int i = 0; i < Board.NB_HOLES; i++) {
      if (validMoves[i]) {
        try {
          double [] sim = new double [Board.NB_HOLES];
          sim[i] = 1;
          this.decision[i] = -negamax(board.playMoveSimulationBoard(board.getCurrentPlayer(), sim), 1, -beta, -alpha);
        }
        catch(InvalidBotException ignored) {}
      }
    }

    return this.decision;
  }

  /**
   * NegaMax algorithm with alpha-beta pruning
   * @param board Board's state
   * @param depth Node's depth
   * @param alpha Alpha threshold
   * @param beta  Beta threshold
   * @return The best evaluation discovered this far
   */
  private double negamax(Board board, int depth, double alpha, double beta) {

    if (depth == MAX_DEPTH || board.getNbSeeds() <= 6) {
      return evaluate(board);
    }

    double maxEval = Double.NEGATIVE_INFINITY;
    // Get all valid moves in the simulated board's position
    boolean[] validMoves = board.validMoves(board.getCurrentPlayer());

    // Go through every possible moves in the board's current state
    for (int i = 0; i < Board.NB_HOLES; i++) {
      if (validMoves[i]) {
        try {
          double[] sim = new double[Board.NB_HOLES];
          sim[i] = 1;


          Board copy = board.playMoveSimulationBoard(board.getCurrentPlayer(), sim);

          // Recursive call : Explore all the possibility up to MAX_DEPTH
          double eval = -negamax(copy, depth + 1, -beta, -alpha);

          //
          maxEval = Math.max(maxEval, eval);

          // Alpha-beta pruning
          alpha = Math.max(alpha, eval);
          if (alpha >= beta) {
            break;
          }
        } catch (InvalidBotException ignored) {}
      }
    }

    return maxEval;
  }

  double evaluate(Board board) {

    // Build a cache key using board string representation and current player
    String key = board.toString() + ":" + board.getCurrentPlayer();
    Double cached = this.evalCache != null ? this.evalCache.get(key) : null;
    if (cached != null) {
      return cached;
    }

    int currentPlayer = board.getCurrentPlayer();
    int opponentPlayer = Board.otherPlayer(currentPlayer);

    // Basic score difference
    double score = board.getScore(currentPlayer) - board.getScore(opponentPlayer);

    // Heuristic: Seed Repartition Between Players
    // This heuristic favors having more seeds in the player's holes compared to the opponent's holes.
    int playerSeeds = 0;
    int opponentSeeds = 0;
    for (int i = 0; i < Board.NB_HOLES; i++) {
      playerSeeds += board.getPlayerHoles()[i];
      opponentSeeds += board.getOpponentHoles()[i];
    }
    score += (playerSeeds - opponentSeeds) * MEDIUM_FACTOR;

    // This heuristic evaluates the potential for capturing opponent's seeds in the next move.
    int potentialCaptures = 0;
    for (int i = 0; i < Board.NB_HOLES; i++) {
      if (board.getPlayerHoles()[i] == 1) {
        potentialCaptures += board.getOpponentHoles()[(i+1) % Board.NB_HOLES];
      }
    }
    score += potentialCaptures * HIGH_FACTOR;

    // Heuristic : Seed Distribution
    // This heuristic favors moves that distribute seeds evenly across the player's holes.
    int seedDistribution = 0;
    for (int i = 0; i < Board.NB_HOLES; i++) {
      seedDistribution += Math.abs(board.getPlayerHoles()[i] - board.getOpponentHoles()[i]);
    }
    score += seedDistribution * LOW_FACTOR;

    // Heuristic : Endgame Considerations
    // This heuristic adjusts the evaluation based on the number of seeds left, favoring moves that lead to a favorable endgame.
    int endGame = 0;
    if (board.getNbSeeds() <= 12) {
      endGame += (board.getScore(currentPlayer) - board.getScore(opponentPlayer));
    }
    score += endGame * EXTREME_FACTOR;

    // Store evaluation in cache
    if (this.evalCache != null) this.evalCache.put(key, score);

    return score;
  }

  @Override
  public void learn() {} // Rien à apprendre
}