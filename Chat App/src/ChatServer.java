import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 6001;
    private static HashSet<PrintWriter> clientWriters = new HashSet<>();
    private static HashMap<String, PrintWriter> userWriters = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
//        } finally {
//            try {
//                if (serverSocket != null) {
//                    serverSocket.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private String userName;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                writer = new PrintWriter(output, true);

                // Request and validate user name
                while (true) {
                    writer.println("SUBMITNAME");
                    userName = reader.readLine();
                    if (userName == null || userName.isEmpty() || userWriters.containsKey(userName)) {
                        writer.println("INVALIDNAME");
                    } else {
                        synchronized (userWriters) {
                            userWriters.put(userName, writer);
                        }
                        break;
                    }
                }

                writer.println("NAMEACCEPTED " + userName);
                clientWriters.add(writer);

                // Notify all users about the new user
                for (PrintWriter w : clientWriters) {
                    w.println("USERJOINED " + userName);
                }

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    // Broadcast message to all users
                    for (PrintWriter w : clientWriters) {
                        w.println("MESSAGE " + userName + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    clientWriters.remove(writer);
                }
                if (userName != null) {
                    userWriters.remove(userName);
                    for (PrintWriter w : clientWriters) {
                        w.println("USERLEFT " + userName);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
