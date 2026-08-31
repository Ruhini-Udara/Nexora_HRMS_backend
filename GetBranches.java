import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetBranches {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
        String user = "postgres.uxvsqjektsssfvsfprtd";
        String password = "1ZThqfcXko1GsU7o";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT branch FROM employee WHERE branch IS NOT NULL AND branch != ''");
            System.out.println("Current branches in database:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("branch"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
