package tvchannel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Sorts;
import java.util.*;

public class AddDefaultPack extends JFrame {
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public AddDefaultPack() {
        setTitle("Add Default Pack");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Connect to MongoDB
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("sample");
        collection = database.getCollection("sample1");

        // Create the content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.BLACK);
        setContentPane(contentPane);

        // SkyStream heading
        JLabel headingLabel = new JLabel("SkyStream", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 48));
        headingLabel.setForeground(Color.WHITE);
        contentPane.add(headingLabel, BorderLayout.NORTH);

        // Form panel with border
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.BLACK);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2), "Add Default Pack");
        border.setTitleColor(Color.WHITE);
        border.setTitleFont(new Font("Arial", Font.BOLD, 16));
        formPanel.setBorder(border);
        JButton backButton = new JButton("Back");
        styleButton(backButton);
        backButton.addActionListener(e -> {
            dispose(); // Close the current window
            new SkyStreamApp().main(null); // Open the SkyStreamApp window
        });

        // Add back button to content pane
        GridBagConstraints gbcBackButton = new GridBagConstraints();
        gbcBackButton.gridx = 0;
        gbcBackButton.gridy = 4; // Adjust the row based on your layout
        gbcBackButton.anchor = GridBagConstraints.WEST;
        formPanel.add(backButton, gbcBackButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel defaultPackLabel = new JLabel("Default Pack Name:");
        styleLabel(defaultPackLabel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(defaultPackLabel, gbc);

        JTextField defaultPackField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(defaultPackField, gbc);

        JLabel tariffLabel = new JLabel("TARIFF:");
        styleLabel(tariffLabel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(tariffLabel, gbc);

        JTextField tariffField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(tariffField, gbc);

        JLabel validityLabel = new JLabel("Validity (in months):");
        styleLabel(validityLabel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(validityLabel, gbc);

        JTextField validityField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(validityField, gbc);

        JButton addButton = new JButton("Add Default Pack");
        styleButton(addButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addButton, gbc);

        contentPane.add(formPanel, BorderLayout.WEST);

        // Display available channels
        JPanel channelPanel = new JPanel();
        channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
        channelPanel.setBackground(Color.BLACK); // Set background color to black
        JLabel selectChannelsLabel = new JLabel("Select Channels");
        selectChannelsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        selectChannelsLabel.setForeground(Color.WHITE);
        selectChannelsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        channelPanel.add(selectChannelsLabel);

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String channelName = doc.getString("CHANNEL NAME");
                String genre = doc.getString("GENRE");
                String language = doc.getString("LANGUAGE");
                int channelId = doc.getInteger("CHANNEL ID"); // Retrieve the channel ID
                if (channelName != null) {
                    JCheckBox checkBox = new JCheckBox(channelName);
                    checkBox.setForeground(Color.WHITE); // Set text color to white
                    checkBox.setBackground(Color.BLACK); // Set background color to black
                    checkBox.setFont(new Font("Arial", Font.BOLD, 14));
                    checkBox.setToolTipText(genre + language);

                    // Create labels for displaying language and genre
                    JLabel genreLabel = new JLabel(genre);
                    JLabel languageLabel = new JLabel(language);
                    JLabel idLabel = new JLabel("ID: " + channelId); // Label to display channel ID
                    genreLabel.setForeground(Color.WHITE);
                    languageLabel.setForeground(Color.WHITE);

                    // Create a panel to hold the checkbox, genre label, language label, and ID label
                    JPanel channelInfoPanel = new JPanel(new GridLayout(1, 4));
                    channelInfoPanel.setBackground(Color.BLACK);
                    channelInfoPanel.add(checkBox);
                    channelInfoPanel.add(genreLabel);
                    channelInfoPanel.add(languageLabel);
                    channelInfoPanel.add(idLabel); // Add ID label
                    channelPanel.add(channelInfoPanel); // Add panel to channel panel
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(channelPanel);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String defaultPackName = defaultPackField.getText().toUpperCase();
                int tariff;
                int validity;

                try {
                    tariff = Integer.parseInt(tariffField.getText());
                    validity = Integer.parseInt(validityField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Tariff and Validity must be integers.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if default pack name already exists (case sensitive)
                Document existingPack = collection.find(new Document("Default Pack", defaultPackName)).first();
                if (existingPack != null) {
                    JOptionPane.showMessageDialog(null, "Default Pack with this name already exists.",
                            "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Generate unique DPack ID
                int dPackId = generateUniqueDPackId();

                // Calculate NOC (Number of Channels)
                int noc = 0;
                ArrayList<Integer> selectedChannelIds = new ArrayList<>();
                Component[] components = channelPanel.getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {
                        JPanel channelInfoPanel = (JPanel) component;
                        JCheckBox checkBox = (JCheckBox) channelInfoPanel.getComponent(0);
                        if (checkBox.isSelected()) {
                            noc++;
                            JLabel idLabel = (JLabel) channelInfoPanel.getComponent(3);
                            int channelId = Integer.parseInt(idLabel.getText().substring(4)); // Extract channel ID from label text
                            selectedChannelIds.add(channelId);
                        }
                    }
                }

                // Check if at least one channel is selected
                if (noc == 0) {
                    JOptionPane.showMessageDialog(null, "Please select at least one channel.",
                            "Selection Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Store the Default Pack information in the database
                Document defaultPack = new Document()
                        .append("Default Pack", defaultPackName)
                        .append("DPack Id", dPackId)
                        .append("NOC", noc)
                        .append("Tariff", tariff)
                        .append("validity", validity)
                        .append("Channels", selectedChannelIds);

                collection = database.getCollection("defaultPack");
                collection.insertOne(defaultPack);

                JOptionPane.showMessageDialog(null, "Default Pack added successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                defaultPackField.setText("");
                tariffField.setText("");
                validityField.setText("");
            }
        });

        setVisible(true);
    }

    private void styleLabel(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(0, 204, 51));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private int generateUniqueDPackId() {
        // Find the maximum DPack Id from the database
        Document maxIdDoc = collection.find()
                .sort(Sorts.descending("DPack Id")).limit(1)
                .first();
        
        // If there are existing DPack Ids, increment the maximum by 1
        if (maxIdDoc != null) {
            int maxId = maxIdDoc.getInteger("DPack Id", 0);
            return maxId + 1;
        } else {
            // If there are no existing DPack Ids, start from 1
            return 1;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AddDefaultPack());
    }
}
