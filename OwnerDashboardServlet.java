
package com.localbusiness.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;

@WebServlet("/owner")
public class OwnerDashboardServlet extends HttpServlet {

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

        HttpSession session = request.getSession(false);

        if (session == null ||
            !"OWNER".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        Integer ownerId = (Integer) session.getAttribute("userId");

        if (ownerId == null) {
            response.sendRedirect("login.html");
            return;
        }

        String ownerName =
            escape(String.valueOf(session.getAttribute("userName")));

        PrintWriter out = response.getWriter();

        int total = 0;
        int approved = 0;
        int pending = 0;
        int rejected = 0;

        StringBuilder rows = new StringBuilder();

        String sql =
            "SELECT id,business_name,address,phone,status " +
            "FROM businesses WHERE owner_id=? ORDER BY id DESC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, ownerId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        total++;

                        int id = rs.getInt("id");
                        String name = rs.getString("business_name");
                        String address = rs.getString("address");
                        String phone = rs.getString("phone");
                        String status = rs.getString("status");

                        if ("APPROVED".equalsIgnoreCase(status))
                            approved++;
                        else if ("PENDING".equalsIgnoreCase(status))
                            pending++;
                        else if ("REJECTED".equalsIgnoreCase(status))
                            rejected++;

                        String statusClass =
                            status == null ? "pending" :
                            status.toLowerCase();

                        rows.append("<tr>");

                        rows.append("<td>#").append(id).append("</td>");
                        rows.append("<td><b>")
                            .append(escape(name)).append("</b></td>");
                        rows.append("<td>")
                            .append(escape(address)).append("</td>");
                        rows.append("<td>")
                            .append(escape(phone)).append("</td>");

                        rows.append("<td><span class='status ")
                            .append(escape(statusClass)).append("'>")
                            .append(escape(status))
                            .append("</span></td>");

                        rows.append("</tr>");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            rows.append("<tr><td colspan='5'>")
                .append("Database error. Check Tomcat logs.")
                .append("</td></tr>");
        }

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Owner Dashboard | LocalHub</title>");

        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

        out.println("<style>");

        out.println("*{margin:0;padding:0;box-sizing:border-box;font-family:Inter,sans-serif}");
        out.println("body{background:#f4f6fb;color:#172c52}");

        out.println(".sidebar{position:fixed;left:0;top:0;width:245px;height:100vh;background:#142642;color:white;padding:30px 20px;display:flex;flex-direction:column;z-index:10}");
        out.println(".logo{font-size:26px;font-weight:800;margin:0 10px 45px;color:white;text-decoration:none}");
        out.println(".logo span{color:#ffa75c}");
        out.println(".menu-title{font-size:10px;color:#91a1b9;letter-spacing:2px;margin:0 12px 15px}");
        out.println(".menu a{display:flex;gap:13px;align-items:center;padding:14px 13px;color:#c7d2e2;text-decoration:none;border-radius:9px;margin-bottom:8px;font-size:13px;font-weight:600;transition:.3s}");
        out.println(".menu a:hover,.menu a.active{background:#294363;color:white}");
        out.println(".menu a i{width:20px;color:#ffa75c}");
        out.println(".sidebar-bottom{margin-top:auto;font-size:12px;color:#91a1b9;padding:15px 10px}");

        out.println(".main{margin-left:245px;padding:35px 4%;}");
        out.println(".topbar{display:flex;justify-content:space-between;align-items:center;gap:15px;margin-bottom:35px}");
        out.println(".topbar h1{font-size:27px;margin-bottom:8px}");
        out.println(".topbar p{font-size:13px;color:#8793a6}");
        out.println(".profile{background:white;padding:12px 18px;border-radius:10px;display:flex;align-items:center;gap:10px;box-shadow:0 3px 15px #17243b08;font-size:13px;font-weight:600}");
        out.println(".profile i{font-size:20px;color:#f28b35}");

