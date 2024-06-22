import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;

public class ChatApplicationUI extends JFrame {
    private String currentUser;

    private JList<User> userList;
    private DefaultListModel<User> userListModel;
    private JLabel chatPartnerLabel;
    private JPanel chatHistoryPanel;
    private JTextField messageField;
    private JButton sendButton;
    private User selectedUser;
    private Socket socket;
    private DataOutputStream out;

    public ChatApplicationUI(String currentUser) {
        this.currentUser = currentUser;

        setTitle("Chat Application - " + currentUser);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        fetchUserList(); // Fetch user list from database
        connectToServer();

        setLocationRelativeTo(null); // Center the frame on screen
    }

    private void initComponents() {
        // Left panel (User list)
        JPanel leftPanel = new JPanel(new BorderLayout());
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserCellRenderer());
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedUser = userList.getSelectedValue();
                if (selectedUser != null) {
                    chatPartnerLabel.setText("Chatting with: " + selectedUser.getName());
                    fetchChatHistory(currentUser, selectedUser.getName());
                }
            }
        });
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        // Right panel (Chat area)
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Top (Chat partner label)
        chatPartnerLabel = new JLabel("Chatting with: ");
        rightPanel.add(chatPartnerLabel, BorderLayout.NORTH);

        // Middle (Chat history)
        chatHistoryPanel = new JPanel();
        chatHistoryPanel.setLayout(new BoxLayout(chatHistoryPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JScrollPane(chatHistoryPanel), BorderLayout.CENTER);

        // Bottom (Message input field and send button)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        messageField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adjust chat bar size and border
        bottomPanel.add(messageField, BorderLayout.CENTER);
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Main panel (Left and Right panels with separator)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300); // Adjust divider location as needed
        splitPane.setDividerSize(10); // Divider thickness

        add(splitPane);
    }

    private void fetchUserList() {
        Connection connection = DatabaseConnection.getConnection();
        if (connection != null) {
            try {
                String query = "SELECT UserName FROM user WHERE UserName != ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, currentUser);
                ResultSet resultSet = preparedStatement.executeQuery();

                userListModel.clear();
                while (resultSet.next()) {
                    String userName = resultSet.getString("UserName");
                    userListModel.addElement(new User(userName));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fetchChatHistory(String user1, String user2) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection != null) {
            try {
                String query = "SELECT * FROM messages " +
                        "WHERE (Sender = ? AND Receiver = ?) OR (Sender = ? AND Receiver = ?) " +
                        "ORDER BY Timestamp ASC";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, user1);
                preparedStatement.setString(2, user2);
                preparedStatement.setString(3, user2);
                preparedStatement.setString(4, user1);
                ResultSet resultSet = preparedStatement.executeQuery();

                chatHistoryPanel.removeAll(); // Clear previous chat history

                while (resultSet.next()) {
                    String sender = resultSet.getString("Sender");
                    String message = resultSet.getString("Message");
                    LocalDateTime timestamp = resultSet.getTimestamp("Timestamp").toLocalDateTime();

                    JPanel messagePanel = new JPanel();
                    messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
                    messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                    JLabel senderLabel = new JLabel(sender + " [" + timestamp + "]:");
                    senderLabel.setFont(new Font("SAN_SERIF", Font.BOLD, 12));

                    JLabel out = new JLabel("<html><p style=\"width:150px\"> " + message + "</p></html>");
                    out.setFont(new Font("Tahoma", Font.PLAIN, 16));
                    out.setBackground(sender.equals(currentUser) ? Color.WHITE : new Color(84, 127, 98));
                    out.setOpaque(true);
                    out.setBorder(new EmptyBorder(15, 15, 15, 50));

                    messagePanel.add(senderLabel);
                    messagePanel.add(out);

                    chatHistoryPanel.add(messagePanel);
                }

                chatHistoryPanel.revalidate();
                chatHistoryPanel.repaint();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty() || selectedUser == null) {
            return;
        }

        Connection connection = DatabaseConnection.getConnection();
        if (connection != null) {
            try {
                String query = "INSERT INTO messages (Sender, Receiver, Message, Timestamp) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, currentUser);
                preparedStatement.setString(2, selectedUser.getName());
                preparedStatement.setString(3, message);
                preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.executeUpdate();

                try {
                    out.writeUTF("MESSAGE:" + currentUser + ":" + selectedUser.getName() + ":" + message);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Update chat history after sending message
                fetchChatHistory(currentUser, selectedUser.getName());

                // Clear message input field
                messageField.setText("");

            } catch (SQLException e) {
                e.printStackTrace();
//            } finally {
//                try {
//                    connection.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 6001);
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(new IncomingReader()).start();
            notifyServerOfStatus("online");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyServerOfStatus(String status) {
        try {
            out.writeUTF("STATUS_UPDATE:" + currentUser + ":" + status);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void dispose() {
        notifyServerOfStatus("offline");
        super.dispose();
    }

    private class IncomingReader implements Runnable {
        public void run() {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("STATUS_UPDATE:")) {
                        String[] parts = message.split(":");
                        String user = parts[1];
                        String status = parts[2];
                        updateUserStatus(user, status);
                    } else if (message.startsWith("GET_STATUSES_RESPONSE:")) {
                        String[] parts = message.split(":");
                        for (int i = 1; i < parts.length; i += 2) {
                            String user = parts[i];
                            String status = parts[i + 1];
                            updateUserStatus(user, status);
                        }
                    } else {
                        if (selectedUser != null) {
                            JPanel messagePanel = new JPanel();
                            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
                            messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                            JLabel senderLabel = new JLabel(selectedUser.getName() + " [Server]:");
                            senderLabel.setFont(new Font("SAN_SERIF", Font.BOLD, 12));

                            JLabel out = new JLabel("<html><p style=\"width:150px\"> " + message + "</p></html>");
                            out.setFont(new Font("Tahoma", Font.PLAIN, 16));
                            out.setBackground(new Color(84, 127, 98));
                            out.setOpaque(true);
                            out.setBorder(new EmptyBorder(15, 15, 15, 50));

                            messagePanel.add(senderLabel);
                            messagePanel.add(out);

                            chatHistoryPanel.add(messagePanel);

                            chatHistoryPanel.revalidate();
                            chatHistoryPanel.repaint();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserStatus(String user, String status) {
        for (int i = 0; i < userListModel.getSize(); i++) {
            User u = userListModel.getElementAt(i);
            if (u.getName().equals(user)) {
                u.setStatus(status);
                userList.repaint();
                break;
            }
        }
    }

    static class User {
        private String name;
        private String status;

        public User(String name) {
            this.name = name;
            this.status = "offline";
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static class UserCellRenderer extends JLabel implements ListCellRenderer<User> {
        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User user, int index, boolean isSelected, boolean cellHasFocus) {
            setText(user.getName());
            setIcon(new ImageIcon("pictures/user_icon.png"));
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setHorizontalTextPosition(RIGHT);

            if (user.getStatus().equals("online")) {
                setText(user.getName() + " (Online)");
            } else {
                setText(user.getName() + " (Offline)");
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    public static void main(String[] args) {
        // Assuming the user has already logged in and we have the username
        String currentUser = "testUser"; // Replace this with the actual logged-in username
        SwingUtilities.invokeLater(() -> new ChatApplicationUI(currentUser).setVisible(true));
    }
}
