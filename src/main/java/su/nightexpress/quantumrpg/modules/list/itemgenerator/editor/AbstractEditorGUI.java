package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public abstract class AbstractEditorGUI extends NGUI<QuantumRPG> {
    public static final String CURRENT_PLACEHOLDER = "%current%";
    static AbstractEditorGUI instance;

    protected final ItemGeneratorManager itemGeneratorManager;
    protected ItemGeneratorManager.GeneratorItem itemGenerator;
    protected Player player;

    public AbstractEditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, int size) {
        super(itemGeneratorManager.plugin, "[&d"+itemGenerator.getId()+"&r] editor", size);
        this.itemGeneratorManager = itemGeneratorManager;
        this.itemGenerator = itemGenerator;
        load(itemGenerator);
        this.setTitle(this.getTitle().replace("%id%", itemGenerator.getId()));
    }

    @Nullable
    public static AbstractEditorGUI getInstance() { return instance; }

    public Player getPlayer() { return player; }

    public ItemGeneratorManager.GeneratorItem getItemGenerator() { return itemGenerator; }

    @Override
    protected boolean ignoreNullClick() { return true; }

    @Override
    protected boolean cancelClick(int i) { return true; }

    @Override
    protected boolean cancelPlayerClick() { return true; }

    protected GuiItem createButton(String id, Enum<?> type, Material material, String name, List<String> lore, int slot, GuiClick guiClick) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setDisplayName(StringUT.color(name));
            List<String> coloredLore = new ArrayList<>();
            for (String loreLine : lore) { coloredLore.add(StringUT.color(loreLine)); }
            meta.setLore(coloredLore);
            itemStack.setItemMeta(meta);
        }
        return createButton(id, type, itemStack, slot, guiClick);
    }

    protected GuiItem createButton(String id, Enum<?> type, ItemStack itemStack, int slot, GuiClick guiClick) {
        GuiItem guiItem = new GuiItem(id, type, itemStack, false, 0, new TreeMap<>(), Collections.emptyMap(), null, new int[] {slot});
        guiItem.setClick(guiClick);
        return guiItem;
    }

    private void load(ItemGeneratorManager.GeneratorItem itemGenerator) {
        if (this.player == null) {
            this.clear();
        } else {
            int page = this.getUserPage(this.player, 0);
            int pages = this.getUserPage(this.player, 1);
            this.clear();
            this.setUserPage(this.player, page, pages);
        }
        this.registerListeners();
        this.itemGenerator = itemGenerator;
    }

    @Override
    public void open(@NotNull Player player, int page) {
        if (AbstractEditorGUI.instance != null) {
            for (HumanEntity humanEntity : instance.getInventory().getViewers()) {
                if (!humanEntity.getUniqueId().equals(player.getUniqueId())) {
                    throw new IllegalStateException("Another editor is already open");
                }
            }
        }
        super.open(player, page);
        AbstractEditorGUI.instance = this;
        this.player = player;
    }

    @Override
    public void shutdown() { AbstractEditorGUI.instance = null; }

    protected List<String> color(List<String> list) {
        List<String> coloredList = new ArrayList<>(list.size());
        for (String string : list) {
            coloredList.add(StringUT.color(string));
        }
        return coloredList;
    }

    protected List<String> color(String... strings) {
        List<String> coloredList = new ArrayList<>(strings.length);
        for (String string : strings) {
            coloredList.add(StringUT.color(string));
        }
        return coloredList;
    }

    protected void saveAndReopen() { saveAndReopen(1); }

    protected void saveAndReopen(int page) {
        final Player player = this.player;
        JYML cfg = this.itemGenerator.getConfig();
        cfg.saveChanges();
        this.load(this.itemGeneratorManager.load(this.itemGenerator.getId(), cfg));
        new BukkitRunnable() {
            @Override
            public void run() {
                AbstractEditorGUI.this.open(player, page);
            }
        }.runTask(plugin);
    }

    protected void setDefault(String path) {
        this.itemGenerator.getConfig().set(path, ItemGeneratorManager.commonItemGenerator.get(path));
    }

    public void onChat(AsyncPlayerChatEvent event) { }

    public enum ItemType {
        NEW,
    }
}
