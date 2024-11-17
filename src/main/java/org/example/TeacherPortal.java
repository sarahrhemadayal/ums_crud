package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TeacherPortal extends JFrame {
    private String username;

    public TeacherPortal(String username) {
        this.username = username;
        setTitle("Teacher Portal - Welcome " + username);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        JButton viewStudentsButton = new JButton("View Students");
        JButton manageGradesButton = new JButton("Manage Grades");
        JButton logAttendanceButton = new JButton("Manage Attendance");
        JButton manageBooksButton = new JButton("Manage Books");
        JButton logoutButton = new JButton("Logout");

        viewStudentsButton.addActionListener(e -> viewStudents());
        manageGradesButton.addActionListener(e -> manageGrades());
        logAttendanceButton.addActionListener(e -> manageAttendance());
        manageBooksButton.addActionListener(e -> manageBooks());
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(viewStudentsButton);
        buttonPanel.add(manageGradesButton);
        buttonPanel.add(logAttendanceButton);
        buttonPanel.add(manageBooksButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // View Students
    private void viewStudents() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Department", "Semester", "Year", "Email"}, 0);
        JTable table = new JTable(model);

        String query = "SELECT student_id, student_name, department, semester, student_year, email_id FROM students";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String studentId = resultSet.getString("student_id");
                String studentName = resultSet.getString("student_name");
                String department = resultSet.getString("department");
                String semester = resultSet.getString("semester");
                int year = resultSet.getInt("student_year");
                String email = resultSet.getString("email_id");
                model.addRow(new Object[]{studentId, studentName, department, semester, year, email});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(table), "View Students", JOptionPane.PLAIN_MESSAGE);
    }

    // Manage Grades (Add/Remove/Modify)
    private void manageGrades() {
        String[] options = {"View Grades", "Add Grade", "Remove Grade", "Modify Grade"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Grades",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0 -> viewGrades();
            case 1 -> addGrade();
            case 2 -> removeGrade();
            case 3 -> modifyGrade();
        }
    }

    private void viewGrades() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Course Code", "Grade"}, 0);
        JTable table = new JTable(model);

        String query = "SELECT g.student_id, s.student_name, g.course_code, g.grade " +
                "FROM grades g " +
                "JOIN students s ON g.student_id = s.student_id " +
                "WHERE g.username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String studentId = resultSet.getString("student_id");
                    String studentName = resultSet.getString("student_name");
                    String courseCode = resultSet.getString("course_code");
                    String grade = resultSet.getString("grade");
                    model.addRow(new Object[]{studentId, studentName, courseCode, grade});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(table), "View Grades", JOptionPane.PLAIN_MESSAGE);
    }

    private void addGrade() {
        JTextField studentIdField = new JTextField();
        JTextField courseCodeField = new JTextField();
        JTextField gradeField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseCodeField);
        panel.add(new JLabel("Grade:"));
        panel.add(gradeField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Grade", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String courseCode = courseCodeField.getText();
            String grade = gradeField.getText();

            String query = "INSERT INTO grades (username, student_id, course_code, grade) VALUES (?, ?, ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, username);
                statement.setString(2, studentId);
                statement.setString(3, courseCode);
                statement.setString(4, grade);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Grade added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void removeGrade() {
        String studentId = JOptionPane.showInputDialog(this, "Enter the Student ID to remove grade:");
        String courseCode = JOptionPane.showInputDialog(this, "Enter the Course Code to remove grade:");

        if (studentId != null && courseCode != null) {
            String query = "DELETE FROM grades WHERE student_id = ? AND course_code = ? AND username = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, studentId);
                statement.setString(2, courseCode);
                statement.setString(3, username);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Grade removed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No grade found with the given Student ID and Course Code.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void modifyGrade() {
        String studentId = JOptionPane.showInputDialog(this, "Enter the Student ID to modify grade:");
        String courseCode = JOptionPane.showInputDialog(this, "Enter the Course Code to modify grade:");
        String newGrade = JOptionPane.showInputDialog(this, "Enter the new grade:");

        if (studentId != null && courseCode != null && newGrade != null) {
            String query = "UPDATE grades SET grade = ? WHERE student_id = ? AND course_code = ? AND username = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, newGrade);
                statement.setString(2, studentId);
                statement.setString(3, courseCode);
                statement.setString(4, username);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Grade updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No grade found with the given Student ID and Course Code.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    // Manage Attendance (Add/Remove/Modify)
    private void manageAttendance() {
        String[] options = {"View Attendance", "Add Attendance", "Remove Attendance", "Modify Attendance"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Attendance",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0 -> viewAttendance();
            case 1 -> addAttendance();
            case 2 -> removeAttendance();
            case 3 -> modifyAttendance();
        }
    }

    private void viewAttendance() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Attendance Date", "Status"}, 0);
        JTable table = new JTable(model);

        String query = "SELECT a.student_id, s.student_name, a.attendance_date, a.status " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.student_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String studentId = resultSet.getString("student_id");
                String studentName = resultSet.getString("student_name");
                Date attendanceDate = resultSet.getDate("attendance_date");
                String status = resultSet.getString("status");  // "Present" or "Absent"
                model.addRow(new Object[]{studentId, studentName, attendanceDate, status});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(table), "View Attendance", JOptionPane.PLAIN_MESSAGE);
    }


    private void addAttendance() {
        JTextField studentIdField = new JTextField();
        JTextField courseCodeField = new JTextField();
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Present", "Absent"});
        JTextField attendanceDateField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseCodeField);
        panel.add(new JLabel("Attendance Status:"));
        panel.add(statusComboBox);
        panel.add(new JLabel("Attendance Date (YYYY-MM-DD)  :"));
        panel.add(attendanceDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Attendance", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String courseCode = courseCodeField.getText();
            String status = (String) statusComboBox.getSelectedItem();
            String attendanceDate = attendanceDateField.getText();

            String query = "INSERT INTO attendance (student_id, course_code, attendance_date, status) VALUES (?, ?, ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, studentId);
                statement.setString(2, courseCode);
                statement.setString(3, attendanceDate);
                statement.setString(4, status);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Attendance added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }



    private void removeAttendance() {
        JTextField studentIdField = new JTextField();
        JTextField attendanceDateField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(new JLabel("Attendance Date (YYYY-MM-DD):"));
        panel.add(attendanceDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Remove Attendance", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String attendanceDate = attendanceDateField.getText();

            String query = "DELETE FROM attendance WHERE student_id = ? AND attendance_date = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, studentId);
                statement.setString(2, attendanceDate);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Attendance removed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No attendance found for the specified student on that date.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }



    private void modifyAttendance() {
        JTextField studentIdField = new JTextField();
        JTextField attendanceDateField = new JTextField();
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Present", "Absent"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(new JLabel("Attendance Date (YYYY-MM-DD):"));
        panel.add(attendanceDateField);
        panel.add(new JLabel("New Attendance Status:"));
        panel.add(statusComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modify Attendance", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText();
            String attendanceDate = attendanceDateField.getText();
            String newStatus = (String) statusComboBox.getSelectedItem();

            String query = "UPDATE attendance SET status = ? WHERE student_id = ? AND attendance_date = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, newStatus);
                statement.setString(2, studentId);
                statement.setString(3, attendanceDate);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Attendance modified successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No attendance found for the specified student on that date.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }



    private void manageBooks() {
        String[] options = {"View Books", "Add Book", "Remove Book"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Books",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0 -> viewBooks();
            case 1 -> addBook();
            case 2 -> removeBook();
        }
    }

    private void viewBooks() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Publisher", "Year", "Is Borrowed"}, 0);
        JTable table = new JTable(model);

        String query = "SELECT book_id, title, author, publisher, year, is_borrowed FROM books ORDER BY title";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String bookId = resultSet.getString("book_id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String publisher = resultSet.getString("publisher");
                int year = resultSet.getInt("year");
                boolean isBorrowed = resultSet.getBoolean("is_borrowed");
                model.addRow(new Object[]{bookId, title, author, publisher, year, isBorrowed ? "Yes" : "No"});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Book Inventory", JOptionPane.PLAIN_MESSAGE);
    }

    private void addBook() {
        JTextField bookIdField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextField yearField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("Publisher:"));
        panel.add(publisherField);
        panel.add(new JLabel("Year:"));
        panel.add(yearField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Book", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String bookId = bookIdField.getText();
            String title = titleField.getText();
            String author = authorField.getText();
            String publisher = publisherField.getText();
            int year = Integer.parseInt(yearField.getText());

            String query = "INSERT INTO books (book_id, title, author, publisher, year, is_borrowed) VALUES (?, ?, ?, ?, ?, 0)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, bookId);
                statement.setString(2, title);
                statement.setString(3, author);
                statement.setString(4, publisher);
                statement.setInt(5, year);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void removeBook() {
        String bookId = JOptionPane.showInputDialog(this, "Enter the Book ID to remove:");
        if (bookId != null) {
            String query = "DELETE FROM books WHERE book_id = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, bookId);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Book removed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No book found with the given ID.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void logout() {
        dispose();
        new LoginPage();
    }

    public static void main(String[] args) {
        new TeacherPortal("t");  // Replace with actual username
    }
}
