import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Cuboid implements Serializable{
	// old data format, kept here for retro-compatibility
	String name;
	int[] coords;	 //	int[]{firstX, firstY, firstZ, secondX, secondY, secondZ}
	boolean protection;
	boolean restricted;
	boolean inventories;
	ArrayList<String> allowedPlayers;
	String welcomeMessage;
	String farewellMessage;
	String warning;
	ArrayList<String> presentPlayers;
	ArrayList<String> disallowedCommands;

	public Cuboid(){
		this.name = "noname";
		this.coords = new int[6];
		this.allowedPlayers = new ArrayList<String>();
		this.protection = false;
		this.restricted = false;
		this.inventories = false;
		this.warning = null;
		this.welcomeMessage = null;
		this.farewellMessage = null;
		this.presentPlayers = new ArrayList<String>();
		this.disallowedCommands = new ArrayList<String>();
	}

	public boolean contains(int X, int Y, int Z){
		if( X >= coords[0] && X <= coords[3] && Z >= coords[2] && Z <= coords[5] && Y >= coords[1] && Y <= coords[4])
			return true;
		return false;
	}
}