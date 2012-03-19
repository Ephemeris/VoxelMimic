package com.thevoxelbox.voxelmimic;

import java.io.File;
import java.util.Scanner;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.*;
import org.bukkit.Location;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 *
 * @author Gavjenks
 */
public class VoxelMimic extends JavaPlugin {  // I changed your unmimicable to a HashSet so if it contains it its as if it was holing a boolean true under the map key but without the key ...

    public static HashMap<String, Boolean> vGuests = new HashMap<String, Boolean>();
    protected static final Logger log = Logger.getLogger("Minecraft");
    public static World w;
    public static Server s;
    public static Boolean voxelGuest = false; //do you have voxelguest installed?  false by default
    public static Boolean voxelGuestMimic = false; //can guests mimic?
    public static HashSet<Integer> unmimicable = new HashSet<Integer>();
    public static int RADIUS = 35;

    @Override
    public void onDisable() {
    
    }

    @Override
    public void onEnable() {
        w = this.getServer().getWorlds().get(0);
        s = this.getServer();
        readMembers();
        loadConfig();
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        String commandName = command.getName().toLowerCase();
        Player player = (Player) sender;


        if (commandName.startsWith("mimic") && sender instanceof Player) {
            int radius;
            try {
                try {
                    radius = Integer.parseInt(args[0]);
                    if (voxelGuest && !voxelGuestMimic && !((vGuests.get(player.getName()) == null)) || voxelGuest && voxelGuestMimic || !voxelGuest) {
                        if (radius > RADIUS) {
                            radius = RADIUS;
                            player.sendMessage(ChatColor.RED + "Radius too large.  Reset to maximum of " + RADIUS);
                        }
                        performMimic(player, radius);
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "You are not permitted to use this command.");
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (voxelGuest && !voxelGuestMimic && !((vGuests.get(player.getName()) == null)) || (voxelGuest && voxelGuestMimic) || !voxelGuest) {
                        performMimic(player, RADIUS);
                        player.sendMessage("himom");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "You are not permitted to use this command.");
                        return true;
                    }
                }
            } catch (Exception ex) {
                player.sendMessage(ChatColor.RED + "Invalid mimic parameters!  /mimic  OR   /mimic [radius]");
                return true;
            }
        }
        return false;
    }

    public void readMembers() {
        try {
            File f = new File("plugins/VoxelGuest/members.txt");
            if (f.exists()) {
                Scanner snr = new Scanner(f);
                while (snr.hasNext()) {
                    String st = snr.nextLine();
                    vGuests.put(st, Boolean.TRUE);
                }
                snr.close();
                voxelGuest = true;
            } else {
                voxelGuest = false;
            }
        } catch (Exception e) {
            voxelGuest = false;
        }
    }

    @Override
    public void saveConfig() { // This... seems... fishy...
        try {
            File f = new File("plugins/VoxelMimic/mimicconfig.txt");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                PrintWriter pw = new PrintWriter(f);
                pw.write("voxelGuest:" + voxelGuest.toString() + "\r\n");
                pw.write("voxelGuestMimic:" + voxelGuestMimic.toString() + "\r\n");
                pw.write("RADIUS:" + RADIUS + "\r\n");
                unmimicable.add(0);
                unmimicable.add(1);
                unmimicable.add(2);
                unmimicable.add(3);
                unmimicable.add(12);
                unmimicable.add(13);
                unmimicable.add(7);
                unmimicable.add(8);
                unmimicable.add(9);
                unmimicable.add(10);
                unmimicable.add(11);
                unmimicable.add(46);
                unmimicable.add(51);
                unmimicable.add(66);
                unmimicable.add(79);
                unmimicable.add(59);
                unmimicable.add(62);
                unmimicable.add(75);
                pw.write("unmimicable:");
                for (int id : unmimicable) {
                    pw.write(id + ",");
                }
                pw.close();
                log.info("[VoxelMimic] Config saved");
            }
        } catch (Exception e) {
            log.warning("[VoxelMimic] Error while saving mimicconfig.txt");
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            File f = new File("plugins/VoxelMimic/mimicconfig.txt");
            if (f.exists()) {
                Scanner snr = new Scanner(f);
                int type = 0;
                voxelGuest = Boolean.parseBoolean(snr.nextLine().split(":")[1]);
                voxelGuestMimic = Boolean.parseBoolean(snr.nextLine().split(":")[1]);
                RADIUS = Integer.parseInt(snr.nextLine().split(":")[1]);
                String[] i = snr.nextLine().split(":")[1].split(",");
                for (String str : i) {
                    if (str != null) {
                        unmimicable.add(Integer.parseInt(str));
                    }
                }
                snr.close();
                log.info("[VoxelMimic] Config loaded");
            } else {
                saveConfig();
            }
        } catch (Exception e) {
            log.warning("[VoxelMimic] Error while loading mimicconfig.txt");
            e.printStackTrace();
        }
    }

    public void performMimic(Player p, int radius) {


        int freqArray[][] = new int[200][16]; //holder for counts of each blocktype, and 15 durability levels for wool

        //get player location
        Location location = p.getLocation();
        int xPos = location.getBlockX();
        int yPos = -1;
        if (location.getBlockY() - (int) radius / 5 > 0) {//this is a hemispehre the top half of a sphere.  This makes it positioned so you still get some blocks a little below you.
            yPos = location.getBlockY() - (int) radius / 5;
        } else {
            yPos = location.getBlockY(); //in case player is just barely above bedrock
        }
        int zPos = location.getBlockZ();

        Inventory inv = p.getInventory();
        int data = 0;
        p.sendMessage("himom2");
        //may want to then use getContents which gives a whole array if stacks, then setContents.
        //get block type frequency within a hemisphere
        for (int x = radius; x >= 0; x--) {
            double xPow = Math.pow(x,2);
            for (int y = radius; y >= 0; y--) {
                double yPow = Math.pow(y,2);
                for (int z = radius; z >= 0; z--) {
                    if (xPow + yPow + Math.pow(z, 2) <= Math.pow(radius + 0.5, 2)) {
                        data = (w.getBlockAt(xPos + x, yPos + y, zPos + z).getData());
                        if (data > 15) {
                            data = 15;
                        }
                        freqArray[w.getBlockTypeIdAt(xPos + x, yPos + y, zPos + z)][data]++;
                        data = (w.getBlockAt(xPos + x, yPos + y, zPos - z).getData());
                        if (data > 15) {
                            data = 15;
                        }
                        freqArray[w.getBlockTypeIdAt(xPos + x, yPos + y, zPos - z)][data]++;
                        //data = (w.getBlockAt(xPos + x, yPos - y, zPos + z).getData());
                        //if (data > 15){data = 15;}
                        //freqArray[w.getBlockTypeIdAt(xPos + x, yPos - y, zPos + z)][data]++;  //uncomment for full sphere version.
                        //data = (w.getBlockAt(xPos + x, yPos - y, zPos - z).getData());
                        //if (data > 15){data = 15;}
                        //freqArray[w.getBlockTypeIdAt(xPos + x, yPos - y, zPos - z)][data]++;
                        data = (w.getBlockAt(xPos - x, yPos + y, zPos + z).getData());
                        if (data > 15) {
                            data = 15;
                        }
                        freqArray[w.getBlockTypeIdAt(xPos - x, yPos + y, zPos + z)][data]++;
                        data = (w.getBlockAt(xPos - x, yPos + y, zPos - z).getData());
                        if (data > 15) {
                            data = 15;
                        }
                        freqArray[w.getBlockTypeIdAt(xPos - x, yPos + y, zPos - z)][data]++;
                        //data = (w.getBlockAt(xPos - x, yPos - y, zPos + z).getData());
                        //if (data > 15){data = 15;}
                        //freqArray[w.getBlockTypeIdAt(xPos - x, yPos - y, zPos + z)][data]++;
                        //data = (w.getBlockAt(xPos - x, yPos - y, zPos - z).getData());
                        //if (data > 15){data = 15;}
                        //freqArray[w.getBlockTypeIdAt(xPos - x, yPos - y, zPos - z)][data]++;
                    }
                }
            }
        }
        p.sendMessage("himom3");
        for (int k = 0; k <= 199; k++) { //ignore anything specified on config file.  By default, some common natural stuff, and explosives and things.
            if (unmimicable.contains(k)) {
                for (int l = 0; l <= 15; l++) {
                    freqArray[k][l] = 0;
                }
            }
        }

        int sum = 0;
        for (int k = 0; k <= 199; k++) { //only one data value type for anything whose data value is irrelevant to inventory (like torches)
            if (k == 66 || k == 18 || k == 84 || k == 61 || k == 62 || k == 55 || k == 92 || k == 8 || k == 10 || k == 23 || k == 25 || k == 26 || k == 46 || k == 50 || k == 51 || k == 52 || k == 53 || k == 54 || k == 63 || k == 64 || k == 65 || k == 67 || k == 68 || k == 69 || k == 71 || k == 75 || k == 76 || k == 77 || k == 85 || k == 86 || k == 91 || k == 93 || k == 94 || k == 90 || k == 70 || k == 72 || k == 96) {
                sum = 0;
                for (int l = 0; l <= 15; l++) {
                    sum = sum + freqArray[k][l];
                    freqArray[k][l] = 0;
                }
                freqArray[k][0] = sum;
            }
        }

        int max = 0;
        int winnerA = 0;
        int winnerB = 0;
        boolean keepLooking = true;
        int replaced = 0;

        int existId;
        int existDur;
        boolean[][] skipThis = new boolean[200][36]; //dont give item types you already have in your inventory.  first dim = item id type, second im = data value
        for (int r = 1; r <= 36; r++){
                ItemStack stack = inv.getItem(r);
                existId = stack.getTypeId();
                existDur = stack.getDurability();
                switch (existId){ //if you have a door item in inventory, don't mimic door BLOCKS, etc.
                        case 331:
                            skipThis[55][0] = true;
                            break;
                        case 323:
                            skipThis[63][0] = true;
                            skipThis[68][0] = true;
                            break;
                        case 324:
                            skipThis[64][0] = true;
                            break;
                        case 330:
                            skipThis[71][0] = true;
                            break;
                        case 338:
                            skipThis[83][0] = true;
                            break;
                    }
                if (existId < 200 && existId > 0){
                    if (existDur < 16 && existDur > -1){
                        skipThis[existId][existDur] = true;
                    }
                }
        }
        for (int index = 0; index <= 17; index++) { //will only replace first two lines of your backpack.
            max = 0;
            winnerA = 0;
            winnerB = 0;
            if (keepLooking) {
                for (int j = 0; j <= 199; j++) { //find current maximum / most popular block type in array
                    for (int m = 0; m <= 15; m++) {
                        if (freqArray[j][m] > max && !skipThis[j][m]) {

                            max = freqArray[j][m];
                            winnerA = j;
                            winnerB = m;
                            freqArray[j][m] = 0; //this should fix the looping problem of last build
                        }
                        else if(skipThis[j][m]){
                            freqArray[j][m] = 0;
                        }
                    }
                }
            }
            if (winnerA != 0) { //if winnerA == 0, then that means we are out of new block types that were found during the scan, so stop messing with the inventory.
                ItemStack stack = inv.getItem(index + 9); //get the stack from inventory we are at right now.

                stack.setTypeId(winnerA); //makes it a stack of the popular type.

                p.sendMessage("winner = " + winnerA);
                if(winnerA == 55 ){
                    stack.setTypeId(331);
                } //changing various things that should be items instead of blocks.
                if(winnerA == 63 || winnerA == 68 ){
                    stack.setTypeId(323);
                }
                if(winnerA == 64 ){
                    stack.setTypeId(324);
                }
                if(winnerA == 71 ){
                    stack.setTypeId(330);
                }
                if(winnerA == 83 ){
                    stack.setTypeId(338);
                }

                stack.setAmount(64); //make it a full stack.
                //MaterialData matData = new MaterialData(winnerB);  //Okay, this is supposed to create a new materialdata type of a certain data value.  winnerB will be 5, for instance, for light green wool.
                stack.setDurability((short) winnerB);  //then it should set the stack to that data value.  Unfortunately, there is only a method here for a full materialdata object, not just an integer GRR
                inv.setItem(index + 9, stack); //put new stack back into current slot.
                freqArray[winnerA][winnerB] = 0; //clear out that winner so we will get the next most popular next time.
                if (index == 17) {
                    replaced = 18; //handles end case;
                }
            } else {
                if (keepLooking) {
                    replaced = index;
                }
                keepLooking = false;
            }
        }
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Environment mimicked in inventory. " + replaced + " stack(s) given (max: 18).");
    }
}
