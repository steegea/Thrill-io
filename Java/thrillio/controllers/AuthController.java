package thrillio.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
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
@WebServlet(urlPatterns = { "/auth", "/auth/logout", "/auth/signup" })
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		 PrintWriter out = response.getWriter();

		System.out.println("Servlet path: " + request.getServletPath());

		if (!request.getServletPath().contains("logout")) {

			// Sign-up page
			if (request.getServletPath().contains("signup")) {
				request.getRequestDispatcher("/signup.jsp").forward(request, response);
				

				// request.getSession().invalidate();

				String email = request.getParameter("signup_email");
				String password = request.getParameter("signup_password");

				String firstName = request.getParameter("signup_firstName");
				String lastName = request.getParameter("signup_lastName");

				String gender = request.getParameter("gender_dropdown");
				
				HttpSession session = request.getSession();
				

				if (email != null && password != null && firstName != null && lastName != null) {

					
					long userId = UserManager.getInstance().newSignUp(email, password, firstName, lastName, gender);

					// User already exists
					if (userId == -1) {

						response.sendRedirect("/ThrillioWeb_Heroku/auth/signup");
						
					}

					// User does not exist
					else {
						
						session.setAttribute("userId", userId);
						session.setAttribute("signup_error", "You have successfully created your account. "
								+ "Please proceed to the login page to start using your account.");
						
						response.sendRedirect("/ThrillioWeb_Heroku/auth");

						System.out.println("UserID:" + userId);
					}

				}

				else {

					session.setAttribute("signup_error", "");
					request.getRequestDispatcher("/signup.jsp").forward(request, response);
					request.getSession().invalidate();
				}

			}

			// Code for users logging into their account
			else {

				String email = request.getParameter("email");
				String password = request.getParameter("password");

				if (email != null && password != null) {
					long userId = UserManager.getInstance().authenticate(email, password);
					System.out.println("UserID:" + userId);

					if (userId != -1) {

						HttpSession session = request.getSession(); // Session ID
						session.setAttribute("userId", userId);
						// request.setAttribute("error", "");
						
						request.getRequestDispatcher("bookmark/mybooks").forward(request, response);

					}

					else {

						HttpSession session = request.getSession();

						session.setAttribute("login_error", "Your email and/or password is incorrect!");
						response.sendRedirect("/ThrillioWeb_Heroku/auth");

					}

				}

				else {

					request.getRequestDispatcher("/login.jsp").forward(request, response);
					request.getSession().invalidate();
				}

			}

		}

		// If a signed-in user logs out
		else {
			request.getSession().invalidate();
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		
		

	}

}
