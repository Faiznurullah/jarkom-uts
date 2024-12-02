import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Map<Socket, String> clientUsernames = new ConcurrentHashMap<>();
    private static Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)
            ) {
                this.out = writer;
                clientWriters.add(writer);

                // Kirim prompt ke client untuk memasukkan username
                writer.println("Enter your username:");
                String username = reader.readLine();
                clientUsernames.put(socket, username);

                System.out.println("User joined: " + username);
                broadcastMessage("Server", username + " has joined the chat!");

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("[" + username + "]: " + message);
                    broadcastMessage(username, message);
                }
            } catch (IOException e) {
                String username = clientUsernames.get(socket);
                System.out.println("User left: " + username);
                broadcastMessage("Server", username + " has left the chat.");
            } finally {
                if (out != null) {
                    clientWriters.remove(out);
                }
                clientUsernames.remove(socket);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String username, String message) {
            for (PrintWriter writer : clientWriters) {
                writer.println(username + ": " + message);
            }
        }
    }
}
