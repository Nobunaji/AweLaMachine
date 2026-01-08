package awele.bot.competitor;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.util.HashMap;
import awele.bot.competitor.TranspositionTable;

/* Améliorations possibles :
  - Optimiser la fonction d'évaluation
  - Implémenter une table de transposition plus avancée
  - Iterative Deepening
  - Trier les coups possibles pour améliorer l'efficacité de l'élagage alpha-beta
  - Utiliser des heuristiques pour prioriser certains coups
*/
public class MinMax extends CompetitorBot {
  private static final int MAX_DEPTH = 12;
  private double[] decision;

  private TranspositionTable transpTable;
  private boolean loggingEnabled = true;
  private String logFilePath = "minmax.log";

  // Logging
  private int moveCounter = 0;
  private long nodesExplored = 0;
  private HashMap<Integer, Long> nodesPerMove;
  private long cacheHits = 0;
  private HashMap<Integer, Long> cacheHitsPerMove;
  private long pruningCount = 0;
  private HashMap<Integer, Long> pruningPerMove;

  /**
   * @brief Constructeur
   * @throws InvalidBotException
  **/
  public MinMax() throws InvalidBotException {
    this.setBotName("AwéLaMachine");
    this.addAuthor("Nicolas BRION");
  }

  @Override
  public void initialize() { 
    this.transpTable = new TranspositionTable();

    // Logging
    this.moveCounter = 0;
    this.nodesExplored = 0;
    this.nodesPerMove = new HashMap<>();
    this.cacheHits = 0;
    this.cacheHitsPerMove = new HashMap<>();
    this.pruningCount = 0;
    this.pruningPerMove = new HashMap<>();
  }

  @Override
  public void finish() {
    if (this.transpTable != null) this.transpTable.clear();
  }

  /**
   * Obtenir la décision du bot en utilisant l'algorithme MinMax avec élagage alpha-beta
   * @param board Etat actuel du plateau
   * @return Tableau de décision
  **/
  public double [] getDecision(Board board) {
    this.moveCounter++;
    long moveNodesStart = this.nodesExplored;
    long moveCacheHitsStart = this.cacheHits;
    long movePruningStart = this.pruningCount;
    
    this.decision = new double[Board.NB_HOLES];

    boolean [] validMoves = board.validMoves(board.getCurrentPlayer());

    for (int i = 0; i < Board.NB_HOLES; i++) {
      Board copy = (Board) board.clone();
      double alpha = Double.NEGATIVE_INFINITY;
      double beta = Double.POSITIVE_INFINITY;

      if (validMoves[i]) {
        try {
          double [] move = new double [Board.NB_HOLES];
          move[i] = 1;
          
          int score = copy.playMoveSimulationScore(board.getCurrentPlayer(), move);
          copy = copy.playMoveSimulationBoard(board.getCurrentPlayer(), move);
          
          this.decision[i] = minmax(copy, false, 1, alpha, beta, score);
        }
        catch(InvalidBotException ignored) {}
      }
    }

    // Store nodes explored for this move
    long moveNodesEnd = this.nodesExplored;
    this.nodesPerMove.put(this.moveCounter, moveNodesEnd - moveNodesStart);
    
    // Store cache hits for this move
    long moveCacheHitsEnd = this.cacheHits;
    this.cacheHitsPerMove.put(this.moveCounter, moveCacheHitsEnd - moveCacheHitsStart);
    
    // Store pruning count for this move
    long movePruningEnd = this.pruningCount;
    this.pruningPerMove.put(this.moveCounter, movePruningEnd - movePruningStart);

    // Log the chosen move and board state
    if (this.loggingEnabled) {
      logDecision(board, this.decision);
    }

    return this.decision;
  }

  /**
   * Algorithme MinMax avec élagage alpha-beta
   * @param board Etat actuel du plateau
   * @param maximizing Indique si ce noeud maximise
   * @param depth Profondeur actuelle de l'exploration
   * @param alpha Seuil alpha pour l'élagage
   * @param beta  Seuil beta pour l'élagage
   * @param score Score de la simulation du coup joué
   * @return Score évalué du plateau (du point de vue du joueur actuel)
  **/
  private double minmax(Board board, boolean maximizing, int depth, double alpha, double beta, int score) {
    // Count this node as explored
    this.nodesExplored++;

    // 🔹 Lecture cache via table de transposition
    Double cachedValue = this.transpTable.get(board, maximizing, depth);
    if(cachedValue != null) {
      this.cacheHits++;
      return cachedValue;
    }

    // 🔹 Conditions Terminales
    if((score < 0) || (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) || (board.getNbSeeds() <= 6)) {
      double evaluation = evaluate(board);
      this.transpTable.put(board, maximizing, depth, evaluation);
      return evaluation;
    }

    if (depth == MAX_DEPTH) {
      double evaluation = evaluate(board);
      this.transpTable.put(board, maximizing, depth, evaluation);
      return evaluation;
    }

    boolean [] validMoves = board.validMoves(board.getCurrentPlayer());

    double evaluation;

    if (maximizing) {
      double maxEval = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < Board.NB_HOLES; i++) {
        if (validMoves[i]) {
          try {
            double [] move = new double[Board.NB_HOLES];
            move[i] = 1;

            int newScore = board.playMoveSimulationScore(board.getCurrentPlayer(), move);
            Board copy = board.playMoveSimulationBoard(board.getCurrentPlayer(), move);
            
            double eval = minmax(copy, false, depth + 1, alpha, beta, newScore);

            maxEval = Math.max(maxEval, eval);

            // Elagage alpha-beta
            alpha = Math.max(alpha, eval);
            if (alpha >= beta) {
              this.pruningCount++;
              break;
            }
          } catch (InvalidBotException ignored) {}
        }
      }
      evaluation = maxEval;
    } 
    else {
      double minEval = Double.POSITIVE_INFINITY;
      for (int i = 0; i < Board.NB_HOLES; i++) {
        if (validMoves[i]) {
          try {
            double [] move = new double[Board.NB_HOLES];
            move[i] = 1;

            int newScore = board.playMoveSimulationScore(board.getCurrentPlayer(), move);
            Board copy = board.playMoveSimulationBoard(board.getCurrentPlayer(), move);

            double eval = minmax(copy, true, depth + 1, alpha, beta, newScore);

            minEval = Math.min(minEval, eval);

            // Elagage alpha-beta
            beta = Math.min(beta, eval);
            if (alpha >= beta) {
              this.pruningCount++;
              break;
            }
          } catch (InvalidBotException ignored) {}
        }
      }
      evaluation = minEval;
    }

