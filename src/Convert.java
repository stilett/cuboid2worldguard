/**
 * Created by IntelliJ IDEA.
 * User: ludek
 * Date: 5/30/11
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
import geometry.BlockVector;
import yaml.*;

import java.io.File;
import java.util.*;

public class Convert {
    private static CuboidPlugin cuboidPlugin;
    private static Configuration config;

    public static void main(String[] args) throws Exception {
        cuboidPlugin = new CuboidPlugin();
        CuboidAreas.loadCuboidAreas();
        System.out.println(CuboidAreas.listOfCuboids.size());
        config = new Configuration(new File("regions.yml"));


        //ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;

        
        for (CuboidC cuboid: CuboidAreas.listOfCuboids) {
            ConfigurationNode node = config.addNode("regions." + cuboid.name);
            node.setProperty("type", "cuboid");
            node.setProperty("min", new BlockVector(cuboid.coords[0], cuboid.coords[1], cuboid.coords[2]));
            node.setProperty("max", new BlockVector(cuboid.coords[3], cuboid.coords[4], cuboid.coords[5]));

            node.setProperty("priority", 0);
            node.setProperty("flags", getFlagData(cuboid));

            List<String> ownersPlayers = new ArrayList<String>();
            List<String> membersPlayers = new ArrayList<String>();
            List<String> membersGroups = new ArrayList<String>();

            for (String player: cuboid.allowedPlayers) {
                if (player.startsWith("o:")) {
                    ownersPlayers.add(player.substring(2));
                } else if (player.startsWith("g:")) {
                    membersGroups.add(player.substring(2));
                } else {
                    membersPlayers.add(player);
                }
            }

            Map<String, Object> owners = new HashMap<String, Object>();
            Map<String, Object> members = new HashMap<String, Object>();

            owners.put("players", ownersPlayers);
            if (!membersPlayers.isEmpty()) members.put("players", membersPlayers);
            if (!membersGroups.isEmpty()) members.put("groups", membersGroups);

            node.setProperty("owners", owners);
            if (!members.isEmpty())  node.setProperty("members", members);
        }

        config.save();
    }

    private static Map<String, String> getFlagData(CuboidC cuboid) {
        Map<String, String> flagData = new HashMap<String, String>();

        if (cuboid.creeper)  flagData.put("creeper-explosion", "deny");
        if (cuboid.sanctuary)  {
            flagData.put("creeper-explosion", "deny");
            flagData.put("mob-damage", "deny");
        }

        return flagData;
    }
}
