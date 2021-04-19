package su.nightexpress.quantumrpg.modules.list.classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.data.api.RPGUser;
import su.nightexpress.quantumrpg.data.api.UserProfile;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAspect;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAspectBonus;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAttribute;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAttributeType;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;

public class AspectManager {

	private QuantumRPG plugin;
	private ClassManager classManager;
	
	private Map<String, ClassAspect> aspects;
	private AspectManager.GUI gui;
	
	AspectManager(@NotNull ClassManager classManager) {
		this.classManager = classManager;
		this.plugin = this.classManager.plugin;
	}
	
	public void setup() {
		this.aspects = new HashMap<>();
		
		JYML cfg = JYML.loadOrExtract(this.plugin, this.classManager.getPath() + "aspects.yml");
		for (String aspectId : cfg.getSection("aspects")) {
			String path2 = "aspects." + aspectId + ".";
			
			String name = cfg.getString(path2 + "name", aspectId);
			Material material = Material.getMaterial(cfg.getString(path2 + "material", "").toUpperCase());
			if (material == null) {
				this.classManager.error("Invalid material for aspect: '" + aspectId + "' in '" + cfg.getFile().getName() + "' !");
				continue;
			}
			
			ClassAspect aspect = new ClassAspect(aspectId, name, material);
			this.aspects.put(aspect.getId(), aspect);
		}
		
		this.gui = new AspectManager.GUI(cfg);
	}
	
	public void shutdown() {
		if (this.gui != null) {
			this.gui.shutdown();
			this.gui = null;
		}
		if (this.aspects != null) {
			this.aspects.clear();
			this.aspects = null;
		}
	}
	
	public void openGUI(@NotNull Player player) {
		if (this.classManager.getUserData(player) == null) {
			plugin.lang().Classes_Error_NoClass.send(player);
			return;
		}
		this.gui.open(player, 1);
	}
	
	public void reallocateAspects(@NotNull Player player) {
		UserClassData data = this.classManager.getUserData(player);
		if (data == null) {
			plugin.lang().Classes_Error_NoClass.send(player);
			return;
		}
		RPGClass rpgClass = data.getPlayerClass();
		
		data.getAspects().clear();
		int points = rpgClass.getAspectPointsPerLevel() * (data.getLevel() - rpgClass.getStartLevel());
		for (RPGClass parent : rpgClass.getParents()) {
			points += parent.getAspectPointsPerLevel() * parent.getMaxLevel();
		}
		data.setAspectPoints(points);
		this.classManager.updateClassData(player);
	}
	
	public void addAspect(@NotNull Player player, @NotNull ClassAspect aspect, int amount, boolean force) {
		UserClassData cData = this.classManager.getUserData(player);
		if (cData == null) return;
		
		if (!force) {
			ClassAspectBonus a = cData.getPlayerClass().getAspectBonus(aspect);
			if (a == null || (a.getMaxValue() >= 0 && cData.getAspect(aspect.getId()) >= a.getMaxValue())) {
				return;
			}
			
			int points = cData.getAspectPoints();
			if (points < amount) {
				plugin.lang().Classes_Aspect_Inc_Error_NoPoints.send(player);
				return;
			}
			
			cData.setAspectPoints(points - amount);
		}
		
		cData.addAspect(aspect.getId(), amount);
		
		plugin.lang().Classes_Aspect_Inc_Done
			.replace("%value%", String.valueOf(cData.getAspect(aspect.getId())))
			.replace("%aspect%", aspect.getName());
		
		this.classManager.updateClassData(player);
	}
	
	@Nullable
	public ClassAspect getAspectById(@NotNull String id) {
		return this.aspects.get(id.toLowerCase());
	}
	
	@NotNull
	public Collection<ClassAspect> getAspects() {
		return this.aspects.values();
	}
	
	public class GUI extends NGUI<QuantumRPG> {
		
		private int[] aspectSlots;
		private List<String> aspectLore;
		
		public GUI(@NotNull JYML cfg) {
			super(AspectManager.this.plugin, cfg, "gui.");
			
			String path = "gui.";
			this.aspectSlots = cfg.getIntArray(path + "aspect-slots");
			this.aspectLore = StringUT.color(cfg.getStringList(path + "aspect-lore"));
			
			GuiClick click = new GuiClick() {
				@Override
				public void click(@NotNull Player player, @Nullable Enum<?> type, @NotNull InventoryClickEvent e) {
					if (type == null) return;
					
					if (type == GUIAspectType.RESET) {
						AspectManager.this.reallocateAspects(player);
						open(player, 1);
						return;
					}
					if (type == ContentType.RETURN) {
						classManager.openStatsGUI(player);
						return;
					}
					if (type == ContentType.EXIT) {
						player.closeInventory();
						return;
					}
				}
			};
			
			for (String sId : cfg.getSection(path + "content")) {
				GuiItem guiItem = cfg.getGuiItem(path + "content." + sId, GUIAspectType.class);
				if (guiItem == null) continue;
				
				if (guiItem.getType() != null) {
					guiItem.setClick(click);
				}
				
				this.addButton(guiItem);
			}
		}

