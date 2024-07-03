package tvchannel;
import javax.swing.*;
import java.awt.*;

public class SkyStreamApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SkyStreamApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("SkyStream");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window
    
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        frame.add(panel);
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20); // Padding
        gbc.anchor = GridBagConstraints.CENTER;
    
        // Top center - "SkyStream" and "Welcome admin"
        JLabel titleLabel = new JLabel("SkyStream");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(titleLabel, gbc);
    
        JLabel welcomeLabel = new JLabel("Welcome admin");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        welcomeLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        panel.add(welcomeLabel, gbc);
    
        // Middle of the screen - Buttons arranged vertically
        JButton addChannelButton = new JButton("Add channel");
        styleButton(addChannelButton);
        addChannelButton.addActionListener(e -> {
            frame.dispose(); // Close current window
            new AddChannel().setVisible(true); // Open AddChannel window
        });
    
        JButton addDefaultPackButton = new JButton("Add default pack");
        styleButton(addDefaultPackButton);
        addDefaultPackButton.addActionListener(e -> {
            frame.dispose(); // Close current window
            new AddDefaultPack().setVisible(true); // Open AddDefaultPack window
        });
    
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton);
        logoutButton.addActionListener(e -> {
            frame.dispose(); // Close current window
            new Sub1().main(null); // Open sub1 window (assuming it's the login window)
        });
    
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 buttons, vertically spaced
        buttonPanel.add(addChannelButton);
        buttonPanel.add(addDefaultPackButton);
        buttonPanel.add(logoutButton);
    
        gbc.gridy = 2;
        panel.add(buttonPanel, gbc);
    
        frame.setVisible(true);
    }
    

    private static void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(0, 204, 51));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 50)); // Increase button size
    }
}
