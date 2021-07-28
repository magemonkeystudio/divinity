package su.nightexpress.quantumrpg.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResourceExtractor {
  protected final JavaPlugin plugin;
  
  protected final File extractfolder;
  
  protected final String folderpath;
  
  protected final String regex;
  
  public ResourceExtractor(JavaPlugin plugin, File extractfolder, String folderpath, String regex) {
    Validate.notNull(plugin, "The plugin cannot be null!");
    Validate.notNull(plugin, "The extract folder cannot be null!");
    Validate.notNull(plugin, "The folder path cannot be null!");
    this.extractfolder = extractfolder;
    this.folderpath = folderpath;
    this.plugin = plugin;
    this.regex = regex;
  }
  
  public void extract() throws IOException {
    extract(false, true);
  }
  
  public void extract(boolean override) throws IOException {
    extract(override, true);
  }
  
  public void extract(boolean override, boolean subpaths) throws IOException {
    File jarfile = null;
    try {
      Method method = JavaPlugin.class.getDeclaredMethod("getFile", new Class[0]);
      method.setAccessible(true);
      jarfile = (File)method.invoke(this.plugin, new Object[0]);
    } catch (Exception e) {
      throw new IOException(e);
    } 
    if (!this.extractfolder.exists())
      this.extractfolder.mkdirs(); 
    JarFile jar = new JarFile(jarfile);
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      File file;
      JarEntry entry = entries.nextElement();
      String path = entry.getName();
      if (!path.startsWith(this.folderpath))
        continue; 
      if (entry.isDirectory()) {
        if (subpaths) {
          file = new File(this.extractfolder, entry.getName().replaceFirst(this.folderpath, ""));
          if (!file.exists())
            file.mkdirs(); 
        } 
        continue;
      } 
      if (subpaths) {
        file = new File(this.extractfolder, path.replaceFirst(this.folderpath, ""));
      } else {
        file = new File(this.extractfolder, path.substring(path.indexOf(File.separatorChar), path.length()));
      } 
      String name = file.getName();
      if (this.regex == null || name.matches(this.regex)) {
        if (file.exists() && override)
          file.delete(); 
        if (!file.exists()) {
          InputStream is = jar.getInputStream(entry);
          FileOutputStream fos = new FileOutputStream(file);
          while (is.available() > 0)
            fos.write(is.read()); 
          fos.close();
          is.close();
        } 
      } 
    } 
    jar.close();
  }
}
