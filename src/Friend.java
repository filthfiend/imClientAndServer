import java.io.*;

public class Friend implements Serializable{
	private String friendName;
	public Friend(String friendName)
	{
		this.friendName = friendName;
	}
	public String getFriendName()
	{
		return friendName;
	}

}
