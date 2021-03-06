package edu.pitt.todolist.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.pitt.todolist.model.ListTreeNode;


/**
 * <h1>AddButton</h1>
 * This class implements the {@code ActionListener} interface to add a new item to the todoList when the add button is pressed.
 * @author Noah Scholfield
 *
 */
public class AddButton implements ActionListener {
	private Controller controller;

	/**
	 * Constructor for AddButton
	 * @param ctrl The controller object.
	 */
	public AddButton(Controller ctrl) {
		controller = ctrl;
	}

	/**
	 * Implemented from the ActionListener class. Adds an item to the todoList when the add button is selected.
	 * @param event The button click event from the JButton.
	 */
	public void actionPerformed(ActionEvent event) {
		String newItemDescription = controller.getView().getTaskName();
		String userFirstName = controller.getView().getFirstName();
		String userLastName = controller.getView().getLastName();
		ListTreeNode selected = controller.getView().getSelectedItem();
		controller.getModel().addListItem(newItemDescription, userFirstName, userLastName, selected);
		controller.getView().updateList();
	}

}
