package src.model;

import java.util.Collections;
import java.util.LinkedList;

public class GameBoard {

    public static LinkedList<PlayerRecord> database = new LinkedList<>();

    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;

    public int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    public int score = 0;
    public int level = 1;
    public int lives = 3;
    public boolean isFlickerMode = false;
    public boolean bonusLifeGiven5000 = false;

    // PERKS
    public LinkedList<String> perks = new LinkedList<>();
    public final int MAX_PERKS = 3;

    public boolean slowTime = false;
    public boolean doubleScore = false;
    public boolean shield = false;
    public boolean bomb = false;
    public boolean lucky = false;

    public int slowTimer = 0;
    public int doubleTimer = 0;
    public int perkCooldown = 0;
    public final int PERK_COOLDOWN_MAX = 15;

    // Real-time 30-second countdown timers (in seconds)
    public static final int PERK_DURATION = 30;
    public int slowTimeSeconds = 0;
    public int doubleScoreSeconds = 0;
    public int shieldSeconds = 0;
    public int bombSeconds = 0;
    public int luckySeconds = 0;

    public final int[][][] SHAPES = {
            { { 1, 1, 1, 1 } },
            { { 1, 1 }, { 1, 1 } },
            { { 0, 1, 0 }, { 1, 1, 1 } },
            { { 1, 0, 0 }, { 1, 1, 1 } },
            { { 0, 0, 1 }, { 1, 1, 1 } },
            { { 0, 1, 1 }, { 1, 1, 0 } },
            { { 1, 1, 0 }, { 0, 1, 1 } }
    };

    public void resetBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++)
            for (int j = 0; j < BOARD_WIDTH; j++)
                board[i][j] = 0;
    }

    public static LinkedList<PlayerRecord> getSortedRecords() {
        LinkedList<PlayerRecord> sorted = new LinkedList<>(database);
        Collections.sort(sorted, (a, b) -> Integer.compare(b.score, a.score));
        return sorted;
    }
}