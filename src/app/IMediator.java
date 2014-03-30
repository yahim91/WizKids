package app;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import statusbar.StatusBar;
import table.ProgressTableModel;

public interface IMediator {

	public void registerProgressTableModel(ProgressTableModel tm);
	public void registerStatusBar(StatusBar sb);
	public void registerUserListModel(DefaultListModel<UserFiles> uf);
	public void considerUser(UserFiles uf);
	public void registerFilesModel(DefaultListModel<String> files);
	public void considerFile(int indexU, int indexF);
	public void addUser(String name, ArrayList<String> files);
	public void updateUserFiles(Integer id, ArrayList<String> files);
	public Config getConfig();
}
