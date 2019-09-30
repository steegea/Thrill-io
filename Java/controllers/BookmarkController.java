package thrillio.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import thrillio.constants.KidFriendlyStatus;
import thrillio.entities.Bookmark;
import thrillio.entities.User;
import thrillio.managers.BookmarkManager;
import thrillio.managers.UserManager;

@WebServlet(urlPatterns = {"/bookmark", "/bookmark/save", "/bookmark/mybooks"
		, "/bookmark/remove", "/bookmark/addbook"/*, "/bookmark/removebook_fromdatabase", "/bookmark/signup"*/})
public class BookmarkController extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public BookmarkController() {
        // TODO Auto-generated constructor stub
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	
		RequestDispatcher dispatcher = null;
		System.out.println("Servlet path: " + request.getServletPath());
		
		if(request.getSession().getAttribute("userId") != null) {
			long userId = (long) request.getSession().getAttribute("userId");
			
			//If the user saves a book
			if(request.getServletPath().contains("save")) {
				
				dispatcher = request.getRequestDispatcher("/mybooks.jsp");
				
				String bid = request.getParameter("bid");
				
				User user = UserManager.getInstance().getUser(userId);
				Bookmark bookmark = BookmarkManager.getInstance().getBook(Long.parseLong(bid));
				BookmarkManager.getInstance().saveUserBookmark(user, bookmark);
				
				Collection<Bookmark> bookList = BookmarkManager.getInstance().getBooks(true, userId);
				request.setAttribute("books", bookList);
				
				request.setAttribute("user", user);
				
				
			//If the user views their list of books	
			} else if(request.getServletPath().contains("mybooks")) {
				
				dispatcher = request.getRequestDispatcher("/mybooks.jsp");
				
				Collection<Bookmark> bookList = BookmarkManager.getInstance().getBooks(true, userId);
				request.setAttribute("books", bookList);
				
				User userInfo = UserManager.getInstance().getUser(userId);
				request.setAttribute("user", userInfo);
			
			//If the user deletes a book from their list
			} else if(request.getServletPath().contains("remove")) {
				
				dispatcher = request.getRequestDispatcher("/mybooks.jsp");
				
				String bid = request.getParameter("bid");
				
				User user = UserManager.getInstance().getUser(userId);
				Bookmark bookmark = BookmarkManager.getInstance().getBook(Long.parseLong(bid));
				BookmarkManager.getInstance().deleteUserBookmark(user, bookmark);
				//dispatcher = request.getRequestDispatcher("/browse.jsp");
				
				Collection<Bookmark> bookList = BookmarkManager.getInstance().getBooks(true, userId);
				request.setAttribute("books", bookList);
				
				User userInfo = UserManager.getInstance().getUser(userId);
				request.setAttribute("user", userInfo);
			
			//If the user adds a book to their list
			} else if(request.getServletPath().contains("addbook")) {
				dispatcher = request.getRequestDispatcher("/browse.jsp");
				
				String bookUrl = request.getParameter("book_url");
				
		    	Document doc = Jsoup.connect(bookUrl).get();
		    	Elements titleLabel = doc.select("h1");
		    	Elements ratingValue = doc.select("span[itemprop=\"ratingValue\"]");
		    	String publicationYearRow = doc.select(".row:nth-child(2)").text();
		    	
		    	Elements image = doc.select("div.editionCover > img");
		    	String authorNameString = doc.select("div.authorname__container").text();
		    	
		    	String title = titleLabel.text();
		    	String goodreadsRating = ratingValue.text();
		    	
		    	//Parsing publication information
				String[] publicationYear = publicationYearRow.split(" ");

				String publisherName = "";
				
				if(publicationYearRow.contains("-") && publicationYearRow.contains("(")) {
					
					Pattern p = Pattern.compile("[a-zA-Z]{1,}-[a-zA-Z]{1,}");
					Matcher m = p.matcher(publicationYearRow);
					
					if (m.find()) {
						
						publisherName = StringUtils.substringBefore(publicationYearRow, " (");
						publisherName = StringUtils.substringAfter(publisherName, "by ");
						
					}
					
					else {
						publisherName = StringUtils.substringBefore(publicationYearRow, " - (");
						publisherName = StringUtils.substringAfter(publisherName, "by ");
					}
				}
				
				
				else {
					publisherName = StringUtils.substringBefore(publicationYearRow, " (");
					publisherName = StringUtils.substringAfter(publisherName, "by ");
					
				}
				
				
				String imageUrl = image.attr("abs:src");
				String authorName = "";
				
				
				if(authorNameString.contains(",")){
					if(authorNameString.contains("(")) {
						
						if(authorNameString.indexOf('(') < authorNameString.indexOf(','))
						{
							authorName = StringUtils.substringBefore(authorNameString, " (");
						}
						
						else{
							authorName = StringUtils.substringBefore(authorNameString, ",");
						}
					}
					
				}
				
				else {
					authorName = StringUtils.substringBefore(authorNameString, " (");
				}
				
				//Pass the book (including the publication year) if it is a valid year
				for(int i = 0; i < publicationYear.length; i++) {
					if(publicationYear[i].matches("[0-9]+") && publicationYear[i].length() == 4) {
						
						BookmarkManager.getInstance().addNewBookToDatabase(title, authorName, bookUrl, imageUrl, publisherName, 
								Integer.parseInt(publicationYear[i]),/*, genre_id,*/ Double.parseDouble(goodreadsRating));
						break;
					}
				}
				
				
				Collection<Bookmark> bookList = BookmarkManager.getInstance().getBooks(false, userId);
				request.setAttribute("books", bookList);
				
				User userInfo = UserManager.getInstance().getUser(userId);
				request.setAttribute("user", userInfo);
			
			} else {
				User user = UserManager.getInstance().getUser(userId);
				dispatcher = request.getRequestDispatcher("/browse.jsp");
				
				Collection<Bookmark> bookList = BookmarkManager.getInstance().getBooks(false, userId);
				request.setAttribute("books", bookList);
				
				request.setAttribute("user", user);
			}
		} else {
			dispatcher = request.getRequestDispatcher("/login.jsp");
		}
		
		dispatcher.forward(request, response);
		
    }
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public void saveUserBookmark(User user, Bookmark bookmark) {
		BookmarkManager.getInstance().saveUserBookmark(user, bookmark);
		
	}

	public void setKidFriendlyStatus(User user, KidFriendlyStatus kidFriendlyStatus, Bookmark bookmark) throws SQLException {
		BookmarkManager.getInstance().setKidFriendlyStatus(user, kidFriendlyStatus, bookmark);
		
	}

	public void share(User user, Bookmark bookmark) throws SQLException {
		BookmarkManager.getInstance().share(user, bookmark);
		
	}
}
