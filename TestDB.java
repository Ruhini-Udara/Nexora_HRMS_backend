import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/hrms_db";
        Connection conn = DriverManager.getConnection(url, "postgres", "postgres");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM employee WHERE id = 26");
        if (rs.next()) {
            System.out.println("EMPLOYEE 26 EXISTS!");
        } else {
            System.out.println("EMPLOYEE 26 DOES NOT EXIST!");
        }
    }
}
