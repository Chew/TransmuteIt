package pw.chew.transmuteit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class DataManager {
    private static File emcFile;
    private static TransmuteIt plugin;
    private static boolean useEconomy;
    private static Economy econ;
    private static JSONObject json;

    public DataManager(TransmuteIt transmuteIt, boolean useEconomyConfig, Economy economy, JSONObject jsonData) {
        plugin = transmuteIt;
        useEconomy = useEconomyConfig;
        econ = economy;
        json = jsonData;
    }

    public static File getDataFolder() {
        File dataFolder = plugin.getDataFolder();
        File loc = new File(dataFolder + "/data");
        if(!loc.exists()) {
            loc.mkdirs();
        }
        return loc;
    }

    public int getEMC(UUID uuid, Player player) {
        if(useEconomy) {
            double emc = econ.getBalance(player);
            return (int)emc;
        } else {
            return getData(uuid).getInt("emc");
        }
    }

    public JSONObject getData(UUID uuid) {
        createDataFileIfNoneExists(uuid);
        prepareDataFile(uuid);
        File userFile = new File(getDataFolder(), uuid.toString() + ".json");
        StringBuilder data = new StringBuilder();
        try {
            Scanner scanner = new Scanner(userFile);
            while (scanner.hasNextLine()) {
                data.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONObject bob;
        try {
            bob = new JSONObject(data.toString());
        } catch(JSONException e) {
            e.printStackTrace();
            bob = new JSONObject("{\"emc\":0,\"discoveries\":[]}");
        }
        return bob;
    }

    public void createDataFileIfNoneExists(UUID uuid) {
        File userFile = new File(getDataFolder(), uuid.toString() + ".json");
        if(!userFile.exists()) {
            try {
                copyFileFromJar(uuid);
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create EMC file! EMC will NOT save!");
            }
        }
    }

    public void copyFileFromJar(UUID uuid) throws IOException {
        String name = "/default.json";
        File target = new File(getDataFolder(), uuid.toString() + ".json");
        if (!target.exists()) {
            InputStream initialStream = getClass().getResourceAsStream(name);
            byte[] buffer = new byte[initialStream.available()];
            initialStream.read(buffer);
            FileOutputStream out = new FileOutputStream(target);
            out.write(buffer);
            out.close();
        }
    }

    public void prepareDataFile(UUID uuid) {
        File userFile = new File(getDataFolder(), uuid.toString() + ".json");
        PrintWriter writer;
        StringBuilder data = new StringBuilder();
        try {
            Scanner scanner = new Scanner(userFile);
            while (scanner.hasNextLine()) {
                data.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            writer = new PrintWriter(userFile);
        } catch(FileNotFoundException e) {
            return;
        }
        JSONObject bob;
        try {
            bob = new JSONObject(data.toString());
        } catch(JSONException e) {
            e.printStackTrace();
            bob = new JSONObject("{\"emc\":0,\"discoveries\":[]}");
        }
        if(bob.length() < 2) {
            if(!bob.has("emc")) {
                bob.put("emc", 0);
            } else if(!bob.has("discoveries")){
                Map<String, Object> discoveries = new HashMap<>();
                bob.put("discoveries", discoveries);
            }
        }
        bob.write(writer);
        writer.close();
    }

    public void writeEMC(UUID uuid, int amount, Player player) {
        if(useEconomy) {
            double balance = econ.getBalance(player);
            EconomyResponse r = econ.withdrawPlayer(player, balance);
            EconomyResponse s = econ.depositPlayer(player, amount);
        } else {
            File userFile = new File(getDataFolder(), uuid.toString() + ".json");
            try {
                JSONObject data = getData(uuid);
                data.put("emc", amount);
                PrintWriter writer = new PrintWriter(userFile);
                data.write(writer);
                writer.close();
            } catch(FileNotFoundException e) {
                plugin.getLogger().severe("Unable to write to EMC file! EMC will NOT save!");
            }
        }
    }

    public void writeEmptyDiscovery(UUID uuid) {
        File userFile = new File(getDataFolder(), uuid.toString() + ".json");
        try {
            JSONObject data = getData(uuid);
            Map<String, Object> discoveries = new HashMap<>();
            data.put("discoveries", discoveries);
            PrintWriter writer = new PrintWriter(userFile);
            data.write(writer);
            writer.close();
        } catch(FileNotFoundException e) {
            plugin.getLogger().severe("Unable to write to EMC file! EMC will NOT save!");
        }
    }

    public void writeDiscovery(UUID uuid, String item) {
        File userFile = new File(getDataFolder(), uuid.toString() + ".json");
        try {
            JSONObject data = getData(uuid);
            data.getJSONArray("discoveries").put(item);
            PrintWriter writer = new PrintWriter(userFile);
            data.write(writer);
            writer.close();
        } catch(FileNotFoundException e) {
            plugin.getLogger().severe("Unable to write to EMC file! EMC will NOT save!");
        }
    }

    public void removeDiscovery(UUID uuid, String item) {
        File userFile = new File(getDataFolder(), uuid.toString() + ".json");
        try {
            JSONObject data = getData(uuid);
            data.getJSONArray("discoveries").toList().remove(item);
            PrintWriter writer = new PrintWriter(userFile);
            data.write(writer);
            writer.close();
        } catch(FileNotFoundException e) {
            plugin.getLogger().severe("Unable to write to EMC file! EMC will NOT save!");
        }
    }

    // Load EMC values from JSON file
    public JSONObject loadEMC() throws FileNotFoundException {
        File dataFolder = plugin.getDataFolder();
        emcFile = new File(dataFolder, "emc.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, Object> map;
        map = gson.fromJson(new FileReader(emcFile), HashMap.class);
        String gsson = gson.toJson(map);
        return new JSONObject(gsson);
    }

    public void writeToEMCFile() {
        try {
            PrintWriter writer = new PrintWriter(emcFile);
            json.write(writer);
            writer.close();
        } catch(FileNotFoundException e) {
            plugin.getLogger().severe("Unable to write to EMC file! EMC will NOT save!");
        }
    }

    public boolean discovered(UUID uuid, String item) {
        JSONObject data = getData(uuid);
        List<Object> bob = data.getJSONArray("discoveries").toList();
        return bob.contains(item);
    }

    // Copy default EMC values from JSON file hidden in the JAR.
    public void copyFileFromJar() throws IOException {
        String name = "/emc.json";
        File dataFolder = plugin.getDataFolder();
        File target = new File(dataFolder, "emc.json");
        if (!target.exists()) {
            InputStream initialStream = getClass().getResourceAsStream(name);
            byte[] buffer = new byte[initialStream.available()];
            initialStream.read(buffer);
            FileOutputStream out = new FileOutputStream(target);
            out.write(buffer);
            out.close();
        }
    }

    public List<Object> discoveries(UUID uuid) {
        JSONObject data = getData(uuid);
        return data.getJSONArray("discoveries").toList();
    }

    public JSONObject getEMCValues() {
        return json;
    }

    public int getAmountOfItemsWithEMC() {
        return getEMCValues().length();
    }
}
