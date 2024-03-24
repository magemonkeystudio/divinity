package com.promcteam.divinity.modules;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.list.arrows.ArrowManager;
import com.promcteam.divinity.modules.list.classes.ClassManager;
import com.promcteam.divinity.modules.list.combatlog.CombatLogManager;
import com.promcteam.divinity.modules.list.consumables.ConsumablesManager;
import com.promcteam.divinity.modules.list.customitems.CustomItemsManager;
import com.promcteam.divinity.modules.list.dismantle.DismantleManager;
import com.promcteam.divinity.modules.list.drops.DropManager;
import com.promcteam.divinity.modules.list.essences.EssencesManager;
import com.promcteam.divinity.modules.list.extractor.ExtractorManager;
import com.promcteam.divinity.modules.list.fortify.FortifyManager;
import com.promcteam.divinity.modules.list.gems.GemManager;
import com.promcteam.divinity.modules.list.identify.IdentifyManager;
import com.promcteam.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import com.promcteam.divinity.modules.list.itemhints.ItemHintsManager;
import com.promcteam.divinity.modules.list.loot.LootManager;
import com.promcteam.divinity.modules.list.magicdust.MagicDustManager;
import com.promcteam.divinity.modules.list.money.MoneyManager;
import com.promcteam.divinity.modules.list.party.PartyManager;
import com.promcteam.divinity.modules.list.refine.RefineManager;
import com.promcteam.divinity.modules.list.repair.RepairManager;
import com.promcteam.divinity.modules.list.runes.RuneManager;
import com.promcteam.divinity.modules.list.sell.SellManager;
import com.promcteam.divinity.modules.list.sets.SetManager;
import com.promcteam.divinity.modules.list.soulbound.SoulboundManager;

public class ModuleCache {

    private final QuantumRPG plugin;

    private GemManager           gemsManager;
    private EssencesManager      essencesManager;
    private RuneManager          runesManager;
    private MagicDustManager     magicDustManager;
    private ArrowManager         arrowManager;
    private MoneyManager         moneyManager;
    private SetManager           setManager;
    private ItemGeneratorManager itemGeneratorManager;
    @Getter
    private CustomItemsManager   customItemsManager;

    private ClassManager classManager;

    private RefineManager    refineManager;
    private FortifyManager   fortifyManager;
    private IdentifyManager  identifyManager;
    private SoulboundManager soulboundManager;
    private RepairManager    repairManager;
    private DismantleManager dismantleManager;
    private ExtractorManager extractorManager;
    private PartyManager     partyManager;
    private ItemHintsManager itemHintsManager;

    private DropManager dropManager;
    private LootManager lootManager;

    private SellManager      sellManager;
    private CombatLogManager combatLogManager;

    private ConsumablesManager consumablesManager;
//    private ActiveItemManager activeItemManager;

