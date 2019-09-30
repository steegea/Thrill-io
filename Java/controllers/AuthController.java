package thrillio.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import thrillio.managers.UserManager;

/**
 * Servlet implementation class AuthController
 */
@WebServlet(urlPatterns = {"/auth", "/auth/logout", "/auth/signup"})
public class AuthController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("Servlet path: " + request.getServletPath());
		
		if(!request.getServletPath().contains("logout")) {
			
			if(request.getServletPath().contains("signup")) {
				request.getRequestDispatcher("/signup.jsp").forward(request, response);
				
				String email = request.getParameter("email");
				String password = request.getParameter("password");
				
				String firstName = request.getParameter("firstName");
				String lastName = request.getParameter("lastName");
				
				String gender= request.getParameter("gender_dropdown");


				
				if(email != "" && password != "" && firstName != "" && lastName != "") {
					
					long userIdSignUp = UserManager.getInstance().newSignUp(email, password, firstName, lastName, gender);
					
					if(userIdSignUp == -1) {
						
						request.getRequestDispatcher("/login.jsp").forward(request, response);
						userIdSignUp = UserManager.getInstance().newSignUp(email, password, firstName, lastName, gender);
					}
					
					
					else {
						
					}
				
				}
				
			}
			
			else {
				//Code for users logging into their account
				
				String email = request.getParameter("email");
				String password = request.getParameter("password");
				
				//String firstName = request.getParameter("firstName");
				//String lastName = request.getParameter("lastName");
				
				long userId = UserManager.getInstance().authenticate(email, password);
				
				if(userId != -1) {
					HttpSession session = request.getSession();	//Session ID
					session.setAttribute("userId", userId);
					
					request.getRequestDispatcher("bookmark/mybooks").forward(request, response);
				} else {
					request.getRequestDispatcher("/login.jsp").forward(request, response);
				}
			}
			
		}
		
		
		//If a signed-in user logs out
		else {
			request.getSession().invalidate();
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}
			
}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
