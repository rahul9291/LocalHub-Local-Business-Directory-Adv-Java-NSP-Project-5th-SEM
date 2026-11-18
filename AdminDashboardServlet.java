
package com.localbusiness.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

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
            !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        PrintWriter out = response.getWriter();

        int total = 0;
        int pending = 0;
        int approved = 0;
        int rejected = 0;

        StringBuilder rows = new StringBuilder();

        String sql =
            "SELECT id,business_name,address,phone,status " +
            "FROM businesses ORDER BY id DESC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    total++;

                    int id = rs.getInt("id");
                    String name = rs.getString("business_name");
                    String address = rs.getString("address");
                    String phone = rs.getString("phone");
                    String status = rs.getString("status");

                    if ("PENDING".equalsIgnoreCase(status))
                        pending++;
                    else if ("APPROVED".equalsIgnoreCase(status))
                        approved++;
                    else if ("REJECTED".equalsIgnoreCase(status))
                        rejected++;

                    String statusClass =
                        status == null ? "pending" : status.toLowerCase();

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
                        .append(escape(status)).append("</span></td>");

                    rows.append("<td>");

                    if ("PENDING".equalsIgnoreCase(status)) {

                        rows.append("<form method='post' action='admin' class='actions'>");

                        rows.append("<input type='hidden' name='id' value='")
                            .append(id).append("'>");

                        rows.append("<button class='approve' name='action' value='APPROVED'>")
                            .append("<i class='fas fa-check'></i> Approve</button>");

                        rows.append("<button class='reject' name='action' value='REJECTED'>")
                            .append("<i class='fas fa-xmark'></i> Reject</button>");

                        rows.append("</form>");

                    } else {
                        rows.append("<span class='no-action'>Completed</span>");
                    }

                    rows.append("</td></tr>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            rows.append("<tr><td colspan='6'>Database error. Check Tomcat logs.</td></tr>");
        }

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Admin Dashboard | LocalHub</title>");

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

        out.println(".main{margin-left:245px;padding:35px 4%}");
        out.println(".topbar{display:flex;justify-content:space-between;align-items:center;gap:15px;margin-bottom:30px}");
        out.println(".topbar h1{font-size:27px;margin-bottom:8px}");
        out.println(".topbar p{font-size:13px;color:#8793a6}");
        out.println(".profile{background:white;padding:12px 18px;border-radius:10px;display:flex;align-items:center;gap:10px;box-shadow:0 3px 15px #17243b08;font-size:13px;font-weight:600}");
        out.println(".profile i{font-size:20px;color:#f28b35}");

        out.println(".welcome{background:linear-gradient(115deg,#172c52,#315a89);padding:30px;border-radius:17px;color:white;margin-bottom:30px}");
        out.println(".welcome h2{font-size:24px;margin-bottom:10px}");
        out.println(".welcome p{color:#d2deed;font-size:13px;line-height:1.8}");

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
        out.println(".table-head{display:flex;justify-content:space-between;align-items:center;gap:15px;margin-bottom:22px;flex-wrap:wrap}");
        out.println(".table-head h2{font-size:18px}");
        out.println(".filters{display:flex;gap:10px;flex-wrap:wrap}");
        out.println(".filters input,.filters select{padding:11px 13px;border:1px solid #e3e8f0;border-radius:8px;outline:none;font-size:12px;background:white;color:#536078}");
        out.println(".filters input:focus,.filters select:focus{border-color:#f28b35}");
        out.println(".table-wrap{overflow-x:auto}");
        out.println("table{width:100%;border-collapse:collapse;min-width:750px}");
        out.println("th{text-align:left;padding:15px 12px;background:#f5f7fb;color:#7b879a;font-size:11px;text-transform:uppercase;letter-spacing:.5px}");
        out.println("td{padding:16px 12px;border-bottom:1px solid #edf0f5;font-size:12px;color:#536078}");
        out.println("tr:last-child td{border-bottom:0}");
        out.println(".status{display:inline-block;padding:6px 11px;border-radius:20px;font-size:10px;font-weight:700}");
        out.println(".approved{background:#e5f7ed;color:#218a51}");
        out.println(".pending{background:#fff3df;color:#c47a17}");
        out.println(".rejected{background:#ffeded;color:#d34c4c}");

        out.println(".actions{display:flex;gap:7px}");
        out.println(".actions button{border:0;padding:9px 11px;border-radius:7px;color:white;font-size:11px;font-weight:600;cursor:pointer;transition:.3s}");
        out.println(".approve{background:#23965c}");
        out.println(".reject{background:#dc5555}");
        out.println(".actions button:hover{transform:translateY(-2px);filter:brightness(.92)}");
        out.println(".no-action{color:#98a2b1;font-size:11px}");
        out.println(".empty{text-align:center;padding:35px;color:#8994a6;font-size:13px}");
        out.println(".footer{text-align:center;color:#9aa4b4;font-size:11px;margin-top:35px}");

        out.println("@media(max-width:1000px){.stats{grid-template-columns:repeat(2,1fr)}}");
        out.println("@media(max-width:700px){.sidebar{position:relative;width:100%;height:auto;padding:18px}.logo{margin:0 10px 15px}.menu-title,.sidebar-bottom{display:none}.menu{display:flex;flex-wrap:wrap}.menu a{padding:10px;font-size:11px;margin:3px}.main{margin-left:0;padding:25px 5%}.topbar{align-items:flex-start;flex-direction:column}.welcome{padding:23px}.table-card{padding:16px}}");
        out.println("@media(max-width:400px){.stats{grid-template-columns:1fr}}");

        out.println("</style></head><body>");

        out.println("<aside class='sidebar'>");
        out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
        out.println("<div class='menu-title'>ADMIN MENU</div>");
        out.println("<div class='menu'>");
        out.println("<a href='admin' class='active'><i class='fas fa-chart-pie'></i> Dashboard</a>");
        out.println("<a href='businesses'><i class='fas fa-store'></i> All Businesses</a>");
        out.println("<a href='index.html'><i class='fas fa-house'></i> Home Page</a>");
        out.println("<a href='logout'><i class='fas fa-right-from-bracket'></i> Logout</a>");
        out.println("</div>");
        out.println("<div class='sidebar-bottom'>© 2026 LocalHub</div>");
        out.println("</aside>");

        out.println("<main class='main'>");

        out.println("<div class='topbar'>");
        out.println("<div><h1>Admin Dashboard</h1>");
        out.println("<p>Monitor businesses and manage listing approvals.</p></div>");
        out.println("<div class='profile'><i class='fas fa-user-shield'></i> Administrator</div>");
        out.println("</div>");

        out.println("<section class='welcome'>");
        out.println("<h2>Welcome to LocalHub Admin Panel 👋</h2>");
        out.println("<p>Manage business listings, review pending submissions and keep the directory up to date.</p>");
        out.println("</section>");

        out.println("<section class='stats'>");

        out.println("<div class='stat-card'><div class='stat-icon blue'><i class='fas fa-store'></i></div><h3>" + total + "</h3><p>Total Businesses</p></div>");
        out.println("<div class='stat-card'><div class='stat-icon orange'><i class='fas fa-clock'></i></div><h3>" + pending + "</h3><p>Pending Approval</p></div>");
        out.println("<div class='stat-card'><div class='stat-icon green'><i class='fas fa-circle-check'></i></div><h3>" + approved + "</h3><p>Approved Businesses</p></div>");
        out.println("<div class='stat-card'><div class='stat-icon red'><i class='fas fa-circle-xmark'></i></div><h3>" + rejected + "</h3><p>Rejected Businesses</p></div>");

        out.println("</section>");

        out.println("<section class='table-card'>");

        out.println("<div class='table-head'>");
        out.println("<h2><i class='fas fa-list-check'></i> Manage Business Listings</h2>");

        out.println("<div class='filters'>");
        out.println("<input type='text' id='searchInput' placeholder='Search businesses...'>");
        out.println("<select id='statusFilter'>");
        out.println("<option value='all'>All Status</option>");
        out.println("<option value='pending'>Pending</option>");
        out.println("<option value='approved'>Approved</option>");
        out.println("<option value='rejected'>Rejected</option>");
        out.println("</select></div></div>");

        out.println("<div class='table-wrap'><table id='businessTable'>");
        out.println("<thead><tr><th>ID</th><th>Business Name</th><th>Address</th><th>Phone</th><th>Status</th><th>Action</th></tr></thead>");
        out.println("<tbody>");

        if (total == 0) {
            out.println("<tr><td colspan='6'><div class='empty'><i class='fas fa-store'></i><br>No business listings found.</div></td></tr>");
        } else {
            out.println(rows.toString());
        }

        out.println("</tbody></table></div>");
        out.println("</section>");

        out.println("<div class='footer'>LocalHub Admin Dashboard · © 2026 All Rights Reserved</div>");
        out.println("</main>");

        out.println("<script>");
        out.println("const search=document.getElementById('searchInput');");
        out.println("const filter=document.getElementById('statusFilter');");
        out.println("function applyFilters(){");
        out.println("const q=search.value.toLowerCase().trim();");
        out.println("const s=filter.value;");
        out.println("document.querySelectorAll('#businessTable tbody tr').forEach(row=>{");
        out.println("const text=row.innerText.toLowerCase();");
        out.println("const status=row.querySelector('.status');");
        out.println("const statusText=status?status.innerText.toLowerCase():'';");
        out.println("row.style.display=text.includes(q)&&(s==='all'||statusText===s)?'':'none';");
        out.println("});}");
        out.println("search.addEventListener('input',applyFilters);");
        out.println("filter.addEventListener('change',applyFilters);");
        out.println("</script>");

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String action = request.getParameter("action");

            if (!"APPROVED".equals(action) &&
                !"REJECTED".equals(action)) {
                response.sendError(400, "Invalid action");
                return;
            }

            String sql =
                "UPDATE businesses SET status=? WHERE id=? AND status='PENDING'";

            try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, action);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            response.sendRedirect("admin");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to update business");
        }
    }
}
