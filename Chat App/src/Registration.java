import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Registration extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public Registration() {
        setTitle("Registration Form");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame on screen
        initComponents();
    }

    private void initComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel for picture
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        JLabel pictureLabel = new JLabel(new ImageIcon("pictures/login_image.png")); // Update with the path to your image
        leftPanel.add(pictureLabel, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(400, 600)); // Set preferred size
        leftPanel.setMinimumSize(new Dimension(400, 600)); // Set minimum size
        leftPanel.setMaximumSize(new Dimension(600, 600)); // Set maximum size

        // Right panel for registration form
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

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("SAN_SERIF", Font.BOLD, 16)); // Larger font size
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        rightPanel.add(registerButton, constraints);

        // Add left and right panels to main panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);


        // Add main panel to frame
        getContentPane().add(mainPanel);
    }

    private void register() {
        // Your registration logic here
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save the user to the database
        // TODO: Add your database code here

        JOptionPane.showMessageDialog(this, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Registration().setVisible(true);
        });
    }
}
