import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetTransferBranches {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
        String user = "postgres.uxvsqjektsssfvsfprtd";
        String password = "1ZThqfcXko1GsU7o";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT current_branch FROM transfer_request WHERE current_branch IS NOT NULL AND current_branch != '' UNION SELECT DISTINCT target_branch FROM transfer_request WHERE target_branch IS NOT NULL AND target_branch != ''");
            System.out.println("Branches in transfer_request:");
            while (rs.next()) {
                System.out.println("- " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
