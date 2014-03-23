package app;
import java.util.Vector;

public class UserFiles {
	private String userName;
	private Vector<String> files;
	
	public UserFiles(String userName, Vector<String> files) {
		this.userName = userName;
		this.files = files;
	}
	
	public String getName() {
		return userName;
	}
	
	public Vector<String> getFiles() {
		return files;
	}
	
	@Override
	public String toString() {
		return userName;
	}
}
