package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeeManagementPage extends JFrame {
    private JTextField studentIdField;
    private JTextField totalFeeField;
    private JTextField amountPaidField;
    private JTextField dueDateField;
    private JButton addButton;
    private JButton removeButton;
    private JButton modifyButton;
    private JTable feeTable;
    private DefaultTableModel tableModel;

    public FeeManagementPage() {
        setTitle("Fee Management");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Add form fields
        addFormField(formPanel, "Student ID:", studentIdField = new JTextField());
        addFormField(formPanel, "Total Fee:", totalFeeField = new JTextField());
        addFormField(formPanel, "Amount Paid:", amountPaidField = new JTextField());
        addFormField(formPanel, "Due Date (YYYY-MM-DD):", dueDateField = new JTextField());

        mainPanel.add(formPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        addButton = new JButton("Add Fee");
        removeButton = new JButton("Remove Fee");
        modifyButton = new JButton("Modify Fee");

        // Add action listeners to the buttons
        addButton.addActionListener(new AddFeeListener());
        removeButton.addActionListener(new RemoveFeeListener());
        modifyButton.addActionListener(new ModifyFeeListener());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);

        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Fee ID", "Student ID", "Total Fee", "Amount Paid", "Due Amount", "Due Date"}, 0);
        feeTable = new JTable(tableModel);
        JTableHeader header = feeTable.getTableHeader();
        header.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(feeTable);
        add(scrollPane, BorderLayout.CENTER);
        loadFees(); // Load existing fees into the table

        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(label));
        field.setPreferredSize(new Dimension(150, 25));
        fieldPanel.add(field);
        panel.add(fieldPanel);
    }

    private void loadFees() {
        tableModel.setRowCount(0);
        String query = "SELECT fee_id, student_id, total_fee, amount_paid, due_amount, due_date FROM fee";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int feeId = resultSet.getInt("fee_id");
                String studentId = resultSet.getString("student_id");
                double totalFee = resultSet.getDouble("total_fee");
                double amountPaid = resultSet.getDouble("amount_paid");
                double dueAmount = resultSet.getDouble("due_amount");
                String dueDate = resultSet.getString("due_date");
                tableModel.addRow(new Object[]{feeId, studentId, totalFee, amountPaid, dueAmount, dueDate});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fees: " + ex.getMessage());
        }
    }

    private class AddFeeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String studentId = studentIdField.getText().trim();
            double totalFee = Double.parseDouble(totalFeeField.getText().trim());
            double amountPaid = Double.parseDouble(amountPaidField.getText().trim());
            String dueDate = dueDateField.getText().trim();

            String query = "INSERT INTO fee (student_id, total_fee, amount_paid, due_date) VALUES (?, ?, ?, ?)";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, studentId);
                statement.setDouble(2, totalFee);
                statement.setDouble(3, amountPaid);
                statement.setString(4, dueDate);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Fee added successfully.");
                loadFees();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Error adding fee: " + ex.getMessage());
            }
        }
    }

    private class RemoveFeeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = feeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Please select a fee to remove.");
                return;
            }

            int feeId = (int) tableModel.getValueAt(selectedRow, 0);
            String query = "DELETE FROM fee WHERE fee_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, feeId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Fee removed successfully.");
                loadFees();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Error removing fee: " + ex.getMessage());
            }
        }
    }

    private class ModifyFeeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = feeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Please select a fee to modify.");
                return;
            }

            int feeId = (int) tableModel.getValueAt(selectedRow, 0);
            String studentId = studentIdField.getText().trim();
            double totalFee = Double.parseDouble(totalFeeField.getText().trim());
            double amountPaid = Double.parseDouble(amountPaidField.getText().trim());
            String dueDate = dueDateField.getText().trim();

            String query = "UPDATE fee SET student_id = ?, total_fee = ?, amount_paid = ?, due_date = ? WHERE fee_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, studentId);
                statement.setDouble(2, totalFee);
                statement.setDouble(3, amountPaid);
                statement.setString(4, dueDate);
                statement.setInt(5, feeId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Fee modified successfully.");
                loadFees();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(FeeManagementPage.this, "Error modifying fee: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        studentIdField.setText("");
        totalFeeField.setText("");
        amountPaidField.setText("");
        dueDateField.setText("");
    }
}
