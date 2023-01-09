package su.nightexpress.quantumrpg.modules.list.classes.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAttribute;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAttributeType;

public class ClassStatsGUI extends NGUI<QuantumRPG> {

	private ClassManager classManager;
	
	public ClassStatsGUI(@NotNull ClassManager classManager, @NotNull JYML cfg, @NotNull String path) {
		super(classManager.plugin, cfg, path);
		this.classManager = classManager;
		
		GuiClick click = (p, type, e) -> {
			if (type == null) return;
			
			if (type.getClass().equals(GUIStatsType.class)) {
				GUIStatsType type2 = (GUIStatsType) type;
				switch (type2) {
					case CLASS_ASPECTS: {
						classManager.getAspectManager().openGUI(p);
						break;
					}
					case CLASS_SKILLS: {
						classManager.openSkillsGUI(p);
						break;
					}
					default: {
						break;
					}
				}
				return;
			}
			else {
				ContentType type2 = (ContentType) type;
				switch (type2) {
					case EXIT: {
						p.closeInventory();
						break;
					}
					default: {
						break;
					}
				}
				return;
			}
		};
		
		for (String sId : cfg.getSection(path + "content")) {
			GuiItem guiItem = cfg.getGuiItem(path + "content." + sId, GUIStatsType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			
			this.addButton(guiItem);
		}
	}

	@Override
	protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
		
	}

	static enum GUIStatsType {
		CLASS_SKILLS,
		CLASS_ASPECTS,
		;
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

	@Override
	protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
		super.replaceMeta(player, item, guiItem);
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		List<String> lore = meta.getLore();
		if (lore == null) return;
		
		UserClassData cData = classManager.getUserData(player);
		if (cData == null) return;
		
		RPGClass rpgClass = cData.getPlayerClass();
		
		for (int i = 0; i < lore.size(); i++) {
			String line = lore.get(i);
			line = line
					.replace("%class%", rpgClass.getName())
					.replace("%aspect-points%", String.valueOf(cData.getAspectPoints()))
					.replace("%skill-points%", String.valueOf(cData.getSkillPoints()))
					.replace("%exp-max%", String.valueOf(cData.getExpToUp(true)))
					.replace("%exp%", String.valueOf(cData.getExp()))
					.replace("%level-max%", String.valueOf(rpgClass.getMaxLevel()))
					.replace("%level%", String.valueOf(cData.getLevel()));
			for (ClassAttributeType type : ClassAttributeType.values()) {
				ClassAttribute a = rpgClass.getAttribute(type);
				if (a == null) continue;
				
				double total = cData.getAttribute(type);
				double aspect = total - rpgClass.getAttributeValue(type, cData.getLevel());
				//double has = c.getAttributeValue(type, cData.getLevel());
				line = a.replace(type, line, total, aspect, (cData.getLevel() - rpgClass.getStartLevel()));
			}
			
			lore.set(i, line);
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}
