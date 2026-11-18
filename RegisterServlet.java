
package com.localbusiness.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/local_business_directory";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");

        if (name == null || email == null || password == null ||
            phone == null || name.isBlank() || email.isBlank() ||
            password.isBlank()) {

            showPage(response, false, "Missing Information",
                "Please fill all required registration fields.",
                "Try Again", "register.html");
            return;
        }

        if (!"USER".equals(role) && !"OWNER".equals(role)) {
            role = "USER";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String hashedPassword = hashPassword(password);

            String sql = "INSERT INTO users " +
                "(name, email, password_hash, phone, role) " +
                "VALUES (?, ?, ?, ?, ?)";

            try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name.trim());
                ps.setString(2, email.trim());
                ps.setString(3, hashedPassword);
                ps.setString(4, phone.trim());
                ps.setString(5, role);

                ps.executeUpdate();

                showPage(response, true,
                    "Registration Successful!",
                    "Welcome to LocalHub, " + name.trim() +
                    "! Your account has been created successfully. You can now login and explore local businesses.",
                    "Login Now", "login.html");
            }

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {

            showPage(response, false,
                "Email Already Registered",
                "An account with this email address already exists. Please login or use another email.",
                "Login Now", "login.html");

        } catch (Exception e) {

            e.printStackTrace();

            showPage(response, false,
                "Registration Failed",
                "Something went wrong while creating your account. Please try again.",
                "Try Again", "register.html");
        }
    }

    private void showPage(HttpServletResponse response,
                          boolean success,
                          String title,
                          String message,
                          String buttonText,
                          String buttonLink) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String color = success ? "#22a06b" : "#e45c5c";
        String icon = success ? "fa-check" : "fa-circle-exclamation";

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + escape(title) + " | LocalHub</title>");

        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

        out.println("<style>");

        out.println("*{margin:0;padding:0;box-sizing:border-box;font-family:Inter,Arial,sans-serif}");
        out.println("body{min-height:100vh;background:#f4f6fb;display:flex;flex-direction:column;color:#172c52}");

        out.println("nav{height:75px;background:white;display:flex;align-items:center;justify-content:space-between;padding:0 8%;box-shadow:0 3px 20px #17243b0c}");
        out.println(".logo{font-size:26px;font-weight:800;color:#172c52;text-decoration:none}");
        out.println(".logo span{color:#f28b35}");
        out.println("nav a:last-child{font-size:13px;color:#52627d;text-decoration:none;font-weight:600}");

        out.println(".main{flex:1;display:flex;align-items:center;justify-content:center;padding:45px 20px}");
        out.println(".card{background:white;width:100%;max-width:510px;text-align:center;padding:48px 38px;border-radius:20px;box-shadow:0 12px 40px #17243b10;border:1px solid #edf0f5;animation:appear .6s ease}");

        out.println(".icon{width:85px;height:85px;background:" + (success ? "#e5f7ed" : "#ffeded") + ";color:" + color + ";border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:36px;margin:0 auto 25px;animation:pop .5s ease}");
        out.println(".card h1{font-size:25px;font-weight:800;margin-bottom:13px;color:#172c52}");
        out.println(".card p{font-size:14px;color:#8290a5;line-height:1.9;margin-bottom:28px}");

        out.println(".btn{display:inline-flex;align-items:center;justify-content:center;gap:10px;padding:14px 25px;background:#f28b35;color:white;text-decoration:none;border-radius:9px;font-size:13px;font-weight:700;transition:.3s}");
        out.println(".btn:hover{background:#d96e20;transform:translateY(-3px);box-shadow:0 8px 20px #f28b3540}");

        out.println(".extra{margin-top:27px;padding-top:20px;border-top:1px solid #edf0f5;color:#9aa5b5;font-size:11px}");
        out.println(".extra i{color:#f28b35;margin-right:5px}");

        out.println("footer{text-align:center;padding:22px;color:#96a1b2;font-size:11px}");

        out.println("@keyframes appear{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}");
        out.println("@keyframes pop{0%{transform:scale(.5);opacity:0}100%{transform:scale(1);opacity:1}}");

        out.println("@media(max-width:500px){nav{padding:0 5%}.card{padding:38px 22px}.card h1{font-size:21px}.main{padding:25px 15px}}");

        out.println("</style></head><body>");

        out.println("<nav>");
        out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
        out.println("<a href='index.html'><i class='fa fa-home'></i> Home</a>");
        out.println("</nav>");

        out.println("<main class='main'>");
        out.println("<section class='card'>");

        out.println("<div class='icon'><i class='fas " + icon + "'></i></div>");

        out.println("<h1>" + escape(title) + "</h1>");
        out.println("<p>" + escape(message) + "</p>");

        out.println("<a class='btn' href='" + buttonLink + "'>");
        out.println(escape(buttonText) + " <i class='fa fa-arrow-right'></i>");
        out.println("</a>");

        out.println("<div class='extra'>");
        out.println("<i class='fa fa-shield-halved'></i>");
        out.println(" Your information is securely stored with LocalHub.");
        out.println("</div>");

        out.println("</section></main>");

        out.println("<footer>© 2026 LocalHub. Discover Local. Support Local.</footer>");

        out.println("</body></html>");
    }

    private String escape(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private String hashPassword(String password) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hash = md.digest(
            password.getBytes(StandardCharsets.UTF_8)
        );

        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }
}
