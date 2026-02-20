package src.view;

import src.model.GameBoard;
import src.model.PlayerRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedList;

public class TetrisLegacy extends JFrame {

    public TetrisLegacy() {
        setResizable(false);
        showMainMenu();
    }

    private void showMainMenu() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(30, 30, 35));
        menuPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        menuPanel.setLayout(new GridLayout(4, 1, 15, 15));

        JLabel title = new JLabel("TETRIS LEGACY", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 32));
        title.setForeground(Color.CYAN);

        JButton startBtn = createStyledButton("START GAME", new Color(46, 204, 113));
        JButton searchBtn = createStyledButton("SEARCH RECORDS", new Color(52, 152, 219));
        JButton exitBtn = createStyledButton("EXIT", new Color(231, 76, 60));

        startBtn.addActionListener(e -> showStartMenu());
        searchBtn.addActionListener(e -> showSearchScreen());
        exitBtn.addActionListener(e -> System.exit(0));

        menuPanel.add(title);
        menuPanel.add(startBtn);
        menuPanel.add(searchBtn);
        menuPanel.add(exitBtn);

        add(menuPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Round Button and darkens slightly if hovered.
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // text centered
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg.darker());
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // prevent default Swing background
        btn.setBorderPainted(false); // we draw our own border above
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showStartMenu() {
        UIManager.put("OptionPane.background", new Color(45, 45, 45));
        UIManager.put("Panel.background", new Color(45, 45, 45));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        String name = JOptionPane.showInputDialog(this, "Enter your name:", "Player Entry", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty())
            name = "Anonymous";

        getContentPane().removeAll();
        TetrisGame gamePanel = new TetrisGame(this, name);
        add(gamePanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
        gamePanel.startGame();
    }

    public void gameOver(String name, int score) {
        GameBoard.database.add(new PlayerRecord(name, score));
        int choice = JOptionPane.showConfirmDialog(this, "GAME OVER, " + name + "!\nScore: " + score + "\nTry again?",
                "Game Over", JOptionPane.YES_NO_OPTION);
        GameBoard.database.add(new PlayerRecord(name, score));
        if (choice == JOptionPane.YES_OPTION) {
            showStartMenu();
        } else {
            showMainMenu();
        }
    }

    private void showSearchScreen() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(40, 40, 45));
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField(12);
        JButton searchBtn = createStyledButton("Search", Color.DARK_GRAY);
        JButton viewAllBtn = createStyledButton("VIEW ALL RECORDS", new Color(52, 152, 219));

        JLabel label = new JLabel("Enter Name: ");
        label.setForeground(Color.WHITE);
        searchPanel.add(label);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(viewAllBtn);

        JTextArea resultArea = new JTextArea(12, 35);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(20, 20, 20));
        resultArea.setForeground(new Color(0, 255, 150));
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            LinkedList<PlayerRecord> sorted = GameBoard.getSortedRecords();
            StringBuilder results = new StringBuilder("--- SEARCH RESULTS ---\n\n");
            if (query.isEmpty()) {
                int rank = 1;
                for (PlayerRecord p : sorted) {
                    results.append(String.format("Rank %d: %-15s | Score: %d%n", rank++, p.name, p.score));
                }
                if (sorted.isEmpty())
                    results.append("No records yet.");
            } else {
                int rank = 1;
                boolean found = false;
                for (PlayerRecord p : sorted) {
                    if (p.name.equalsIgnoreCase(query)) {
                        results.append(String.format("Rank %d: %-15s | Score: %d%n", rank, p.name, p.score));
                        found = true;
                    }
                    rank++;
                }
                if (!found)
                    results.append("No record found for: ").append(query);
            }
            resultArea.setText(results.toString());
        });

        viewAllBtn.addActionListener(e -> {
            LinkedList<PlayerRecord> sorted = GameBoard.getSortedRecords();
            StringBuilder results = new StringBuilder("--- ALL RECORDS ---\n\n");
            int rank = 1;
            for (PlayerRecord p : sorted) {
                results.append(String.format("Rank %d: %-15s | Score: %d%n", rank++, p.name, p.score));
            }
            if (sorted.isEmpty())
                results.append("No records yet.");
            resultArea.setText(results.toString());
        });

        JButton backBtn = createStyledButton("BACK TO MENU", new Color(100, 100, 100));
        backBtn.addActionListener(e -> showMainMenu());

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(backBtn, BorderLayout.SOUTH);

        revalidate();
        repaint();
        pack();
    }

}