package tvchannel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

import com.mongodb.client.*;
import com.mongodb.client.model.Sorts;

import org.bson.Document;
import org.passay.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class Sub1 extends JFrame {
    private static MongoClient mongoClient;
    private static MongoCollection<Document> collection;
    private static String loggedInPhoneNumber;
    private Random random = new Random();
    private static int deviceId;
    private static int devId;
    private static int custId;
    private JButton adminButton; // Declare adminButton as a class-level variable

    public static void main(String[] args) {
        Sub1 sub1 = new Sub1();
        sub1.showWelcomePage();
    }

    public Sub1() {
        setTitle("Welcome to TV Subscription System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void showWelcomePage() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            URL imageUrl = getClass().getResource("3170.jpg");

            ImageIcon backgroundImageIcon = new ImageIcon(imageUrl);
            Image originalImage = backgroundImageIcon.getImage();
            Image scaledImage = originalImage.getScaledInstance((int) screenSize.getWidth(),
                    (int) screenSize.getHeight(), Image.SCALE_SMOOTH);
            final ImageIcon scaledBackgroundImageIcon = new ImageIcon(scaledImage);

            final JPanel contentPane = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(scaledBackgroundImageIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            };
            setContentPane(contentPane);
            contentPane.setLayout(null);

            JLabel welcomeLabel = new JLabel("Welcome to TV Subscription System");
            welcomeLabel.setForeground(new Color(255, 255, 255, 220));
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            welcomeLabel.setBounds(0, screenSize.height - 200, screenSize.width, 100);
            contentPane.add(welcomeLabel);

            // Add User Button
            JButton userButton = new JButton("User");
            userButton.setFont(new Font("Arial", Font.PLAIN, 24));
            userButton.setBounds((screenSize.width - 200) / 2 - 150, screenSize.height - 100, 200, 60);
            userButton.setBackground(new Color(30, 144, 255));
            userButton.setForeground(Color.WHITE);
            userButton.setFocusPainted(false);
            userButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            userButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loginSignup();
                    contentPane.remove(userButton);
                    contentPane.remove(adminButton);
                }
            });
            contentPane.add(userButton);

            // Add Admin Button
            adminButton = new JButton("Admin");
            adminButton.setFont(new Font("Arial", Font.PLAIN, 24));
            adminButton.setBounds((screenSize.width - 200) / 2 + 150, screenSize.height - 100, 200, 60);
            adminButton.setBackground(new Color(30, 144, 255));
            adminButton.setForeground(Color.WHITE);
            adminButton.setFocusPainted(false);
            adminButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            adminButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Invoke Sub2
                    sub2 sub2 = new sub2();
                    sub2.setVisible(true);

                    // Close the current window
                    dispose();
                }
            });
            contentPane.add(adminButton);

            setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loginSignup() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("sample");
        collection = database.getCollection("UserRecords");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        final JPanel contentPane = new JPanel();
        setContentPane(contentPane);
        contentPane.setBackground(Color.BLACK);
        contentPane.setLayout(null);

        // Create and add the SkyStream heading at the very top
        JLabel headingLabel = new JLabel("SkyStream", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 48));
        headingLabel.setForeground(Color.WHITE);
        headingLabel.setBounds(0, 20, screenSize.width, 50);
        contentPane.add(headingLabel);

        // Main panel with login and signup columns
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 50, 0));
        mainPanel.setOpaque(true);
        mainPanel.setBackground(Color.BLACK);
        int mainPanelWidth = screenSize.width / 2 + 100; // increased by 100 (an inch) on each side
        int mainPanelHeight = screenSize.height / 2 + 100; // increased by 100 (an inch) on each side
        int mainPanelX = (screenSize.width - mainPanelWidth) / 2;
        int mainPanelY = (screenSize.height - mainPanelHeight) / 2 + 60;
        mainPanel.setBounds(mainPanelX, mainPanelY, mainPanelWidth, mainPanelHeight);
        contentPane.add(mainPanel);

        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setOpaque(false);
        mainPanel.add(loginPanel);

        JPanel signupPanel = new JPanel(new BorderLayout());
        signupPanel.setBackground(new Color(0, 0, 0, 100));
        mainPanel.add(signupPanel);

        TitledBorder loginBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2), "Login");
        loginBorder.setTitleColor(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                loginBorder,
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Login form panel
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setForeground(Color.WHITE);
        loginFormPanel.add(phoneLabel, gbc);

        gbc.gridy++;

        final JTextField phoneNumField = new JTextField();
        phoneNumField.setPreferredSize(new Dimension(200, 25));
        phoneNumField.setForeground(Color.BLACK);
        phoneNumField.setMargin(new Insets(5, 5, 5, 5));
        loginFormPanel.add(phoneNumField, gbc);

        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        loginFormPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        final JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 25));
        passwordField.setForeground(Color.BLACK);
        passwordField.setMargin(new Insets(5, 5, 5, 5));
        loginFormPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(0, 204, 51));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(80, 25));
        loginButton.setMargin(new Insets(10, 10, 10, 10));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String phoneNumber = phoneNumField.getText();
                String password = new String(passwordField.getPassword());

                if (phoneNumber.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in both phone number and password fields.",
                            "Empty Fields", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Retrieve user from the database
                Document user = collection.find(new Document("phoneNumber", phoneNumber)).first();
                if (user != null) {
                    devId = user.getInteger("deviceId");
                    custId = user.getInteger("customerId");

                    if (BCrypt.checkpw(password, user.getString("hashedPassword"))) {
                        dispose();
                        openChannelSelector();
                        loggedInPhoneNumber = phoneNumber;
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid phone number or password.", "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid phone number or password.", "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginFormPanel.add(loginButton, gbc);
        loginPanel.add(loginFormPanel, BorderLayout.WEST);

        TitledBorder signupBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2), "Sign Up/Register");
        signupBorder.setTitleColor(Color.WHITE);
        signupPanel.setBorder(signupBorder);

        // Signup form panel
        JPanel signupFormPanel = new JPanel(new GridBagLayout());
        signupFormPanel.setOpaque(false);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel2 = new JLabel("Name:");
        nameLabel2.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        signupFormPanel.add(nameLabel2, gbc);

        final JTextField nameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        signupFormPanel.add(nameField, gbc);

        JLabel phoneLabel2 = new JLabel("Phone Number:");
        phoneLabel2.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        signupFormPanel.add(phoneLabel2, gbc);

        final JTextField phoneField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        signupFormPanel.add(phoneField, gbc);

        JLabel passwordLabel2 = new JLabel("Password:");
        passwordLabel2.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        signupFormPanel.add(passwordLabel2, gbc);

        final JPasswordField passwordField2 = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        signupFormPanel.add(passwordField2, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        signupFormPanel.add(confirmPasswordLabel, gbc);

        final JPasswordField confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        signupFormPanel.add(confirmPasswordField, gbc);

        signupPanel.add(signupFormPanel, BorderLayout.CENTER);

        // Signup Button
        JButton signupButton = new JButton("SIGN UP");
        signupButton.setBackground(new Color(0, 204, 51));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 8; // Adjust the row index to move the button up
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button horizontally

        signupFormPanel.add(signupButton, gbc);
        signupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String phoneNumber = phoneField.getText();
                String password = new String(passwordField2.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                if (name.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Empty Fields",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match.", "Password Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if phone number is already registered
                Document existingUser = collection.find(new Document("phoneNumber", phoneNumber)).first();
                if (existingUser != null) {
                    JOptionPane.showMessageDialog(null, "Phone number is already registered.", "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Password validation using Passay library
                PasswordValidator validator = new PasswordValidator(Arrays.asList(
                        new LengthRule(8, 30), // Password must be between 8 and 30 characters
                        new CharacterRule(EnglishCharacterData.UpperCase, 1), // At least one upper-case character
                        new CharacterRule(EnglishCharacterData.LowerCase, 1), // At least one lower-case character
                        new CharacterRule(EnglishCharacterData.Digit, 1), // At least one digit
                        new CharacterRule(EnglishCharacterData.Special, 1), // At least one special character
                        new WhitespaceRule() // No whitespace allowed
                ));
                RuleResult result = validator.validate(new PasswordData(password));

                if (!result.isValid()) {
                    JOptionPane.showMessageDialog(null,
                            "Password must be 8-30 characters long, contain at least one upper-case letter, one lower-case letter, one digit, and one special character, and must not contain whitespace.",
                            "Password Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Hash the password
                deviceId = generateRandomDeviceId();
                int nextCustomerId = getNextCustomerId(collection);
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // Save user to the database
                Document newUser = new Document("name", name)
                        .append("phoneNumber", phoneNumber)
                        .append("hashedPassword", hashedPassword)
                        .append("customerId", nextCustomerId)
                        .append("deviceId", deviceId);

                collection.insertOne(newUser);

                JOptionPane.showMessageDialog(null, "Registration successful. Your device ID is: " + deviceId,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                phoneField.setText("");
                passwordField2.setText("");
                confirmPasswordField.setText("");

            }

            public int getNextCustomerId(MongoCollection<Document> collection) {
                Document maxIdDoc = collection.find()
                        .sort(Sorts.descending("customerId"))
                        .limit(1)
                        .first();

                if (maxIdDoc != null) {
                    return maxIdDoc.getInteger("customerId", 0) + 1;
                } else {
                    return 1;
                }
            }
        });

        signupPanel.add(signupFormPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void openChannelSelector() {
        new SubscriptionApp();
    }

    private int generateRandomDeviceId() {
        return random.nextInt(9999) + 1;
    }

    public static String getLoggedInPhoneNumber() {
        return loggedInPhoneNumber;
    }

    public int getDeviceId() {
        return devId;
    }

    public int getCustId() {
        return custId;
    }
}
