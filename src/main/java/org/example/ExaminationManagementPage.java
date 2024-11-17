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

public class ExaminationManagementPage extends JFrame {
    private JTextField examNameField;
    private JTextField examCodeField;
    private JTextField studentCountField;
    private JButton addButton;
    private JButton removeButton;
    private JButton modifyButton;
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> semesterComboBox;
    private JComboBox<Integer> yearComboBox;

    private String selectedExamCode;
    private boolean isModifying;

    public ExaminationManagementPage() {
        setTitle("Examination Management");
        setSize(600, 500);
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
        addFormField(formPanel, "Exam Code:", examCodeField = new JTextField());
        addFormField(formPanel, "Exam Name:", examNameField = new JTextField());
        addFormField(formPanel, "Number of Students:", studentCountField = new JTextField());

        // Semester combo box
        JPanel semesterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        semesterPanel.add(new JLabel("Select Semester:"));
        semesterComboBox = new JComboBox<>(new String[]{"Odd", "Even"});
        semesterPanel.add(semesterComboBox);
        formPanel.add(semesterPanel);

        // Year combo box
        JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yearPanel.add(new JLabel("Select Year:"));
        yearComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        yearPanel.add(yearComboBox);
        formPanel.add(yearPanel);

        mainPanel.add(formPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        addButton = new JButton("Add Exam");
        removeButton = new JButton("Remove Exam");
        modifyButton = new JButton("Modify Exam");

        // Add action listeners to the buttons
        addButton.addActionListener(new AddExamListener());
        removeButton.addActionListener(new RemoveExamListener());
        modifyButton.addActionListener(new ModifyExamListener());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);

        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Exam Code", "Exam Name", "Students", "Semester", "Year"}, 0);
        examTable = new JTable(tableModel);
        JTableHeader header = examTable.getTableHeader();
        header.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(examTable);
        add(scrollPane, BorderLayout.CENTER);
        loadExams(); // Load existing exams into the table

        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(label));
        field.setPreferredSize(new Dimension(150, 25));
        fieldPanel.add(field);
        panel.add(fieldPanel);
    }

    private void loadExams() {
        tableModel.setRowCount(0);
        String query = "SELECT exam_code, exam_name, students, semester, exam_year FROM exams";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String examCode = resultSet.getString("exam_code");
                String examName = resultSet.getString("exam_name");
                int students = resultSet.getInt("students");
                String semester = resultSet.getString("semester");
                int year = resultSet.getInt("exam_year");
                tableModel.addRow(new Object[]{examCode, examName, students, semester, year});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading exams: " + ex.getMessage());
        }
    }

    private class AddExamListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String examCode = examCodeField.getText().trim();
            String examName = examNameField.getText().trim();
            int studentCount;

            try {
                studentCount = Integer.parseInt(studentCountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Invalid number of students.");
                return;
            }

            int examYear = (int) yearComboBox.getSelectedItem();

            if (isModifying) {
                // Update the exam in the database
                String query = "UPDATE exams SET exam_name = ?, students = ?, semester = ?, exam_year = ? WHERE exam_code = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, examName);
                    statement.setInt(2, studentCount);
                    statement.setString(3, (String) semesterComboBox.getSelectedItem());
                    statement.setInt(4, examYear);
                    statement.setString(5, selectedExamCode);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Exam modified successfully.");
                    loadExams();
                    clearFields();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Error modifying exam: " + ex.getMessage());
                }
                isModifying = false;
            } else {
                // Add the exam to the database
                String query = "INSERT INTO exams (exam_code, exam_name, students, semester, exam_year) VALUES (?, ?, ?, ?, ?)";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, examCode);
                    statement.setString(2, examName);
                    statement.setInt(3, studentCount);
                    statement.setString(4, (String) semesterComboBox.getSelectedItem());
                    statement.setInt(5, examYear);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Exam added successfully.");
                    loadExams();
                    clearFields();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Error adding exam: " + ex.getMessage());
                }
            }
        }
    }

    private class RemoveExamListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = examTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Please select an exam to remove.");
                return;
            }

            String examCode = (String) tableModel.getValueAt(selectedRow, 0);
            String query = "DELETE FROM exams WHERE exam_code = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, examCode);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Exam removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Exam not found.");
                }
                loadExams();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Error removing exam: " + ex.getMessage());
            }
        }
    }

    private class ModifyExamListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = examTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ExaminationManagementPage.this, "Please select an exam to modify.");
                return;
            }

            selectedExamCode = (String) tableModel.getValueAt(selectedRow, 0);
            String examName = (String) tableModel.getValueAt(selectedRow, 1);
            String students = tableModel.getValueAt(selectedRow, 2).toString();
            String semester = (String) tableModel.getValueAt(selectedRow, 3);
            int year = (int) tableModel.getValueAt(selectedRow, 4);

            examCodeField.setText(selectedExamCode);
            examNameField.setText(examName);
            studentCountField.setText(students);
            semesterComboBox.setSelectedItem(semester);
            yearComboBox.setSelectedItem(year);

            isModifying = true;
        }
    }

    private void clearFields() {
        examCodeField.setText("");
        examNameField.setText("");
        studentCountField.setText("");
        semesterComboBox.setSelectedIndex(0);
        yearComboBox.setSelectedIndex(0);
        isModifying = false;
    }
}
