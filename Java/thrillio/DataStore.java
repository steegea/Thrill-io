package thrillio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import thrillio.constants.BookGenre;
import thrillio.constants.Gender;
import thrillio.constants.MovieGenre;
import thrillio.constants.UserType;
import thrillio.entities.Bookmark;
import thrillio.entities.User;
import thrillio.entities.UserBookmark;
import thrillio.managers.BookmarkManager;
import thrillio.managers.UserManager;
import thrillio.util.IOUtil;

public class DataStore {
	
	private static List<User> users = new ArrayList<>();
	
	
	public static List<User> getUsers() {
		return users;
	}

	private static List<List<Bookmark>> bookmarks = new ArrayList<>();
	
	
	public static List<List<Bookmark>> getBookmarks() {
		return bookmarks;
	}

	private static List<UserBookmark> userBookmarks = new ArrayList<>();

	
	public static void loadData()
	{
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false", "root", "root12345");
				Statement stmt = conn.createStatement();){
				
				loadUsers(stmt);
				loadWebLinks(stmt);
				loadMovies(stmt);
				loadBooks(stmt);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Method that reads user data from the project database
	 * and creates instances for each user
	 * @throws SQLException 
	 */
	
	private static void loadUsers(Statement stmt) throws SQLException {
		String query = "Select * from User";
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next()) {
			long id = rs.getLong("id");
			String email = rs.getString("email");
			String password = rs.getString("password");
			String firstName = rs.getString("first_name");
			String lastName = rs.getString("last_name");
			int gender_id = rs.getInt("gender_id");
			Gender gender = Gender.values()[gender_id];
			int user_type_id = rs.getInt("user_type_id");
			UserType userType = UserType.values()[user_type_id];
			
			User user = UserManager.getInstance().createUser(id, email, password, firstName, lastName, gender, userType);
			users.add(user);
		}
		
	}
	
	
	/**
	 * Method that reads web link data from the project database
	 * and creates instances for each web link
	 * @throws SQLException 
	 */
	
	private static void loadWebLinks(Statement stmt) throws SQLException {
		String query = "Select * from WebLink";
		ResultSet rs = stmt.executeQuery(query);
		
		List<Bookmark> bookmarkList = new ArrayList<>();
		
		while(rs.next()) {
			long id = rs.getLong("id");
			String title = rs.getString("title");
			String url = rs.getString("url");
			String host = rs.getString("host");
			
			Bookmark bookmark = BookmarkManager.getInstance().createWebLink(id, title, "", url, host);
			bookmarkList.add(bookmark);
		}
		
		bookmarks.add(bookmarkList);
	}
	
	
    /**
     * Method that reads movie data from the project database
	 * and creates instances for each movie
     * @throws SQLException 
     */
	
	private static void loadMovies(Statement stmt) throws SQLException {
		String query = "Select m.id, title, release_year, GROUP_CONCAT(DISTINCT a.name SEPARATOR ',') AS cast, GROUP_CONCAT(DISTINCT d.name SEPARATOR ',') AS directors, movie_genre_id, imdb_rating"
				+ " from Movie m, Actor a, Movie_Actor ma, Director d, Movie_Director md "
				+ "where m.id = ma.movie_id and ma.actor_id = a.id and "
				      + "m.id = md.movie_id and md.director_id = d.id group by m.id";
		
		ResultSet rs = stmt.executeQuery(query);
		
		List<Bookmark> bookmarkList = new ArrayList<>();
		
		while(rs.next()) {
			long id = rs.getLong("id");
			String title = rs.getString("title");
			int releaseYear = rs.getInt("release_year");
			String[] cast = rs.getString("cast").split(",");
			String[] directors = rs.getString("directors").split(",");			
			int genre_id = rs.getInt("movie_genre_id");
			MovieGenre genre = MovieGenre.values()[genre_id];
			double imdbRating = rs.getDouble("imdb_rating");
			
			Bookmark bookmark = BookmarkManager.getInstance().createMovie(id, title, "", releaseYear, cast, directors, genre, imdbRating/*, values[7]*/);
			bookmarkList.add(bookmark);
		}
		
		bookmarks.add(bookmarkList);
	}
	
	
	/**
	 * Method that reads book data from the project database
	 * and creates instances for each book
	 * @throws SQLException
	 * 
	 *  SQL --> Index starts at 1
	 */
	private static void loadBooks(Statement stmt) throws SQLException {
		String query = "Select b.id, title, publication_year, p.name, GROUP_CONCAT(a.name SEPARATOR ',') AS authors, book_genre_id, amazon_rating, created_date"
				+ " from Book b, Publisher p, Author a, Book_Author ba "
				+ "where b.publisher_id = p.id and b.id = ba.book_id and ba.author_id = a.id group by b.id";
		
		ResultSet rs = stmt.executeQuery(query);
		
		List<Bookmark> bookmarkList = new ArrayList<>();
		while(rs.next()) {
			
			//Obtains the fields/columns for each row
			long id = rs.getLong("id");
			String title = rs.getString("title");
			int publicationYear = rs.getInt("publication_year");
			String publisher = rs.getString("name");		
			String[] authors = rs.getString("authors").split(",");
			int genre_id = rs.getInt("book_genre_id");
			BookGenre genre = BookGenre.values()[genre_id];
			double amazonRating = rs.getDouble("amazon_rating");
			
			System.out.print("\n");
			
			Date createdDate = rs.getDate("created_date");
			System.out.println("createdDate: " + createdDate);
			
			Timestamp timeStamp = rs.getTimestamp(8); //8th column
			System.out.println("timeStamp: " + timeStamp);
			
			System.out.println("localDateTime: " + timeStamp.toLocalDateTime());
			
			System.out.println("id: " + id + ", title: " + title + ", publication year: " + publicationYear + ", publisher: " 
			+ publisher + ", authors: " + String.join(", ", authors) + ", genre: " + genre + ", amazonRating: " + amazonRating);
			
			Bookmark bookmark =  BookmarkManager.getInstance().createBook(id, title, publisher, publisher, publicationYear, publisher, authors, genre, amazonRating/*, values[7]*/);
			bookmarkList.add(bookmark);
		}
		
		bookmarks.add(bookmarkList);
	}
	
	
	public static void add(UserBookmark userBookmark) {
		//userBookmarks[bookmarkIndex] = userBookmark;
		//bookmarkIndex++;
		
		userBookmarks.add(userBookmark);
		
	}

}
