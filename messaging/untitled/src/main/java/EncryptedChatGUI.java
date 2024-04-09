import javax.swing.*;
import java.awt.*;

public class EncryptedChatGUI {
    private final JTextField inputField;
    private final JButton sendButton;
    private final JTextArea chatArea;

    public EncryptedChatGUI(String title, String name) {
        JFrame frame = new JFrame(name + " (" + title + ")");
        frame.setDefaultCloseOperation(3);
        frame.setSize(400, 600);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        this.chatArea = new JTextArea();
        this.chatArea.setEditable(false);
        this.chatArea.setBackground(new Color(240, 240, 240));
        this.chatArea.setFont(new Font("Arial", 0, 14));
        this.chatArea.setLineWrap(true);
        this.chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(this.chatArea);
        this.inputField = new JTextField();
        this.inputField.setFont(new Font("Arial", 0, 14));
        this.sendButton = new JButton("Send");
        this.sendButton.setBackground(new Color(59, 89, 152));
        this.sendButton.setForeground(Color.WHITE);
        this.sendButton.setFocusPainted(false);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.add(this.inputField, "Center");
        inputPanel.add(this.sendButton, "East");
        frame.add(chatScrollPane, "Center");
        frame.add(inputPanel, "South");
        frame.setVisible(true);
    }

    public JTextArea getChatArea() {
        return this.chatArea;
    }

    public JTextField getInputField() {
        return this.inputField;
    }

    public JButton getSendButton() {
        return this.sendButton;
    }
}
