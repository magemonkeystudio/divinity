package su.nightexpress.quantumrpg.modules.drops.drops2.objects;

import java.util.List;

public class Rnd {
  public static float get() {
    return rnd.nextFloat();
  }
  
  public static float get(boolean normal) {
    float f = get();
    if (normal)
      f *= 100.0F; 
    return f;
  }
  
  public static int get(int n) {
    return (int)Math.floor(rnd.nextDouble() * n);
  }
  
  public static int get(int min, int max) {
    return min + (int)Math.floor(rnd.nextDouble() * (max - min + 1));
  }
  
  public static boolean chance(int chance) {
    return (chance >= 1 && (chance > 99 || nextInt(99) + 1 <= chance));
  }
  
  public static boolean chance(double chance) {
    return (nextDouble() <= chance / 100.0D);
  }
  
  public static <E> E get(Object[] list) {
    return (E)list[get(list.length)];
  }
  
  public static int get(int[] list) {
    return list[get(list.length)];
  }
  
  public static <E> E get(List<E> list) {
    return list.get(get(list.size()));
  }
  
  public static int nextInt(int n) {
    return (int)Math.floor(rnd.nextDouble() * n);
  }
  
  public static int nextInt() {
    return rnd.nextInt();
  }
  
  public static double nextDouble() {
    return rnd.nextDouble();
  }
  
  public static double nextGaussian() {
    return rnd.nextGaussian();
  }
  
  public static boolean nextBoolean() {
    return rnd.nextBoolean();
  }
  
  private static final MTRandom rnd = new MTRandom();
}
