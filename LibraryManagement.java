import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Calendar;

public class LibraryManagement extends JFrame implements ActionListener {

    private JTextField textField1, textField2, textField3, textField4, textField5, textField6, textField7;
    private JButton addButton, viewButton, editButton, deleteButton, clearButton, reserveButton, reservedBooksButton;
    private JPanel panel;
    private Connection connection;

    public LibraryManagement() {
        setTitle("Library Management System");
        setSize(600, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library_management", "root", "1234");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        textField1 = new JTextField(10);
        textField2 = new JTextField(20);
        textField3 = new JTextField(20);
        textField4 = new JTextField(20);
        textField5 = new JTextField(10);
        textField6 = new JTextField(20);
        textField7 = new JTextField(10);

        addButton = new JButton("Add");
        viewButton = new JButton("View");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");
        reserveButton = new JButton("Reserve");
        reservedBooksButton = new JButton("View Reserved Books");

        addButton.addActionListener(this);
        viewButton.addActionListener(this);
        editButton.addActionListener(this);
        deleteButton.addActionListener(this);
        clearButton.addActionListener(this);
        reservedBooksButton.addActionListener(this);
        reserveButton.addActionListener(this);

        panel = new JPanel(new GridLayout(10, 2));
        panel.add(new JLabel("Book ID"));
        panel.add(textField1);
        panel.add(new JLabel("Book Title"));
        panel.add(textField2);
        panel.add(new JLabel("Author"));
        panel.add(textField3);
        panel.add(new JLabel("Publisher"));
        panel.add(textField4);
        panel.add(new JLabel("Year of Publication"));
        panel.add(textField5);
        panel.add(new JLabel("ISBN"));
        panel.add(textField6);
        panel.add(new JLabel("Number of Copies"));
        panel.add(textField7);
        panel.add(addButton);
        panel.add(viewButton);
        panel.add(editButton);
        panel.add(deleteButton);

        panel.add(reserveButton);
        panel.add(reservedBooksButton);
        panel.add(clearButton);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {

            String bookId = textField1.getText();
            String bookTitle = textField2.getText();
            String author = textField3.getText();
            String publisher = textField4.getText();
            String yearOfPublicationStr = textField5.getText();
            String isbn = textField6.getText();
            String numOfCopiesStr = textField7.getText();

            if (bookId.isEmpty() || bookTitle.isEmpty() || author.isEmpty() || publisher.isEmpty()
                    || yearOfPublicationStr.isEmpty() || isbn.isEmpty() || numOfCopiesStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required");
                return;
            }

            int yearOfPublication;
            int numOfCopies;
            try {
                yearOfPublication = Integer.parseInt(yearOfPublicationStr);
                numOfCopies = Integer.parseInt(numOfCopiesStr);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (yearOfPublication < 1900 || yearOfPublication > currentYear) {
                    JOptionPane.showMessageDialog(this,
                            "Year of publication should be between 1900 and the current year");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year of publication and number of copies must be numeric");
                return;
            }

            try {
                String query = "INSERT INTO books VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, bookId);
                preparedStatement.setString(2, bookTitle);
                preparedStatement.setString(3, author);
                preparedStatement.setString(4, publisher);
                preparedStatement.setInt(5, yearOfPublication);
                preparedStatement.setString(6, isbn);
                preparedStatement.setInt(7, numOfCopies);
                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book added successfully");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage());
            }
        } else if (e.getSource() == viewButton) {
            try {
                String query = "SELECT * FROM books";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                displayResultSet(resultSet);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error viewing books: " + ex.getMessage());
            }
        } else if (e.getSource() == editButton) {

            try {
                String bookId = JOptionPane.showInputDialog(this, "Enter book ID to edit:");
                if (bookId == null || bookId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Book ID cannot be empty");
                    return;
                }

                String columnName = JOptionPane.showInputDialog(this,
                        "Enter column name to update (book_title, auther, publisher, year_of_publication, isbn, num_of_copies):");

                String updatedValue = JOptionPane.showInputDialog(this, "Enter new value for " + columnName + ":");

                String updateQuery = "UPDATE books SET " + columnName.toLowerCase().replace(" ", "_")
                        + " = ? WHERE book_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setString(1, updatedValue);
                updateStatement.setString(2, bookId);
                int rowsUpdated = updateStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Book updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Error updating book: Book ID not found");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage());
            }

        } else if (e.getSource() == deleteButton) {
            String bookId = JOptionPane.showInputDialog(this, "Enter book ID to delete:");
            try {
                String checkReservedQuery = "SELECT * FROM reserved_books WHERE book_id = ?";
                PreparedStatement checkReservedStatement = connection.prepareStatement(checkReservedQuery);
                checkReservedStatement.setString(1, bookId);
                ResultSet reservedResultSet = checkReservedStatement.executeQuery();

                if (reservedResultSet.next()) {
                    JOptionPane.showMessageDialog(this, "This book is reserved and cannot be deleted.");
                    return;
                }

                String deleteQuery = "DELETE FROM books WHERE book_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, bookId);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Book not found");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting book: " + ex.getMessage());
            }
        } else if (e.getSource() == reservedBooksButton) {

            try {
                String query = "SELECT * FROM reserved_books";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                displayResultSet(resultSet);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error viewing reserved books: " + ex.getMessage());
            }
        } else if (e.getSource() == clearButton) {
            clearFields();
        } else if (e.getSource() == reserveButton) {
            if (e.getSource() == reserveButton) {
                String customerId = JOptionPane.showInputDialog(this, "Enter customer ID:");
                if (customerId == null || customerId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Customer ID cannot be empty");
                    return;
                }

                try {
                    String bookId = JOptionPane.showInputDialog(this, "Enter book ID to reserve:");
                    if (bookId == null || bookId.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Book ID cannot be empty");
                        return;
                    }

                    String query = "SELECT num_of_copies FROM books WHERE book_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, bookId);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        int numCopies = resultSet.getInt("num_of_copies");
                        if (numCopies > 0) {
                            String reserveQuery = "INSERT INTO reserved_books (book_id, customer_id, reservation_date) VALUES (?, ?, ?)";
                            PreparedStatement reserveStatement = connection.prepareStatement(reserveQuery);
                            reserveStatement.setString(1, bookId);
                            reserveStatement.setString(2, customerId);
                            reserveStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                            reserveStatement.executeUpdate();

                            String updateQuery = "UPDATE books SET num_of_copies = ? WHERE book_id = ?";
                            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                            updateStatement.setInt(1, numCopies - 1);
                            updateStatement.setString(2, bookId);
                            updateStatement.executeUpdate();

                            JOptionPane.showMessageDialog(this, "Book reserved successfully");
                        } else {
                            JOptionPane.showMessageDialog(this, "No copies available for reservation");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Book not found");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error reserving book: " + ex.getMessage());
                }
            }

        }
    }

    private void displayResultSet(ResultSet resultSet) throws SQLException {
        JFrame frame = new JFrame("View Books");
        JTable table = new JTable(buildTableModel(resultSet));
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
        frame.setSize(800, 400);
        frame.setVisible(true);
    }

    private void clearFields() {
        textField1.setText("");
        textField2.setText("");
        textField3.setText("");
        textField4.setText("");
        textField5.setText("");
        textField6.setText("");
        textField7.setText("");
    }

    public static void main(String[] args) {
        new LibraryManagement();
    }

    public static DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        while (resultSet.next()) {
            Object[] rowData = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                rowData[i - 1] = resultSet.getObject(i);
            }
            tableModel.addRow(rowData);
        }
        return tableModel;
    }
}