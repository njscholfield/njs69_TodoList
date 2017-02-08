package edu.pitt.todolist.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

/**
 * <h1>Model</h1>
 * This class handles the storage and editing of the todoList data.
 * @author Noah Scholfield
 *
 */
public class Model {
	private Vector<User> users;
	private HashMap<ListItem, User> data;
	private Connection connection;

	/**
	 * Creates a new model object.
	 */
	public Model() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection("jdbc:mysql://sis-teach-01.sis.pitt.edu/njs69is1017", "njs69is1017", "njs69@pitt.edu");
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} catch(InstantiationException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		fetchUsersFromDatabase(); // fetches users from the database
		fetchItemsFromDatabase(); // fetches tasks and their owners
	}

	private void fetchItemsFromDatabase() {
		data = new HashMap<ListItem, User>();
		try {
			String query = "SELECT todolist.id AS list_id, description, timestamp, user.id AS user_id, firstname, lastname " +
					"FROM todolist JOIN user_todo JOIN user " + 
					"ON todolist.id = user_todo.todo_id AND user.id = user_todo.user_id " + 
					"ORDER BY todolist.id;";
			Statement statement = connection.createStatement();
			ResultSet rs =  statement.executeQuery(query);
			
			while(rs.next()) {
				ListItem item = new ListItem(rs.getInt("list_id"), rs.getString("description"), rs.getTimestamp("timestamp"));
				User user = getUser(rs.getInt("user_id"));
				if(user == null) {
					System.out.println("Somehow a user got missed...");
					user = new User(rs.getInt("user_id"), rs.getString("firstname"), rs.getString("lastname"));
					users.addElement(user);
				}
				data.put(item, user);
			}
		} catch(SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void fetchUsersFromDatabase() {
		users = new Vector<User>();
		try {
			String query = "SELECT * FROM user;";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			
			while(rs.next()) {
				users.addElement(new User(rs.getInt("id"), rs.getString("firstname"), rs.getString("lastname")));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Adds a new item to the todoList.
	 * @param itemDescription The description of the new item.
	 * @return The new ListItem object that was added.
	 */
	public void addListItem(String itemDescription, String firstName, String lastName) {
		if(itemDescription.equals("")) {
			return;
		}
		String addItem = "INSERT INTO todolist (description) VALUES ('" + itemDescription + "');";
		String getNewItem = "SELECT * FROM todolist WHERE description = '" + itemDescription + "';";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(addItem);
			ResultSet rs = statement.executeQuery(getNewItem);
			rs.next();
			ListItem newItem = new ListItem(rs.getInt("id"), rs.getString("description"), rs.getTimestamp("timestamp"));
			User user = getOrCreateUser(firstName, lastName);
			String createConnection = "INSERT INTO user_todo (user_id, todo_id) VALUES (" + user.getID() + ", " + newItem.getID() + ");";
			statement.executeUpdate(createConnection);
			data.put(newItem, user);
		} catch(SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			// will throw an exception if there is a duplicate
		}
	}
	
	public User getUser(int id) {
		for(User user : users) {
			if(user.getID() == id) {
				return user;
			}
		}
		return null; // return null if not found
	}
	
	public User getOrCreateUser(String fn, String ln) {
		for(User user : users) {
			if(user.getFirstName().equals(fn) && user.getLastName().equals(ln)) {
				return user;
			}
		}
		createUser(fn, ln);
		return getOrCreateUser(fn, ln);
	}
	
	public void createUser(String fn, String ln) {
		try {
			String addNewUser = "INSERT INTO user (firstname, lastname) VALUES ('" + fn + "', '" + ln + "');";
			Statement statement = connection.createStatement();
			statement.executeUpdate(addNewUser);
			fetchUsersFromDatabase();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes the passed item from the todoList.
	 * @param item The item to be removed from the todoList.
	 */
	public void deleteListItem(ListItem item) {
		String deleteItem = "DELETE FROM todolist WHERE description = '" + item.getDescription() + "';";
		String removePairing = "DELETE FROM user_todo WHERE todo_id = " + item.getID() + ";";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(deleteItem);
			statement.executeUpdate(removePairing);
			
			data.remove(item);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the todoList.
	 * @return The todoList Vector.
	 */
	public HashMap<ListItem, User> getList() {
		return data;
	}
}
