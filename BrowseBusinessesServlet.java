
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;

@WebServlet("/businesses")
public class BrowseBusinessesServlet extends HttpServlet {

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

    private String icon(String category) {
        String c = category == null ? "" : category.toLowerCase();

        if (c.contains("restaurant") || c.contains("food"))
            return "fa-utensils";
        if (c.contains("cafe") || c.contains("coffee"))
            return "fa-mug-hot";
        if (c.contains("shop") || c.contains("store"))
            return "fa-bag-shopping";
        if (c.contains("salon") || c.contains("beauty"))
            return "fa-scissors";
        if (c.contains("medical") || c.contains("health"))
            return "fa-heart-pulse";
        if (c.contains("education") || c.contains("school"))
            return "fa-graduation-cap";

        return "fa-store";
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String sql = "SELECT b.id, b.business_name, b.address, b.phone, " +
                     "b.email, b.description, c.category_name " +
                     "FROM businesses b " +
                     "JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.status = 'APPROVED' " +
                     "ORDER BY b.created_at DESC";

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Explore Businesses | LocalHub</title>");

        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box;font-family:Inter,sans-serif}");
        out.println("body{background:#f5f7fb;color:#172c52}");
        out.println("nav{height:75px;background:white;display:flex;align-items:center;justify-content:space-between;padding:0 8%;box-shadow:0 3px 20px #17243b0c;position:sticky;top:0;z-index:10}");
        out.println(".logo{font-size:26px;font-weight:800;color:#172c52;text-decoration:none}");
        out.println(".logo span{color:#f28b35}");
        out.println(".navlinks{display:flex;gap:25px;align-items:center}");
        out.println(".navlinks a{color:#536078;text-decoration:none;font-size:14px;font-weight:600;transition:.3s}");
        out.println(".navlinks a:hover{color:#f28b35}");

        out.println(".hero{padding:75px 8%;background:linear-gradient(120deg,#142846,#254b7c);color:white}");
        out.println(".hero small{color:#ffb36d;font-weight:700;letter-spacing:2px}");
        out.println(".hero h1{font-size:42px;margin:17px 0}");
        out.println(".hero p{color:#d1dbea;line-height:1.8;font-size:15px}");

        out.println(".search-area{max-width:850px;margin:35px auto 0;background:white;padding:12px;border-radius:13px;display:flex;gap:10px;box-shadow:0 10px 30px #0002}");
        out.println(".search-area input{flex:1;min-width:0;padding:13px;border:0;outline:0;font-size:14px;color:#172c52}");
        out.println(".search-area select{padding:12px;border:1px solid #e6eaf0;border-radius:8px;color:#536078;outline:0;background:white}");
        out.println(".search-icon{color:#f28b35;padding:13px}");

        out.println(".main{padding:65px 8%}");
        out.println(".heading{text-align:center;margin-bottom:35px}");
        out.println(".heading h2{font-size:30px;margin-bottom:10px}");
        out.println(".heading p{color:#8994a6;font-size:14px}");

        out.println(".business-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:25px}");
        out.println(".card{background:white;border:1px solid #e9edf4;border-radius:15px;overflow:hidden;transition:.3s;animation:appear .6s ease;box-shadow:0 5px 20px #17243b08}");
        out.println(".card:hover{transform:translateY(-8px);box-shadow:0 16px 35px #17243b16}");
        out.println(".card-banner{height:135px;background:linear-gradient(135deg,#172c52,#42658d);display:flex;align-items:center;justify-content:center;color:white;font-size:45px;position:relative}");
        out.println(".card:nth-child(3n+2) .card-banner{background:linear-gradient(135deg,#b76532,#f2a15b)}");
        out.println(".card:nth-child(3n) .card-banner{background:linear-gradient(135deg,#27645e,#56a69a)}");
        out.println(".category{position:absolute;right:14px;top:14px;background:#ffffffeb;color:#dc792b;padding:7px 11px;border-radius:20px;font-size:10px;font-weight:700}");
        out.println(".card-body{padding:22px}");
        out.println(".card h3{font-size:19px;margin-bottom:15px;color:#172c52}");
        out.println(".detail{display:flex;align-items:flex-start;gap:10px;margin:12px 0;color:#78849a;font-size:12px;line-height:1.6;overflow-wrap:anywhere}");
        out.println(".detail i{color:#f28b35;margin-top:3px;min-width:14px}");
        out.println(".description{font-size:13px;color:#7c899e;line-height:1.7;margin-top:15px;border-top:1px solid #edf0f5;padding-top:14px}");
        out.println(".view-btn{display:inline-flex;align-items:center;gap:10px;margin-top:18px;padding:11px 17px;background:#f28b35;color:white;text-decoration:none;border-radius:8px;font-size:12px;font-weight:700;transition:.3s}");
        out.println(".view-btn:hover{background:#d96e20;transform:translateY(-2px)}");

        out.println(".empty{text-align:center;background:white;padding:55px 20px;border-radius:15px;grid-column:1/-1;color:#8994a6}");
        out.println(".empty i{font-size:45px;color:#f28b35;margin-bottom:18px}");
        out.println(".empty h3{color:#172c52;margin-bottom:10px}");

        out.println(".footer{background:#101e38;color:white;text-align:center;padding:30px 15px;font-size:13px}");
        out.println(".footer p{color:#bdc9da;margin-top:10px}");
        out.println("@keyframes appear{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}");

