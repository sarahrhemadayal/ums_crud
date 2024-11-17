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

public class FacultyManagementPage extends JFrame {
    private JTextField facultyIdField;
    private JTextField facultyNameField;
    private JTextField departmentField;
    private JTextField dobField;
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField appraisalScoreField;
    private JButton addButton;
    private JButton removeButton;
    private JButton modifyButton;
    private JTable facultyTable;
    private DefaultTableModel tableModel;

    public FacultyManagementPage() {
        setTitle("Faculty Management");
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
        addFormField(formPanel, "Faculty ID:", facultyIdField = new JTextField());
        addFormField(formPanel, "Faculty Name:", facultyNameField = new JTextField());
        addFormField(formPanel, "Department:", departmentField = new JTextField());
        addFormField(formPanel, "Date of Birth (YYYY-MM-DD):", dobField = new JTextField());
        addFormField(formPanel, "Email ID:", emailField = new JTextField());
        addFormField(formPanel, "Username:", usernameField = new JTextField());
        addFormField(formPanel, "Password:", passwordField = new JPasswordField());
        addFormField(formPanel, "Appraisal Score:", appraisalScoreField = new JTextField());

        mainPanel.add(formPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        addButton = new JButton("Add Faculty");
        removeButton = new JButton("Remove Faculty");
        modifyButton = new JButton("Modify Faculty");

        // Add action listeners to the buttons
        addButton.addActionListener(new AddFacultyListener());
        removeButton.addActionListener(new RemoveFacultyListener());
        modifyButton.addActionListener(new ModifyFacultyListener());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);

        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Sr. No", "Faculty ID", "Name", "Department", "DOB", "Email", "Appraisal Score"}, 0);
        facultyTable = new JTable(tableModel);
        JTableHeader header = facultyTable.getTableHeader();
        header.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(facultyTable);
        add(scrollPane, BorderLayout.CENTER);
        loadFaculties(); // Load existing faculties into the table

        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(label));
        field.setPreferredSize(new Dimension(150, 25));
        fieldPanel.add(field);
        panel.add(fieldPanel);
    }

    private void loadFaculties() {
        tableModel.setRowCount(0);
        String query = "SELECT sr_no, faculty_id, faculty_name, department, dob, email_id, appraisal_score FROM faculty";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int srNo = resultSet.getInt("sr_no");
                String facultyId = resultSet.getString("faculty_id");
                String facultyName = resultSet.getString("faculty_name");
                String department = resultSet.getString("department");
                String dob = resultSet.getString("dob");
                String email = resultSet.getString("email_id");
                int appraisalScore = resultSet.getInt("appraisal_score");
                tableModel.addRow(new Object[]{srNo, facultyId, facultyName, department, dob, email, appraisalScore});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading faculty: " + ex.getMessage());
        }
    }

    private class AddFacultyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String facultyId = facultyIdField.getText().trim();
            String facultyName = facultyNameField.getText().trim();
            String department = departmentField.getText().trim();
            String dob = dobField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            int appraisalScore = Integer.parseInt(appraisalScoreField.getText().trim());

            String queryFaculty = "INSERT INTO faculty (faculty_id, faculty_name, department, dob, email_id, appraisal_score) VALUES (?, ?, ?, ?, ?, ?)";
            String queryUser = "INSERT INTO users (role, username, password) VALUES ('teacher', ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement facultyStatement = connection.prepareStatement(queryFaculty);
                 PreparedStatement userStatement = connection.prepareStatement(queryUser)) {

                // Insert faculty details
                facultyStatement.setString(1, facultyId);
                facultyStatement.setString(2, facultyName);
                facultyStatement.setString(3, department);
                facultyStatement.setString(4, dob);
                facultyStatement.setString(5, email);
                facultyStatement.setInt(6, appraisalScore);
                facultyStatement.executeUpdate();

                // Insert user details for username and password
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                userStatement.executeUpdate();

                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Faculty added successfully.");
                loadFaculties();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Error adding faculty: " + ex.getMessage());
            }
        }
    }

    private class RemoveFacultyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = facultyTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Please select a faculty to remove.");
                return;
            }

            String facultyId = (String) tableModel.getValueAt(selectedRow, 1);
            String query = "DELETE FROM faculty WHERE faculty_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, facultyId);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Faculty removed successfully.");
                loadFaculties();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Error removing faculty: " + ex.getMessage());
            }
        }
    }

    private class ModifyFacultyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = facultyTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Please select a faculty to modify.");
                return;
            }

            String facultyId = (String) tableModel.getValueAt(selectedRow, 1);
            String facultyName = facultyNameField.getText().trim();
            String department = departmentField.getText().trim();
            String dob = dobField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            int appraisalScore = Integer.parseInt(appraisalScoreField.getText().trim());

            String queryFaculty = "UPDATE faculty SET faculty_name = ?, department = ?, dob = ?, email_id = ?, appraisal_score = ? WHERE faculty_id = ?";
            String queryUser = "UPDATE users SET username = ?, password = ? WHERE username = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement facultyStatement = connection.prepareStatement(queryFaculty);
                 PreparedStatement userStatement = connection.prepareStatement(queryUser)) {

                // Update faculty details
                facultyStatement.setString(1, facultyName);
                facultyStatement.setString(2, department);
                facultyStatement.setString(3, dob);
                facultyStatement.setString(4, email);
                facultyStatement.setInt(5, appraisalScore);
                facultyStatement.setString(6, facultyId);
                facultyStatement.executeUpdate();

                // Update user details for username and password
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                userStatement.setString(3, username);
                userStatement.executeUpdate();

                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Faculty modified successfully.");
                loadFaculties();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(FacultyManagementPage.this, "Error modifying faculty: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        facultyIdField.setText("");
        facultyNameField.setText("");
        departmentField.setText("");
        dobField.setText("");
        emailField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        appraisalScoreField.setText("");
    }

    public static void main(String[] args) {
        new FacultyManagementPage();
    }
}