    public ModuleCache(@NotNull QuantumRPG plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        this.gemsManager = (GemManager) this.plugin.getModuleManager().register(new GemManager(plugin));
        this.essencesManager = (EssencesManager) this.plugin.getModuleManager().register(new EssencesManager(plugin));
        this.runesManager = (RuneManager) this.plugin.getModuleManager().register(new RuneManager(plugin));
        this.arrowManager = (ArrowManager) this.plugin.getModuleManager().register(new ArrowManager(plugin));
        this.setManager = (SetManager) this.plugin.getModuleManager().register(new SetManager(plugin));
        this.itemGeneratorManager =
                (ItemGeneratorManager) this.plugin.getModuleManager().register(new ItemGeneratorManager(plugin));
        this.customItemsManager =
                (CustomItemsManager) this.plugin.getModuleManager().register(new CustomItemsManager(plugin));
        this.moneyManager = (MoneyManager) this.plugin.getModuleManager().register(new MoneyManager(plugin));

//		this.classManager = (ClassManager) this.plugin.getModuleManager().register(new ClassManager(plugin));
//		classManager.loadClasses();

        // Utility
        this.refineManager = (RefineManager) this.plugin.getModuleManager().register(new RefineManager(plugin));
        this.fortifyManager = (FortifyManager) this.plugin.getModuleManager().register(new FortifyManager(plugin));
        this.identifyManager = (IdentifyManager) this.plugin.getModuleManager().register(new IdentifyManager(plugin));
        this.magicDustManager =
                (MagicDustManager) this.plugin.getModuleManager().register(new MagicDustManager(plugin));
        this.soulboundManager =
                (SoulboundManager) this.plugin.getModuleManager().register(new SoulboundManager(plugin));
        this.repairManager = (RepairManager) this.plugin.getModuleManager().register(new RepairManager(plugin));
        this.dismantleManager =
                (DismantleManager) this.plugin.getModuleManager().register(new DismantleManager(plugin));
        this.extractorManager =
                (ExtractorManager) this.plugin.getModuleManager().register(new ExtractorManager(plugin));
        this.partyManager = (PartyManager) this.plugin.getModuleManager().register(new PartyManager(plugin));
        this.itemHintsManager =
                (ItemHintsManager) this.plugin.getModuleManager().register(new ItemHintsManager(plugin));

        this.sellManager = (SellManager) this.plugin.getModuleManager().register(new SellManager(plugin));
        this.combatLogManager =
                (CombatLogManager) this.plugin.getModuleManager().register(new CombatLogManager(plugin));

        this.consumablesManager =
                (ConsumablesManager) this.plugin.getModuleManager().register(new ConsumablesManager(plugin));
//        this.activeItemManager = (ActiveItemManager) this.plugin.getModuleManager().register(new ActiveItemManager(plugin));

        this.dropManager = (DropManager) this.plugin.getModuleManager().register(new DropManager(plugin));
        this.lootManager = (LootManager) this.plugin.getModuleManager().register(new LootManager(plugin));
    }

    public void shutdown() {
        this.gemsManager = null;
        this.essencesManager = null;
        this.runesManager = null;
        this.magicDustManager = null;
        this.arrowManager = null;
        this.setManager = null;
        this.itemGeneratorManager = null;
        this.classManager = null;
        this.refineManager = null;
        this.fortifyManager = null;
        this.identifyManager = null;
        this.soulboundManager = null;
        this.repairManager = null;
        this.dismantleManager = null;
        this.extractorManager = null;
        this.partyManager = null;
        this.itemHintsManager = null;
        this.dropManager = null;
        this.lootManager = null;
        this.sellManager = null;
        this.combatLogManager = null;
    }

    @Nullable
    public GemManager getGemManager() {
        return this.gemsManager;
    }

    @Nullable
    public EssencesManager getEssenceManager() {
        return this.essencesManager;
    }

    @Nullable
    public RuneManager getRuneManager() {
        return this.runesManager;
    }

    @Nullable
    public MagicDustManager getMagicDustManager() {
        return this.magicDustManager;
    }

    @Nullable
    public ArrowManager getArrowManager() {
        return this.arrowManager;
    }

    @Nullable
    public SetManager getSetManager() {
        return this.setManager;
    }

    @Nullable
    public ItemGeneratorManager getTierManager() {
        return this.itemGeneratorManager;
    }

    @Nullable
    public ClassManager getClassManager() {
        return this.classManager;
    }

    @Nullable
    public RefineManager getRefineManager() {
        return this.refineManager;
    }

    @Nullable
    public FortifyManager getFortifyManager() {
        return this.fortifyManager;
    }

    @Nullable
    public IdentifyManager getIdentifyManager() {
        return this.identifyManager;
    }

    @Nullable
    public SoulboundManager getSoulboundManager() {
        return this.soulboundManager;
    }

    @Nullable
    public RepairManager getRepairManager() {
        return this.repairManager;
    }

    @Nullable
    public DismantleManager getResolveManager() {
        return this.dismantleManager;
    }

    @Nullable
    public ExtractorManager getExtractManager() {
        return this.extractorManager;
    }

    @Nullable
    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    @Nullable
    public ItemHintsManager getItemHintsManager() {
        return this.itemHintsManager;
    }

    @Nullable
    public DropManager getDropManager() {
        return this.dropManager;
    }

    @Nullable
    public LootManager getLootManager() {
        return this.lootManager;
    }

    @Nullable
    public SellManager getSellManager() {
        return this.sellManager;
    }

    @Nullable
    public CombatLogManager getCombatLogManager() {
        return this.combatLogManager;
    }

    @Nullable
    public MoneyManager getMoneyManager() {
        return this.moneyManager;
    }
}