package su.nightexpress.quantumrpg.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import su.nightexpress.quantumrpg.QuantumRPG;

public class Files {
  public static void copy(InputStream inputStream, File file) {
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      byte[] array = new byte[1024];
      int read;
      while ((read = inputStream.read(array)) > 0)
        fileOutputStream.write(array, 0, read); 
      fileOutputStream.close();
      inputStream.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } 
  }
  
  public static void mkdir(File file) {
    try {
      file.mkdir();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public static void create(File f) {
    f.getParentFile().mkdirs();
    try {
      f.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public static List<String> getFilesFolder(String folderz) {
    List<String> names = new ArrayList<>();
    File folder = new File(QuantumRPG.instance.getDataFolder() + "/modules/" + folderz + "/");
    File[] listOfFiles = folder.listFiles();
    if (listOfFiles == null)
      return names; 
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile())
        names.add(listOfFiles[i].getName()); 
    } 
    return names;
  }
}
