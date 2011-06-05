import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class CuboidB implements Serializable{
	String name= "noname";
	int[] coords = new int[6];	//	int[]{firstX, firstY, firstZ, secondX, secondY, secondZ}
	boolean protection = false;
	boolean restricted = false;
	boolean inventories = false;
	boolean PvP = true;
	boolean heal = false;
	boolean creeper = true;
	boolean sanctuary = false;
	ArrayList<String> allowedPlayers = new ArrayList<String>();
	String welcomeMessage = null;
	String farewellMessage = null;
	String warning = null;
	ArrayList<String> presentPlayers = new ArrayList<String>();
	ArrayList<String> disallowedCommands = new ArrayList<String>();

	public CuboidB(){}

	public boolean contains(int X, int Y, int Z){
		if( X >= coords[0] && X <= coords[3] && Z >= coords[2] && Z <= coords[5] && Y >= coords[1] && Y <= coords[4])
			return true;
		return false;
	}

}