package su.nightexpress.quantumrpg.config;

import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.utils.Files;

public class MyConfig {
  private JavaPlugin plugin;
  
  private String name;
  
  private String path;
  
  private JYML fileConfiguration;
  
  private File file;
  
  public MyConfig(JavaPlugin plugin, String path, String name) {
    this.plugin = plugin;
    this.name = name;
    this.path = path;
    load();
  }
  
  private void load() {
    if (!this.plugin.getDataFolder().exists())
      Files.mkdir(this.plugin.getDataFolder()); 
    File folder = new File(this.plugin.getDataFolder() + "/" + this.path);
    if (!folder.exists())
      folder.mkdirs(); 
    File file = new File(this.plugin.getDataFolder() + "/" + this.path, this.name);
    if (!file.exists())
      Files.copy(QuantumRPG.class.getResourceAsStream(String.valueOf(this.path) + "/" + this.name), file); 
    this.file = file;
    this.fileConfiguration = new JYML(file);
    this.fileConfiguration.options().copyDefaults(true);
  }
  
  public void save() {
    try {
      this.fileConfiguration.options().copyDefaults(true);
      this.fileConfiguration.save(this.file);
    } catch (IOException ex) {
      ex.printStackTrace();
    } 
  }
  
  public JYML getConfig() {
    return this.fileConfiguration;
  }
}
