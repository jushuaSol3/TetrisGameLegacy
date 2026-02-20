package src.presentation;

import src.model.GameBoard;
import src.model.PlayerRecord;

import java.util.Random;

import javax.management.timer.Timer;

public class GamePresenter {

    private GameBoard model;
    private int curX = 0, curY = 0;
    private int[][] currentPiece;
    private int currentPieceType = 0;
    private Random rand = new Random();

    private GameView view;
    private String playerName;

    private Timer timerRef; // reference to swing timer for delay changes

    public interface TimerCallback {
        void setDelay(int delay);
    }

    private TimerCallback timerCallback;

    public GamePresenter(GameBoard model, String playerName, GameView view, TimerCallback timerCallback) {
        this.model = model;
        this.playerName = playerName;
        this.view = view;
        this.timerCallback = timerCallback;
    }

    public int getCurX() {
        return curX;
    }

    public int getCurY() {
        return curY;
    }

    public int[][] getCurrentPiece() {
        return currentPiece;
    }

    public int getCurrentPieceType() {
        return currentPieceType;
    }

    public void newPiece() {
        if (model.lucky) {
            currentPiece = model.SHAPES[0];
            currentPieceType = 0;
            model.lucky = false;
        } else {
            currentPieceType = rand.nextInt(model.SHAPES.length);
            currentPiece = model.SHAPES[currentPieceType];
        }

        curX = GameBoard.BOARD_WIDTH / 2 - 1;
        curY = 0;

        if (!canMove(currentPiece, curX, curY)) {
            if (model.shield) {
                model.shield = false;
                eraseLine();
                return;
            }

            model.lives--;

            if (model.lives <= 0) {
                view.onGameOver(playerName, model.score);
            } else {
                model.resetBoard();
                newPiece();
            }
        }
    }

    public void rotatePiece() {
        int r = currentPiece.length;
        int c = currentPiece[0].length;
        int[][] rotated = new int[c][r];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                rotated[j][r - 1 - i] = currentPiece[i][j];
        if (canMove(rotated, curX, curY))
            currentPiece = rotated;
    }

