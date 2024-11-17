package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentPortal extends JFrame {
    private String username;
    private int studentYear;

    public StudentPortal(String username) {
        this.username = username;
        this.studentYear = fetchStudentYear(); // Fetch the student's year from the database
        setTitle("Student Portal - Welcome " + username);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 10, 10));

        JButton viewCoursesButton = new JButton("View Courses");
        JButton checkGradesButton = new JButton("Check Grades");
        JButton borrowBookButton = new JButton("Borrow Book");
        JButton returnBookButton = new JButton("Return Book");
        JButton logoutButton = new JButton("Logout");

        viewCoursesButton.addActionListener(e -> viewCourses());
        checkGradesButton.addActionListener(e -> checkGrades());
        borrowBookButton.addActionListener(e -> borrowBook());
        returnBookButton.addActionListener(e -> returnBook());
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(viewCoursesButton);
        buttonPanel.add(checkGradesButton);
        buttonPanel.add(borrowBookButton);
        buttonPanel.add(returnBookButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private int fetchStudentYear() {
        int year = 0;
        String query = "SELECT studentYear FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                year = resultSet.getInt("studentYear");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
        return year;
    }

    private void viewCourses() {
        StringBuilder coursesList = new StringBuilder("Courses for Year " + studentYear + ":\n");
        String query = "SELECT course_name, course_code, credits, semester FROM courses WHERE course_year = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, studentYear);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                String courseCode = resultSet.getString("course_code");
                int credits = resultSet.getInt("credits");
                String semester = resultSet.getString("semester");
                coursesList.append(courseName).append(" (").append(courseCode).append(") - ")
                        .append(credits).append(" credits, ").append(semester).append("\n");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            return;
        }

        if (coursesList.length() == ("Courses for Year " + studentYear + ":\n").length()) {
            coursesList.append("No courses available for this year.");
        }

        JOptionPane.showMessageDialog(this, coursesList.toString());
    }

    private void checkGrades() {
        StringBuilder gradesList = new StringBuilder("Your Grades:\n");
        String query = "SELECT g.grade, c.course_name, c.course_code " +
                "FROM grades g " +
                "JOIN courses c ON g.course_code = c.course_code " +
                "WHERE g.username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String grade = resultSet.getString("grade");
                String courseName = resultSet.getString("course_name");
                String courseCode = resultSet.getString("course_code");
                gradesList.append(courseName).append(" (").append(courseCode).append("): ").append(grade).append("\n");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            return;
        }

        if (gradesList.length() == ("Your Grades:\n").length()) {
            gradesList.append("No grades available for this student.");
        }

        JOptionPane.showMessageDialog(this, gradesList.toString());
    }

    private void borrowBook() {
        String bookId = JOptionPane.showInputDialog(this, "Enter Book ID to borrow:");
        if (bookId == null || bookId.trim().isEmpty()) {
            return;
        }

        String checkQuery = "SELECT title, is_borrowed FROM books WHERE book_id = ?";
        String updateQuery = "UPDATE books SET is_borrowed = TRUE WHERE book_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            checkStatement.setString(1, bookId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                boolean isBorrowed = resultSet.getBoolean("is_borrowed");
                if (isBorrowed) {
                    JOptionPane.showMessageDialog(this, "This book is already borrowed.");
                } else {
                    updateStatement.setString(1, bookId);
                    updateStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "You have successfully borrowed: " + resultSet.getString("title"));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Book not found.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void returnBook() {
        String bookId = JOptionPane.showInputDialog(this, "Enter Book ID to return:");
        if (bookId == null || bookId.trim().isEmpty()) {
            return;
        }

        String checkQuery = "SELECT title, is_borrowed FROM books WHERE book_id = ?";
        String updateQuery = "UPDATE books SET is_borrowed = FALSE WHERE book_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            checkStatement.setString(1, bookId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                boolean isBorrowed = resultSet.getBoolean("is_borrowed");
                if (!isBorrowed) {
                    JOptionPane.showMessageDialog(this, "This book is not currently borrowed.");
                } else {
                    updateStatement.setString(1, bookId);
                    updateStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "You have successfully returned: " + resultSet.getString("title"));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Book not found.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void logout() {
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            dispose();
            new LoginPage();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentPortal("RealUsername")); // Use actual username for login
    }
}
