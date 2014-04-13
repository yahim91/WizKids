package app;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import network.Network;
import statusbar.StatusBar;
import table.ProgressTableModel;

public interface IMediator {

	public void registerProgressTableModel(ProgressTableModel tm);
	
	public void registerStatusBar(StatusBar sb);
	public void registerUserListModel(DefaultListModel<UserFiles> uf);
	public void considerUser(UserFiles uf);
	public void registerFilesModel(DefaultListModel<String> files);
	public void registerNetwork(Network network);
	public void registerConfig(Config config);
	public void considerFile(int indexU, int indexF);
	public void addUser(String name, ArrayList<String> files);
	public void updateUserInfo(String name, Integer port, String address);
	public void updateUserFiles(String name, ArrayList<String> files);
}
