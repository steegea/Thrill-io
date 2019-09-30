package thrillio.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import thrillio.DataStore;
import thrillio.constants.Gender;
import thrillio.constants.UserType;
import thrillio.entities.User;
import thrillio.managers.UserManager;

public class UserDao {
	
	
	public List<User> getUsers(){
		return DataStore.getUsers();
	}

	public User getUser(long userId) {
		User user = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false", "root", "root12345");
				Statement stmt = conn.createStatement();) {	
			String query = "Select * from User where id = " + userId;
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				long id = rs.getLong("id");
				String email = rs.getString("email");
				String password = rs.getString("password");
				String firstName = rs.getString("first_name");
				String lastName = rs.getString("last_name");
				int gender_id = rs.getInt("gender_id");
				Gender gender = Gender.values()[gender_id];
				int user_type_id = rs.getInt("user_type_id");
				UserType userType = UserType.values()[user_type_id];
				
				user = UserManager.getInstance().createUser(id, email, password, firstName, lastName, gender, userType);
	    	}			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return user;
		
	}

	public long authenticate(String email, String password) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false", "root", "root12345");
				Statement stmt = conn.createStatement();) {	
			String query = "Select id from User where email = '" + email + "' and password = '" + password + "'";
			System.out.println("query: " + query);
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				return rs.getLong("id");				
	    	}			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return -1;
	}
	
	
	public long newSignUp(String email, String password, String firstName, String lastName, String gender) {
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		System.out.println(dtf.format(now));  
		   
		   
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jid_thrillio?useSSL=false", "root", "root12345");
				Statement stmt = conn.createStatement();) {
			
			
			if(gender.equalsIgnoreCase("Male")) {
				String query = "Insert into User (email, password, first_name, last_name, gender_id, user_type_id, created_date) " +
						" values('" + email + "', '" + password + "', '" + firstName + "', '" + lastName + "' , '" + Gender.MALE.ordinal() + "' , '" + 0 + "' , '" + dtf.format(now) + "' " + ")"; 
					System.out.println("query: " + query);
					int rs = stmt.executeUpdate(query);
					
					return rs;
			}
			
			else if(gender.equalsIgnoreCase("Female")) {
				String query = "Insert into User (email, password, first_name, last_name, gender_id, user_type_id, created_date) " +
						" values('" + email + "', '" + password + "', '" + firstName + "', '" + lastName + "' , '" + Gender.FEMALE.ordinal() + "' , '" + 0 + "' , '" + dtf.format(now) + "' " + ")"; 
					System.out.println("query: " + query);
					int rs = stmt.executeUpdate(query);
					
					return rs;
			}
			
			else if(gender.equalsIgnoreCase("Other")){
				String query = "Insert into User (email, password, first_name, last_name, gender_id, user_type_id, created_date) " +
						" values('" + email + "', '" + password + "', '" + firstName + "', '" + lastName + "' , '" + Gender.OTHER.ordinal() + "' , '" + 0 + "' , '" + dtf.format(now) + "' " + ")"; 
					System.out.println("query: " + query);
					int rs = stmt.executeUpdate(query);
					
					return rs;
			}
			
			else {
				String insertQuery = "Insert into User (email, password, first_name, last_name, gender_id, user_type_id, created_date) " +
						" values('" + email + "', '" + password + "', '" + firstName + "', '" + lastName + "' , '" + 0 + "' , '" + 0 + "' , '" + dtf.format(now) + "' " + ")";
					 
				
				String noGenderSpecifiedQuery = " UPDATE User SET gender_id=null WHERE email=" + " '" + email + "'" + ";";
					System.out.println("insertQuery: " + insertQuery);
					System.out.println("noGenderSpecifiedQuery: " + insertQuery);
					
					int rs = stmt.executeUpdate(insertQuery);
					rs = stmt.executeUpdate(noGenderSpecifiedQuery);
					
					return rs;
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		return -1;
	}
	
}
