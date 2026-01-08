package awele.bot.competitor;

import awele.core.Board;

import java.util.HashMap;
import java.util.Random;

/**
 * Table de transposition pour optimiser l'algorithme MinMax
 * Utilise un HashMap avec Zobrist hash comme clé
 */
public class TranspositionTable {

  /**
   * Classe interne pour stocker les entrées de la table de transposition
   */
  private static class ttEntry {
    double evaluation;
    int depth;
    // 0: exact, 1: lower bound, 2: upper bound
    byte type; 

    ttEntry(double evaluation, int depth, byte type) {
      this.evaluation = evaluation;
      this.depth = depth;
      this.type = type;
    }
  }

  private HashMap<Long, ttEntry> table;
  
  // Zobrist hash tables
  private static final long[][] zobristTable = initializeZobristTable();
  private static final long zobristPlayer = 0xABCDEF1234567890L;
  
  /**
   * Initialise la table Zobrist avec des nombres aléatoires
   */
  private static long[][] initializeZobristTable() {
    long[][] table = new long[12][49];
    // What is the answer to life, the universe and everything?
    Random random = new Random(42); 
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 49; j++) {
        table[i][j] = random.nextLong();
      }
    }
    return table;
  }

  /**
   * Constructeur
   */
  public TranspositionTable() {
    this.table = new HashMap<>();
  }

  /**
   * Calcule le hash Zobrist de la position du plateau
   * Utilise XOR pour combiner les hash des différents éléments
   */
  private long getZobristHash(Board board, boolean maximizing, int depth) {
    long hash = 0;

    // Récupérer l'état du plateau
    int[] playerHoles = board.getPlayerHoles();
    int[] opponentHoles = board.getOpponentHoles();

    // XOR avec les graines de chaque trou du joueur (positions 0-5)
    for (int i = 0; i < 6; i++) {
      hash ^= zobristTable[i][playerHoles[i]];
    }

    // XOR avec les graines de chaque trou de l'adversaire (positions 6-11)
    for (int i = 0; i < 6; i++) {
      hash ^= zobristTable[6 + i][opponentHoles[i]];
    }

    // XOR avec le joueur actuel
    if (board.getCurrentPlayer() == 1) {
      hash ^= zobristPlayer;
    }

    // Ajouter des informations supplémentaires si nécessaire
    // XOR avec la profondeur (optionnel, pour différencier les évaluations par profondeur)
    hash ^= ((long) depth << 56); // Shift pour utiliser les bits hauts

    return hash;
  }

  /**
   * Ajoute une entrée dans la table de transposition
   */
  public void put(Board board, boolean maximizing, int depth, double evaluation) {
    long hash = getZobristHash(board, maximizing, depth);
    this.table.put(hash, new ttEntry(evaluation, depth, (byte) 0)); // 0 = exact value
  }

  /**
   * Récupère une évaluation de la table de transposition
   */
  public Double get(Board board, boolean maximizing, int depth) {
    long hash = getZobristHash(board, maximizing, depth);
    ttEntry entry = this.table.get(hash);
    
    if (entry != null && entry.depth >= depth) {
      return entry.evaluation;
    }
    
    return null;
  }

  /**
   * Vide la table de transposition
   */
  public void clear() {
    this.table.clear();
  }

  /**
   * Retourne le nombre d'entrées dans la table
   */
  public int getSize() {
    return this.table.size();
  }
}
