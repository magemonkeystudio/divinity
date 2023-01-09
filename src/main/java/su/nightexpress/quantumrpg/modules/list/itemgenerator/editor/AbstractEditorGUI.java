package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public abstract class AbstractEditorGUI extends NGUI<QuantumRPG> {
    static final String CURRENT_PLACEHOLDER = "%current%";
    static YamlConfiguration commonItemGenerator;
    static AbstractEditorGUI instance;

    protected final ItemGeneratorManager itemGeneratorManager;
    protected ItemGeneratorManager.GeneratorItem itemGenerator;
    protected Player player;

    public AbstractEditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, int size) {
        super(itemGeneratorManager.plugin, "[&d"+itemGenerator.getId()+"&r] editor", size);
        this.itemGeneratorManager = itemGeneratorManager;
        this.itemGenerator = itemGenerator;
        load(itemGenerator);
        init();
        this.setTitle(this.getTitle().replace("%id%", itemGenerator.getId()));
    }

    private void init() {
        if (AbstractEditorGUI.commonItemGenerator == null) {
            try (InputStreamReader in = new InputStreamReader(Objects.requireNonNull(plugin.getClass().getResourceAsStream(this.itemGeneratorManager.getPath()+"items/common.yml")))) {
                AbstractEditorGUI.commonItemGenerator = YamlConfiguration.loadConfiguration(in);
            } catch (IOException exception) { throw new RuntimeException(exception); }
        }
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

    protected List<String> replaceLore(List<String> lore, String value, int maxLength) {
        lore = color(lore);

        List<String> splitValue = new ArrayList<>();
        while (value.length() > maxLength) {
            int i = value.lastIndexOf(' ', maxLength);
            if (i < 0) { i = maxLength; }
            splitValue.add(value.substring(0, i));
            value = value.substring(i);
        }
        splitValue.add(value);

        for (int i = 0, loreSize = lore.size(); i < loreSize; i++) {
            String line = lore.get(i);
            int pos = line.indexOf(CURRENT_PLACEHOLDER);
            if (pos < 0) { continue; }
            String format = StringUT.getColor(line.substring(0, pos));
            lore.set(i, line.substring(0, pos)+splitValue.get(0));
            for (int j = 1, valueSize = splitValue.size(); j < valueSize; j++) {
                i++;
                lore.add(i, format+splitValue.get(j));
            }
            lore.set(i, lore.get(i)+line.substring(pos+CURRENT_PLACEHOLDER.length()));
        }
        return lore;
    }

    protected List<String> replaceLore(List<String> lore, List<String> value) {
        if (value.isEmpty()) { value = List.of("[]"); }
        lore = color(lore);
        for (int i = 0, loreSize = lore.size(); i < loreSize; i++) {
            String line = lore.get(i);
            int pos = line.indexOf(CURRENT_PLACEHOLDER);
            if (pos < 0) { continue; }
            String format = StringUT.getColor(line.substring(0, pos));
            lore.set(i, line.replace(CURRENT_PLACEHOLDER, value.get(0)));
            for (int j = 1, size = value.size(); j < size; j++) {
                i++;
                lore.add(i, format+value.get(j));
            }
        }
        return lore;
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
        this.itemGenerator.getConfig().set(path, commonItemGenerator.get(path));
    }

    public void onChat(AsyncPlayerChatEvent event) { }

    public enum ItemType {
        NEW,
    }
}
