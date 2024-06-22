// LoginUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;



    public LoginUI() {
        setTitle("Login Form");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame on screen
        setVisible(true);
        initComponents();
    }



    private void initComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel for picture
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        JLabel pictureLabel = new JLabel(new ImageIcon("pictures/login_image.jpg")); // Update with the path to your image
        leftPanel.add(pictureLabel, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(400, 600)); // Set preferred size
        leftPanel.setMinimumSize(new Dimension(400, 600)); // Set minimum size
        leftPanel.setMaximumSize(new Dimension(600, 600)); // Set maximum size

        // Right panel for login form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10); // Padding between components
        constraints.fill = GridBagConstraints.HORIZONTAL;

        // Username label and field
        JLabel usernameLabel = new JLabel("Username:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        rightPanel.add(usernameLabel, constraints);

        usernameField = new JTextField(15);
        constraints.gridx = 1;
        constraints.gridy = 0;
        rightPanel.add(usernameField, constraints);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        rightPanel.add(passwordLabel, constraints);

        passwordField = new JPasswordField(15);
        constraints.gridx = 1;
        constraints.gridy = 1;
        rightPanel.add(passwordField, constraints);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SAN_SERIF", Font.BOLD, 16)); // Larger font size
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        rightPanel.add(loginButton, constraints);

        // Registration button
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("SAN_SERIF", Font.BOLD, 16)); // Larger font size
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Registration regUI = new Registration();
                regUI.setVisible(true);
                // Close login window
                dispose();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        rightPanel.add(registerButton, constraints);

        // Add left and right panels to main panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Add main panel to frame
        getContentPane().add(mainPanel);
    }
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validate username and password
        if (authenticate(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            // Open chat application UI
            ChatApplicationUI chatUI = new ChatApplicationUI(username);
            chatUI.setVisible(true);
            // Close login window
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.");
            usernameField.setText("");
            passwordField.setText("");

        }
    }

    private boolean authenticate(String username, String password) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection != null) {
            try {
                String query = "SELECT * FROM user WHERE UserName = ? AND Password = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                return resultSet.next(); // Return true if user exists with provided username and password
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
        return false; // Return false if any exception occurs or user not found
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginUI();
        });
    }
}
