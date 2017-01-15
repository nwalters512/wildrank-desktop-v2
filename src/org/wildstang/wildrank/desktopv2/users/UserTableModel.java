package org.wildstang.wildrank.desktopv2.users;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UserTableModel extends AbstractTableModel {

	public static final int COLUMN_NAME_INDEX = 0;
	public static final int COLUMN_ID_INDEX = 1;
	public static final int COLUMN_ADMIN_INDEX = 2;

	private static final String[] COLUMN_NAMES = { "Name", "ID", "Admin" };

	private List<User> users;

	public UserTableModel(List<User> users) {
		this.users = users;
	}

	@Override
	public int getRowCount() {
		return users.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return getValueAt(0, columnIndex).getClass();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		User user = users.get(rowIndex);

		switch (columnIndex) {
		case COLUMN_NAME_INDEX:
			return user.name;
		case COLUMN_ID_INDEX:
			return user.id;
		case COLUMN_ADMIN_INDEX:
			return user.admin;
		default:
			return null;
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Don't allow editing of ID; should be stable for any user
		return columnIndex != COLUMN_ID_INDEX;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		User user = users.get(rowIndex);
		switch (columnIndex) {
		case COLUMN_NAME_INDEX:
			user.name = (String) aValue;
			break;
		case COLUMN_ID_INDEX:
			user.id = (String) aValue;
			break;
		case COLUMN_ADMIN_INDEX:
			user.admin = (Boolean) aValue;
			break;
		default:
			return;
		}
	}

}
