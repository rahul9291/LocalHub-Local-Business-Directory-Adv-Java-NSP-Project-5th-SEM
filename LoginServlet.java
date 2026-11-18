
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/local_business_directory";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String sql = "SELECT id, name, role FROM users " +
                     "WHERE email=? AND password_hash=?";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, hashPassword(password));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        HttpSession session = request.getSession();
                        session.setAttribute("userId", rs.getInt("id"));
                        session.setAttribute("userName", rs.getString("name"));
                        session.setAttribute("role", rs.getString("role"));

                       response.sendRedirect("home");
                    } else {
                        response.getWriter().println(
                            "Invalid email or password. <a href='login.html'>Try again</a>"
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Login error. Check server logs.");
        }
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