        out.println(".welcome{background:linear-gradient(115deg,#172c52,#315a89);padding:32px;border-radius:17px;color:white;margin-bottom:30px;position:relative;overflow:hidden}");
        out.println(".welcome:after{content:'\\\\f54e';font-family:'Font Awesome 6 Free';font-weight:900;position:absolute;right:45px;top:25px;font-size:100px;color:#ffffff12}");
        out.println(".welcome h2{font-size:25px;margin-bottom:12px}");
        out.println(".welcome p{color:#d2deed;font-size:13px;line-height:1.8;margin-bottom:22px}");
        out.println(".btn{display:inline-block;padding:12px 18px;background:#f28b35;color:white;text-decoration:none;border-radius:8px;font-size:12px;font-weight:700;transition:.3s}");
        out.println(".btn:hover{background:#dc7020;transform:translateY(-3px)}");
        out.println(".btn-secondary{background:white;color:#172c52;margin-left:8px}");
        out.println(".btn-secondary:hover{background:#eaf0f8}");

        out.println(".section-title{font-size:19px;margin-bottom:20px}");
        out.println(".stats{display:grid;grid-template-columns:repeat(4,1fr);gap:18px;margin-bottom:35px}");
        out.println(".stat-card{background:white;padding:22px;border-radius:13px;box-shadow:0 4px 18px #17243b08;transition:.3s;border:1px solid #edf0f5}");
        out.println(".stat-card:hover{transform:translateY(-5px);box-shadow:0 12px 25px #17243b12}");
        out.println(".stat-icon{width:42px;height:42px;border-radius:11px;display:grid;place-items:center;margin-bottom:16px;font-size:17px}");
        out.println(".blue{background:#e8efff;color:#3769d5}");
        out.println(".green{background:#e5f7ed;color:#26965b}");
        out.println(".orange{background:#fff0e2;color:#e78b35}");
        out.println(".red{background:#ffeded;color:#db5555}");
        out.println(".stat-card h3{font-size:27px;margin-bottom:5px}");
        out.println(".stat-card p{font-size:12px;color:#8793a6}");

        out.println(".table-card{background:white;padding:25px;border-radius:15px;box-shadow:0 4px 18px #17243b08}");
        out.println(".table-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:22px;gap:10px}");
        out.println(".table-head h2{font-size:18px}");
        out.println(".table-wrap{overflow-x:auto}");
        out.println("table{width:100%;border-collapse:collapse;min-width:650px}");
        out.println("th{text-align:left;padding:15px 12px;background:#f5f7fb;color:#7b879a;font-size:11px;text-transform:uppercase;letter-spacing:.5px}");
        out.println("td{padding:16px 12px;border-bottom:1px solid #edf0f5;font-size:12px;color:#536078}");
        out.println("tr:last-child td{border-bottom:0}");
        out.println(".status{display:inline-block;padding:6px 11px;border-radius:20px;font-size:10px;font-weight:700}");
        out.println(".approved{background:#e5f7ed;color:#218a51}");
        out.println(".pending{background:#fff3df;color:#c47a17}");
        out.println(".rejected{background:#ffeded;color:#d34c4c}");

        out.println(".empty{text-align:center;padding:35px;color:#8994a6;font-size:13px}");
        out.println(".empty i{font-size:35px;color:#f28b35;margin-bottom:15px}");
        out.println(".footer{text-align:center;color:#9aa4b4;font-size:11px;margin-top:35px}");

        out.println("@media(max-width:1000px){.stats{grid-template-columns:repeat(2,1fr)}}");
        out.println("@media(max-width:700px){.sidebar{position:relative;width:100%;height:auto;padding:18px}.logo{margin:0 10px 15px}.menu-title,.sidebar-bottom{display:none}.menu{display:flex;flex-wrap:wrap}.menu a{padding:10px;font-size:11px;margin:3px}.main{margin-left:0;padding:25px 5%}.topbar{align-items:flex-start;flex-direction:column}.welcome:after{display:none}.stats{grid-template-columns:repeat(2,1fr)}}");
        out.println("@media(max-width:400px){.stats{grid-template-columns:1fr}.welcome{padding:23px}.welcome h2{font-size:21px}.btn{margin:5px 0}.btn-secondary{margin-left:0}}");

