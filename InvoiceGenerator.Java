import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvoiceGenerator {

    private JFrame frame;
    private JTextField companyField;
    private JTextField vatField;
    private List<JTextField> descriptions;
    private List<JTextField> quantities;
    private List<JTextField> prices;
    private final int maxItems = 5;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InvoiceGenerator::new);
    }

    public InvoiceGenerator() {
        frame = new JFrame("Invoice Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLayout(new GridBagLayout());

        descriptions = new ArrayList<>();
        quantities = new ArrayList<>();
        prices = new ArrayList<>();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);


        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(new JLabel("Company Name:"), gbc);

        companyField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        frame.add(companyField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(new JLabel("VAT Rate (%):"), gbc);

        vatField = new JTextField("5");
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        frame.add(vatField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        frame.add(new JLabel("Description"), gbc);
        gbc.gridx = 1;
        frame.add(new JLabel("Quantity"), gbc);
        gbc.gridx = 2;
        frame.add(new JLabel("Price"), gbc);
        for (int i = 0; i < maxItems; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 3;
            JLabel label = new JLabel("Item " + (i + 1));
            frame.add(label, gbc);

            JTextField description = new JTextField(10);
            gbc.gridx = 0;
            frame.add(description, gbc);
            descriptions.add(description);

            JTextField quantity = new JTextField(5);
            gbc.gridx = 1;
            frame.add(quantity, gbc);
            quantities.add(quantity);

            JTextField price = new JTextField(5);
            gbc.gridx = 2;
            frame.add(price, gbc);
            prices.add(price);
        }


        JButton generateButton = new JButton("Generate Invoice");
        gbc.gridx = 0;
        gbc.gridy = maxItems + 3;
        gbc.gridwidth = 3;
        generateButton.addActionListener(new GenerateInvoiceAction());
        frame.add(generateButton, gbc);

        frame.setVisible(true);
    }

    private class GenerateInvoiceAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String companyName = companyField.getText().trim();
            String vatRateText = vatField.getText().trim();

            if (companyName.isEmpty() || vatRateText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter both company name and VAT rate.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double vatRate;
            try {
                vatRate = Double.parseDouble(vatRateText);
                if (vatRate < 0) {
                    throw new NumberFormatException("VAT rate cannot be negative.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid VAT rate.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter("invoice.txt"))) {
                writer.println("Company: " + companyName);
                writer.println("Invoice Generated on " + java.time.LocalDate.now());
                writer.println();
                writer.println(String.format("%-20s %-10s %10s %10s", "Description", "Quantity", "Price", "Total"));

                DecimalFormat df = new DecimalFormat("0.00");
                double grandTotal = 0;

                for (int i = 0; i < maxItems; i++) {
                    String description = descriptions.get(i).getText();
                    String quantityText = quantities.get(i).getText();
                    String priceText = prices.get(i).getText();

                    if (!description.isEmpty() && !quantityText.isEmpty() && !priceText.isEmpty()) {
                        try {
                            int quantity = Integer.parseInt(quantityText);
                            double price = Double.parseDouble(priceText);
                            double total = quantity * price;
                            grandTotal += total;
                            writer.println(String.format("%-20s %-10d %10.2f %10.2f", description, quantity, price, total));
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for quantity and price.", "Input Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                double vatAmount = grandTotal * (vatRate / 100);
                double totalWithVAT = grandTotal + vatAmount;

                writer.println();
                writer.println(String.format("%-40s %10.2f", "Subtotal", grandTotal));
                writer.println(String.format("%-40s %10.2f", "VAT (" + df.format(vatRate) + "%)", vatAmount));
                writer.println(String.format("%-40s %10.2f", "Total (Including VAT)", totalWithVAT));
                JOptionPane.showMessageDialog(frame, "Invoice generated successfully! Check 'invoice.txt'");
            } catch (IOException ioException) {
                ioException.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error writing invoice file.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}