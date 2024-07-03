package tvchannel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ChannelSelector extends JFrame {
    private static JFrame frame;
    private static JPanel panel;
    private static JTextArea textArea;
    private static JLabel totalPriceLabel;
    private static JButton payNowButton;
    private static JButton backButton; // New back button
    private static List<JCheckBox> checkBoxes = new ArrayList<>();
    private static MongoDatabase database;
    private static MongoCollection<Document> packCollection;
    private static MongoCollection<Document> channelCollection;
    private static MongoCollection<Document> subscriptionCollection;
    private static MongoCollection<Document> paymentCollection;
    

    private static int customerId;
    private static int deviceId;
    private static final String MONGODB_URI = "mongodb://localhost:27017";

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create(MONGODB_URI);
        database = mongoClient.getDatabase("sample");
        packCollection = database.getCollection("defaultPack");
        channelCollection = database.getCollection("sample1");
        subscriptionCollection = database.getCollection("subscription");
        paymentCollection = database.getCollection("payment");

        retrieveIds();

        frame = new JFrame("Channel Selector");
        panel = new JPanel(new GridBagLayout());
        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);
        textArea.setForeground(Color.WHITE); // Set text color to white
        textArea.setBackground(Color.BLACK); // Set background color to black
        totalPriceLabel = new JLabel("Total Price: Rs. 0");
        totalPriceLabel.setForeground(Color.WHITE); // Set text color to white
        payNowButton = new JButton("Next");
        payNowButton.setBackground(new Color(0, 204, 51)); 
        payNowButton.setForeground(Color.WHITE); // Set button color to RGB(0, 204, 51)
        backButton = new JButton("Back"); // Initialize back button
        backButton.setBackground(new Color(0, 204, 51)); 
        backButton.setForeground(Color.WHITE); 

        setupUI();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize the JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void retrieveIds() {
        // Retrieve Customer ID
        Sub1 sub1Instance = new Sub1(); // Assume Sub1 is defined elsewhere
        customerId = sub1Instance.getCustId();

        deviceId = sub1Instance.getDeviceId();
    }

    private static void setupUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
    
        try (MongoCursor<Document> cursor = packCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document pack = cursor.next();
                String packName = pack.getString("Default Pack");
                Integer packPrice = pack.getInteger("Tariff");
                Integer packValidity = pack.getInteger("validity");
    
                if (packName == null || packPrice == null || packValidity == null) {
                    continue;
                }
    
                JCheckBox checkBox = new JCheckBox(packName + " ( Rs. " + packPrice + ", Validity: " + packValidity + " month )");
                checkBoxes.add(checkBox);
    
                checkBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        updateTextArea();
                        updateTotalPrice();
                    }
                });
    
                panel.add(checkBox, gbc);
                gbc.gridy++;
            }
        } catch (Exception ex) {
            System.err.println("Error fetching default packs: " + ex.getMessage());
        }
    
        // Set background color of the panel containing checkboxes to black
        panel.setBackground(Color.BLACK);
    
        // Iterate through the components added using GridBagLayout
        for (Component comp : panel.getComponents()) {
            comp.setBackground(Color.BLACK); // Set background color to black
            comp.setForeground(Color.WHITE); // Set text color to white
        }
    
        JPanel textPanel = new JPanel(new BorderLayout());
        JScrollPane textScrollPane = new JScrollPane(textArea);
        textPanel.setBackground(Color.BLACK);
        textPanel.add(textScrollPane, BorderLayout.CENTER);
    
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pricePanel.add(totalPriceLabel);

        pricePanel.add(backButton); 
        pricePanel.add(payNowButton);
       

        pricePanel.setBackground(Color.BLACK);
    
        frame.getContentPane().add(panel, BorderLayout.WEST);
        frame.getContentPane().add(textPanel, BorderLayout.CENTER);
        frame.getContentPane().add(pricePanel, BorderLayout.SOUTH);
    
        payNowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openValidationDialog();
            }
        });
    
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // Handle back button functionality to go to SubscriptionApp
                SubscriptionApp.main(new String[]{});
                frame.dispose(); // Close the current frame
            }
        });
    }
    

    private static void updateTextArea() {
        textArea.setText("");
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                String packName = checkBox.getText().split(" \\(")[0];
                Document pack = packCollection.find(new Document("Default Pack", packName)).first();

                if (pack != null) {
                    List<Integer> channelIds = pack.getList("Channels", Integer.class);

                    if (channelIds != null) {
                        textArea.append("Channels in " + packName + ":\n");
                        for (Integer channelId : channelIds) {
                            Document channel = channelCollection.find(new Document("CHANNEL ID", channelId)).first();
                            if (channel != null) {
                                String channelName = channel.getString("CHANNEL NAME");
                                if (channelName != null) {
                                    textArea.append(" - " + channelName + "\n");
                                }
                            }
                        }
                        textArea.append("\n");
                    }
                }
            }
        }
    }

    private static void updateTotalPrice() {
        int totalPrice = 0;
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                String packName = checkBox.getText().split(" \\(")[0];
                Document pack = packCollection.find(new Document("Default Pack", packName)).first();
                if (pack != null) {
                    Integer packPrice = pack.getInteger("Tariff");
                    if (packPrice != null) {
                        totalPrice += packPrice;
                    }
                }
            }
        }
        totalPriceLabel.setText("Total Price: Rs. " + totalPrice);
    }

    private static void openValidationDialog() {
        boolean isSelected = false;
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                isSelected = true;
                break;
            }
        }
        if (!isSelected) {
            JOptionPane.showMessageDialog(frame, "Please select at least one pack.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        int totalPrice = 0;
        TreeMap<String, String> selectedChannels = new TreeMap<>();
        List<ObjectId> selectedPackIds = new ArrayList<>();
    
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                String packName = checkBox.getText().split(" \\(")[0];
                Document pack = packCollection.find(new Document("Default Pack", packName)).first();
                if (pack != null) {
                    Integer packPrice = pack.getInteger("Tariff");
                    if (packPrice != null) {
                        totalPrice += packPrice;
                        selectedChannels.put(packName, "Rs. " + packPrice);
                        selectedPackIds.add(pack.getObjectId("_id"));
                    }
                }
            }
        }
    
        new TVChannelValidity(
            selectedChannels,
            totalPrice,
            customerId,
            deviceId,
            subscriptionCollection,
            paymentCollection
        );
    }
}