package src;

import javax.swing.SwingUtilities;

import src.view.TetrisLegacy;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TetrisLegacy frame = new TetrisLegacy();
            frame.setTitle("Tetris Legacy Pro");
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}