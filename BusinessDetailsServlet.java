
package com.localbusiness.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;

@WebServlet("/business-details")
public class BusinessDetailsServlet extends HttpServlet {

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

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql =
                "SELECT b.business_name, b.address, b.phone, b.email, " +
                "b.description, c.category_name " +
                "FROM businesses b " +
                "LEFT JOIN categories c ON b.category_id=c.id " +
                "WHERE b.id=? AND b.status='APPROVED'";

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        response.sendError(404, "Business not found.");
                        return;
                    }

                    String name = escape(rs.getString("business_name"));
                    String category = escape(rs.getString("category_name"));
                    String address = escape(rs.getString("address"));
                    String phone = escape(rs.getString("phone"));
                    String email = escape(rs.getString("email"));
                    String description = escape(rs.getString("description"));

                    out.println("<!DOCTYPE html><html lang='en'><head>");
                    out.println("<meta charset='UTF-8'>");
                    out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                    out.println("<title>" + name + " | LocalHub</title>");

                    out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
                    out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

                    out.println("<style>");
                    out.println("*{margin:0;padding:0;box-sizing:border-box;font-family:Inter,sans-serif}");
                    out.println("body{background:#f5f7fb;color:#172c52}");
                    out.println("nav{background:white;padding:20px 8%;display:flex;justify-content:space-between;align-items:center;box-shadow:0 3px 15px #17243b0c}");
                    out.println(".logo{font-size:25px;font-weight:800;color:#172c52;text-decoration:none}");
                    out.println(".logo span{color:#f28b35}");
                    out.println("nav a.back{color:#536078;text-decoration:none;font-size:14px;font-weight:600}");
                    out.println(".hero{background:linear-gradient(120deg,#142846,#315783);padding:75px 8%;color:white}");
                    out.println(".hero small{color:#ffb36d;font-weight:700;letter-spacing:2px}");
                    out.println(".hero h1{font-size:40px;margin:18px 0}");
                    out.println(".hero .tag{display:inline-block;background:#ffffff20;padding:9px 16px;border-radius:25px;font-size:13px}");
                    out.println(".container{max-width:1000px;margin:45px auto;padding:0 20px}");
                    out.println(".grid{display:grid;grid-template-columns:1.5fr 1fr;gap:25px}");
                    out.println(".card{background:white;padding:30px;border-radius:15px;box-shadow:0 5px 25px #17243b0a;border:1px solid #e9edf4}");
                    out.println(".card h2{font-size:20px;margin-bottom:22px}");
                    out.println(".description{font-size:14px;color:#78849a;line-height:1.9;white-space:pre-wrap}");
                    out.println(".info{display:flex;gap:14px;align-items:flex-start;margin:22px 0;color:#68758c;font-size:13px;line-height:1.7;overflow-wrap:anywhere}");
                    out.println(".info i{width:38px;height:38px;flex-shrink:0;background:#fff1e7;color:#f28b35;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:15px}");
                    out.println(".info strong{display:block;color:#172c52;margin-bottom:3px}");
                    out.println(".buttons{display:flex;gap:12px;margin-top:25px;flex-wrap:wrap}");
                    out.println(".btn{padding:13px 19px;background:#f28b35;color:white;text-decoration:none;border-radius:8px;font-size:13px;font-weight:700}");
                    out.println(".btn:hover{background:#d96e20}");
                    out.println(".btn.secondary{background:#172c52}");
                    out.println(".footer{text-align:center;background:#101e38;color:#bdc9da;padding:28px;font-size:13px;margin-top:55px}");
                    out.println("@media(max-width:700px){.grid{grid-template-columns:1fr}.hero h1{font-size:30px}nav{padding:18px 5%}.hero{padding:55px 6%}}");
                    out.println("</style></head><body>");

                    out.println("<nav>");
                    out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
                    out.println("<a class='back' href='businesses'><i class='fa fa-arrow-left'></i> Back to Businesses</a>");
                    out.println("</nav>");

                    out.println("<section class='hero'>");
                    out.println("<small>LOCALHUB BUSINESS PROFILE</small>");
                    out.println("<h1>" + name + "</h1>");
                    out.println("<span class='tag'><i class='fa fa-store'></i> " + category + "</span>");
                    out.println("</section>");

                    out.println("<main class='container'><div class='grid'>");

                    out.println("<section class='card'>");
                    out.println("<h2><i class='fa fa-circle-info' style='color:#f28b35'></i> About This Business</h2>");
                    out.println("<p class='description'>" +
                        (description.isEmpty() ? "No description available." : description) +
                        "</p>");
                    out.println("<div class='buttons'>");
                    out.println("<a class='btn' href='businesses'><i class='fa fa-search'></i> Explore More</a>");
                    out.println("<a class='btn secondary' href='index.html'>Home</a>");
                    out.println("</div></section>");

                    out.println("<aside class='card'>");
                    out.println("<h2>Contact Information</h2>");

                    out.println("<div class='info'><i class='fa fa-location-dot'></i><div><strong>Address</strong>" +
                        (address.isEmpty() ? "Not provided" : address) + "</div></div>");

                    out.println("<div class='info'><i class='fa fa-phone'></i><div><strong>Phone</strong>" +
                        (phone.isEmpty() ? "Not provided" : phone) + "</div></div>");

                    out.println("<div class='info'><i class='fa fa-envelope'></i><div><strong>Email</strong>" +
                        (email.isEmpty() ? "Not provided" : email) + "</div></div>");

                    out.println("</aside></div></main>");

                    out.println("<footer class='footer'>© 2026 LocalHub. Discover Local. Support Local.</footer>");
                    out.println("</body></html>");
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid business ID.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to load business details.");
        }
    }
}
