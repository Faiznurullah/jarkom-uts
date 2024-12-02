import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JTextArea messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        JTextField inputField = new JTextField();

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);

        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Prompt user for username
            String username = JOptionPane.showInputDialog(frame, "Enter your username:");
            out.println(username); // Send username to server

            Thread listenerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        messageArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    System.out.println("Connection lost.");
                }
            });
            listenerThread.start();

            inputField.addActionListener(e -> {
                String text = inputField.getText();
                out.println(text);
                inputField.setText("");
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Unable to connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        frame.setVisible(true);
    }
}
