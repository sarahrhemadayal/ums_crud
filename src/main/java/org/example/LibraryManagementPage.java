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

public class LibraryManagementPage extends JFrame {
    private JTextField bookIdField;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField yearField;
    private JButton addButton;
    private JButton removeButton;
    private JButton modifyButton;
    private JTable bookTable;
    private DefaultTableModel tableModel;

    public LibraryManagementPage() {
        setTitle("Library Management");
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
        addFormField(formPanel, "Book ID:", bookIdField = new JTextField());
        addFormField(formPanel, "Title:", titleField = new JTextField());
        addFormField(formPanel, "Author:", authorField = new JTextField());
        addFormField(formPanel, "Publisher:", publisherField = new JTextField());
        addFormField(formPanel, "Year:", yearField = new JTextField());

        mainPanel.add(formPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        addButton = new JButton("Add Book");
        removeButton = new JButton("Remove Book");
        modifyButton = new JButton("Modify Book");

        // Add action listeners to the buttons
        addButton.addActionListener(new AddBookListener());
        removeButton.addActionListener(new RemoveBookListener());
        modifyButton.addActionListener(new ModifyBookListener());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);

        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Book ID", "Title", "Author", "Publisher", "Year"}, 0);
        bookTable = new JTable(tableModel);
        JTableHeader header = bookTable.getTableHeader();
        header.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        loadBooks(); // Load existing books into the table
        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(label));
        field.setPreferredSize(new Dimension(150, 25));
        fieldPanel.add(field);
        panel.add(fieldPanel);
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        String query = "SELECT book_id, title, author, publisher, year FROM books";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String bookId = resultSet.getString("book_id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String publisher = resultSet.getString("publisher");
                int year = resultSet.getInt("year");
                tableModel.addRow(new Object[]{bookId, title, author, publisher, year});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private class AddBookListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String bookId = bookIdField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            String query = "INSERT INTO books (book_id, title, author, publisher, year) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, bookId);
                statement.setString(2, title);
                statement.setString(3, author);
                statement.setString(4, publisher);
                statement.setInt(5, year);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Book added successfully.");
                loadBooks();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Error adding book: " + ex.getMessage());
            }
        }
    }

    private class RemoveBookListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Please select a book to remove.");
                return;
            }

            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            String query = "DELETE FROM books WHERE book_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, bookId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Book removed successfully.");
                loadBooks();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Error removing book: " + ex.getMessage());
            }
        }
    }

    private class ModifyBookListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Please select a book to modify.");
                return;
            }

            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            String query = "UPDATE books SET title = ?, author = ?, publisher = ?, year = ? WHERE book_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, title);
                statement.setString(2, author);
                statement.setString(3, publisher);
                statement.setInt(4, year);
                statement.setString(5, bookId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Book modified successfully.");
                loadBooks();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LibraryManagementPage.this, "Error modifying book: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        bookIdField.setText("");
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        yearField.setText("");
    }
}