		@Override
		protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
			RPGUser user = plugin.getUserManager().getOrLoadUser(player);
			if (user == null) return;
			
			UserProfile prof = user.getActiveProfile();
			UserClassData cData = prof.getClassData();
			if (cData == null) return;
			
			RPGClass clazz = cData.getPlayerClass();
			int j = 0;
			for (Map.Entry<ClassAspect, ClassAspectBonus> entry : clazz.getAspectBonuses().entrySet()) {
				ClassAspect aspect = entry.getKey();
				ClassAspectBonus aspectBonus = entry.getValue();
				ItemStack icon = new ItemStack(aspect.getMaterial());
				
				ItemMeta meta = icon.getItemMeta();
				if (meta == null) continue;
				
				List<String> lore2 = new ArrayList<>();
				
				String plCurrent = "%aspect_current%";
				String plMax = "%aspect_max%";
				String cost = "%cost%";
				BonusMap bMap = aspectBonus.getBonusMap();
				
				String aspectMax = String.valueOf(aspectBonus.getMaxValue());
				String aspectHas = String.valueOf(cData.getAspect(aspect.getId()));
				double aspectCur = cData.getAspect(aspect);
				
				for (String line : this.aspectLore) {
					if (line.contains("%att_name%")) {
						for (ClassAttributeType aType : ClassAttributeType.values()) {
							ClassAttribute att = clazz.getAttribute(aType);
							if (att == null) continue;
							
							double inc = aspectBonus.getPerPointAttribute(aType);
							if (inc == 0) continue;
							
							double total = cData.getAttribute(aType);
							
							lore2.add(line
									.replace("%att_total%", NumberUT.format(total))
									.replace("%att_name%", aType.getName())
									.replace("%att_inc%", NumberUT.format(inc)));
						}
						continue;
					}
					if (line.contains("%item_stat_name%")) {
						bMap.getStatBonuses().forEach((statAtt, statFunc) -> {
							String line2 = this.formatBonusMap(line, "item_stat", statAtt, statFunc, aspectCur);
							if (line2 == null) return;

							lore2.add(line2);
						});
						continue;
					}
					if (line.contains("%damage_name%")) {
						bMap.getDamageBonuses().forEach((dmgAtt, dmgFunc) -> {
							String line2 = this.formatBonusMap(line, "damage", dmgAtt, dmgFunc, aspectCur);
							if (line2 == null) return;

							lore2.add(line2);
						});
						continue;
					}
					if (line.contains("%defense_name%")) {
						bMap.getDefenseBonuses().forEach((defAtt, defFunc) -> {
							String line2 = this.formatBonusMap(line, "defense", defAtt, defFunc, aspectCur);
							if (line2 == null) return;

							lore2.add(line2);
						});
						continue;
					}
					
					lore2.add(line
							.replace("%aspect-points%", String.valueOf(cData.getAspectPoints()))
							.replace(cost, "1")
							.replace(plMax, aspectMax)
							.replace(plCurrent, aspectHas));
				}
				
				meta.setDisplayName(aspect.getName());
				meta.setLore(lore2);
				icon.setItemMeta(meta);
				
				GuiClick click = new GuiClick() {
					@Override
					public void click(@NotNull Player p, @Nullable Enum<?> type, @NotNull InventoryClickEvent e) {
						AspectManager.this.addAspect(p, aspect, 1, false);
						open(p, 1);
					}
				};
				
				JIcon button = new JIcon(icon);
				button.setClick(click);
				this.addButton(player, button, this.aspectSlots[j++]);
			}
		}
		
		@Nullable
		private final String formatBonusMap(
				@NotNull String line, 
				@NotNull String placeholder,
				@NotNull ItemLoreStat<?> stat,
				@NotNull BiFunction<Boolean, Double, Double> func,
				double aspectCur) {
			
			double valRaw = func.apply(false, 0D);
			double valBonus = func.apply(true, 0D);
			if (valRaw == 0D && valBonus == 0D) return null;
			
			double valTotal = valRaw != 0D ? valRaw : valBonus;
			String valFormat = NumberUT.format(valTotal);
			String valStr = valBonus != 0D ? valFormat += EngineCfg.LORE_CHAR_PERCENT : valFormat;
			
			return line
					.replace("%" + placeholder + "_total%", NumberUT.format(valTotal * aspectCur))
					.replace("%" + placeholder + "_name%", stat.getName())
					.replace("%" + placeholder + "_inc%", valStr);
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
	
	static enum GUIAspectType {
		RESET,
		;
	}
}
