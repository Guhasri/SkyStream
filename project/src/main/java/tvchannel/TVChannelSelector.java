package tvchannel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import com.mongodb.client.*;
import org.bson.Document;
import com.mongodb.client.model.Filters;

public class TVChannelSelector extends JFrame {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private static MongoCollection<Document> paymentCollection;
    private static JLabel totalPriceLabel;
    private static int totalPrice = 0;
    private static TreeMap<String, String> selectedChannels = new TreeMap<>();
    private static int deviceId;
    private static int customerId;

    public static void main(String[] args) {
        try {
            // Initialize MongoDB connection
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase("sample");
            collection = database.getCollection("sample1");
            paymentCollection = database.getCollection("payment");

            final JFrame frame = new JFrame("TV Channel Selector");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.BLACK); // Set background color to black

            // Create title bar with a close button
            JPanel titleBar = createTitleBar("TV Channel Selector", frame);

            JPanel channelPanel = new JPanel();
            channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
            channelPanel.setBackground(Color.BLACK); // Set background color to black

            // Organize channels by genre
            final TreeMap<String, Vector<JCheckBox>> genreMap = new TreeMap<>();

            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String genre = doc.getString("GENRE");
                    String channelName = doc.getString("CHANNEL NAME");

                    if (genre != null && channelName != null) {
                        JCheckBox checkBox = new JCheckBox(channelName);
                        checkBox.setForeground(Color.WHITE); // Set text color to white
                        checkBox.setBackground(Color.BLACK); // Set background color to black
                        checkBox.addItemListener(new ItemListener() {
                            @Override
                            public void itemStateChanged(ItemEvent event) {
                                updateTotalPrice(genreMap);
                                updateSelectedChannels(genreMap);
                            }
                        });

                        if (genreMap.containsKey(genre)) {
                            genreMap.get(genre).add(checkBox);
                        } else {
                            Vector<JCheckBox> checkboxes = new Vector<>();
                            checkboxes.add(checkBox);
                            genreMap.put(genre, checkboxes);
                        }
                    }
                }
            }

            for (Map.Entry<String, Vector<JCheckBox>> entry : genreMap.entrySet()) {
                String genre = entry.getKey();
                Vector<JCheckBox> checkboxes = entry.getValue();

                JLabel genreLabel = new JLabel(genre);
                genreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                genreLabel.setForeground(Color.WHITE); // Set text color to white
                channelPanel.add(genreLabel);

                for (JCheckBox checkBox : checkboxes) {
                    JPanel channelSubPanel = new JPanel(new GridLayout(1, 3)); // Grid layout for channel info
                    channelSubPanel.setBackground(Color.BLACK); // Set background color to black
                    checkBox.setForeground(Color.WHITE); // Set text color to white
                    channelSubPanel.add(checkBox); // Checkbox

                    String channelName = checkBox.getText();
                    int channelPrice = getPriceFromDatabase(channelName); // Get channel price
                    String channelLanguage = getLanguageFromDatabase(channelName); // Get channel language

                    JLabel priceLabel = new JLabel("Rs. " + channelPrice);
                    priceLabel.setForeground(Color.WHITE); // Set text color to white
                    JLabel languageLabel = new JLabel(channelLanguage);
                    languageLabel.setForeground(Color.WHITE); // Set text color to white

                    channelSubPanel.add(priceLabel); // Add price label
                    channelSubPanel.add(languageLabel); // Add language label

                    channelPanel.add(channelSubPanel); // Add to channel panel
                }

                // Add a line separator between genres
                channelPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            }

            // Bottom panel for total price and buttons
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.BLACK); // Set background color to black
            totalPriceLabel = new JLabel("Total Price: Rs. 0");
            totalPriceLabel.setForeground(Color.WHITE); // Set text color to white
            bottomPanel.add(totalPriceLabel);

            JButton backButton = new JButton("Back");
            backButton.setBackground(new Color(0, 204, 51)); // Set button color to RGB(0, 204, 51)
            backButton.setForeground(Color.WHITE); // Set text color to white
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    SubscriptionApp.main(new String[]{});
                    frame.dispose();
                }
            });
            

            JButton nextButton = new JButton("Next");
            nextButton.setBackground(new Color(0, 204, 51)); // Set button color to RGB(0, 204, 51)
            nextButton.setForeground(Color.WHITE); // Set text color to white
            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (totalPrice == 0) {
                        JOptionPane.showMessageDialog(frame, "Please select at least one channel before proceeding.");
                        return; // Stop further execution
                    }
                    collection = database.getCollection("subscription");
                    Sub1 sub1Instance = new Sub1(); // Assume Sub1 is defined elsewhere
                    customerId = sub1Instance.getCustId();
                    deviceId = sub1Instance.getDeviceId();
                    new TVChannelValidity(selectedChannels, totalPrice, customerId, deviceId, collection, paymentCollection);
                }
            });
            bottomPanel.add(backButton);
            bottomPanel.add(nextButton);


            mainPanel.add(new JScrollPane(channelPanel), BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH); // Place bottom panel
            mainPanel.add(titleBar, BorderLayout.NORTH); // Place title bar

            frame.add(mainPanel);
            frame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static JPanel createTitleBar(String title, JFrame frame) {
        final JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.GRAY);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        final JButton closeButton = new JButton("X");
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(Color.GRAY);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(Color.GRAY);
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                frame.dispose(); // Close the frame
            }
        });

        titleBar.add(closeButton, BorderLayout.LINE_END);

        return titleBar;
    }

    private static void updateTotalPrice(TreeMap<String, Vector<JCheckBox>> genreMap) {
        totalPrice = 0;
        for (Map.Entry<String, Vector<JCheckBox>> genreEntry : genreMap.entrySet()) {
            Vector<JCheckBox> checkboxes = genreEntry.getValue();
            for (JCheckBox checkBox : checkboxes) {
                if (checkBox.isSelected()) {
                    String channelName = checkBox.getText();
                    totalPrice += getPriceFromDatabase(channelName);
                }
            }
        }
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalPriceLabel.setText("Total Price: Rs. " + totalPrice);
        
    }

    private static int getPriceFromDatabase(String channelName) {
        Document doc = collection.find(Filters.eq("CHANNEL NAME", channelName)).first();
        if (doc != null) {
            try {
                Integer price = doc.getInteger("TARIFF");
                if (price != null) {
                    return price;
                }
            } catch (ClassCastException e) {
                // If it's not an integer, check if it's a String and handle FTA
                String tariff = doc.getString("TARIFF");
                if ("FTA".equalsIgnoreCase(tariff)) {
                    return 0; // Free-to-air
                }
            }
        }
        return 0; // Default value if no valid price found
    }

    private static void updateSelectedChannels(TreeMap<String, Vector<JCheckBox>> genreMap) {
        selectedChannels.clear();
        for (Map.Entry<String, Vector<JCheckBox>> genreEntry : genreMap.entrySet()) {
            Vector<JCheckBox> checkboxes = genreEntry.getValue();
            for (JCheckBox checkBox : checkboxes) {
                if (checkBox.isSelected()) {
                    String channelName = checkBox.getText();
                    Document channelDocument = collection.find(Filters.eq("CHANNEL NAME", channelName)).first();
                    if (channelDocument != null) {
                        selectedChannels.put(channelName, getTariffText(channelDocument));
                    }
                }
            }
        }
    }

    private static String getTariffText(Document doc) {
        if (doc != null && doc.containsKey("TARIFF")) {
            Object tariffValue = doc.get("TARIFF");
            if (tariffValue instanceof Integer) {
                return "Rs. " + tariffValue;
            } else if ((tariffValue instanceof String) && ("FTA".equalsIgnoreCase((String) tariffValue))) {
                return "Free";
            }
        }
        return "Not Available";
    }

    private static String getLanguageFromDatabase(String channelName) {
        Document doc = collection.find(Filters.eq("CHANNEL NAME", channelName)).first();
        if (doc != null) {
            String language = doc.getString("LANGUAGE");
            return language != null ? language : "Unknown";
        }
        return "Unknown";
    }
}
