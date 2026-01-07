package awele.bot.competitor;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.util.HashMap;
import java.util.Map;


/* Améliorations possibles :
  - Optimiser la fonction d'évaluation
  - Implémenter une table de transposition plus avancée
  - Ajuster la profondeur maximale en fonction du temps de calcul disponible
  - Trier les coups possibles pour améliorer l'efficacité de l'élagage alpha-beta
  - Utiliser des heuristiques pour prioriser certains coups

*/

public class MinMax extends CompetitorBot {
  private static final int MAX_DEPTH = 10;
  private double[] decision;

  private HashMap<String, Double> evalCache;

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
    this.evalCache = new HashMap<>();
  }

  @Override
  public void finish() {
    if (this.evalCache != null) this.evalCache.clear();
  }

  /**
   * Obtenir la décision du bot en utilisant l'algorithme MinMax avec élagage alpha-beta
   * @param board Etat actuel du plateau
   * @return Tableau de décision
  **/
  public double [] getDecision(Board board) {
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

    // 🔹 Lecture cache
    String key = board.toString()
    + "|P:" + board.getCurrentPlayer()
    + "|M:" + maximizing
    + "|D:" + depth;
    if(evalCache.containsKey(key)) {
      return evalCache.get(key);
    }

    // 🔹 Conditions Terminales
    if((score < 0) || (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) || (board.getNbSeeds() <= 6)) {
      double evaluation = evaluate(board);
      evalCache.put(key, evaluation);
      return evaluation;
    }

    if (depth == MAX_DEPTH) {
      double evaluation = evaluate(board);
      evalCache.put(key, evaluation);
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
              break;
            }
          } catch (InvalidBotException ignored) {}
        }
      }
      evaluation = minEval;
    }

    // 🔹 Ecriture cache
    evalCache.put(key, evaluation);
    return evaluation;
  }

  private double evaluate(Board board) {
    int opponentPlayer = Board.otherPlayer(board.getCurrentPlayer());

    double score = board.getScore(board.getCurrentPlayer()) - board.getScore(opponentPlayer);

    return score;
  }

  @Override
  public void learn() {}   
}