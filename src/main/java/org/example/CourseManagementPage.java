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

public class CourseManagementPage extends JFrame {
    private JTextField courseNameField;
    private JTextField courseCodeField;
    private JTextField courseCreditField;
    private JButton addButton;
    private JButton removeButton;
    private JButton modifyButton;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> semesterComboBox;
    private JComboBox<Integer> yearComboBox;

    private String selectedCourseCode;
    private boolean isModifying;

    public CourseManagementPage() {
        setTitle("Course Management");
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
        addFormField(formPanel, "Course Code:", courseCodeField = new JTextField());
        addFormField(formPanel, "Course Name:", courseNameField = new JTextField());
        addFormField(formPanel, "Course Credits:", courseCreditField = new JTextField());

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

        addButton = new JButton("Add Course");
        removeButton = new JButton("Remove Course");
        modifyButton = new JButton("Modify Course");

        // Add action listeners to the buttons
        addButton.addActionListener(new AddCourseListener());
        removeButton.addActionListener(new RemoveCourseListener());
        modifyButton.addActionListener(new ModifyCourseListener());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(modifyButton);

        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Course Code", "Course Name", "Credits", "Semester", "Year"}, 0);
        courseTable = new JTable(tableModel);
        JTableHeader header = courseTable.getTableHeader();
        header.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        add(scrollPane, BorderLayout.CENTER);
        loadCourses(); // Load existing courses into the table

        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(label));
        field.setPreferredSize(new Dimension(150, 25));
        fieldPanel.add(field);
        panel.add(fieldPanel);
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        String query = "SELECT course_code, course_name, credits, semester, course_year FROM courses";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String courseCode = resultSet.getString("course_code");
                String courseName = resultSet.getString("course_name");
                int credits = resultSet.getInt("credits");
                String semester = resultSet.getString("semester");
                int year = resultSet.getInt("course_year");
                tableModel.addRow(new Object[]{courseCode, courseName, credits, semester, year});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }
    }

    private class AddCourseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String courseCode = courseCodeField.getText().trim();
            String courseName = courseNameField.getText().trim();
            int courseCredits;

            try {
                courseCredits = Integer.parseInt(courseCreditField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CourseManagementPage.this, "Invalid course credits.");
                return;
            }

            int courseYear = (int) yearComboBox.getSelectedItem();

            if (isModifying) {
                // Update the course in the database
                String query = "UPDATE courses SET course_name = ?, credits = ?, semester = ?, course_year = ? WHERE course_code = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, courseName);
                    statement.setInt(2, courseCredits);
                    statement.setString(3, (String) semesterComboBox.getSelectedItem());
                    statement.setInt(4, courseYear);
                    statement.setString(5, selectedCourseCode);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(CourseManagementPage.this, "Course modified successfully.");
                    loadCourses();
                    clearFields();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CourseManagementPage.this, "Error modifying course: " + ex.getMessage());
                }
                isModifying = false;
            } else {
                // Add the course to the database
                String query = "INSERT INTO courses (course_code, course_name, credits, semester, course_year) VALUES (?, ?, ?, ?, ?)";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, courseCode);
                    statement.setString(2, courseName);
                    statement.setInt(3, courseCredits);
                    statement.setString(4, (String) semesterComboBox.getSelectedItem());
                    statement.setInt(5, courseYear);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(CourseManagementPage.this, "Course added successfully.");
                    loadCourses();
                    clearFields();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CourseManagementPage.this, "Error adding course: " + ex.getMessage());
                }
            }
        }
    }

    private class RemoveCourseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(CourseManagementPage.this, "Please select a course to remove.");
                return;
            }

            String courseCode = (String) tableModel.getValueAt(selectedRow, 0);
            String query = "DELETE FROM courses WHERE course_code = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, courseCode);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(CourseManagementPage.this, "Course removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(CourseManagementPage.this, "Course not found.");
                }
                loadCourses();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(CourseManagementPage.this, "Error removing course: " + ex.getMessage());
            }
        }
    }

    private class ModifyCourseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(CourseManagementPage.this, "Please select a course to modify.");
                return;
            }

            selectedCourseCode = (String) tableModel.getValueAt(selectedRow, 0);
            String courseName = (String) tableModel.getValueAt(selectedRow, 1);
            String credits = tableModel.getValueAt(selectedRow, 2).toString();
            String semester = (String) tableModel.getValueAt(selectedRow, 3);
            int year = (int) tableModel.getValueAt(selectedRow, 4);

            courseCodeField.setText(selectedCourseCode);
            courseNameField.setText(courseName);
            courseCreditField.setText(credits);
            semesterComboBox.setSelectedItem(semester);
            yearComboBox.setSelectedItem(year);

            isModifying = true;
        }
    }

    private void clearFields() {
        courseCodeField.setText("");
        courseNameField.setText("");
        courseCreditField.setText("");
        semesterComboBox.setSelectedIndex(0);
        yearComboBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        new CourseManagementPage();
    }
}
