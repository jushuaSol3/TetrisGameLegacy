package src.presentation;

public interface GameView {
    void onRepaint();

    void onGameOver(String name, int score);
}