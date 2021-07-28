package su.nightexpress.quantumrpg.modules.drops.drops2.objects;

public class DropItem {
  private int index = 0;
  
  private int count = 0;
  
  private Drop dropTemplate;
  
  public DropItem(Drop dropTemplate) {
    this.dropTemplate = dropTemplate;
  }
  
  public void calculateCount() {
    this.count = Rnd.get(this.dropTemplate.getMinAmount(), this.dropTemplate.getMaxAmount());
  }
  
  public int getIndex() {
    return this.index;
  }
  
  public void setIndex(int index) {
    this.index = index;
  }
  
  public int getCount() {
    return this.count;
  }
  
  public void setCount(int count) {
    this.count = count;
  }
  
  public Drop getDropTemplate() {
    return this.dropTemplate;
  }
}
