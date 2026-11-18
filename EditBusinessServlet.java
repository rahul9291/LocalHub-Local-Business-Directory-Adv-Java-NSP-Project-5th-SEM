
package com.localbusiness.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;

@WebServlet("/edit-business")
public class EditBusinessServlet extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/local_business_directory";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null ||
            !"OWNER".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        int ownerId = Integer.parseInt(
            session.getAttribute("userId").toString()
        );

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql =
                "SELECT * FROM businesses WHERE id=? AND owner_id=?";

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);
                ps.setInt(2, ownerId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        response.sendError(404, "Business not found.");
                        return;
                    }

                    String businessName = escape(rs.getString("business_name"));
                    String address = escape(rs.getString("address"));
                    String phone = escape(rs.getString("phone"));
                    String email = escape(rs.getString("email"));
                    String description = escape(rs.getString("description"));
                    int currentCategory = rs.getInt("category_id");

                    PrintWriter out = response.getWriter();

                    out.println("<!DOCTYPE html><html><head>");
                    out.println("<meta charset='UTF-8'>");
                    out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                    out.println("<title>Edit Business | LocalHub</title>");

                    out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
                    out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
                    out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>");

                    out.println("<style>");

                    out.println("*{margin:0;padding:0;box-sizing:border-box}");
                    out.println("body{font-family:'Inter',Arial,sans-serif;background:#f4f6fb;color:#17233c}");

                    out.println(".navbar{height:72px;background:#101d38;display:flex;align-items:center;justify-content:space-between;padding:0 7%;color:white}");
                    out.println(".brand{font-size:25px;font-weight:800;color:white;text-decoration:none}");
                    out.println(".brand span{color:#ff8b36}");
                    out.println(".navlinks{display:flex;gap:25px;align-items:center}");
                    out.println(".navlinks a{color:#dce4f2;text-decoration:none;font-size:13px;font-weight:600}");
                    out.println(".navlinks a:hover{color:#ff963f}");

                    out.println(".container{max-width:1050px;margin:40px auto;padding:0 20px}");
                    out.println(".breadcrumb{font-size:13px;color:#8290a8;margin-bottom:20px}");
                    out.println(".breadcrumb a{color:#f18436;text-decoration:none;font-weight:600}");

                    out.println(".heading{margin-bottom:25px}");
                    out.println(".heading h1{font-size:29px;font-weight:800;color:#14213d;margin-bottom:9px}");
                    out.println(".heading p{color:#7b879d;font-size:14px;line-height:1.6}");

                    out.println(".layout{display:grid;grid-template-columns:1fr 290px;gap:22px;align-items:start}");
                    out.println(".form-card,.side-card{background:white;border-radius:16px;padding:30px;box-shadow:0 8px 30px rgba(25,45,80,.06);border:1px solid #edf0f6}");

                    out.println(".section-title{font-size:17px;font-weight:700;margin-bottom:22px;color:#182641;display:flex;align-items:center;gap:10px}");
                    out.println(".section-title i{color:#f18436}");

                    out.println(".field{margin-bottom:19px}");
                    out.println("label{display:block;font-size:13px;font-weight:700;color:#34425c;margin-bottom:8px}");
                    out.println("input,select,textarea{width:100%;padding:13px 14px;border:1px solid #dfe4ed;border-radius:9px;background:#fbfcfe;font-family:inherit;font-size:14px;color:#26344e;outline:none;transition:.2s}");
                    out.println("input:focus,select:focus,textarea:focus{border-color:#f18a3d;box-shadow:0 0 0 3px rgba(241,138,61,.12);background:white}");
                    out.println("textarea{min-height:125px;resize:vertical;line-height:1.6}");
                    out.println(".hint{font-size:11px;color:#98a2b3;margin-top:6px}");

                    out.println(".buttons{display:flex;gap:12px;margin-top:25px;flex-wrap:wrap}");
                    out.println(".btn{padding:13px 21px;border-radius:9px;text-decoration:none;border:0;cursor:pointer;font-size:13px;font-weight:700;font-family:inherit;display:inline-flex;align-items:center;justify-content:center;gap:9px;transition:.2s}");
                    out.println(".btn-primary{background:#f18436;color:white;flex:1}");
                    out.println(".btn-primary:hover{background:#df7026;transform:translateY(-2px)}");
                    out.println(".btn-secondary{background:#edf1f7;color:#34425c}");
                    out.println(".btn-secondary:hover{background:#e1e7f0}");

                    out.println(".side-card{padding:24px}");
                    out.println(".side-icon{width:48px;height:48px;background:#fff1e7;color:#f18436;border-radius:13px;display:flex;align-items:center;justify-content:center;font-size:21px;margin-bottom:17px}");
                    out.println(".side-card h3{font-size:16px;margin-bottom:10px}");
                    out.println(".side-card p{font-size:13px;color:#7b879d;line-height:1.7;margin-bottom:17px}");
                    out.println(".tip{display:flex;gap:10px;margin:15px 0;font-size:12px;color:#65738b;line-height:1.6}");
                    out.println(".tip i{color:#f18436;margin-top:3px}");

                    out.println(".footer{text-align:center;color:#9aa4b5;font-size:12px;padding:30px 10px}");

                    out.println("@media(max-width:760px){.layout{grid-template-columns:1fr}.navbar{padding:0 5%}.navlinks{gap:12px}.navlinks a{font-size:11px}.form-card{padding:22px}.heading h1{font-size:24px}}");
                    out.println("@media(max-width:430px){.navlinks a:first-child{display:none}.buttons{flex-direction:column}.btn{width:100%}}");

                    out.println("</style></head><body>");

                    out.println("<nav class='navbar'>");
                    out.println("<a class='brand' href='home'>Local<span>Hub</span></a>");
                    out.println("<div class='navlinks'>");
                    out.println("<a href='home'><i class='fa fa-home'></i> Dashboard</a>");
                    out.println("<a href='my-businesses'><i class='fa fa-store'></i> My Businesses</a>");
                    out.println("<a href='logout'><i class='fa fa-sign-out-alt'></i> Logout</a>");
                    out.println("</div></nav>");

                    out.println("<main class='container'>");

                    out.println("<div class='breadcrumb'><a href='home'>Dashboard</a> / <a href='my-businesses'>My Businesses</a> / Edit Business</div>");

                    out.println("<div class='heading'>");
                    out.println("<h1>Edit Your Business</h1>");
                    out.println("<p>Update your business information and keep your LocalHub listing accurate.</p>");
                    out.println("</div>");

                    out.println("<div class='layout'>");

                    out.println("<section class='form-card'>");
                    out.println("<div class='section-title'><i class='fa fa-edit'></i> Business Information</div>");

                    out.println("<form method='post' action='edit-business'>");
                    out.println("<input type='hidden' name='id' value='" + id + "'>");

                    out.println("<div class='field'>");
                    out.println("<label>Business Name *</label>");
                    out.println("<input name='businessName' required maxlength='150' placeholder='Enter business name' value='" + businessName + "'>");
                    out.println("</div>");

                    out.println("<div class='field'>");
                    out.println("<label>Business Category *</label>");
                    out.println("<select name='categoryId' required>");
                    out.println("<option value=''>Select Category</option>");

                    String categorySql =
                        "SELECT id,category_name FROM categories ORDER BY category_name";

                    try (PreparedStatement cps = con.prepareStatement(categorySql);
                         ResultSet crs = cps.executeQuery()) {

                        while (crs.next()) {
                            int categoryId = crs.getInt("id");
                            String selected =
                                categoryId == currentCategory ? "selected" : "";

                            out.println("<option value='" + categoryId + "' " +
                                selected + ">" +
                                escape(crs.getString("category_name")) +
                                "</option>");
                        }
                    }

                    out.println("</select></div>");

                    out.println("<div class='field'>");
                    out.println("<label>Business Address *</label>");
                    out.println("<input name='address' required maxlength='255' placeholder='Enter complete address' value='" + address + "'>");
                    out.println("</div>");

                    out.println("<div class='field'>");
                    out.println("<label>Contact Phone</label>");
                    out.println("<input type='tel' name='phone' maxlength='20' placeholder='Enter phone number' value='" + phone + "'>");
                    out.println("</div>");

                    out.println("<div class='field'>");
                    out.println("<label>Business Email</label>");
                    out.println("<input type='email' name='email' maxlength='150' placeholder='Enter email address' value='" + email + "'>");
                    out.println("</div>");

                    out.println("<div class='field'>");
                    out.println("<label>Business Description</label>");
                    out.println("<textarea name='description' maxlength='2000' placeholder='Tell customers about your business...'>" + description + "</textarea>");
                    out.println("<div class='hint'>Write a short description to help customers understand your business.</div>");
                    out.println("</div>");

                    out.println("<div class='buttons'>");
                    out.println("<button class='btn btn-primary' type='submit'><i class='fa fa-save'></i> Update Business</button>");
                    out.println("<a class='btn btn-secondary' href='my-businesses'><i class='fa fa-arrow-left'></i> Cancel</a>");
                    out.println("</div>");

                    out.println("</form></section>");

                    out.println("<aside class='side-card'>");
                    out.println("<div class='side-icon'><i class='fa fa-lightbulb'></i></div>");
                    out.println("<h3>Helpful Tips</h3>");
                    out.println("<p>Keep your business profile clear and up to date so customers can find you easily.</p>");

                    out.println("<div class='tip'><i class='fa fa-check-circle'></i><span>Use your correct business name and address.</span></div>");
                    out.println("<div class='tip'><i class='fa fa-check-circle'></i><span>Add a working contact number and email.</span></div>");
                    out.println("<div class='tip'><i class='fa fa-check-circle'></i><span>Write a useful and simple description.</span></div>");
                    out.println("<div class='tip'><i class='fa fa-info-circle'></i><span>After updating, your listing will go for admin approval again.</span></div>");
                    out.println("</aside>");

                    out.println("</div></main>");
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

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null ||
            !"OWNER".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        int ownerId = Integer.parseInt(
            session.getAttribute("userId").toString()
        );

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));

            String sql =
                "UPDATE businesses SET business_name=?, category_id=?, " +
                "address=?, phone=?, email=?, description=?, status='PENDING' " +
                "WHERE id=? AND owner_id=?";

            try (Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, request.getParameter("businessName"));
                ps.setInt(2, categoryId);
                ps.setString(3, request.getParameter("address"));
                ps.setString(4, request.getParameter("phone"));
                ps.setString(5, request.getParameter("email"));
                ps.setString(6, request.getParameter("description"));
                ps.setInt(7, id);
                ps.setInt(8, ownerId);

                int updated = ps.executeUpdate();

                if (updated == 0) {
                    response.sendError(404, "Business not found.");
                    return;
                }
            }

            response.sendRedirect("my-businesses");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Unable to update business.");
        }
    }

    private String escape(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
