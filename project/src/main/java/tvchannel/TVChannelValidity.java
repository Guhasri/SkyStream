package tvchannel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.util.regex.Pattern;

public class TVChannelValidity {
    private final MongoCollection<Document> subscriptionCollection;
    private final MongoCollection<Document> paymentCollection;
    private final TreeMap<String, String> selectedChannels;
    private final int totalPrice;
    private final int customerId;
    private final int deviceId;
    private JRadioButton oneMonthRadio;
    private JRadioButton threeMonthsRadio;
    private JRadioButton twelveMonthsRadio;
    private JLabel finalPriceLabel;

    public TVChannelValidity(
        TreeMap<String, String> selectedChannels,
        int totalPrice,
        int customerId,
        int deviceId,
        MongoCollection<Document> subscriptionCollection,
        MongoCollection<Document> paymentCollection
    ) {
        this.subscriptionCollection = subscriptionCollection;
        this.paymentCollection = paymentCollection;
        this.selectedChannels = selectedChannels;
        this.totalPrice = totalPrice;
        this.customerId = customerId;
        this.deviceId = deviceId;

        setupUI();
    }

    private void setupUI() {
        final JFrame frame = new JFrame("TV Channel Validity");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Title bar with a close button
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("X");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                frame.dispose();
            }
        });

        titleBar.add(closeButton);
        mainPanel.add(titleBar, BorderLayout.NORTH);

        // Panel for validity options
        JPanel radioPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcRadio = new GridBagConstraints();
        gbcRadio.insets = new Insets(10, 10, 10, 10);
        gbcRadio.gridx = 0;
        gbcRadio.gridy = 0;

        // Add label above the radio buttons
        JLabel validityLabel = new JLabel("Validity Options");
        validityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        radioPanel.add(validityLabel, gbcRadio);

        // Add radio buttons below the label
        oneMonthRadio = new JRadioButton("1 Month");
        threeMonthsRadio = new JRadioButton("3 Months");
        twelveMonthsRadio = new JRadioButton("12 Months");

        ButtonGroup validityGroup = new ButtonGroup();
        validityGroup.add(oneMonthRadio);
        validityGroup.add(threeMonthsRadio);
        validityGroup.add(twelveMonthsRadio);

        gbcRadio.gridy = 1;
        radioPanel.add(oneMonthRadio, gbcRadio);
        gbcRadio.gridy = 2;
        radioPanel.add(threeMonthsRadio, gbcRadio);
        gbcRadio.gridy = 3;
        radioPanel.add(twelveMonthsRadio, gbcRadio);

        mainPanel.add(radioPanel, BorderLayout.NORTH);

        // Table to display selected channels
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Sno", "Pack", "Price"}, 0);
        int sno = 1;

        for (Map.Entry<String, String> entry : selectedChannels.entrySet()) {
            tableModel.addRow(new Object[]{sno++, entry.getKey(), entry.getValue()});
        }

        JTable table = new JTable(tableModel);

        // Set preferred size for table
        table.setPreferredScrollableViewportSize(new Dimension(700, 200));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD)); // Set headers to bold
        
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER); // Center align headers
        
        JScrollPane scrollPane = new JScrollPane(table);

        // Center the table within a panel
        JPanel tablePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTable = new GridBagConstraints();
        gbcTable.gridx = 0;
        gbcTable.gridy = 0;
        gbcTable.insets = new Insets(10, 10, 10, 10);
        tablePanel.add(scrollPane, gbcTable);
        mainPanel.add(tablePanel, BorderLayout.CENTER); 

        // Final price label
        finalPriceLabel = new JLabel("Final Price: Rs. -");
        finalPriceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        finalPriceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(finalPriceLabel, BorderLayout.CENTER);

        // Panel to hold the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Button to initiate the payment process
        JButton payNowButton = new JButton("Pay Now");
        payNowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int finalPrice = calculateFinalPrice();
                int validityMonths = getValidityMonths();

                showPaymentMethodsDialog(frame, finalPrice, validityMonths);
            }
        });
        buttonPanel.add(payNowButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        oneMonthRadio.setSelected(true);
        updateFinalPriceLabel();

        ActionListener validityUpdateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateFinalPriceLabel();
            }
        };

        oneMonthRadio.addActionListener(validityUpdateListener);
        threeMonthsRadio.addActionListener(validityUpdateListener);
        twelveMonthsRadio.addActionListener(validityUpdateListener);
    }

    private void updateFinalPriceLabel() {
        int calculatedPrice = calculateFinalPrice();

        String validityText = oneMonthRadio.isSelected() ? "1 Month" :
                (threeMonthsRadio.isSelected() ? "3 Months" :
                        (twelveMonthsRadio.isSelected() ? "12 Months" : ""));

        finalPriceLabel.setText("Final Price: Rs. " + calculatedPrice + " (" + validityText + ")");
    }

    private int calculateFinalPrice() {
        int basePrice = totalPrice;

        if (threeMonthsRadio.isSelected()) {
            basePrice *= 3;
        } else if( twelveMonthsRadio.isSelected()) {
            basePrice *= 12;
        }

        return basePrice;
    }

    private int getValidityMonths() {
        if (oneMonthRadio.isSelected()) {
            return 1;
        } else if (threeMonthsRadio.isSelected()) {
            return 3;
        } else if (twelveMonthsRadio.isSelected()) {
            return 12;
        }
        return 1;
    }

    private int getNextSubscriptionId() {
        Document maxIdDoc = subscriptionCollection.find()
                .sort(Sorts.descending("subscriptionId"))
                .limit(1)
                .first();

        if (maxIdDoc != null) {
            return maxIdDoc.getInteger("subscriptionId", 0) + 1;
        } else {
            return 1; 
        }
    }

    private int getNextPaymentId() {
        Document maxIdDoc = paymentCollection.find()
                .sort(Sorts.descending("PaymentId"))
                .limit(1)
                .first();

        if (maxIdDoc != null) {
            return maxIdDoc.getInteger("PaymentId", 0) + 1;
        } else {
            return 1;
        }
    }

    private void showPaymentMethodsDialog(JFrame frame, int finalPrice, int validityMonths) {
        String[] paymentMethods = {"Debit Card", "UPI", "Bank Transfer"};
        String selectedMethod = (String) JOptionPane.showInputDialog(
            frame,
            "Choose a payment method:",
            "Payment Methods",
            JOptionPane.PLAIN_MESSAGE,
            null,
            paymentMethods,
            paymentMethods[0]
        );

        int paymentId = getNextPaymentId(); // Generate the payment ID

        if (selectedMethod == null) {
            // Store canceled payment
            storePaymentStatus(paymentId, customerId, "Canceled");
            JOptionPane.showMessageDialog(
                frame,
                "Payment was not completed. Transaction canceled.",
                "Payment Canceled",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean paymentSuccessful = false;

        if (selectedMethod.equals("Debit Card")) {
            paymentSuccessful = processDebitCardPayment();
        } else if (selectedMethod.equals("UPI")) {
            paymentSuccessful = processUPIPayment();
        } else if (selectedMethod.equals("Bank Transfer")) {
            paymentSuccessful = processBankTransferPayment();
        }

        if (paymentSuccessful) {
            JOptionPane.showMessageDialog(
                frame,
                "Payment successful. Thanks for choosing us :).",
                "Payment Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
            storeSubscription(finalPrice, validityMonths, paymentId);
            frame.dispose();
        } else {
            // Store failed payment
            storePaymentStatus(paymentId, customerId, "Failed");
            JOptionPane.showMessageDialog(
                frame,
                "Payment failed. Transaction canceled.",
                "Payment Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean processDebitCardPayment() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField cardNumberField = new JTextField();
        JTextField expiryDateField = new JTextField();
        JTextField cvvField = new JTextField();

        panel.add(new JLabel("Debit Card Number:"));
        panel.add(cardNumberField);
        panel.add(new JLabel("Expiry Date (MM/YY):"));
        panel.add(expiryDateField);
        panel.add(new JLabel("CVV:"));
        panel.add(cvvField);

        int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Enter Debit Card Details",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.CANCEL_OPTION) {
            return false; // Payment was canceled
        }

        // Validate card details
        if (!validateCardDetails(cardNumberField.getText(), expiryDateField.getText(), cvvField.getText())) {
            JOptionPane.showMessageDialog(null, "Invalid card details. Please try again.", "Payment Failed", JOptionPane.ERROR_MESSAGE);
            return false; // Payment failed due to invalid card details
        }

        // Implement card processing logic here...
        return true; // Assume payment is successful
    }

    private boolean validateCardDetails(String cardNumber, String expiryDate, String cvv) {
        // Validate card number (simple check for 16 digits)
        if (!Pattern.matches("\\d{16}", cardNumber)) {
            return false;
        }

        // Validate expiry date (MM/YY format)
        if (!Pattern.matches("(0[1-9]|1[0-2])/[0-9]{2}", expiryDate)) {
            return false;
        }

        // Validate CVV (3 digits)
        if (!Pattern.matches("\\d{3}", cvv)) {
            return false;
        }

        return true;
    }

    private boolean processUPIPayment() {
        String upiId = JOptionPane.showInputDialog(
            null,
            "Enter UPI ID:"
        );

        if (upiId == null || !validateUPIId(upiId)) {
            JOptionPane.showMessageDialog(null, "Invalid UPI ID. Please try again.", "Payment Failed", JOptionPane.ERROR_MESSAGE);
            return false; // Payment failed due to invalid UPI ID
        }

        String password = JOptionPane.showInputDialog(
            null,
            "Enter UPI password:"
        );

        if (password == null) {
            return false; // Payment was canceled
        }

        // Implement UPI processing logic...
        return true; // Assume payment is successful
    }

    private boolean validateUPIId(String upiId) {
        // Simple UPI ID validation (should contain "@" character)
        return upiId.contains("@");
    }

    private boolean processBankTransferPayment() {
        String accountNumber = JOptionPane.showInputDialog(
            null,
            "Enter bank account number:"
        );

        if (accountNumber == null || !validateBankAccountNumber(accountNumber)) {
            JOptionPane.showMessageDialog(null, "Invalid bank account number. Please try again.", "Payment Failed", JOptionPane.ERROR_MESSAGE);
            return false; // Payment failed due to invalid bank account number
        }

        String routingNumber = JOptionPane.showInputDialog(
            null,
            "Enter routing number:"
        );

        if (routingNumber == null || !validateRoutingNumber(routingNumber)) {
            JOptionPane.showMessageDialog(null, "Invalid routing number. Please try again.", "Payment Failed", JOptionPane.ERROR_MESSAGE);
            return false; // Payment failed due to invalid routing number
        }

        // Implement bank transfer processing logic...
        return true; // Assume payment is successful
    }

    private boolean validateBankAccountNumber(String accountNumber) {
        // Validate bank account number (simple check for 10-12 digits)
        return Pattern.matches("\\d{10,12}", accountNumber);
    }

    private boolean validateRoutingNumber(String routingNumber) {
        // Validate routing number (simple check for 9 digits)
        return Pattern.matches("\\d{9}", routingNumber);
    }

    private void storePaymentStatus(int paymentId, int customerId, String status) {
        Document paymentDoc = new Document("PaymentId", paymentId)
            .append("customerId", customerId)
            .append("status", status);

        try {
            paymentCollection.insertOne(paymentDoc);
        } catch (Exception ex) {
            System.err.println("Error storing payment status: " + ex.getMessage());
        }
    }

    private void storePaymentStatus(int paymentId, int customerId, String status, int subscriptionId) {
        Document paymentDoc = new Document("PaymentId", paymentId)
            .append("customerId", customerId)
            .append("status", status)
            .append("subscriptionId", subscriptionId);

        try {
            paymentCollection.insertOne(paymentDoc);
        } catch (Exception ex) {
            System.err.println("Error storing payment status: " + ex.getMessage());
        }
    }

    private void storeSubscription(int finalPrice, int validityMonths, int paymentId) {
        int subscriptionId = getNextSubscriptionId(); // Get new subscription ID
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String startDate = dateFormat.format(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, validityMonths);
        String endDate = dateFormat.format(cal.getTime());
        storePaymentStatus(paymentId, customerId, "Success", subscriptionId);

        Document subscription = new Document("subscriptionId", subscriptionId)
            .append("customerId", customerId)
            .append("deviceId", deviceId)
            .append("total_price", finalPrice)
            .append("packs", new ArrayList<>(selectedChannels.keySet()))
            .append("start_date", startDate)
            .append("end_date", endDate)
            .append("validity_months", validityMonths)
            .append("paymentId", paymentId) // Add the payment ID
            .append("status", "Paid");

        try {
            subscriptionCollection.insertOne(subscription);
        } catch (Exception ex) {
            System.err.println("Error storing subscription: " + ex.getMessage());
        }
    }
}
