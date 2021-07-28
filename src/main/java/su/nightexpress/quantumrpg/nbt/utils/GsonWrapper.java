package su.nightexpress.quantumrpg.nbt.utils;

import com.google.gson.Gson;

public class GsonWrapper {
  public static String getString(Object obj) {
    return gson.toJson(obj);
  }
  
  public static <T> T deserializeJson(String json, Class<T> type) {
    try {
      if (json == null)
        return null; 
      T obj = (T)gson.fromJson(json, type);
      return type.cast(obj);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  private static final Gson gson = new Gson();
}
