package app;
import java.util.ArrayList;

public class UserFiles {
	private Integer id;
	private String userName;
	private ArrayList<String> files;
	private IMediator mediator;
	
	public UserFiles(String userName, ArrayList<String> files, IMediator med) {
		this.userName = userName;
		this.files = files;
		this.mediator = med;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public String getName() {
		return userName;
	}
	
	public ArrayList<String> getFiles() {
		return files;
	}
	
	@Override
	public String toString() {
		return userName;
	}
	
	public void updateFiles(ArrayList<String> files) {
		this.files = files;
		this.mediator.considerUser(this);
	}
}
