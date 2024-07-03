package tvchannel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

import com.mongodb.client.*;
import org.bson.Document;

public class sub2 extends JFrame {
    private static MongoClient mongoClient;
    private static MongoCollection<Document> collection;
    private static String loggedInAdminId;
    private static int devId;
    private static int custId;

    public static void main(String[] args) {
        new sub2();
    }

    public sub2() {
        setTitle("TV Subscription System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login();
    }

    private void login() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("sample");
        collection = database.getCollection("AdminRecords");

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

        // Main panel with login column
        JPanel mainPanel = new JPanel(new GridLayout(1, 1, 50, 0));
        mainPanel.setOpaque(true);
        mainPanel.setBackground(Color.BLACK);
        int mainPanelWidth = screenSize.width / 2;
        int mainPanelHeight = screenSize.height / 2;
        int mainPanelX = (screenSize.width - mainPanelWidth) / 2;
        int mainPanelY = (screenSize.height - mainPanelHeight) / 2 + 60;
        mainPanel.setBounds(mainPanelX, mainPanelY, mainPanelWidth, mainPanelHeight);
        contentPane.add(mainPanel);

        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setOpaque(false);
        mainPanel.add(loginPanel);

        TitledBorder loginBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2), "Login");
        loginBorder.setTitleColor(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                loginBorder,
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Login form panel
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel adminLabel = new JLabel("Admin ID:");
        adminLabel.setForeground(Color.WHITE);
        loginFormPanel.add(adminLabel, gbc);

        gbc.gridy++;

        final JTextField adminIdField = new JTextField();
        adminIdField.setPreferredSize(new Dimension(200, 25));
        adminIdField.setForeground(Color.BLACK);
        adminIdField.setMargin(new Insets(5, 5, 5, 5));
        loginFormPanel.add(adminIdField, gbc);

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
        String adminIdStr = adminIdField.getText();
        String password = new String(passwordField.getPassword());

        if (adminIdStr.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in both admin ID and password fields.",
                    "Empty Fields", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int adminId = Integer.parseInt(adminIdStr);

        // Retrieve admin from the database
        Document admin = collection.find(new Document("admin_id", adminId)).first();
        System.out.println("Retrieved Admin Document: " + admin);

        if (admin != null) {
            devId = admin.getInteger("deviceId", 0);  // Defaulting to 0 if not present
            custId = admin.getInteger("customerId", 0);  // Defaulting to 0 if not present

            String storedPassword = admin.getString("password");
            System.out.println("Entered Password: " + password);
            System.out.println("Stored Password: " + storedPassword);

            if (password.equals(storedPassword)) {
                JOptionPane.showMessageDialog(null, "Login successful.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                openChannelSelector(); // Call openChannelSelector method
                loggedInAdminId = adminIdStr;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid admin ID or password.", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid admin ID or password.", "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
});

loginFormPanel.add(loginButton, gbc);
loginPanel.add(loginFormPanel, BorderLayout.CENTER);

setVisible(true);
}

    private void openChannelSelector() {
            new SkyStreamApp().main(null);;
    }

    public static String getLoggedInAdminId() {
        return loggedInAdminId;
    }

    public int getDeviceId() {
        return devId;
    }

    public int getCustId() {
        return custId;
    }
}
