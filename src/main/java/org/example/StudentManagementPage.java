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

public class StudentManagementPage extends JFrame {
    private JTextField studentIdField;
    private JTextField studentNameField;
    private JTextField dobField;
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton addButton;
    private JButton removeButton;
    private JButton modifyButton;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public StudentManagementPage() {
        setTitle("Student Management");
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
        addFormField(formPanel, "Student Name:", studentNameField = new JTextField());
        addFormField(formPanel, "Date of Birth (YYYY-MM-DD):", dobField = new JTextField());
        addFormField(formPanel, "Email ID:", emailField = new JTextField());
        addFormField(formPanel, "Username:", usernameField = new JTextField());
        addFormField(formPanel, "Password:", passwordField = new JPasswordField());

        mainPanel.add(formPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        addButton = new JButton("Add Student");
        removeButton = new JButton("Remove Student");
        modifyButton = new JButton("Modify Student");

        // Add action listeners to the buttons
        addButton.addActionListener(new AddStudentListener());
        removeButton.addActionListener(new RemoveStudentListener());
        modifyButton.addActionListener(new ModifyStudentListener());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);

        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Sr. No", "Student ID", "Name", "DOB", "Email"}, 0);
        studentTable = new JTable(tableModel);
        JTableHeader header = studentTable.getTableHeader();
        header.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        add(scrollPane, BorderLayout.CENTER);
        loadStudents(); // Load existing students into the table

        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(label));
        field.setPreferredSize(new Dimension(150, 25));
        fieldPanel.add(field);
        panel.add(fieldPanel);
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        String query = "SELECT sr_no, student_id, student_name, dob, email_id FROM students";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int srNo = resultSet.getInt("sr_no");
                String studentId = resultSet.getString("student_id");
                String studentName = resultSet.getString("student_name");
                String dob = resultSet.getString("dob");
                String email = resultSet.getString("email_id");
                tableModel.addRow(new Object[]{srNo, studentId, studentName, dob, email});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    private class AddStudentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String studentId = studentIdField.getText().trim();
            String studentName = studentNameField.getText().trim();
            String dob = dobField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            String queryStudent = "INSERT INTO students (student_id, student_name, dob, email_id) VALUES (?, ?, ?, ?)";
            String queryUser = "INSERT INTO users (role, username, password) VALUES ('student', ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement studentStatement = connection.prepareStatement(queryStudent);
                 PreparedStatement userStatement = connection.prepareStatement(queryUser)) {

                // Insert student details
                studentStatement.setString(1, studentId);
                studentStatement.setString(2, studentName);
                studentStatement.setString(3, dob);
                studentStatement.setString(4, email);
                studentStatement.executeUpdate();

                // Insert user details for username and password
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                userStatement.executeUpdate();

                JOptionPane.showMessageDialog(StudentManagementPage.this, "Student added successfully.");
                loadStudents();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(StudentManagementPage.this, "Error adding student: " + ex.getMessage());
            }
        }
    }

    private class RemoveStudentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(StudentManagementPage.this, "Please select a student to remove.");
                return;
            }

            String studentId = (String) tableModel.getValueAt(selectedRow, 1);
            String query = "DELETE FROM students WHERE student_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, studentId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(StudentManagementPage.this, "Student removed successfully.");
                loadStudents();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(StudentManagementPage.this, "Error removing student: " + ex.getMessage());
            }
        }
    }

    private class ModifyStudentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(StudentManagementPage.this, "Please select a student to modify.");
                return;
            }

            String studentId = (String) tableModel.getValueAt(selectedRow, 1);
            String studentName = studentNameField.getText().trim();
            String dob = dobField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            String queryStudent = "UPDATE students SET student_name = ?, dob = ?, email_id = ? WHERE student_id = ?";
            String queryUser = "UPDATE users SET username = ?, password = ? WHERE username = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement studentStatement = connection.prepareStatement(queryStudent);
                 PreparedStatement userStatement = connection.prepareStatement(queryUser)) {

                // Update student details
                studentStatement.setString(1, studentName);
                studentStatement.setString(2, dob);
                studentStatement.setString(3, email);
                studentStatement.setString(4, studentId);
                studentStatement.executeUpdate();

                // Update user details for username and password
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                userStatement.setString(3, username);
                userStatement.executeUpdate();

                JOptionPane.showMessageDialog(StudentManagementPage.this, "Student modified successfully.");
                loadStudents();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(StudentManagementPage.this, "Error modifying student: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        studentIdField.setText("");
        studentNameField.setText("");
        dobField.setText("");
        emailField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    public static void main(String[] args) {
        new StudentManagementPage();
    }
}
