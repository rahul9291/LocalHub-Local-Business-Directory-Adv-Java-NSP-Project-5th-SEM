
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.sql.*;


public class BusinessListServlet extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/local_business_directory";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String search = request.getParameter("q");
        if (search == null) search = "";
        search = search.trim();

        String sql =
            "SELECT b.id, b.business_name, b.address, b.phone, " +
            "b.email, b.description, c.category_name " +
            "FROM businesses b " +
            "LEFT JOIN categories c ON b.category_id = c.id " +
            "WHERE b.status='APPROVED' " +
            "AND (b.business_name LIKE ? OR b.address LIKE ? " +
            "OR c.category_name LIKE ?) " +
            "ORDER BY b.id DESC";

        out.println("<!DOCTYPE html><html><head>");
        out.println("<title>Browse Businesses</title>");
        out.println("<meta name='viewport' content='width=device-width,initial-scale=1'>");
        out.println("<style>");
        out.println("body{font-family:Arial;background:#f1f3f8;margin:0;color:#222}");
        out.println("header{background:#2864e8;color:white;padding:20px 5%;display:flex;justify-content:space-between}");
        out.println(".container{max-width:1100px;margin:35px auto;padding:15px}");
        out.println("h1{color:#2864e8}");
        out.println("input{padding:13px;width:70%;max-width:500px;border:1px solid #ccc;border-radius:6px}");
        out.println("button{padding:13px 22px;background:#2864e8;color:white;border:0;border-radius:6px;cursor:pointer}");
        out.println(".cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:20px;margin-top:25px}");
        out.println(".card{background:white;padding:22px;border-radius:10px;box-shadow:0 3px 12px #ddd}");
        out.println(".category{color:#2864e8;font-weight:bold}");
        out.println(".empty{background:white;padding:25px;border-radius:8px}");
        out.println("</style></head><body>");

        out.println("<header><b>Local Business Directory</b>");
        out.println("<a style='color:white' href='index.html'>Home</a></header>");

        out.println("<div class='container'>");
        out.println("<h1>Discover Local Businesses</h1>");
        out.println("<p>Find restaurants, shops and services near you.</p>");

        out.println("<form method='get' action='businesses'>");
        out.println("<input name='q' placeholder='Search business, address or category...' value='" +
                    escape(search) + "'>");
        out.println("<button type='submit'>Search</button>");
        out.println("</form>");

        out.println("<div class='cards'>");

        int count = 0;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                String pattern = "%" + search + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        count++;

                        out.println("<div class='card'>");
                        out.println("<h2>" +
                            escape(rs.getString("business_name")) + "</h2>");

                        out.println("<p class='category'>" +
                            escape(rs.getString("category_name")) + "</p>");

                        out.println("<p><b>Address:</b> " +
                            escape(rs.getString("address")) + "</p>");

                        out.println("<p><b>Phone:</b> " +
                            escape(rs.getString("phone")) + "</p>");

                        out.println("<p><b>Email:</b> " +
                            escape(rs.getString("email")) + "</p>");

                        out.println("<p>" +
                            escape(rs.getString("description")) + "</p>");

                        out.println("</div>");
                    }
                }
            }

            if (count == 0) {
                out.println("<div class='empty'>");
                out.println("<h3>No businesses found.</h3>");
                out.println("<p>Try another search.</p>");
                out.println("</div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p>Unable to load businesses. Check server logs.</p>");
        }

        out.println("</div></div></body></html>");
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