        out.println("</style></head><body>");

        out.println("<aside class='sidebar'>");
        out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
        out.println("<div class='menu-title'>OWNER MENU</div>");
        out.println("<div class='menu'>");
        out.println("<a class='active' href='owner'><i class='fas fa-chart-pie'></i> Dashboard</a>");
        out.println("<a href='my-businesses'><i class='fas fa-store'></i> My Businesses</a>");
        out.println("<a href='add-business.html'><i class='fas fa-plus-circle'></i> Add Business</a>");
        out.println("<a href='businesses'><i class='fas fa-compass'></i> Explore Listings</a>");
        out.println("<a href='index.html'><i class='fas fa-house'></i> Home Page</a>");
        out.println("<a href='logout'><i class='fas fa-right-from-bracket'></i> Logout</a>");
        out.println("</div>");
        out.println("<div class='sidebar-bottom'>© 2026 LocalHub</div>");
        out.println("</aside>");

        out.println("<main class='main'>");

        out.println("<div class='topbar'>");
        out.println("<div><h1>Owner Dashboard</h1>");
        out.println("<p>Manage your businesses and track their performance.</p></div>");
        out.println("<div class='profile'><i class='fas fa-circle-user'></i>");
        out.println(ownerName + "</div></div>");

        out.println("<section class='welcome'>");
        out.println("<h2>Welcome back, " + ownerName + "! 👋</h2>");
        out.println("<p>Manage your business listings, add new businesses and monitor approval status from one place.</p>");
        out.println("<a class='btn' href='add-business.html'><i class='fas fa-plus'></i> Add New Business</a>");
        out.println("<a class='btn btn-secondary' href='my-businesses'><i class='fas fa-store'></i> My Businesses</a>");
        out.println("</section>");

        out.println("<h2 class='section-title'>Business Overview</h2>");
        out.println("<section class='stats'>");

        out.println("<div class='stat-card'><div class='stat-icon blue'><i class='fas fa-store'></i></div><h3>" + total + "</h3><p>Total Businesses</p></div>");
        out.println("<div class='stat-card'><div class='stat-icon green'><i class='fas fa-circle-check'></i></div><h3>" + approved + "</h3><p>Approved Listings</p></div>");
        out.println("<div class='stat-card'><div class='stat-icon orange'><i class='fas fa-clock'></i></div><h3>" + pending + "</h3><p>Pending Approval</p></div>");
        out.println("<div class='stat-card'><div class='stat-icon red'><i class='fas fa-circle-xmark'></i></div><h3>" + rejected + "</h3><p>Rejected Listings</p></div>");

        out.println("</section>");

        out.println("<section class='table-card'>");
        out.println("<div class='table-head'><h2><i class='fas fa-list'></i> My Business Listings</h2>");
        out.println("<a class='btn' href='my-businesses'>Manage All</a></div>");

        out.println("<div class='table-wrap'><table>");
        out.println("<thead><tr><th>ID</th><th>Business Name</th><th>Address</th><th>Phone</th><th>Status</th></tr></thead>");
        out.println("<tbody>");

        if (total == 0) {
            out.println("<tr><td colspan='5'><div class='empty'><i class='fas fa-store'></i><br>");
            out.println("<b>No businesses added yet</b><br><br>");
            out.println("Start by adding your first business.</div></td></tr>");
        } else {
            out.println(rows.toString());
        }

        out.println("</tbody></table></div></section>");

        out.println("<div class='footer'>LocalHub Owner Dashboard · © 2026 All Rights Reserved</div>");
        out.println("</main></body></html>");
    }
}