    // 🔹 Ecriture cache via table de transposition
    this.transpTable.put(board, maximizing, depth, evaluation);
    return evaluation;
  }

  private double evaluate(Board board) {
    int opponentPlayer = Board.otherPlayer(board.getCurrentPlayer());

    double score = board.getScore(board.getCurrentPlayer()) - board.getScore(opponentPlayer);

    return score;
  }

  /**
   * Logs the board state and the chosen move
   * @param board The current board state
   * @param decision The decision vector containing move evaluations
   */
  private void logDecision(Board board, double[] decision) {
    // Find the best move
    boolean[] validMoves = board.validMoves(board.getCurrentPlayer());
    int bestMove = -1;
    double bestValue = Double.NEGATIVE_INFINITY;
    
    for (int i = 0; i < Board.NB_HOLES; i++) {
      if (validMoves[i] && decision[i] > bestValue) {
        bestValue = decision[i];
        bestMove = i;
      }
    }

    // Get nodes explored for this move
    long nodesThisMove = this.nodesPerMove.getOrDefault(this.moveCounter, 0L);
    long cacheHitsThisMove = this.cacheHitsPerMove.getOrDefault(this.moveCounter, 0L);
    long pruningThisMove = this.pruningPerMove.getOrDefault(this.moveCounter, 0L);

    // Build the log message
    StringBuilder logMessage = new StringBuilder();

    logMessage.append("=".repeat(70)).append("\n");
    logMessage.append("MinMax Decision Log - Move #").append(this.moveCounter).append("\n");
    logMessage.append("\n");
    
    // Score Information
    logMessage.append("Current Scores - Player 0: ").append(board.getScore(0)).append(" | Player 1: ").append(board.getScore(1)).append("\n");
    
    // Board state
    logMessage.append("\nBoard State:\n");
    logMessage.append(formatBoardState(board));
    
    // Move evaluations
    logMessage.append("\nMove Evaluations:\n");
    for (int i = 0; i < Board.NB_HOLES; i++) {
      if (validMoves[i]) {
        logMessage.append(String.format("  Hole %d: %.2f", i, decision[i]));
        logMessage.append("\n");
      } else {
        logMessage.append(String.format("  Hole %d: INVALID\n", i));
      }
    }
    
    logMessage.append("\nChosen Move: Hole ").append(bestMove).append(" (Value: ").append(String.format("%.2f", bestValue)).append(")\n");
    
    // Statistics
    logMessage.append("\nStatistics:\n");
    logMessage.append(String.format("  Nodes explored for this move: %d\n", nodesThisMove));
    logMessage.append(String.format("  Total nodes explored so far: %d\n", this.nodesExplored));
    logMessage.append(String.format("  Cache hits for this move: %d\n", cacheHitsThisMove));
    logMessage.append(String.format("  Total cache hits so far: %d\n", this.cacheHits));
    logMessage.append(String.format("  Alpha-beta pruning cuts for this move: %d\n", pruningThisMove));
    logMessage.append(String.format("  Total alpha-beta pruning cuts so far: %d\n", this.pruningCount));
    
    logMessage.append("=".repeat(70)).append("\n");
    
    // Write to log file
    writeToLogFile(logMessage.toString());
  }
  
  /**
   * Formats the board state for logging with visual representation
   * @param board The board to format
   * @return A formatted string representation of the board
   */
  private String formatBoardState(Board board) {
    StringBuilder sb = new StringBuilder();
    
    // Top row (Player 1)
    sb.append("Player 1:  ");
    int[] player1Holes = board.getOpponentHoles();
    for (int i = Board.NB_HOLES - 1; i >= 0; i--) {
      sb.append(String.format("[%d] ", player1Holes[i]));
    }
    sb.append("\n");
    
    // Bottom row (Player 0)
    sb.append("Player 0:  ");
    int[] player0Holes = board.getPlayerHoles();
    for (int i = 0; i < Board.NB_HOLES; i++) {
      sb.append(String.format("[%d] ", player0Holes[i]));
    }
    sb.append("\n");
    
    return sb.toString();
  }

  /**
   * Writes the log message to the log file
   * @param message The message to write
   */
  private void writeToLogFile(String message) {
    try {
      java.io.FileWriter fw = new java.io.FileWriter(this.logFilePath, true);
      java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
      java.io.PrintWriter out = new java.io.PrintWriter(bw);
      out.println(message);
      out.close();
    } catch (java.io.IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void learn() {}   
}