        out.println("@media(max-width:900px){.business-grid{grid-template-columns:repeat(2,1fr)}}");
        out.println("@media(max-width:600px){nav{padding:15px 5%;height:auto;gap:15px;flex-wrap:wrap}.navlinks{gap:15px;flex-wrap:wrap}.hero{padding:55px 6%}.hero h1{font-size:32px}.main{padding:45px 6%}.business-grid{grid-template-columns:1fr}.search-area{flex-wrap:wrap}.search-area input{width:100%;flex-basis:100%}.search-area select{flex:1;min-width:0}}");

        out.println("</style></head><body>");

        out.println("<nav>");
        out.println("<a href='index.html' class='logo'>Local<span>Hub.</span></a>");
        out.println("<div class='navlinks'>");
        out.println("<a href='index.html'>Home</a>");
        out.println("<a href='businesses'>Explore</a>");
        out.println("<a href='login.html'>Login</a>");
        out.println("<a href='register.html'>Register</a>");
        out.println("</div></nav>");

        out.println("<section class='hero'>");
        out.println("<small>DISCOVER YOUR NEIGHBOURHOOD</small>");
        out.println("<h1>Explore Local Businesses</h1>");
        out.println("<p>Find restaurants, shops, cafes and trusted services around your community.</p>");

        out.println("<div class='search-area'>");
        out.println("<i class='fas fa-search search-icon'></i>");
        out.println("<input type='text' id='searchInput' placeholder='Search business, address or description...'>");
        out.println("<select id='categoryFilter'><option value='all'>All Categories</option>");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD)) {

                String categorySql =
                    "SELECT DISTINCT c.category_name FROM categories c " +
                    "JOIN businesses b ON b.category_id=c.id " +
                    "WHERE b.status='APPROVED' ORDER BY c.category_name";

                try (PreparedStatement cps = con.prepareStatement(categorySql);
                     ResultSet crs = cps.executeQuery()) {

                    while (crs.next()) {
                        String category = crs.getString("category_name");
                        out.println("<option value='" + escape(category).toLowerCase() +
                                    "'>" + escape(category) + "</option>");
                    }
                }

                out.println("</select></div></section>");

                out.println("<main class='main'>");
                out.println("<div class='heading'><h2>Available Businesses</h2>");
                out.println("<p>Explore approved businesses and discover something new.</p></div>");
                out.println("<div class='business-grid' id='businessGrid'>");

                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    boolean found = false;

                    while (rs.next()) {
                        found = true;

                        int businessId = rs.getInt("id");
                        String name = rs.getString("business_name");
                        String category = rs.getString("category_name");
                        String address = rs.getString("address");
                        String phone = rs.getString("phone");
                        String email = rs.getString("email");
                        String description = rs.getString("description");

                        String searchText = (name + " " + category + " " +
                            address + " " + description).toLowerCase();

                        out.println("<article class='card' data-category='" +
                            escape(category).toLowerCase() + "' data-search='" +
                            escape(searchText) + "'>");

                        out.println("<div class='card-banner'>");
                        out.println("<i class='fas " + icon(category) + "'></i>");
                        out.println("<span class='category'>" + escape(category) + "</span>");
                        out.println("</div>");

                        out.println("<div class='card-body'>");
                        out.println("<h3>" + escape(name) + "</h3>");

                        out.println("<div class='detail'><i class='fas fa-location-dot'></i><span>" +
                            escape(address) + "</span></div>");

                        out.println("<div class='detail'><i class='fas fa-phone'></i><span>" +
                            escape(phone) + "</span></div>");

                        out.println("<div class='detail'><i class='fas fa-envelope'></i><span>" +
                            escape(email) + "</span></div>");

                        out.println("<div class='description'>" +
                            escape(description) + "</div>");

                        out.println("<a class='view-btn' href='business-details?id=" +
                            businessId + "'>View Details <i class='fas fa-arrow-right'></i></a>");

                        out.println("</div></article>");
                    }

                    if (!found) {
                        out.println("<div class='empty'><i class='fas fa-store-slash'></i>");
                        out.println("<h3>No Businesses Available Yet</h3>");
                        out.println("<p>Approved business listings will appear here.</p></div>");
                    }
                }

                out.println("</div></main>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div class='empty'><h3>Unable to load businesses</h3>");
            out.println("<p>Please check the database connection and Tomcat logs.</p></div>");
        }

        out.println("<footer class='footer'>");
        out.println("<div class='logo' style='color:white'>Local<span>Hub.</span></div>");
        out.println("<p>Connecting people with local businesses.</p>");
        out.println("<p>© 2026 LocalHub. All Rights Reserved.</p>");
        out.println("</footer>");

        out.println("<script>");
        out.println("const search=document.getElementById('searchInput');");
        out.println("const filter=document.getElementById('categoryFilter');");
        out.println("function applyFilters(){");
        out.println("const q=search.value.toLowerCase().trim();");
        out.println("const c=filter.value;");
        out.println("document.querySelectorAll('.card').forEach(card=>{");
        out.println("const matchText=card.dataset.search.includes(q);");
        out.println("const matchCategory=c==='all'||card.dataset.category===c;");
        out.println("card.style.display=matchText&&matchCategory?'':'none';");
        out.println("});}");
        out.println("search.addEventListener('input',applyFilters);");
        out.println("filter.addEventListener('change',applyFilters);");
        out.println("</script>");

        out.println("</body></html>");
    }
}
