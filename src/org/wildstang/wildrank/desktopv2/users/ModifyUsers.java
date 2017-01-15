package org.wildstang.wildrank.desktopv2.users;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.wildstang.wildrank.desktopv2.DatabaseManager;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;

/*
 * ModifyUsers is a window that is used to add or edit users
 */
public class ModifyUsers extends JPanel implements ActionListener {
	JButton save;
	JButton add;
	JButton read;
	JTable table;

	List<User> users = new ArrayList<>();

	public ModifyUsers() {
		super(new BorderLayout());
		
		// Create control buttons
		save = new JButton("Save");
		save.addActionListener(this);
		add = new JButton("Add Another");
		add.addActionListener(this);
		read = new JButton("Read from CSV");
		read.addActionListener(this);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		buttons.add(add);
		buttons.add(save);
		buttons.add(read);
		add(buttons, BorderLayout.PAGE_START);
		
		// Create the users table
		table = new JTable(new UserTableModel(users));
		table.setPreferredScrollableViewportSize(new Dimension(500, 300));
		table.setFillsViewportHeight(true);
		
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
		
		// Load users
		try {
			loadUsers();
		} catch (IOException | CouchbaseLiteException e) {
			e.printStackTrace();
		}
		updateTable();
	}

	// Loads users into memory from database
	public void loadUsers() throws IOException, CouchbaseLiteException {
		Query query = DatabaseManager.getInstance().getAllUsers();
		QueryEnumerator enumerator = query.run();
		List<QueryRow> rows = new ArrayList<>();
		for (Iterator<QueryRow> it = enumerator; it.hasNext();) {
			rows.add(it.next());
		}

		for (int i = 0; i < rows.size(); i++) {
			Document document = rows.get(i).getDocument();
			if (document != null) {
				Map<String, Object> user = document.getProperties();
				users.add(new User((String) user.get("id"), (String) user.get("name"), (Boolean) user.get("admin")));
			} else {
				System.out.println("Document is null");
			}
		}
	}

	// responds to button presses
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(add)) {
			// Add a blank user and rerender the table
			users.add(new User());
			updateTable();
		} else if (e.getSource().equals(save)) {
			saveToDatabase();
		} else if (e.getSource().equals(read)) {
			// Prompt for location of CSV file
			JFileChooser chooser = new JFileChooser();
			File startFile = new File(System.getProperty("user.home"));
			chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select the Local location");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
			chooser.setFileFilter(filter);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					// when it is found read the users from the file
					readFromCSV(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			updateTable();
		}
	}
	
	private void updateTable() {
		((UserTableModel) table.getModel()).fireTableDataChanged();
	}

	public void saveToDatabase() {
		System.out.println("Saving users");

		try {
			Database database = DatabaseManager.getInstance().getDatabase();

			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i);

				Map<String, Object> usermap = new HashMap<>();
				usermap.put("id", user.id);
				usermap.put("name", user.name);
				usermap.put("admin", user.admin);
				usermap.put("type", "user");
				System.out.println("User " + i + ": " + usermap.toString());

				Document document = database.getDocument("user:" + user.id);
				UnsavedRevision revision = document.createRevision();
				revision.setProperties(usermap);
				revision.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// creates users and populates the window based on data from a csv file
	public void readFromCSV(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		List<String[]> rawUsers = new ArrayList<>();
		// creates a series of strings from the file (each line should be a
		// user)
		while ((line = br.readLine()) != null) {
			rawUsers.add(line.split(","));
		}
		br.close();
		// parses through strings
		for (int i = 0; i < rawUsers.size(); i++) {
			String id = rawUsers.get(i)[0].replace("\"", "");
			String name = rawUsers.get(i)[1].replace("\"", "");
			Boolean admin = Boolean.parseBoolean(rawUsers.get(i)[2].replace("\"", ""));
			// creates a new userrow for each found user
			users.add(new User(id, name, admin));
		}
	}
}
