
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;

@WebServlet("/addBusiness")
public class AddBusinessServlet extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/local_business_directory";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private void showPage(HttpServletResponse response,
                          String title,
                          String message,
                          boolean success) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        String icon = success ? "fa-check" : "fa-circle-exclamation";
        String color = success ? "#22a06b" : "#e45c5c";
        String bg = success ? "#e5f7ed" : "#ffeded";

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + escape(title) + " | LocalHub</title>");

        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

        out.println("<style>");

        out.println("*{margin:0;padding:0;box-sizing:border-box;font-family:Inter,Arial,sans-serif}");
        out.println("body{min-height:100vh;background:#f4f6fb;color:#172c52;display:flex;flex-direction:column}");

        out.println("nav{height:75px;background:white;display:flex;align-items:center;justify-content:space-between;padding:0 8%;box-shadow:0 3px 20px #17243b0c}");
        out.println(".logo{font-size:26px;font-weight:800;color:#172c52;text-decoration:none}");
        out.println(".logo span{color:#f28b35}");
        out.println("nav a:last-child{font-size:13px;color:#536078;text-decoration:none;font-weight:600}");

        out.println(".main{flex:1;display:flex;align-items:center;justify-content:center;padding:45px 20px}");
        out.println(".card{background:white;width:100%;max-width:560px;text-align:center;padding:48px 38px;border-radius:20px;box-shadow:0 12px 40px #17243b10;border:1px solid #edf0f5;animation:appear .6s ease}");

        out.println(".icon{width:85px;height:85px;background:" + bg + ";color:" + color + ";border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:36px;margin:0 auto 25px;animation:pop .5s ease}");
        out.println(".card h1{font-size:25px;font-weight:800;margin-bottom:13px;color:#172c52}");
        out.println(".card p{font-size:14px;color:#8290a5;line-height:1.9;margin-bottom:20px}");

        out.println(".status{display:inline-flex;align-items:center;gap:8px;background:#fff2df;color:#c77b20;padding:10px 16px;border-radius:25px;font-size:11px;font-weight:800;margin-bottom:26px}");

        out.println(".buttons{display:flex;justify-content:center;gap:12px;flex-wrap:wrap}");
        out.println(".btn{display:inline-flex;align-items:center;justify-content:center;gap:9px;padding:14px 21px;background:#f28b35;color:white;text-decoration:none;border-radius:9px;font-size:12px;font-weight:700;transition:.3s}");
        out.println(".btn:hover{background:#d96e20;transform:translateY(-3px)}");
        out.println(".btn.secondary{background:#172c52}");
        out.println(".btn.secondary:hover{background:#254b7c}");

        out.println(".note{margin-top:27px;padding-top:20px;border-top:1px solid #edf0f5;color:#9aa5b5;font-size:11px;line-height:1.8}");
        out.println("footer{text-align:center;padding:22px;color:#96a1b2;font-size:11px}");

        out.println("@keyframes appear{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}");
        out.println("@keyframes pop{0%{transform:scale(.5);opacity:0}100%{transform:scale(1);opacity:1}}");

        out.println("@media(max-width:500px){nav{padding:0 5%}.card{padding:38px 22px}.card h1{font-size:21px}.main{padding:25px 15px}.buttons{flex-direction:column}.btn{width:100%}}");

        out.println("</style></head><body>");

        out.println("<nav>");
        out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
        out.println("<a href='home'><i class='fa fa-house'></i> Dashboard</a>");
        out.println("</nav>");

        out.println("<main class='main'><section class='card'>");

        out.println("<div class='icon'><i class='fas " + icon + "'></i></div>");
        out.println("<h1>" + escape(title) + "</h1>");
        out.println("<p>" + escape(message) + "</p>");

        if (success) {
            out.println("<div class='status'><i class='fa fa-clock'></i> PENDING APPROVAL</div>");
        }

        out.println("<div class='buttons'>");

        if (success) {
            out.println("<a class='btn' href='my-businesses'><i class='fa fa-briefcase'></i> My Businesses</a>");
            out.println("<a class='btn secondary' href='home'><i class='fa fa-house'></i> Dashboard</a>");
        } else {
            out.println("<a class='btn' href='add-business.html'><i class='fa fa-rotate-right'></i> Try Again</a>");
            out.println("<a class='btn secondary' href='home'>Dashboard</a>");
        }

        out.println("</div>");

        out.println("<div class='note'><i class='fa fa-shield-halved'></i> Your business listing will become visible to customers after admin approval.</div>");

        out.println("</section></main>");

        out.println("<footer>© 2026 LocalHub. Discover Local. Support Local.</footer>");

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        if (!"OWNER".equals(session.getAttribute("role"))) {
            response.sendError(403, "Only business owners can add businesses.");
            return;
        }

        int ownerId = Integer.parseInt(
            session.getAttribute("userId").toString()
        );

        try {
            int categoryId =
                Integer.parseInt(request.getParameter("categoryId"));

            String businessName = request.getParameter("businessName");
            String address = request.getParameter("address");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");
            String description = request.getParameter("description");

            if (businessName == null || businessName.isBlank() ||
                address == null || address.isBlank()) {

                showPage(response, "Missing Information",
                    "Please enter your business name and address.", false);
                return;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql = "INSERT INTO businesses " +
                "(owner_id, category_id, business_name, address, phone, email, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";

            try (Connection con =
                DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, ownerId);
                ps.setInt(2, categoryId);
                ps.setString(3, businessName.trim());
                ps.setString(4, address.trim());
                ps.setString(5, phone);
                ps.setString(6, email);
                ps.setString(7, description);

                int inserted = ps.executeUpdate();

                if (inserted > 0) {
                    showPage(response,
                        "Business Submitted Successfully!",
                        "Your business, " + businessName.trim() +
                        ", has been submitted successfully. It is now waiting for admin review.",
                        true);
                } else {
                    showPage(response, "Submission Failed",
                        "Your business could not be submitted. Please try again.", false);
                }
            }

        } catch (NumberFormatException e) {
            showPage(response, "Invalid Category",
                "Please select a valid business category.", false);

        } catch (SQLException e) {
            e.printStackTrace();
            showPage(response, "Database Error",
                "Unable to save your business. Please check your information and try again.", false);

        } catch (Exception e) {
            e.printStackTrace();
            showPage(response, "Something Went Wrong",
                "Your business could not be submitted. Please try again.", false);
        }
    }
}