    public boolean canMove(int[][] piece, int nx, int ny) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[0].length; j++) {
                if (piece[i][j] != 0) {
                    int x = nx + j, y = ny + i;
                    if (x < 0 || x >= GameBoard.BOARD_WIDTH || y >= GameBoard.BOARD_HEIGHT)
                        return false;
                    if (y >= 0 && model.board[y][x] != 0)
                        return false;
                }
            }
        }
        return true;
    }

    public void moveLeft() {
        if (canMove(currentPiece, curX - 1, curY))
            curX--;
    }

    public void moveRight() {
        if (canMove(currentPiece, curX + 1, curY))
            curX++;
    }

    public void moveDown() {
        if (canMove(currentPiece, curX, curY + 1))
            curY++;
    }

    public void usePerk(int i) {
        if (i >= model.perks.size())
            return;
        if (model.perkCooldown > 0)
            return; // cooldown active, block usage

        String p = model.perks.remove(i);
        model.perkCooldown = model.PERK_COOLDOWN_MAX; // start cooldown

        if (p.equals("Slow Time")) {
            model.slowTime = true;
            model.slowTimer = 300;
            model.slowTimeSeconds = GameBoard.PERK_DURATION;
            timerCallback.setDelay(1000);
        }

        if (p.equals("Bomb")) {
            model.bomb = true;
            model.bombSeconds = GameBoard.PERK_DURATION;
        }

        if (p.equals("Line Erase"))
            eraseLine();

        if (p.equals("Double Score")) {
            model.doubleScore = true;
            model.doubleTimer = 300;
            model.doubleScoreSeconds = GameBoard.PERK_DURATION;
        }

        if (p.equals("Shield")) {
            model.shield = true;
            model.shieldSeconds = GameBoard.PERK_DURATION;
        }

        if (p.equals("Lucky")) {
            model.lucky = true;
            model.luckySeconds = GameBoard.PERK_DURATION;
        }
    }

    private void givePerk() {
        if (model.perks.size() >= model.MAX_PERKS)
            return;

        String[] list = {
                "Slow Time",
                "Bomb",
                "Line Erase",
                "Double Score",
                "Shield",
                "Lucky"
        };

        model.perks.add(list[rand.nextInt(list.length)]);
        GameEnhancer.playBonusSound();
    }

    private void eraseLine() {
        for (int i = GameBoard.BOARD_HEIGHT - 1; i > 0; i--)
            model.board[i] = model.board[i - 1].clone();
        model.board[0] = new int[GameBoard.BOARD_WIDTH];
    }

    private void explode(int x, int y) {
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++) {
                int nx = x + j, ny = y + i;
                if (nx >= 0 && nx < GameBoard.BOARD_WIDTH && ny >= 0 && ny < GameBoard.BOARD_HEIGHT)
                    model.board[ny][nx] = 0;
            }
    }

    public void tick() {
        if (canMove(currentPiece, curX, curY + 1)) {
            curY++;
        } else {
            placePiece();
            checkLines();
            newPiece();
        }
        updateLogic();
    }

    private void placePiece() {
        for (int i = 0; i < currentPiece.length; i++)
            for (int j = 0; j < currentPiece[0].length; j++)
                if (currentPiece[i][j] != 0) {
                    if (model.bomb)
                        explode(curX + j, curY + i);
                    else
                        model.board[curY + i][curX + j] = currentPieceType + 1;
                }
        model.bomb = false;
    }

    private void checkLines() {
        for (int i = GameBoard.BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < GameBoard.BOARD_WIDTH; j++)
                if (model.board[i][j] == 0) {
                    full = false;
                    break;
                }

            if (full) {
                model.score += model.doubleScore ? 200 : 100;

                if (rand.nextInt(100) < 30)
                    givePerk();

                for (int k = i; k > 0; k--)
                    model.board[k] = model.board[k - 1].clone();
                model.board[0] = new int[GameBoard.BOARD_WIDTH];
                i++;
            }
        }
    }

    private void updateLogic() {
        if (model.score >= 1000 && model.level == 1) {
            model.level = 2;
            timerCallback.setDelay(350);
        }

        if (model.score >= 2000 && model.level == 2) {
            model.level = 3;
            model.isFlickerMode = true;
        }

        if (model.score >= 3000 && model.level == 3) {
            model.level = 4;
            model.lives++;
            model.isFlickerMode = true;
        }

        if (model.score >= 4000 && model.level == 4) {
            model.level = 5;
            model.lives++;
            model.isFlickerMode = true;
        }

        if (model.score >= 5000 && model.level == 5) {
            model.level = 6;
            model.lives++;
            model.isFlickerMode = true;
        }

        if (model.score >= 5000 && !model.bonusLifeGiven5000) {
            if (model.lives < 3) {
                model.lives++;
                GameEnhancer.playBonusSound();
            }
            model.bonusLifeGiven5000 = true;
        }

        if (model.perkCooldown > 0)
            model.perkCooldown--;

        if (model.slowTime) {
            model.slowTimer--;
            if (model.slowTimer <= 0) {
                model.slowTime = false;
                timerCallback.setDelay(600);
            }
        }

        if (model.doubleScore) {
            model.doubleTimer--;
            if (model.doubleTimer <= 0)
                model.doubleScore = false;
        }
    }

    public int getGhostY() {
        int ghostY = curY;
        while (canMove(currentPiece, curX, ghostY + 1))
            ghostY++;
        return ghostY;
    }

    // Called every 1 real second to count down active perk timers
    public void tickSeconds() {
        if (model.slowTime) {
            model.slowTimeSeconds--;
            if (model.slowTimeSeconds <= 0) {
                model.slowTime = false;
                model.slowTimeSeconds = 0;
                timerCallback.setDelay(600);
            }
        }
        if (model.doubleScore) {
            model.doubleScoreSeconds--;
            if (model.doubleScoreSeconds <= 0) {
                model.doubleScore = false;
                model.doubleScoreSeconds = 0;
            }
        }
        if (model.shield) {
            model.shieldSeconds--;
            if (model.shieldSeconds <= 0) {
                model.shield = false;
                model.shieldSeconds = 0;
            }
        }
        if (model.bomb) {
            model.bombSeconds--;
            if (model.bombSeconds <= 0) {
                model.bomb = false;
                model.bombSeconds = 0;
            }
        }
        if (model.lucky) {
            model.luckySeconds--;
            if (model.luckySeconds <= 0) {
                model.lucky = false;
                model.luckySeconds = 0;
            }
        }
    }
}