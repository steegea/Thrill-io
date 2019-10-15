package thrillio.managers;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import thrillio.constants.BookGenre;
import thrillio.constants.KidFriendlyStatus;
import thrillio.constants.MovieGenre;
import thrillio.dao.BookmarkDao;
import thrillio.entities.Book;
import thrillio.entities.Bookmark;
import thrillio.entities.Movie;
import thrillio.entities.User;
import thrillio.entities.UserBookmark;
import thrillio.entities.WebLink;
import thrillio.util.StringUtil;

public class BookmarkManager {

	private static BookmarkManager instance = new BookmarkManager();
	private static BookmarkDao dao = new BookmarkDao();

	private BookmarkManager() {
	}

	public static BookmarkManager getInstance() {
		return instance;
	}

	public Movie createMovie(long id, String title, String profileUrl, int releaseYear, String[] cast,
			String[] directors, MovieGenre genre, double imdbRating) {
		Movie movie = new Movie();

		movie.setId(id);
		movie.setTitle(title);
		movie.setProfileUrl(profileUrl);
		movie.setReleaseYear(releaseYear);
		movie.setCast(cast);
		movie.setDirectors(directors);
		movie.setGenre(genre);
		movie.setImdbRating(imdbRating);

		return movie;
	}

	public Book createBook(long id, String title, String bookUrl, String imageUrl, int publicationYear, String publisher, String[] authors, BookGenre genre,
			double goodreadsRating) {
		Book book = new Book();

		book.setId(id);
		book.setTitle(title);
		book.setBookUrl(bookUrl);
		book.setImageUrl(imageUrl);
		book.setPublicationYear(publicationYear);
		book.setPublisher(publisher);
		book.setAuthors(authors);
		book.setGenre(genre);
		book.setGoodreadsRating(goodreadsRating);

		return book;
	}

	public WebLink createWebLink(long id, String title, String imageUrl, String url, String host) {
		WebLink weblink = new WebLink();

		weblink.setId(id);
		weblink.setTitle(title);
		weblink.setImageUrl(imageUrl);
		weblink.setUrl(url);
		weblink.setHost(host);

		return weblink;
	}

	public List<List<Bookmark>> getBookmarks() {
		return dao.getBookmarks();
	}

	public void saveUserBookmark(User user, Bookmark bookmark) {
		UserBookmark userBookmark = new UserBookmark();
		userBookmark.setUser(user);
		userBookmark.setBookmark(bookmark);
		
		/*
		 * Code that downloads web link bookmarks
		 */
		
		dao.saveUserBookmark(userBookmark);
	}
	
	public void deleteUserBookmark(User user, Bookmark bookmark) {
		// TODO Auto-generated method stub
		
		UserBookmark userBookmark = new UserBookmark();
		userBookmark.setUser(user);
		userBookmark.setBookmark(bookmark);
		
		dao.deleteUserBookmark(userBookmark);
	}

	public void setKidFriendlyStatus(User user, KidFriendlyStatus kidFriendlyStatus, Bookmark bookmark) throws SQLException {
		bookmark.setKidFriendlyStatus(kidFriendlyStatus);
		bookmark.setKidFriendlyMarkedBy(user);
		
		dao.updateKidFriendlyStatus(bookmark);
		
		System.out.println(
				"Kid-friendly status: " + kidFriendlyStatus + ", Marked by: " + user.getEmail() + ", " + bookmark);

	}

	public void share(User user, Bookmark bookmark) throws SQLException {
		bookmark.setSharedBy(user);
		
		System.out.println("Data to be shared: ");
		
		if(bookmark instanceof Book)
		{
			System.out.println(((Book) bookmark).getItemData());
		} 
		
		else if(bookmark instanceof WebLink)
		{
			System.out.println(((WebLink) bookmark).getItemData());
		}
		
		dao.sharedByInfo(bookmark);
		
	}

	public Collection<Bookmark> getBooks(boolean isBookmarked, long id) {
		return dao.getBooks(isBookmarked, id);
	}
	
	
	public Collection<Bookmark> getMovies(boolean isBookmarked, long id) {
		return dao.getMovies(isBookmarked, id);
	}

	public Bookmark getBook(long bid) {
		return dao.getBook(bid);
	}
	
	public long addNewBookToDatabase(String title, String authorName, String bookUrl, String imageUrl, String publisherName, int publicationYear, /*long publisherId, int genre_id,*/ double goodreadsRating) {
		
		return dao.addNewBookToDatabase(title, authorName, bookUrl, imageUrl, publisherName, publicationYear, /*publisherId, genre_id,*/ goodreadsRating); //Substitute Goodreads rating for Amazon rating
	}

	
	/*
	public Collection<Bookmark> getWebLinks(boolean isBookmarked, long id) {
		return dao.getWebLinks(isBookmarked, id);
	}
	*/
}
