package src.view;

import src.model.GameBoard;
import src.presentation.GamePresenter;
import src.presentation.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class TetrisGame extends JPanel implements ActionListener, GameView {

    private final int TILE_SIZE = 35;
    private Timer timer;
    private Timer perkTimer; // 1-second real-time perk countdown
    private String playerName;
    private TetrisLegacy parent;
    private boolean isPaused = false;

    private GameBoard model;
    private GamePresenter presenter;

    private final Color[] PIECE_COLORS = {
            Color.CYAN, Color.YELLOW, new Color(155, 89, 182), Color.ORANGE,
            Color.BLUE, Color.GREEN, Color.RED
    };

    public TetrisGame(TetrisLegacy parent, String name) {
        this.parent = parent;
        this.playerName = name;

        model = new GameBoard();
        presenter = new GamePresenter(model, name, this, delay -> timer.setDelay(delay));

        setPreferredSize(new Dimension(GameBoard.BOARD_WIDTH * TILE_SIZE + 220, GameBoard.BOARD_HEIGHT * TILE_SIZE));
        setBackground(new Color(15, 15, 20));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_P) {
                    isPaused = !isPaused;
                    repaint();
                    return;
                }
                if (isPaused)
                    return;
                if (key == KeyEvent.VK_LEFT)
                    presenter.moveLeft();
                if (key == KeyEvent.VK_RIGHT)
                    presenter.moveRight();
                if (key == KeyEvent.VK_DOWN)
                    presenter.moveDown();
                if (key == KeyEvent.VK_UP)
                    presenter.rotatePiece();

                if (key == KeyEvent.VK_1)
                    presenter.usePerk(0);
                if (key == KeyEvent.VK_2)
                    presenter.usePerk(1);
                if (key == KeyEvent.VK_3)
                    presenter.usePerk(2);

                repaint();
            }
        });
    }

    public void startGame() {
        timer = new Timer(600, this);
        perkTimer = new Timer(1000, e -> {
            if (!isPaused) {
                presenter.tickSeconds();
                repaint();
            }
        });
        presenter.newPiece();
        timer.start();
        perkTimer.start();
    }

    @Override
    public void onRepaint() {
        repaint();
    }

    @Override
    public void onGameOver(String name, int score) {
        timer.stop();
        perkTimer.stop();
        parent.gameOver(name, score);
    }

    public void actionPerformed(ActionEvent e) {
        if (isPaused)
            return;
        presenter.tick();
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (model.isFlickerMode && new Random().nextInt(10) > 8)
            return;

        // --- DRAW BOARD BOUNDARY ---
        g2.setColor(new Color(40, 40, 50));
        g2.fillRect(0, 0, GameBoard.BOARD_WIDTH * TILE_SIZE, GameBoard.BOARD_HEIGHT * TILE_SIZE);

        // Draw Grid
        g2.setColor(new Color(60, 60, 70, 50));
        for (int x = 0; x <= GameBoard.BOARD_WIDTH * TILE_SIZE; x += TILE_SIZE)
            g2.drawLine(x, 0, x, GameBoard.BOARD_HEIGHT * TILE_SIZE);
        for (int y = 0; y <= GameBoard.BOARD_HEIGHT * TILE_SIZE; y += TILE_SIZE)
            g2.drawLine(0, y, GameBoard.BOARD_WIDTH * TILE_SIZE, y);

        int[][] currentPiece = presenter.getCurrentPiece();
        int curX = presenter.getCurX();
        int curY = presenter.getCurY();

        // GHOST PIECE ---
        int ghostY = presenter.getGhostY();
        g2.setColor(new Color(255, 255, 255, 40));
        drawPiece(g2, currentPiece, curX, ghostY, null);

        // Draw Fallen Blocks
        for (int i = 0; i < GameBoard.BOARD_HEIGHT; i++) {
            for (int j = 0; j < GameBoard.BOARD_WIDTH; j++) {
                if (model.board[i][j] != 0) {
                    drawBlock(g2, j * TILE_SIZE, i * TILE_SIZE, PIECE_COLORS[model.board[i][j] - 1]);
                }
            }
        }

        // Drawing the Active Piece
        if (!isPaused) {
            drawPiece(g2, currentPiece, curX, curY, PIECE_COLORS[presenter.getCurrentPieceType()]);
        }

        // SIDEBAR
        int sx = GameBoard.BOARD_WIDTH * TILE_SIZE + 20;
        g2.setColor(new Color(30, 30, 40));
        g2.fillRoundRect(sx - 10, 20, 190, 150, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("PLAYER", sx, 50);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2.setColor(Color.CYAN);
        g2.drawString(playerName, sx, 75);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("SCORE: " + model.score, sx, 110);
        g2.drawString("LEVEL: " + model.level, sx, 130);

        g2.setColor(Color.ORANGE);
        g2.drawString("LIVES: " + "❤".repeat(Math.max(0, model.lives)), sx, 155);

        // --- PERKS DISPLAY ---
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("PERKS:", sx, 185);

        if (model.perkCooldown > 0) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("COOLDOWN: " + model.perkCooldown, sx, 200);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            for (int i = 0; i < model.perks.size(); i++) {
                g2.setColor(Color.DARK_GRAY); // grayed out during cooldown
                g2.drawString((i + 1) + " : " + model.perks.get(i), sx, 218 + i * 20);
            }
        } else {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            for (int i = 0; i < model.perks.size(); i++) {
                g2.setColor(Color.YELLOW);
                g2.drawString((i + 1) + " : " + model.perks.get(i), sx, 205 + i * 20);
            }
            if (model.perks.isEmpty()) {
                g2.setColor(Color.DARK_GRAY);
                g2.drawString("none", sx, 205);
            }
        }

        // Active perk indicators with countdown
        int indY = 275;
        if (model.slowTime) {
            g2.setColor(Color.CYAN);
            g2.drawString("~ SLOW TIME ~   " + model.slowTimeSeconds + "s", sx, indY);
            indY += 18;
        }
        if (model.doubleScore) {
            g2.setColor(Color.MAGENTA);
            g2.drawString("~ 2x SCORE ~    " + model.doubleScoreSeconds + "s", sx, indY);
            indY += 18;
        }
        if (model.shield) {
            g2.setColor(Color.GREEN);
            g2.drawString("~ SHIELD ON ~   " + model.shieldSeconds + "s", sx, indY);
            indY += 18;
        }
        if (model.bomb) {
            g2.setColor(Color.RED);
            g2.drawString("~ BOMB READY ~ " + model.bombSeconds + "s", sx, indY);
            indY += 18;
        }
        if (model.lucky) {
            g2.setColor(Color.YELLOW);
            g2.drawString("~ LUCKY ON ~    " + model.luckySeconds + "s", sx, indY);
            indY += 18;
        }

        // Controls
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString("CONTROLS:", sx, 540);
        g2.drawString("Arrows : Move/Rotate", sx, 555);
        g2.drawString("1/2/3  : Use Perk", sx, 570);
        g2.drawString("P      : Pause", sx, 585);

        if (isPaused) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 40));
            g2.drawString("PAUSED", (GameBoard.BOARD_WIDTH * TILE_SIZE) / 2 - 80,
                    (GameBoard.BOARD_HEIGHT * TILE_SIZE) / 2);
        }
    }

    private void drawPiece(Graphics2D g, int[][] piece, int x, int y, Color color) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[0].length; j++) {
                if (piece[i][j] != 0) {
                    if (color == null) {
                        g.drawRect((x + j) * TILE_SIZE + 2, (y + i) * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                    } else {
                        drawBlock(g, (x + j) * TILE_SIZE, (y + i) * TILE_SIZE, color);
                    }
                }
            }
        }
    }

    private void drawBlock(Graphics2D g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRoundRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2, 8, 8);
        g.setColor(color.brighter());
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x + 2, y + 2, TILE_SIZE - 5, TILE_SIZE - 5, 5, 5);
    }
}