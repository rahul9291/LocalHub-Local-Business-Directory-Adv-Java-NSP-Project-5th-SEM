
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

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

    private int getCount(String sql, Integer ownerId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                if (ownerId != null) {
                    ps.setInt(1, ownerId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String name = (String) session.getAttribute("userName");
        String role = (String) session.getAttribute("role");

        if (name == null) name = "User";
        if (role == null) role = "USER";

        int userId = Integer.parseInt(
            session.getAttribute("userId").toString()
        );

        String safeName = escape(name);
        String safeRole = escape(role);

        Integer ownerId = "OWNER".equals(role) ? userId : null;

        String baseSql = "SELECT COUNT(*) FROM businesses";
        String filter = ownerId != null ? " WHERE owner_id=?" : "";

        int total = getCount(baseSql + filter, ownerId);
        int pending = getCount(
            baseSql + " WHERE status='PENDING'" +
            (ownerId != null ? " AND owner_id=?" : ""), ownerId);
        int approved = getCount(
            baseSql + " WHERE status='APPROVED'" +
            (ownerId != null ? " AND owner_id=?" : ""), ownerId);
        int rejected = getCount(
            baseSql + " WHERE status='REJECTED'" +
            (ownerId != null ? " AND owner_id=?" : ""), ownerId);

        int users = getCount(
            "SELECT COUNT(*) FROM users", null);

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Dashboard | LocalHub</title>");

        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

        out.println("<style>");

        out.println("*{margin:0;padding:0;box-sizing:border-box;font-family:Inter,Arial,sans-serif}");
        out.println("body{background:#f4f6fb;color:#182641}");

        out.println(".sidebar{position:fixed;left:0;top:0;width:245px;height:100vh;background:#101e38;padding:28px 18px;color:white;display:flex;flex-direction:column;z-index:10}");
        out.println(".logo{font-size:26px;font-weight:800;color:white;text-decoration:none;padding:0 12px;margin-bottom:42px}");
        out.println(".logo span{color:#f28b35}");
        out.println(".menu-label{font-size:10px;color:#8999b2;letter-spacing:2px;font-weight:700;padding:0 13px;margin-bottom:15px}");
        out.println(".menu a{display:flex;align-items:center;gap:13px;padding:14px 13px;color:#b9c7db;text-decoration:none;font-size:13px;font-weight:600;border-radius:9px;margin:5px 0;transition:.25s}");
        out.println(".menu a i{width:18px;text-align:center;font-size:15px}");
        out.println(".menu a:hover,.menu a.active{background:#233a5c;color:white}");
        out.println(".menu a.active{border-left:3px solid #f28b35}");
        out.println(".sidebar-bottom{margin-top:auto;border-top:1px solid #293b56;padding-top:18px}");

        out.println(".main{margin-left:245px;padding:30px 4%;min-height:100vh}");
        out.println(".topbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:35px;gap:15px}");
        out.println(".topbar h2{font-size:21px;font-weight:800}");
        out.println(".topbar p{font-size:12px;color:#8a96aa;margin-top:6px}");
        out.println(".profile{display:flex;align-items:center;gap:12px;background:white;padding:10px 15px;border-radius:12px;box-shadow:0 3px 15px #17243b08}");
        out.println(".avatar{width:39px;height:39px;background:#fff0e4;color:#ed8537;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:800}");
        out.println(".profile strong{font-size:12px;display:block}");
        out.println(".profile small{color:#8c97a9;font-size:10px}");

        out.println(".welcome{background:linear-gradient(115deg,#142846,#315783);padding:32px;border-radius:17px;color:white;display:flex;align-items:center;justify-content:space-between;gap:20px;margin-bottom:30px;overflow:hidden;position:relative}");
        out.println(".welcome small{color:#ffb36d;font-size:11px;font-weight:700;letter-spacing:2px}");
        out.println(".welcome h1{font-size:27px;margin:13px 0 9px}");
        out.println(".welcome p{color:#d3dfed;font-size:13px;line-height:1.7}");
        out.println(".welcome-icon{font-size:76px;color:#ffffff20;padding:15px}");

        out.println(".section-title{font-size:17px;font-weight:800;margin-bottom:18px}");
        out.println(".stats{display:grid;grid-template-columns:repeat(4,1fr);gap:17px;margin-bottom:35px}");
        out.println(".stat{background:white;border:1px solid #edf0f5;border-radius:13px;padding:21px;box-shadow:0 4px 18px #17243b06;transition:.25s}");
        out.println(".stat:hover{transform:translateY(-5px);box-shadow:0 10px 25px #17243b12}");
        out.println(".stat-top{display:flex;justify-content:space-between;align-items:center}");
        out.println(".stat-icon{width:42px;height:42px;border-radius:11px;display:flex;align-items:center;justify-content:center;font-size:17px}");
        out.println(".orange{background:#fff0e4;color:#ef8536}.blue{background:#eaf1ff;color:#4777dc}.green{background:#e6f7ef;color:#269b67}.red{background:#ffeded;color:#db5b5b}");
        out.println(".stat h3{font-size:27px;margin:17px 0 5px}");
        out.println(".stat p{font-size:11px;color:#8b97a9;font-weight:600}");

        out.println(".content-grid{display:grid;grid-template-columns:1.4fr 1fr;gap:20px}");
        out.println(".panel{background:white;padding:25px;border:1px solid #edf0f5;border-radius:14px;box-shadow:0 4px 18px #17243b06}");
        out.println(".panel h3{font-size:16px;margin-bottom:10px}");
        out.println(".panel p{font-size:12px;color:#8491a5;line-height:1.8}");
        out.println(".actions{display:grid;grid-template-columns:repeat(2,1fr);gap:13px;margin-top:20px}");
        out.println(".action{padding:19px 13px;border:1px solid #edf0f5;border-radius:11px;text-decoration:none;color:#263854;transition:.25s}");
        out.println(".action:hover{border-color:#f28b35;background:#fffaf6;transform:translateY(-3px)}");
        out.println(".action i{display:block;color:#f28b35;font-size:21px;margin-bottom:13px}");
        out.println(".action strong{font-size:12px;display:block}");
        out.println(".action small{font-size:10px;color:#8b97a9;display:block;margin-top:6px}");

        out.println(".role-tag{display:inline-block;background:#fff0e4;color:#e67c2d;padding:7px 13px;border-radius:20px;font-size:10px;font-weight:800;margin-top:14px}");
        out.println(".footer{margin-top:35px;text-align:center;color:#9aa5b5;font-size:11px}");

        out.println("@media(max-width:1050px){.stats{grid-template-columns:repeat(2,1fr)}.content-grid{grid-template-columns:1fr}}");
        out.println("@media(max-width:650px){.sidebar{width:68px;padding:25px 9px}.logo{font-size:0;padding:0;text-align:center}.logo span{font-size:20px}.menu-label{display:none}.menu a{justify-content:center;padding:15px 5px;font-size:0}.menu a i{font-size:17px}.sidebar-bottom .menu a{font-size:0}.main{margin-left:68px;padding:22px 13px}.topbar h2{font-size:17px}.profile{padding:7px}.profile div:last-child{display:none}.welcome{padding:23px}.welcome h1{font-size:21px}.welcome-icon{display:none}.stats{gap:10px}.stat{padding:14px}.stat h3{font-size:23px}.actions{grid-template-columns:1fr 1fr}}");

        out.println("</style></head><body>");

        // Sidebar
        out.println("<aside class='sidebar'>");
        out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
        out.println("<div class='menu-label'>MAIN MENU</div>");
        out.println("<div class='menu'>");
        out.println("<a class='active' href='home'><i class='fa fa-house'></i> Dashboard</a>");

        if ("ADMIN".equals(role)) {
            out.println("<a href='admin'><i class='fa fa-shield-halved'></i> Manage Businesses</a>");
            out.println("<a href='businesses'><i class='fa fa-store'></i> Explore Businesses</a>");
        } else if ("OWNER".equals(role)) {
            out.println("<a href='add-business.html'><i class='fa fa-plus-circle'></i> Add Business</a>");
            out.println("<a href='my-businesses'><i class='fa fa-briefcase'></i> My Businesses</a>");
            out.println("<a href='businesses'><i class='fa fa-store'></i> Explore Businesses</a>");
        } else {
            out.println("<a href='businesses'><i class='fa fa-store'></i> Browse Businesses</a>");
        }

        out.println("<a href='index.html'><i class='fa fa-globe'></i> Visit Website</a>");
        out.println("</div>");

        out.println("<div class='sidebar-bottom menu'>");
        out.println("<a href='logout'><i class='fa fa-right-from-bracket'></i> Logout</a>");
        out.println("</div></aside>");

        // Main area
        out.println("<main class='main'>");

        out.println("<div class='topbar'>");
        out.println("<div><h2>Dashboard Overview</h2><p>Welcome to your LocalHub workspace</p></div>");
        out.println("<div class='profile'><div class='avatar'>" +
            escape(name.substring(0, 1).toUpperCase()) +
            "</div><div><strong>" + safeName +
            "</strong><small>" + safeRole + " ACCOUNT</small></div></div>");
        out.println("</div>");

        out.println("<section class='welcome'>");
        out.println("<div><small>LOCALHUB WORKSPACE</small>");
        out.println("<h1>Welcome back, " + safeName + "!</h1>");

        if ("ADMIN".equals(role)) {
            out.println("<p>Manage your platform, review listings and keep the local business community growing.</p>");
        } else if ("OWNER".equals(role)) {
            out.println("<p>Manage your business listings and connect with more local customers.</p>");
        } else {
            out.println("<p>Discover great local businesses and explore your neighbourhood.</p>");
        }

        out.println("<span class='role-tag'>" + safeRole + " DASHBOARD</span></div>");
        out.println("<div class='welcome-icon'><i class='fa fa-chart-line'></i></div>");
        out.println("</section>");

        out.println("<div class='section-title'>Dashboard Statistics</div>");
        out.println("<section class='stats'>");

        out.println("<div class='stat'><div class='stat-top'><span class='stat-icon orange'><i class='fa fa-store'></i></span></div><h3>" + total + "</h3><p>" + ("OWNER".equals(role) ? "MY BUSINESSES" : "TOTAL BUSINESSES") + "</p></div>");

        out.println("<div class='stat'><div class='stat-top'><span class='stat-icon blue'><i class='fa fa-clock'></i></span></div><h3>" + pending + "</h3><p>PENDING APPROVAL</p></div>");

        out.println("<div class='stat'><div class='stat-top'><span class='stat-icon green'><i class='fa fa-circle-check'></i></span></div><h3>" + approved + "</h3><p>APPROVED BUSINESSES</p></div>");

        out.println("<div class='stat'><div class='stat-top'><span class='stat-icon red'><i class='fa fa-ban'></i></span></div><h3>" + rejected + "</h3><p>REJECTED BUSINESSES</p></div>");

        out.println("</section>");

        out.println("<section class='content-grid'>");

        out.println("<div class='panel'><h3>Quick Actions</h3>");
        out.println("<p>Choose an action to continue working on LocalHub.</p>");
        out.println("<div class='actions'>");

        if ("ADMIN".equals(role)) {
            out.println("<a class='action' href='admin'><i class='fa fa-clipboard-check'></i><strong>Manage Businesses</strong><small>Review and approve listings</small></a>");
            out.println("<a class='action' href='businesses'><i class='fa fa-store'></i><strong>Explore Businesses</strong><small>View public listings</small></a>");
        } else if ("OWNER".equals(role)) {
            out.println("<a class='action' href='add-business.html'><i class='fa fa-plus-circle'></i><strong>Add Business</strong><small>Create a new listing</small></a>");
            out.println("<a class='action' href='my-businesses'><i class='fa fa-briefcase'></i><strong>My Businesses</strong><small>Manage your listings</small></a>");
        } else {
            out.println("<a class='action' href='businesses'><i class='fa fa-magnifying-glass'></i><strong>Browse Businesses</strong><small>Discover local services</small></a>");
            out.println("<a class='action' href='index.html'><i class='fa fa-house'></i><strong>Home Page</strong><small>Visit LocalHub website</small></a>");
        }

        out.println("</div></div>");

        out.println("<div class='panel'><h3>Account Information</h3>");
        out.println("<p>Your account is active on LocalHub.</p>");
        out.println("<div style='margin-top:20px;padding:15px;background:#f7f8fb;border-radius:10px'>");
        out.println("<p><strong style='color:#172c52'>Name:</strong> " + safeName + "</p>");
        out.println("<p><strong style='color:#172c52'>Role:</strong> " + safeRole + "</p>");
        out.println("<p><strong style='color:#172c52'>Account ID:</strong> " + userId + "</p>");
        if ("ADMIN".equals(role)) {
            out.println("<p><strong style='color:#172c52'>Registered Users:</strong> " + users + "</p>");
        }
        out.println("</div>");
        out.println("<a href='logout' style='display:inline-block;margin-top:18px;color:#e47736;text-decoration:none;font-size:12px;font-weight:700'><i class='fa fa-right-from-bracket'></i> Logout</a>");
        out.println("</div>");

        out.println("</section>");

        out.println("<div class='footer'>© 2026 LocalHub. Discover Local. Support Local.</div>");
        out.println("</main></body></html>");
    }
}
