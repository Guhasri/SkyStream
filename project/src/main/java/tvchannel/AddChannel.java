package tvchannel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

public class AddChannel extends JFrame {
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public AddChannel() {
        setTitle("Add Channel");
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
                BorderFactory.createLineBorder(Color.WHITE, 2), "Add Channel");
        border.setTitleColor(Color.WHITE);
        border.setTitleFont(new Font("Arial", Font.BOLD, 16));
        formPanel.setBorder(border);

        JButton backButton = new JButton("Back");
        styleButton(backButton);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the current frame
                SkyStreamApp.main(new String[0]); // Open the SkyStreamApp
            }
        });

        // Positioning the back button at the bottom right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel genreLabel = new JLabel("GENRE:");
        styleLabel(genreLabel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(genreLabel, gbc);

        JTextField genreField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(genreField, gbc);

        JLabel languageLabel = new JLabel("LANGUAGE:");
        styleLabel(languageLabel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(languageLabel, gbc);

        JTextField languageField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(languageField, gbc);

        JLabel channelNameLabel = new JLabel("CHANNEL NAME:");
        styleLabel(channelNameLabel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(channelNameLabel, gbc);

        JTextField channelNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(channelNameField, gbc);

        JLabel tariffLabel = new JLabel("TARIFF:");
        styleLabel(tariffLabel);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(tariffLabel, gbc);

        JTextField tariffField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(tariffField, gbc);

        JButton addButton = new JButton("Add Channel");
        styleButton(addButton);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String genre = genreField.getText().toUpperCase();
                String language = languageField.getText().toUpperCase();
                String channelName = channelNameField.getText().toUpperCase();
                int tariff;

                try {
                    tariff = Integer.parseInt(tariffField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Tariff must be an integer.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if the channel name already exists
                Bson nameFilter = Filters.eq("CHANNEL NAME", channelName);
                if (collection.find(nameFilter).first() != null) {
                    JOptionPane.showMessageDialog(null, "Channel name already exists.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Generate unique channel ID
                int channelId = generateUniqueChannelId();

                Document channel = new Document("GENRE", genre)
                        .append("LANGUAGE", language)
                        .append("CHANNEL NAME", channelName)
                        .append("CHANNEL ID", channelId)
                        .append("TARIFF", tariff);

                collection.insertOne(channel);

                JOptionPane.showMessageDialog(null, "Channel added successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                genreField.setText("");
                languageField.setText("");
                channelNameField.setText("");
                tariffField.setText("");
            }
        });

        // Center form panel in the middle
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.BLACK);
        centerPanel.add(formPanel);
        contentPane.add(centerPanel, BorderLayout.CENTER);

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

    private int generateUniqueChannelId() {
        Document maxIdDoc = collection.find()
                .sort(Sorts.descending("CHANNEL ID"))
                .limit(1)
                .first();
    
        if (maxIdDoc != null) {
            return maxIdDoc.getInteger("CHANNEL ID", 0) + 1;
        } else {
            return 1;
        }
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AddChannel());
    }
}
