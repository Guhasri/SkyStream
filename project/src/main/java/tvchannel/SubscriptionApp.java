package tvchannel;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SubscriptionApp extends JFrame {
    private int userId;
    private String userName;
    private int deviceId;
    private static int customerId;
    private List<Document> subscriptions;

    public SubscriptionApp() {
        setTitle("Subscription App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full-screen mode

        // Fetch user details from MongoDB
        fetchUserDetails();

        // Get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        // Calculate button size based on screen size
        int buttonWidth = (int) (screenWidth * 0.25);
        int buttonHeight = (int) (screenHeight * 0.1);

        // Create the main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        // Segment 1: User Details (Center)
        JPanel userDetailsPanel = new JPanel(new GridLayout(3, 1, 0, 0)); // Vertical arrangement without spacing
        userDetailsPanel.setBackground(Color.BLACK);
        userDetailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel nameLabel = new JLabel("Name: " + userName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        nameLabel.setForeground(Color.WHITE);
        userDetailsPanel.add(nameLabel);

        JLabel idLabel = new JLabel("User ID: " + userId, SwingConstants.CENTER);
        idLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        idLabel.setForeground(Color.WHITE);
        userDetailsPanel.add(idLabel);

        JLabel deviceIdLabel = new JLabel("Device ID: " + deviceId, SwingConstants.CENTER);
        deviceIdLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        deviceIdLabel.setForeground(Color.WHITE);
        userDetailsPanel.add(deviceIdLabel);

        mainPanel.add(userDetailsPanel, BorderLayout.NORTH);

        // Segment 2: Existing Packs and Channels (Center)
        JPanel packsAndChannelsPanel = new JPanel(new BorderLayout());
        packsAndChannelsPanel.setBackground(Color.BLACK);
        packsAndChannelsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Create a scroll pane for packs and channels
        JScrollPane scrollPane = new JScrollPane(createPacksAndChannelsPanel(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Only vertical scrollbar
        scrollPane.setPreferredSize(new Dimension(400, 200)); // Set preferred size for scroll pane
        packsAndChannelsPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(packsAndChannelsPanel, BorderLayout.CENTER); // Add packsAndChannelsPanel to the main panel

        // Segment 3: Buttons (Right)
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 20)); // Add one more row for renew button
        buttonsPanel.setBackground(Color.BLACK);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton customPackButton = new JButton("Custom Pack");
        customPackButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        customPackButton.setBackground(new Color(0, 204, 51));
        customPackButton.setForeground(Color.WHITE);
        buttonsPanel.add(customPackButton);

        JButton defaultPackButton = new JButton("Default Pack");
        defaultPackButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        defaultPackButton.setBackground(new Color(0, 204, 51));
        defaultPackButton.setForeground(Color.WHITE);
        buttonsPanel.add(defaultPackButton);

        JButton openMainScreenButton = new JButton("Logout"); // Add back button
        openMainScreenButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        openMainScreenButton.setBackground(new Color(0, 204, 51));
        openMainScreenButton.setForeground(Color.WHITE);
        buttonsPanel.add(openMainScreenButton); // Add back button to the panel

        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        add(mainPanel);

        // Action listeners
        customPackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openChannelSelector();
            }
        });

        defaultPackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openChannelSelector2();
            }
        });

        openMainScreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMainScreen();
            }
        });

        setVisible(true);
    }

    // Method to create the panel containing the packs and channels list
    private JPanel createPacksAndChannelsPanel() {
        JPanel packsAndChannelsPanel = new JPanel();
        packsAndChannelsPanel.setLayout(new BoxLayout(packsAndChannelsPanel, BoxLayout.Y_AXIS)); // Vertical arrangement
        packsAndChannelsPanel.setBackground(Color.BLACK);

        for (Document subscription : subscriptions) {
            JPanel packPanel = new JPanel(new BorderLayout()); // Panel for each subscription
            packPanel.setBackground(Color.DARK_GRAY);
            packPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Segment each subscription with a border

            // Pack and price label
            JLabel packLabel = new JLabel("  - Packs: " + String.join(", ", subscription.getList("packs", String.class)), SwingConstants.LEFT);
            packLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            packLabel.setForeground(Color.WHITE);
            packPanel.add(packLabel, BorderLayout.NORTH);

            // Subscription details panel
            JPanel subscriptionDetailsPanel = new JPanel(new GridLayout(4, 1, 0, 0)); // No spacing between lines
            subscriptionDetailsPanel.setBackground(Color.DARK_GRAY);

            JLabel startDateLabel = new JLabel("Start Date: " + subscription.getString("start_date"), SwingConstants.LEFT);
            startDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            startDateLabel.setForeground(Color.WHITE);
            subscriptionDetailsPanel.add(startDateLabel);

            JLabel endDateLabel = new JLabel("End Date: " + subscription.getString("end_date"), SwingConstants.LEFT);
            endDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            endDateLabel.setForeground(Color.WHITE);
            subscriptionDetailsPanel.add(endDateLabel);

            JLabel validityLabel = new JLabel("Validity (months): " + subscription.getInteger("validity_months"), SwingConstants.LEFT);
            validityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            validityLabel.setForeground(Color.WHITE);
            subscriptionDetailsPanel.add(validityLabel);

            JLabel totalPriceLabel = new JLabel("Total Price : Rs.  " + subscription.getInteger("total_price"), SwingConstants.LEFT);
            totalPriceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            totalPriceLabel.setForeground(Color.WHITE);
            subscriptionDetailsPanel.add(totalPriceLabel);

            // Status label
            JLabel statusLabel = new JLabel();
            statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
            subscriptionDetailsPanel.add(statusLabel);

            // Set status based on end date
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date currentDate = new Date();
                Date endDate = dateFormat.parse(subscription.getString("end_date"));

                if (currentDate.before(endDate) || currentDate.equals(endDate)) {
                    statusLabel.setText("Status: Active");
                    statusLabel.setForeground(new Color(0, 204, 51)); // Green color for active status
                } else {
                    statusLabel.setText("Status: Expired");
                    statusLabel.setForeground(Color.RED); // Red color for expired status
                }
            } catch (ParseException ex) {
                ex.printStackTrace(); // Handle parsing exception
            }

            packPanel.add(subscriptionDetailsPanel, BorderLayout.CENTER);

            // Renew button panel
            JPanel renewButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            renewButtonPanel.setBackground(Color.DARK_GRAY);

            JButton renewPackButton = new JButton("Renew");
            renewPackButton.setBackground(new Color(0, 204, 51));
            renewPackButton.setForeground(Color.WHITE);

            // Disable renew button if subscription is active
            renewPackButton.setEnabled(!statusLabel.getText().contains("Active"));

            // Action listener for renew button
            renewPackButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    renewSubscription(subscription);
                }
            });

            renewButtonPanel.add(renewPackButton);

            packPanel.add(renewButtonPanel, BorderLayout.SOUTH);

            packsAndChannelsPanel.add(packPanel);
            packsAndChannelsPanel.add(Box.createVerticalStrut(10)); // Add space between subscriptions
        }
        return packsAndChannelsPanel;
    }

    // Method to renew subscription
    private void renewSubscription(Document subscription) {
        try {
            // Update start date to current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date currentDate = new Date();
            String newStartDate = dateFormat.format(currentDate);
            subscription.put("start_date", newStartDate); // Update start date in the document

            // Calculate new end date based on validity months
            int validityMonths = subscription.getInteger("validity_months");
            Date startDate = dateFormat.parse(newStartDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, validityMonths);
            Date newEndDate = calendar.getTime();
            String newEndDateStr = dateFormat.format(newEndDate);
            subscription.put("end_date", newEndDateStr); // Update end date in the document

            // Update document in the database
       

            // Refresh the display
          

            // Show success message
            JOptionPane.showMessageDialog(null, "Redirecting to payment options...", "Renewal Status", JOptionPane.INFORMATION_MESSAGE);

            // Navigate to payment options
            int subid=subscription.getInteger("subscriptionId");
            new PaymentProcess1(customerId,subid) ;
            updateSubscriptionInDatabase(subscription);
            refreshDisplay();

        } catch (ParseException ex) {
            ex.printStackTrace(); // Handle parsing exception
        }
    }

    // Method to update subscription in the database
    private void updateSubscriptionInDatabase(Document subscription) {
        try {
            // Connect to MongoDB server
            String connectionString = "mongodb://localhost:27017";
            MongoDatabase database = MongoClients.create(connectionString).getDatabase("sample");

            // Access the subscription collection
            MongoCollection<Document> collection = database.getCollection("subscription");

            // Update the document in the collection
            collection.replaceOne(new Document("_id", subscription.getObjectId("_id")), subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to refresh the display
    private void refreshDisplay() {
        dispose(); // Dispose of the current frame
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SubscriptionApp(); // Create a new instance of SubscriptionApp
            }
        });
    }

    // Method to fetch user details
    private void fetchUserDetails() {
        // Connect to MongoDB and fetch user details
        try {
            // Connect to MongoDB server
            String connectionString = "mongodb://localhost:27017";
            MongoDatabase database = MongoClients.create(connectionString).getDatabase("sample");

            // Access the userRecords collection
            MongoCollection<Document> collection = database.getCollection("UserRecords");

            // Query to find the user details
            Sub1 sub1Instance = new Sub1();
            customerId = sub1Instance.getCustId();
            Document query = new Document("customerId", customerId);

            // Execute the query and get the result
            Document result = collection.find(query).first();
            subscriptions = new ArrayList<>();
            MongoCollection<Document> subsCollection = database.getCollection("subscription");
            for (Document doc : subsCollection.find(query)) {
                subscriptions.add(doc);
            }

            // Extract user details
            if (result != null) {
                userId = result.getInteger("customerId");
                userName = result.getString("name");
                deviceId = result.getInteger("deviceId");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to open channel selector
    private void openChannelSelector() {
        // Instantiate and show TVChannelSelector
        TVChannelSelector tvChannelSelector = new TVChannelSelector();
        tvChannelSelector.main(new String[0]);
        dispose();
    }

    // Method to open channel selector 2
    private void openChannelSelector2() {
        ChannelSelector chnlSelector = new ChannelSelector();
        chnlSelector.main(new String[0]);
        dispose();
    }

    private void openMainScreen() {
        Sub1.main(new String[0]);
        dispose();
    }
    
    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SubscriptionApp(); // Start the app
            }
        });
    }
}
