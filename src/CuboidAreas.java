import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;

/*
 * This is the interface for stored cuboidB areas.
 * Loading / Saving happens here, with all needed retro-compatibility
 * In the longrun, I want to replace ArrayList<CuboidB> by a Hilbert R-tree, to optimize performance
 */

public class CuboidAreas {
	static ArrayList<CuboidC> listOfCuboids;
	static HashMap<String, ArrayList<CuboidC>> inside;

	static Timer healTimer = new Timer();
	static int healPower = 0;
	static long healDelay = 1000;

	static String SQLdriver = "com.mysql.jdbc.Driver";
	static String SQLusername = "root";
	static String SQLpassword = "root";
	static String SQLdb = "jdbc:mysql://localhost:3306/minecraft";

	static String currentDataVersion = "C";
	static int addedHeight = 0;
	static boolean newestHavePriority = true;

	@SuppressWarnings("unchecked")
	public static void loadCuboidAreas(){
		listOfCuboids = new ArrayList<CuboidC>();

		File dataSource = new File("cuboids/cuboidAreas.dat");
		if ( !dataSource.exists() ){
			if ( new File("cuboids/protectedCuboids.txt").exists() )
				readFromTxtFile();
			else
				CuboidPlugin.log.info("CuboidPlugin : No datafile to load from (its fine)");
			return;
		}
		try {
			File versionFile = new File("cuboids/cuboidAreas.version");
			if ( !versionFile.exists() ){
				readFromOldFormat(null);
				return;
			}
			String dataVersion = new Scanner(versionFile).nextLine();
			if ( dataVersion.equalsIgnoreCase(currentDataVersion) ){
				ObjectInputStream ois =
					new ObjectInputStream(
						new BufferedInputStream(
							new FileInputStream(
								new File("cuboids/cuboidAreas.dat"))));
		       listOfCuboids = (ArrayList<CuboidC>)( ois.readObject() );
		       ois.close();
		       CuboidPlugin.log.info("CuboidPlugin : cuboidAreas.dat (format " + currentDataVersion + ") loaded");
			}
			else{
				readFromOldFormat(dataVersion);
			}
		} catch (Exception e) {
            e.printStackTrace();
			CuboidPlugin.log.severe("Cuboid plugin : Error while reading cuboidAreas.dat");
		}
	}

