package su.nightexpress.quantumrpg.modules.list.extractor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.hooks.external.VaultHK;
import mc.promcteam.engine.manager.api.gui.*;
import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.modules.api.socketing.ModuleSocket;
import su.nightexpress.quantumrpg.modules.list.extractor.event.PlayerExtractSocketEvent;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ExtractGUI extends NGUI<QuantumRPG> {

    private static final NamespacedKey    META_KEY_SOCKET_SELECT  = new NamespacedKey(QuantumRPG.getInstance(), "QRPG_EXTRACTOR_GUI_SOCKET_SELECTOR");
    private static final NamespacedKey    META_KEY_SOCKET_SELECT2 = NamespacedKey.fromString("quantumrpg:qrpg_extractor_gui_socket_selector");
    private final        int[]            socketSlots;
    protected            ExtractorManager extractorManager;
    protected            int              itemSlot;
    protected            int              srcSlot;
    protected            int              resultSlot;
    protected            String           socketName;
    protected            List<String>     socketLore;

    public ExtractGUI(@NotNull ExtractorManager extractorManager) {
        super(extractorManager.plugin, extractorManager.getJYML(), "gui.");
        this.extractorManager = extractorManager;

        JYML   cfg  = extractorManager.getJYML();
        String path = "gui.";

        this.itemSlot = cfg.getInt(path + "item-slot");
        this.srcSlot = cfg.getInt(path + "source-slot");
        this.resultSlot = cfg.getInt(path + "result-slot");

        this.socketName = StringUT.color(cfg.getString(path + "socket-name", "&7Extract: %name%"));
        this.socketLore = StringUT.color(cfg.getStringList(path + "socket-lore"));
        this.socketSlots = cfg.getIntArray(path + "socket-slots");

        GuiClick clickMain = new GuiClick() {
            @Override
            public void click(
                    @NotNull Player p, @Nullable Enum<?> type, @NotNull InventoryClickEvent e) {

                if (type == ContentType.EXIT) {
                    p.closeInventory();
                }
            }
        };

        for (String itemId : cfg.getSection(path + "content")) {
            GuiItem guiItem = cfg.getGuiItem(path + "content." + itemId, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(clickMain);
            }

            this.addButton(guiItem);
        }

        GuiClick clickSocket = new GuiClick() {
            @Override
            public void click(
                    @NotNull Player p, @Nullable Enum<?> type, @NotNull InventoryClickEvent e) {

                if (type == null || !type.getClass().equals(SocketAttribute.Type.class)) return;
                SocketAttribute.Type socketType = (SocketAttribute.Type) type;

                Inventory inv    = e.getInventory();
                ItemStack target = inv.getItem(itemSlot);
                ItemStack src    = inv.getItem(srcSlot);

                // Prevent duplication for onClose event
                inv.setItem(itemSlot, null);
                inv.setItem(srcSlot, null);

                open(p, target, src, socketType);
            }
        };

        for (String itemId : cfg.getSection(path + "socket-types")) {
            GuiItem guiItem = cfg.getGuiItem(path + "socket-types." + itemId, SocketAttribute.Type.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(clickSocket);
            }

            this.addButton(guiItem);
        }
    }

    public void open(
            @NotNull Player player,
            @Nullable ItemStack target,
            @Nullable ItemStack source,
            @Nullable SocketAttribute.Type type) {

        if (target == null) {
            target = new ItemStack(Material.AIR);
        }
        if (source == null) {
            source = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) source.getItemMeta();
            if (meta == null) return;

            meta.setDisplayName(player.getName());
            meta.setOwningPlayer(player);
            source.setItemMeta(meta);
        }

        // GUI Fix
        this.clearUserCache(player);
        this.LOCKED_CACHE.add(player.getName());

        // Add items
        this.addButton(player, new JIcon(target), this.itemSlot);
        this.addButton(player, new JIcon(source), this.srcSlot);

        // Add socket preview for selected type
        Label_Sockets:
        if (type != null && target.getType() != Material.AIR) {
            ModuleSocket<?> mod = type.getModule();
            if (mod == null) break Label_Sockets;

            int indexMain = 0;
            for (SocketAttribute socketAtt : ItemStats.getSockets(type)) {
                String socketCategory = socketAtt.getId();
                for (Map.Entry<Integer, String[]> en : mod.getFilledSocketKeys(target, socketCategory).entrySet()) {
                    if (indexMain >= this.socketSlots.length) break Label_Sockets;

                    String[] values       = en.getValue();
                    String   itemId       = values[0];
                    int      lvl          = StringUT.getInteger(values[1], -1);
                    double   extractPrice = this.extractorManager.getExtractionPrice(type, socketCategory, lvl);

                    ItemStack item = QuantumAPI.getItemByModule(mod, itemId, lvl, -1, 0);
                    if (item == null) continue;
                    this.replaceCostHave(player, item, socketAtt, extractPrice);

                    final int indexSocket = en.getKey();
                    ItemStack src         = source;
                    GuiClick itemClick = new GuiClick() {
                        @Override
                        public void click(
                                @NotNull Player p, @Nullable Enum<?> type2, @NotNull InventoryClickEvent e) {

                            Inventory inv = e.getInventory();

                            // Remove glow from other socket items
                            for (int socketSlot : socketSlots) {
                                ItemStack socketItem = inv.getItem(socketSlot);
                                if (socketItem != null) {
                                    socketItem.removeEnchantment(Enchantment.ARROW_DAMAGE);
                                }
                            }

                            // Add glow to selected socket item
                            ItemStack item = e.getCurrentItem();
                            if (item != null) {
                                item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
                            }

                            ItemStack target = getItem(inv, itemSlot);
                            ItemStack result = mod.extractSocket(new ItemStack(target), socketCategory, indexSocket);
                            if (e.isLeftClick()) {
                                inv.setItem(resultSlot, result);
                            }
                            if (e.isRightClick()) {
                                PlayerExtractSocketEvent eve = new PlayerExtractSocketEvent(p, target, result, type);

                                VaultHK vh = plugin.getVault();
                                if (extractPrice > 0 && vh != null) {
                                    double userBalance = vh.getBalance(p);
                                    if (userBalance < extractPrice) {
                                        plugin.lang().Extractor_Extract_Error_TooExpensive
                                                .replace("%cost%", NumberUT.format(extractPrice))
                                                .replace("%balance%", NumberUT.format(userBalance))
                                                .send(p);
                                        eve.setFailed(true);
                                    } else {
                                        vh.take(p, extractPrice);
                                    }
                                }

                                plugin.getPluginManager().callEvent(eve);
                                if (eve.isCancelled() || eve.isFailed()) return;

                                // Prevent to dupe after close
                                inv.setItem(srcSlot, null);
                                inv.setItem(itemSlot, result);

                                if (extractorManager.isItemOfThisModule(src)) {
                                    extractorManager.takeItemCharge(src);
                                    if (extractorManager.getItemCharges(src) == 0) {
                                        p.closeInventory();
                                        return;
                                    }
                                }

                                // Do not return item if we will continue extracting
                                inv.setItem(itemSlot, null);
                                open(p, result, src, type);
                            }
                        }
                    };

                    JIcon ico = new JIcon(item);
                    ico.setClick(itemClick);

                    int slot = this.socketSlots[indexMain++];
                    this.addButton(player, ico, slot);
                }
            }
        }

        // Just a hack to add a glow to selected type.
        for (GuiItem guiItem : this.getContent().values()) {
            Enum<?> type2 = guiItem.getType();
            if (type2 != null && type2 == type) {
                ItemStack itemGlow = guiItem.getItem();
                itemGlow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
                DataUT.setData(itemGlow, META_KEY_SOCKET_SELECT, "true");
                JIcon active = new JIcon(itemGlow);
                active.setClick(guiItem.getClick());

                for (int slot : guiItem.getSlots()) {
                    this.addButton(player, active, slot);
                }
                break;
            }
        }

        super.open(player, 1);
    }

    @Nullable
    private SocketAttribute.Type getSelectedType(@NotNull Inventory inv) {
        for (GuiItem gi : this.getContent().values()) {
            Enum<?> type2 = gi.getType();
            if (type2 == null || !type2.getClass().equals(SocketAttribute.Type.class)) continue;

            for (int slot : gi.getSlots()) {
                ItemStack item = inv.getItem(slot);
                if (item == null) continue;

                String data = DataUT.getStringData(item, META_KEY_SOCKET_SELECT);
                if (data == null)
                    data = DataUT.getStringData(item, META_KEY_SOCKET_SELECT2);
                if (data != null && data.equalsIgnoreCase("true")) {
                    return (SocketAttribute.Type) type2;
                }
            }
        }
        return null;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

    }

    @Override
    protected void onReady(@NotNull Player player, @NotNull Inventory inv, int page) {
        super.onReady(player, inv, page);
    }

    private void replaceCostHave(
            @NotNull Player p,
            @NotNull ItemStack item,
            @NotNull SocketAttribute socketAtt,
            double price) {

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        VaultHK vh = plugin.getVault();


        List<String> lore = new ArrayList<>();
        String       cost = NumberUT.format(price);
        String       have = "0";
        if (vh != null && vh.getEconomy() != null) {
            have = NumberUT.format(vh.getBalance(p));
        }

        if (meta.hasDisplayName()) {
            String n = meta.getDisplayName();
            meta.setDisplayName(socketName
                    .replace("%socket%", socketAtt.getName())
                    .replace("%name%", n)
                    .replace("%cost%", cost).replace("%have%", have));
        }
        for (String s : socketLore) {
            lore.add(s
                    .replace("%socket%", socketAtt.getName())
                    .replace("%cost%", cost)
                    .replace("%have%", have));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @Override
    public void click(@NotNull Player player, @Nullable ItemStack item, int slot, InventoryClickEvent e) {
        Inventory inv = e.getInventory();

        ItemStack target = this.getItem(inv, this.itemSlot);
        ItemStack source = this.getItem(inv, this.srcSlot);

        // Put item from inventory to extract
        if (slot >= inv.getSize() && target.getType() == Material.AIR && item != null) {
            if (this.extractorManager.openExtraction(player, item, source, this.getSelectedType(inv), true)) {
                player.getInventory().removeItem(source); // Duplication fix
            }
            return;
        }

        // Take item from extract back to inventory
        if (slot == this.itemSlot && target.getType() != Material.AIR) {
            inv.setItem(this.srcSlot, null);
            this.open(player, null, source, this.getSelectedType(inv));
            return;
        }

        super.click(player, item, slot, e);
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        Inventory inv  = e.getInventory();
        ItemStack item = inv.getItem(this.itemSlot);
        ItemStack src  = inv.getItem(this.srcSlot);

        if (item != null) {
            ItemUT.addItem(player, item);
        }
        if (src != null && this.extractorManager.isItemOfThisModule(src)) {
            ItemUT.addItem(player, src);
        }
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }
}
