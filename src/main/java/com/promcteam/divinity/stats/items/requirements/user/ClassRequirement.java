package com.promcteam.divinity.stats.items.requirements.user;

import com.promcteam.codex.config.api.ILangMsg;
import com.promcteam.codex.utils.DataUT;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.config.EngineCfg;
import com.promcteam.divinity.hooks.HookClass;
import com.promcteam.divinity.stats.items.ItemTags;
import com.promcteam.divinity.stats.items.requirements.api.DynamicUserRequirement;
import com.promcteam.divinity.utils.LoreUT;

public class ClassRequirement extends DynamicUserRequirement<String[]> {

    public ClassRequirement(
            @NotNull String name,
            @NotNull String format
    ) {
        super(
                "class",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_CLASS,
                ItemTags.TAG_REQ_USER_CLASS,
                DataUT.STRING_ARRAY);

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_item_user_classclass"));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_item_user_classclass"));
    }

    @Override
    @NotNull
    public Class<String[]> getParameterClass() {
        return String[].class;
    }

    @Override
    @NotNull
    public String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_CLASS;
    }

    @Override
    public boolean canUse(@NotNull Player player, @Nullable String[] value) {
        HookClass classPlugin = EngineCfg.HOOK_PLAYER_CLASS_PLUGIN;
        if (classPlugin == null) return true;

        if (value == null || value.length == 0) return true;

        String playerClass = classPlugin.getClass(player);
        for (String reqClass : value) {
            if (playerClass.equalsIgnoreCase(reqClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String[] value) {
        if (value.length == 0 || value[0].isEmpty()) return "";

        String sep   = EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_SEPAR;
        String color = EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_COLOR;

        return LoreUT.getStrSeparated(value,
                sep,
                color,
                EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_MAX,
                EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_NEWLINE);
    }

    @Override
    @NotNull
    public ILangMsg getDenyMessage(@NotNull Player p, @NotNull ItemStack src) {
        return QuantumRPG.getInstance().lang().Module_Item_Interact_Error_Class;
    }
}
