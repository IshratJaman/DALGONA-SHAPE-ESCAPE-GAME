import javax.swing.*;

public class DalgonaGameMain{
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("<DALGONA SHAPE ESCAPE!>");

            ImageIcon icon = new ImageIcon("C:\\Users\\ishra\\IdeaProjects\\Dalgona Shape Escape SPL FINAL\\src\\pookie guard 3.jpg");
            frame.setIconImage(icon.getImage());

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);

            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
