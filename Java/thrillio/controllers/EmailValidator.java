package thrillio.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EmailValidator extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
		
	}
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		/* Experimental code */
		

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			String email = request.getParameter("signup_email");
			PreparedStatement ps = conn.prepareStatement("select id from user where email=?");
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				out.println("<font color=green><b>" + email + "</b> is avaliable");

			} else {
				out.println("<font color=red><b>" + email + "</b> is already in use</font>");
			}
			out.println();

		} catch (Exception ex) {

			out.println("Error ->" + ex.getMessage());

		} finally {
			out.close();
		}

	}

}
