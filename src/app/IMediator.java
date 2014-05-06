package app;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import network.Network;
import network.WebServerClient;
import statusbar.StatusBar;
import table.ProgressTableModel;
import table.RowData;

public interface IMediator {

	public void registerProgressTableModel(ProgressTableModel tm);

	public void registerStatusBar(StatusBar sb);

	public void registerUserListModel(DefaultListModel<UserFiles> uf);

	public void registerWebClient(WebServerClient s);

	public void considerUser(UserFiles uf);

	public void registerFilesModel(DefaultListModel<String> files);

	public void registerNetwork(Network network);

	public void registerConfig(Config config);

	public void considerFile(int indexU, int indexF);

	public ProgressTableModel getTableModel();

	public String getUserName();

	public void addUser(String name, ArrayList<String> files, String address,
			Integer port);

	public void updateUserInfo(String name, Integer port, String address);

	public void updateUserFiles(String name, ArrayList<String> files);

	public ArrayList<String> getUserFiles(String name);

	public void displayMessage(String message);

	public void publishUser();

	public void unpublishUser();

	public String getOwnFiles();

	public Integer getPort();

	public String getAddress();

	public void updateUsers();

	public void sendUpdateFiles();

	public void updateUsersList(ArrayList<String> names,
			ArrayList<Integer> ports, ArrayList<String> addresses);
	
	public void addNewOwnFile(String fileName);
}
