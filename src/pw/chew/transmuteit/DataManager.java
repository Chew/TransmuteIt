package pw.chew.transmuteit;
import org.json.JSONException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.json.JSONObject;
import java.util.UUID;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DataManager {
  public DataManager() {

  }


  public int getEMC(UUID uuid) {
    return getData(uuid).getInt("emc");
  }

  public static File getDataFolder() {
    File dataFolder = ((TransmuteIt)Bukkit.getPluginManager().getPlugin("TransmuteIt")).getDataFolder();
    File loc = new File(dataFolder + "/data");
    if(!loc.exists()) {
      loc.mkdirs();
    }
    return loc;
  }

  public JSONObject getData(UUID uuid) {
    createDataFileIfNoneExists(uuid);
    prepareDataFile(uuid);
    File userFile = new File(getDataFolder(), uuid.toString() + ".json");
    String data = "";
    try {
      Scanner scanner = new Scanner(userFile);
      while (scanner.hasNextLine()) {
        data += (scanner.nextLine());
      }
      scanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    JSONObject bob;
    try {
      bob = new JSONObject(data);
    } catch(JSONException e) {
      e.printStackTrace();
      System.out.println("JSON EXCEPTION OCCURED. YOU FOOL. YOU BOMBASTIC BEANLORD");
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
        System.out.println("[TransmuteIt] Unable to create EMC file! EMC will NOT save!");
      }
    }
  }

  private void copyFileFromJar(UUID uuid) throws IOException {
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
    System.out.println("does userFile exist on line 80?" + userFile.exists());
    PrintWriter writer;
    System.out.println("Let's print this scanner");
    String data = "";
    try {
      Scanner scanner = new Scanner(userFile);
      while (scanner.hasNextLine()) {
        data += (scanner.nextLine());
        System.out.println(data);
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
    System.out.println("data at line 107:" + data);
    try {
      bob = new JSONObject(data);
      System.out.println("bob at line 110:" + bob.toString());
    } catch(JSONException e) {
      e.printStackTrace();
      System.out.println("JSON EXCEPTION OCCURED. YOU FOOL. YOU BOMBASTIC BEANLORD");
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

  public void writeEMC(UUID uuid, int amount) {
    File userFile = new File(getDataFolder(), uuid.toString() + ".json");
    try {
      JSONObject data = getData(uuid);
      data.put("emc", amount);
      PrintWriter writer = new PrintWriter(userFile);
      data.write(writer);
      writer.close();
    } catch(FileNotFoundException e) {
      System.out.println("[TransmuteIt] Unable to write to EMC file! EMC will NOT save!");
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
      System.out.println("[TransmuteIt] Unable to write to EMC file! EMC will NOT save!");
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
      System.out.println("[TransmuteIt] Unable to write to EMC file! EMC will NOT save!");
    }
  }

  public boolean discovered(UUID uuid, String item) {
    JSONObject data = getData(uuid);
    List<Object> bob = data.getJSONArray("discoveries").toList();
    return bob.contains(item);
  }
}