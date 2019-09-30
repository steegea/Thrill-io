package thrillio.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import thrillio.DataStore;
import thrillio.constants.BookGenre;
import thrillio.constants.Gender;
import thrillio.entities.Book;
import thrillio.entities.Bookmark;
import thrillio.entities.Movie;
import thrillio.entities.UserBookmark;
import thrillio.entities.WebLink;
import thrillio.managers.BookmarkManager;

public class BookmarkDao {
	
	
	public List<List<Bookmark>> getBookmarks() {
		return DataStore.getBookmarks();
	}

	public void saveUserBookmark(UserBookmark userBookmark) {
		
		
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			if (userBookmark.getBookmark() instanceof Book) {
				saveUserBook(userBookmark, stmt);
			}

			else if (userBookmark.getBookmark() instanceof Movie) {
				saveUserMovie(userBookmark, stmt);
			}

			else {
				// saveUserWebLink(userBookmark, stmt);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteUserBookmark(UserBookmark userBookmark) {
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			if (userBookmark.getBookmark() instanceof Book) {
				deleteUserBook(userBookmark, stmt);
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Adds Book bookmark and the corresponding user to the User_Book table
	 */
	private void saveUserBook(UserBookmark userBookmark, Statement stmt) throws SQLException {
		String query = "Insert into User_Book (user_id, book_id) values (" + userBookmark.getUser().getId() + ", "
				+ userBookmark.getBookmark().getId() + ")";

		stmt.executeUpdate(query);
	}

	private void deleteUserBook(UserBookmark userBookmark, Statement stmt) throws SQLException {
		String query = "Delete from User_Book where user_id = " + userBookmark.getUser().getId() + " and book_id = "
				+ userBookmark.getBookmark().getId();

		stmt.executeUpdate(query);
	}

	/*
	 * Adds Movie bookmark and the corresponding user to the User_Movie table
	 */
	private void saveUserMovie(UserBookmark userBookmark, Statement stmt) throws SQLException {
		String query = "Insert into User_Movie (user_id, movie_id) values (" + userBookmark.getUser().getId() + ", "
				+ userBookmark.getBookmark().getId() + ")";

		stmt.executeUpdate(query);
	}

	/*
	 * Adds WebLink bookmark and the corresponding user to the User_WebLink table
	 */

	/*
	 * private void saveUserWebLink(UserBookmark userBookmark, Statement stmt)
	 * throws SQLException { String query =
	 * "Insert into User_WebLink (user_id, weblink_id) values (" +
	 * userBookmark.getUser().getId() + ", " + userBookmark.getBookmark().getId() +
	 * ")";
	 * 
	 * stmt.executeUpdate(query); }
	 */

	
	/*
	 * Downloads all web links
	 * 
	 * Used when application is initially launched
	 */
	public List<WebLink> getAllWebLinks() {
		List<WebLink> result = new ArrayList<>();

		List<List<Bookmark>> bookmarks = DataStore.getBookmarks();
		List<Bookmark> allWebLinks = bookmarks.get(0);

		// Return a list of web links
		for (Bookmark bookmark : allWebLinks) {
			result.add((WebLink) bookmark);
		}

		return result;
	}

	/*
	 * Retrieves new web links
	 * 
	 * New web links --> Web links whose download status is "not attempted"
	 * 
	 * In other words, web links that were added since the previous set of web-pages
	 * were downloaded
	 */
	public List<WebLink> getWebLinks(WebLink.DownloadStatus downloadStatus) {
		List<WebLink> result = new ArrayList<>();

		List<WebLink> allWebLinks = getAllWebLinks();

		for (WebLink webLink : allWebLinks) {
			if (webLink.getDownloadStatus().equals(downloadStatus)) {
				result.add(webLink);
			}
		}
		return result;
	}

	/*
	 * Updates the appropriate database table based on the kid-friendly status of
	 * bookmarks that were marked as "approved" or "rejected"
	 */
	public void updateKidFriendlyStatus(Bookmark bookmark) throws SQLException {
		int kidFriendlyStatus = bookmark.getKidFriendlyStatus().ordinal();
		long userId = bookmark.getKidFriendlyMarkedBy().getId();

		String tableToUpdate = "Book";

		if (bookmark instanceof Movie) {
			tableToUpdate = "Movie";
		}

		else if (bookmark instanceof WebLink) {
			tableToUpdate = "WebLink";
		}

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			String query = "Update " + tableToUpdate + " set kid_friendly_status = " + kidFriendlyStatus
					+ ", kid_friendly_marked_by = " + userId + " where id = " + bookmark.getId();

			System.out.println("query (updateKidFriendlyStatus) : " + query);
			stmt.executeUpdate(query);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Updates the appropriate database table based on bookmarks that have been
	 * shared
	 * 
	 * Movie bookmarks cannot be shared
	 */
	public void sharedByInfo(Bookmark bookmark) throws SQLException {
		long userId = bookmark.getSharedBy().getId();

		String tableToUpdate = "Book";

		if (bookmark instanceof WebLink) {
			tableToUpdate = "WebLink";
		}

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			String query = "Update " + tableToUpdate + " set shared_by = " + userId + " where id = " + bookmark.getId();

			System.out.println("query (sharedByInfo): " + query);
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Collection<Bookmark> getBooks(boolean isBookmarked, long userId) {

		Collection<Bookmark> result = new ArrayList<>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			String query = "";
			if (!isBookmarked) {
				query = "Select b.id, title, book_url, image_url, publication_year, GROUP_CONCAT(a.name SEPARATOR ',') AS authors, book_genre_id, "
						+ "goodreads_rating from Book b, Author a, Book_Author ba where b.id = ba.book_id and ba.author_id = a.id and "
						+ "b.id NOT IN (select ub.book_id from User u, User_Book ub where u.id = " + userId
						+ " and u.id = ub.user_id) group by b.id";
			} else {
				query = "Select b.id, title, book_url, image_url, publication_year, GROUP_CONCAT(a.name SEPARATOR ',') AS authors, book_genre_id, "
						+ "goodreads_rating from Book b, Author a, Book_Author ba where b.id = ba.book_id and ba.author_id = a.id and "
						+ "b.id IN (select ub.book_id from User u, User_Book ub where u.id = " + userId
						+ " and u.id = ub.user_id) group by b.id";
			}

			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				long id = rs.getLong("id");
				String title = rs.getString("title");
				String bookUrl = rs.getString("book_url");
				String imageUrl = rs.getString("image_url");
				int publicationYear = rs.getInt("publication_year");
				// String publisher = rs.getString("name");
				String[] authors = rs.getString("authors").split(",");
				int genre_id = rs.getInt("book_genre_id");
				BookGenre genre = BookGenre.values()[genre_id];
				double goodreadsRating = rs.getDouble("goodreads_rating");

				System.out.println("id: " + id + ", title: " + title + ", publication year: " + publicationYear
						+ ", authors: " + String.join(", ", authors) + ", genre: " + genre + ", goodreadsRating: "
						+ goodreadsRating);

				Bookmark bookmark = BookmarkManager.getInstance().createBook(id, title, bookUrl, imageUrl,
						publicationYear, null, authors, genre, goodreadsRating/* , values[7] */);
				result.add(bookmark);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	public Collection<Bookmark> getMovies(boolean isBookmarked, long userId) {
		Collection<Bookmark> result = new ArrayList<>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			String query = "";
			if (!isBookmarked) {
				query = "Select m.id, title, release_year, GROUP_CONCAT(DISTINCT a.name SEPARATOR ',') AS cast, GROUP_CONCAT(DISTINCT d.name SEPARATOR ',') AS directors, movie_genre_id, imdb_rating"
						+ " from Movie m, Actor a, Movie_Actor ma, Director d, Movie_Director md "
						+ "where m.id = ma.movie_id and ma.actor_id = a.id and "
						+ "m.id = md.movie_id and md.director_id = d.id "
						+ " NOT IN (select um.movie_id from User u, User_Movie um where u.id = " + userId
						+ " and u.id = um.user_id) group by m.id";
			} 

			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				long id = rs.getLong("id");
				String title = rs.getString("title");
				String imageUrl = rs.getString("image_url");
				int publicationYear = rs.getInt("publication_year");
				// String publisher = rs.getString("name");
				String[] authors = rs.getString("authors").split(",");
				int genre_id = rs.getInt("book_genre_id");
				BookGenre genre = BookGenre.values()[genre_id];
				double goodreadsRating = rs.getDouble("goodreads_rating");

				System.out.println("id: " + id + ", title: " + title + ", publication year: " + publicationYear
						+ ", authors: " + String.join(", ", authors) + ", genre: " + genre + ", goodreadsRating: "
						+ goodreadsRating);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	public Bookmark getBook(long bookId) {
		Book book = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {
			String query = "Select b.id, title, book_url, image_url, publication_year, p.name, GROUP_CONCAT(a.name SEPARATOR ',') AS authors, book_genre_id, goodreads_rating, created_date"
					+ " from Book b, Publisher p, Author a, Book_Author ba " + "where b.id = " + bookId
					+ " and b.publisher_id = p.id and b.id = ba.book_id and ba.author_id = a.id group by b.id";
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				long id = rs.getLong("id");
				String title = rs.getString("title");
				String bookUrl = rs.getString("book_url");
				String imageUrl = rs.getString("image_url");
				int publicationYear = rs.getInt("publication_year");
				String publisher = rs.getString("name");
				String[] authors = rs.getString("authors").split(",");
				int genre_id = rs.getInt("book_genre_id");
				BookGenre genre = BookGenre.values()[genre_id];
				double goodreadsRating = rs.getDouble("goodreads_rating");

				System.out.println("id: " + id + ", title: " + title + ", publication year: " + publicationYear
						+ ", publisher: " + publisher + ", authors: " + String.join(", ", authors) + ", genre: " + genre
						+ ", goodreadsRating: " + goodreadsRating);

				book = BookmarkManager.getInstance().createBook(id, title, bookUrl, imageUrl, publicationYear,
						publisher, authors, genre, goodreadsRating/* , values[7] */);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return book;
	}

	public long addNewBookToDatabase(String title, String authorName, String bookUrl, String imageUrl,
			String publisherName, int publicationYear, /* long publisherId, int genre_id, */ double goodreadsRating) {
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		System.out.println(dtf.format(now));

		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false",
				"root", "root12345"); Statement stmt = conn.createStatement();) {

			// Check for single-quotes in book titles
			int rs;

			title = title.replace("'", "''");
			authorName = authorName.replace("'", "''");
			publisherName = publisherName.replace("'", "''");

			// Creates a new entry in the Book table
			String newBookQuery = "Insert into Book (title, book_url, image_url, publication_year, goodreads_rating, created_date) "
					+ " values('" + title + "', '" + bookUrl + "', '" + imageUrl + "', '" + publicationYear + "' , '"
					+ goodreadsRating + "' , '" + dtf.format(now) + "' " + ")";

			System.out.println("newBookQuery: " + newBookQuery);
			rs = stmt.executeUpdate(newBookQuery);

			// Creates a new entry in the Author table
			String newAuthorQuery = "Insert into Author (name) " + " select '" + authorName + "'"
					+ "Where not exists (select * from Author where name = " + "'" + authorName + "')";

			System.out.println("newAuthorQuery: " + newAuthorQuery);
			rs = stmt.executeUpdate(newAuthorQuery);

			// Adds an entry to the Book_Author table
			String newBookAuthorQuery = "Insert into Book_Author (book_id, author_id) "
					+ " values ((select id from Book where title = '" + title + "'),"
					+ " (select id from Author where name = '" + authorName + "'))";

			System.out.println("newBookAuthorQuery: " + newBookAuthorQuery);
			rs = stmt.executeUpdate(newBookAuthorQuery);

			// Adds an entry to the Publisher table
			String newPublisherQuery = "Insert into Publisher (name) " + " select '" + publisherName + "'"
					+ "Where not exists (select id from Publisher where name = " + "'" + publisherName + "')";
			System.out.println("newPublisherQuery: " + newPublisherQuery);
			rs = stmt.executeUpdate(newPublisherQuery);

			// Updates a book's publisher id
			String updateBook_PublisherId = "Update Book Set publisher_id = (Select id from Publisher where name = "
					+ "'" + publisherName + "')" + " where title = " + "'" + title + "'";

			System.out.println("updateBook_PublisherId: " + updateBook_PublisherId);
			rs = stmt.executeUpdate(updateBook_PublisherId);

			return rs;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}
}