	public static void readFromTxtFile(){
		CuboidPlugin.log.info("CuboidPlugin : protectedCuboids.txt found, initializing conversion...");
		File dataSource = new File("cuboids/protectedCuboids.txt");
		try {
			Scanner scanner = new Scanner(dataSource);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("#") || line.equals("")) {
					continue;
				}
				String[] donnees = line.split(",");
				if (donnees.length < 8) {
					continue;
				}

				CuboidC newArea = new CuboidC();
				int[] tempCoords = new int[6];
				for (short i=0; i<6; i++){
					tempCoords[i] = Integer.parseInt(donnees[i]);
				}
				newArea.coords[0] = (tempCoords[0] <= tempCoords[3]) ? tempCoords[0] : tempCoords[3];
				newArea.coords[1] = (tempCoords[1] <= tempCoords[4]) ? tempCoords[1] : tempCoords[4];
				newArea.coords[2] = (tempCoords[2] <= tempCoords[5]) ? tempCoords[2] : tempCoords[5];
				newArea.coords[3] = (tempCoords[0] >= tempCoords[3]) ? tempCoords[0] : tempCoords[3];
				newArea.coords[4] = (tempCoords[1] >= tempCoords[4]) ? tempCoords[1] : tempCoords[4];
				newArea.coords[5] = (tempCoords[2] >= tempCoords[5]) ? tempCoords[2] : tempCoords[5];
				for (String owner : donnees[6].trim().split(" ")){
					newArea.allowedPlayers.add(owner);
				}
				newArea.name = donnees[7];
				newArea.protection = true;
				listOfCuboids.add(newArea);
			}
			scanner.close();
			CuboidPlugin.log.info("CuboidPlugin : Conversion to new format successful.");
			CuboidPlugin.log.info("CuboidPlugin : cuboids areas loaded.");
		} catch (Exception e) {
			CuboidPlugin.log.severe("Cuboid plugin : Error while reading protectedCuboids.txt");
		}
	}

	@SuppressWarnings("unchecked")
	public static void readFromOldFormat( String dataVersion ){
		if ( dataVersion == null ){
			ArrayList<Cuboid> oldCuboids;
			try {
				ObjectInputStream ois =
					new ObjectInputStream(
						new BufferedInputStream(
							new FileInputStream(
								new File("cuboids/cuboidAreas.dat"))));
				oldCuboids = (ArrayList<Cuboid>)( ois.readObject() );
				ois.close();
			}
			catch (Exception e) {
				oldCuboids = new ArrayList<Cuboid>();
				CuboidPlugin.log.severe("Cuboid plugin : Error while reading cuboidAreas.dat");
			}

			listOfCuboids = new ArrayList<CuboidC>();
			for ( Cuboid oldCuboid : oldCuboids ){
				CuboidC newCuboid = new CuboidC();
				newCuboid.name = oldCuboid.name;
				newCuboid.coords = oldCuboid.coords;
				newCuboid.allowedPlayers = oldCuboid.allowedPlayers;
				newCuboid.protection = oldCuboid.protection;
				newCuboid.restricted = oldCuboid.restricted;
				newCuboid.warning = oldCuboid.warning;
				newCuboid.welcomeMessage = oldCuboid.welcomeMessage;
				newCuboid.farewellMessage = oldCuboid.farewellMessage;
				newCuboid.disallowedCommands = oldCuboid.disallowedCommands;
				listOfCuboids.add( newCuboid );
			}

			CuboidPlugin.log.info("CuboidPlugin : conversion of cuboidAreas.dat from f.A to f.B sucessful");
			CuboidPlugin.log.info("CuboidPlugin : cuboidAreas.dat loaded.");
		}
		else if ( dataVersion.equalsIgnoreCase("B")){
			ArrayList<CuboidB> oldCuboids;
			try {
				ObjectInputStream ois =
					new ObjectInputStream(
						new BufferedInputStream(
							new FileInputStream(
								new File("cuboids/cuboidAreas.dat"))));
				oldCuboids = (ArrayList<CuboidB>)( ois.readObject() );
				ois.close();
			}
			catch (Exception e) {
				oldCuboids = new ArrayList<CuboidB>();
				CuboidPlugin.log.severe("Cuboid plugin : Error while reading cuboidAreas.dat");
			}

			listOfCuboids = new ArrayList<CuboidC>();
			for ( CuboidB oldCuboid : oldCuboids ){
				CuboidC newCuboid = new CuboidC();
				newCuboid.name = oldCuboid.name;
				newCuboid.coords = oldCuboid.coords;
				newCuboid.allowedPlayers = oldCuboid.allowedPlayers;
				newCuboid.protection = oldCuboid.protection;
				newCuboid.restricted = oldCuboid.restricted;
				newCuboid.warning = oldCuboid.warning;
				newCuboid.welcomeMessage = oldCuboid.welcomeMessage;
				newCuboid.farewellMessage = oldCuboid.farewellMessage;
				newCuboid.disallowedCommands = oldCuboid.disallowedCommands;
				listOfCuboids.add( newCuboid );
			}

			CuboidPlugin.log.info("CuboidPlugin : conversion of cuboidAreas.dat from f.B to f.C sucessful");
			CuboidPlugin.log.info("CuboidPlugin : cuboidAreas.dat loaded.");
		}
		else{
			CuboidPlugin.log.severe("CuboidPlugin : unsupported data version");
			listOfCuboids = new ArrayList<CuboidC>();
		}
	}
    

	public static void updateSQL(CuboidC cuboid){
		// // SQL
		Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    try {
	    	conn = DriverManager.getConnection(SQLdb, SQLusername, SQLpassword);
	        ps = conn.prepareStatement("INSERT INTO cuboidAreas (name, X1, Y1, Z1, X2, Y2, Z2, protection, restriction," +
	        		" trespassing, pvp, heal, creeper, sanctuary, welcome, farewell, warning, owners, present" +
	        		"cmdblacklist, playersINVs)" + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
	        		, Statement.RETURN_GENERATED_KEYS);
	        ps.setString(1, cuboid.name);
	        ps.setInt(2, cuboid.coords[0]);
	        ps.setInt(3, cuboid.coords[1]);
	        ps.setInt(4, cuboid.coords[2]);
	        ps.setInt(5, cuboid.coords[3]);
	        ps.setInt(6, cuboid.coords[4]);
	        ps.setInt(7, cuboid.coords[5]);
	        ps.setBoolean(8, cuboid.protection);
	        ps.setBoolean(9, cuboid.restricted);
	        ps.setBoolean(10, cuboid.trespassing);
	        ps.setBoolean(11, cuboid.PvP);
	        ps.setBoolean(12, cuboid.heal);
	        ps.setBoolean(13, cuboid.creeper);
	        ps.setBoolean(14, cuboid.sanctuary);
	        ps.setString(15, cuboid.welcomeMessage);
	        ps.setString(16, cuboid.farewellMessage);
	        ps.setString(17, cuboid.warning);
	        // owners
	        // cmdblacklist
	        // playersINVs
	        ps.executeUpdate();
	    }
	    catch (SQLException ex) {
	    	CuboidPlugin.log.severe("Unable to log alert into SQL");
	    }
	    finally {
	        try {
	            if (ps != null)
	                ps.close();
	            if (rs != null)
	                rs.close();
	            if (conn != null)
	                conn.close();
	        } catch (SQLException ex) {}
	    }
	}

	////////////////////////////
	////    DATA SENDING    ////
	////////////////////////////

	public static CuboidC findCuboidArea(int X, int Y, int Z){
		CuboidC lastEntry = null;
		for (CuboidC cuboid : listOfCuboids){
			if ( cuboid.contains(X, Y, Z) ){
				if ( newestHavePriority ){
					lastEntry = cuboid;
				}
				else{
					return cuboid;
				}
			}
		}
		return lastEntry;
	}

	public static CuboidC findCuboidArea(String cuboidName){
		CuboidC lastEntry = null;
		for (CuboidC cuboid : listOfCuboids){
			if ( cuboid.name.equalsIgnoreCase(cuboidName) ){
				if ( newestHavePriority ){
					lastEntry = cuboid;
				}
				else{
					return cuboid;
				}
			}
		}
		return lastEntry;
	}
}
