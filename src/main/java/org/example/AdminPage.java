package org.example;

import javax.swing.*;
import java.awt.*;

public class AdminPage extends JFrame {
    public AdminPage() {
        setTitle("Admin Dashboard");
        setSize(800, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();

        // Setting GridLayout with 2 rows and 3 columns for an even grid
        panel.setLayout(new GridLayout(2, 3, 10, 10)); // 10px gaps between buttons

        // Buttons for management pages
        JButton studentButton = new JButton("Manage Students");
        JButton facultyButton = new JButton("Manage Faculty");
        JButton courseButton = new JButton("Manage Courses");
        JButton libraryButton = new JButton("Manage Library");
        JButton examButton = new JButton("Manage Examinations");
        JButton feeButton = new JButton("Manage Fees");

        // Add Action Listeners
        studentButton.addActionListener(e -> new StudentManagementPage());
        facultyButton.addActionListener(e -> new FacultyManagementPage());
        courseButton.addActionListener(e -> new CourseManagementPage());
        libraryButton.addActionListener(e -> new LibraryManagementPage());
        examButton.addActionListener(e -> new ExaminationManagementPage());
        feeButton.addActionListener(e -> new FeeManagementPage());

        // Add buttons to panel
        panel.add(studentButton);
        panel.add(facultyButton);
        panel.add(courseButton);
        panel.add(libraryButton);
        panel.add(examButton);
        panel.add(feeButton);

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminPage::new);
    }
}
