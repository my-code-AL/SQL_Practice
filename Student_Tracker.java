import java.sql.*;
import java.util.Scanner;

public class Student_Tracker {
    // all my information used to log into my current DB
    private static final String DB_URL = "jdbc:mysql://localhost:3306/project_db";
    private static final String DB_USER = "main_user";
    private static final String DB_PASSWORD = "my_password";

    public static void main(String[] args) {

        initializeDatabase();

        Scanner scanner = new Scanner(System.in);
        // scanner to keep track of user input into console

        while (true) {
            System.out.println("\n=== Student Course Tracker ===");
            System.out.println("1. Enroll new student");
            System.out.println("2. Add new course");
            System.out.println("3. Enroll student in a course");
            System.out.println("4. View students in a course");
            System.out.println("5. View courses for a student on a specific day");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();
            // command line inpt options that execute the following methods based on command
            // line input (integers: 1-6)
            switch (choice) {
                case 1:
                    enrollNewStudent(scanner);
                    break;
                case 2:
                    addNewCourse(scanner);
                    break;
                case 3:
                    enrollStudentInCourse(scanner);
                    break;
                case 4:
                    viewStudentsInCourse(scanner);
                    break;
                case 5:
                    viewCoursesForStudentOnDay(scanner);
                    break;
                case 6:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice.  Try again.");
            }
        }
    }

    /**
     * This method initializes a database for command line user input
     */
    private static void initializeDatabase() {

        // connects to database with my DB information
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement statement = connection.createStatement()) {

            // Create SQL strings that create tables if they don't exist
            String createStudentsTable = "CREATE TABLE IF NOT EXISTS students (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50))";
            String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), day VARCHAR(50), time VARCHAR(50))";
            String createEnrollmentsTable = "CREATE TABLE IF NOT EXISTS enrollments (student_id INTEGER, course_id INTEGER, PRIMARY KEY (student_id, course_id))";

            // initialize all necessary tables if they do not already exist
            statement.executeUpdate(createStudentsTable);
            statement.executeUpdate(createCoursesTable);
            statement.executeUpdate(createEnrollmentsTable);

        }
        // catch any error that pertains to inability to connect to DB or any related
        // issue
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * enrolls a new student based upon a user specified input Student ID and
     * Student name
     * 
     * @param scanner
     */
    private static void enrollNewStudent(Scanner scanner) {
        System.out.println("Enter student ID:");
        int studentId = scanner.nextInt(); // take student ID input
        scanner.nextLine(); // get to the newline
        System.out.println("Enter student name:");
        String name = scanner.nextLine(); // take student name input

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection
                        .prepareStatement("INSERT INTO students (id, name) VALUES (?, ?)")) {

            // Check if the entered ID already exists in the table
            if (isIdAvailable(connection, studentId)) {
                preparedStatement.setInt(1, studentId);
                preparedStatement.setString(2, name);
                preparedStatement.executeUpdate();
                System.out.println("Student enrolled successfully. ID: " + studentId);
            } else {
                System.out.println("Student with ID " + studentId + " already exists. Please choose a different ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // print the full stack trace
        }
    }

    /**
     * helper method that determines if a user specified student ID is already in
     * use within the database (IDs must be unique, obviously)
     * 
     * @param connection - the connection to the database
     * @param studentId  - the studentID we want to consider for availability
     * @return - true if input ID is unique and available for use in database
     * @throws SQLException - if error for database access
     */
    private static boolean isIdAvailable(Connection connection, int studentId) throws SQLException {
        // generate SQL query (checkStatement) to see if input studentID exists
        try (PreparedStatement checkStatement = connection.prepareStatement("SELECT 1 FROM students WHERE id = ?")) {
            // 1 is replaced with the input studentID parameter
            checkStatement.setInt(1, studentId);
            // querry is executed
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                return !resultSet.next(); // Returns true if ID is available (not in use)
            }
        }
    }

    /**
     * adds new course into the database
     * 
     * @param scanner - user input
     */
    private static void addNewCourse(Scanner scanner) {
        // takes all necessary information one line at a time
        System.out.println("Enter course name:");
        String name = scanner.nextLine();
        System.out.println("Enter day of the week:");
        String day = scanner.nextLine();
        System.out.println("Enter time:");
        String time = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection
                        .prepareStatement("INSERT INTO courses (name, day, time) VALUES (?, ?, ?)")) {
            // use prep statement to throw name, day, and time into the insertion
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, day);
            preparedStatement.setString(3, time);
            preparedStatement.executeUpdate();
            System.out.println("Course added successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * enrolls an existing student in the database into an existing class
     * 
     * @param scanner - user input
     */
    private static void enrollStudentInCourse(Scanner scanner) {
        System.out.println("Enter student ID:");
        int studentId = scanner.nextInt();
        System.out.println("Enter course ID:");
        int courseId = scanner.nextInt();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection
                        .prepareStatement("INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)")) {

            preparedStatement.setInt(1, studentId);
            preparedStatement.setInt(2, courseId);
            preparedStatement.executeUpdate();
            System.out.println("Student enrolled in the course successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * this makes a short terminal list of all the students in a class
     * 
     * @param scanner - user input
     */
    private static void viewStudentsInCourse(Scanner scanner) {
        System.out.println("Enter course ID:");
        int courseId = scanner.nextInt();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT s.name FROM students s " +
                                "JOIN enrollments e ON s.id = e.student_id " +
                                "WHERE e.course_id = ?")) {

            preparedStatement.setInt(1, courseId);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println();
            System.out.println();
            System.out.println("Students in the course:");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * lets you view all the courses a student is taking on any given day via
     * command line list
     * 
     * @param scanner - user input
     */
    private static void viewCoursesForStudentOnDay(Scanner scanner) {
        System.out.println("Enter student ID:");
        int studentId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter day of the week:");
        String day = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT c.name, c.time FROM courses c " +
                                "JOIN enrollments e ON c.id = e.course_id " +
                                "WHERE e.student_id = ? AND c.day = ?")) {

            preparedStatement.setInt(1, studentId);
            preparedStatement.setString(2, day);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println();
            System.out.println();
            System.out.println("Courses for the student on " + day + ":");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name") + " at " + resultSet.getString("time"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
