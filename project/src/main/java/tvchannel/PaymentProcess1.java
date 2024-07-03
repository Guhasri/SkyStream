package tvchannel;

import javax.swing.*;
import java.awt.*;
import com.mongodb.client.*;
import org.bson.Document;
import com.mongodb.client.model.Sorts;


public class PaymentProcess1 extends JFrame {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> paymentCollection;
    private static int customerId;
    private static int subscriptionId;

    public PaymentProcess1(int customerId, int subscriptionId) {
        PaymentProcess1.customerId = customerId;
        PaymentProcess1.subscriptionId = subscriptionId;

        // Initialize MongoDB connection
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("sample");
        paymentCollection = database.getCollection("payment");

        showPaymentMethodsDialog();
    }

    private void showPaymentMethodsDialog() {
        String[] paymentMethods = {"Debit Card", "UPI", "Bank Transfer"};
        String selectedMethod = (String) JOptionPane.showInputDialog(null, "Choose a payment method:",
                "Payment Methods", JOptionPane.PLAIN_MESSAGE, null, paymentMethods, paymentMethods[0]);
        if (selectedMethod != null) {
            if (selectedMethod.equals("Debit Card")) {
                processDebitCardPayment(); 
            } else if (selectedMethod.equals("UPI")) {
                processUPIPayment();
            } else if (selectedMethod.equals("Bank Transfer")) {
                processBankTransferPayment();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Payment method not selected. Transaction canceled.", "Payment Canceled",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processDebitCardPayment() {
        // Create a panel to hold the debit card input fields
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

        // Show the custom dialog box
        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Debit Card Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If the user cancels the dialog, terminate the payment process
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(null, "Payment failed. Transaction canceled.", "Payment Failed",
                    JOptionPane.ERROR_MESSAGE);
            updateDatabaseWithPaymentDetails("Canceled");
            return;
        }

        // Validate debit card details
        String cardNumber = cardNumberField.getText();
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        if (!isValidDebitCard(cardNumber, expiryDate, cvv)) {
            JOptionPane.showMessageDialog(null, "Invalid debit card details. Please try again.", "Invalid Details",
                    JOptionPane.ERROR_MESSAGE);
            processDebitCardPayment();
            return;
        }

        // Simulate successful payment processing
        updateDatabaseWithPaymentDetails("Success");
        showSuccessMessage();
    }

    private void processUPIPayment() {
        // Create a panel to hold the UPI input fields
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField upiIdField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("UPI ID:"));
        panel.add(upiIdField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        // Show the custom dialog box
        int result = JOptionPane.showConfirmDialog(null, panel, "Enter UPI Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If the user cancels the dialog, terminate the payment process
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(null, "Payment failed. Transaction canceled.", "Payment Failed",
                    JOptionPane.ERROR_MESSAGE);
            updateDatabaseWithPaymentDetails("Canceled");
            return;
        }

        // Validate UPI details
        String upiId = upiIdField.getText();
        String password = new String(passwordField.getPassword());

        if (!isValidUPI(upiId, password)) {
            JOptionPane.showMessageDialog(null, "Invalid UPI details. Please try again.", "Invalid Details",
                    JOptionPane.ERROR_MESSAGE);
            processUPIPayment();
            return;
        }

        // Simulate successful payment processing
        updateDatabaseWithPaymentDetails("Success");
        showSuccessMessage();
    }

    private void processBankTransferPayment() {
        // Create a panel to hold the bank transfer input fields
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField accountNumberField = new JTextField();
        JTextField routingNumberField = new JTextField();
        panel.add(new JLabel("Bank Account Number:"));
        panel.add(accountNumberField);
        panel.add(new JLabel("Routing Number:"));
        panel.add(routingNumberField);

        // Show the custom dialog box
        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Bank Transfer Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If the user cancels the dialog, terminate the payment process
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(null, "Payment failed. Transaction canceled.", "Payment Failed",
                    JOptionPane.ERROR_MESSAGE);
            updateDatabaseWithPaymentDetails("Canceled");
            return;
        }

        // Validate bank transfer details
        String accountNumber = accountNumberField.getText();
        String routingNumber = routingNumberField.getText();

        if (!isValidBankTransfer(accountNumber, routingNumber)) {
            JOptionPane.showMessageDialog(null, "Invalid bank transfer details. Please try again.", "Invalid Details",
                    JOptionPane.ERROR_MESSAGE);
            processBankTransferPayment();
            return;
        }

        // Simulate successful payment processing
        updateDatabaseWithPaymentDetails("Success");
        showSuccessMessage();
    }

    private boolean isValidDebitCard(String cardNumber, String expiryDate, String cvv) {
        return cardNumber.matches("\\d{16}") &&
               expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}") &&
               cvv.matches("\\d{3}");
    }

    private boolean isValidUPI(String upiId, String password) {
        return upiId.contains("@") &&
               password.length() >= 8;
    }

    private boolean isValidBankTransfer(String accountNumber, String routingNumber) {
        return accountNumber.matches("\\d{9,18}") &&
               routingNumber.matches("\\d{9}");
    }

    private void showSuccessMessage() {
        JOptionPane.showMessageDialog(null, "Payment successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateDatabaseWithPaymentDetails(String status) {
        int paymentId = getNextPaymentId();
        Document paymentDoc = new Document()
                .append("PaymentId", paymentId)
                .append("customerId", customerId)
                .append("status", status)
                .append("subscriptionId", subscriptionId);

        paymentCollection.insertOne(paymentDoc);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int customerId = 0;
                int subscriptionId = 0;
                new PaymentProcess1(customerId, subscriptionId);
            }
        });
    }
}
