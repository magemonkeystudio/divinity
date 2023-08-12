package su.nightexpress.quantumrpg;

import mc.promcteam.engine.NexDataPlugin;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.commands.api.IGeneralCommand;
import mc.promcteam.engine.hooks.Hooks;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.command.BuffCommand;
import su.nightexpress.quantumrpg.command.ModifyCommand;
import su.nightexpress.quantumrpg.command.SetCommand;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.data.RPGUserData;
import su.nightexpress.quantumrpg.data.UserManager;
import su.nightexpress.quantumrpg.data.api.RPGUser;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.*;
import su.nightexpress.quantumrpg.hooks.external.mimic.MimicHook;
import su.nightexpress.quantumrpg.hooks.external.mythicmobs.MythicMobsHK;
import su.nightexpress.quantumrpg.hooks.external.mythicmobs.MythicMobsHKv5;
import su.nightexpress.quantumrpg.manager.EntityManager;
import su.nightexpress.quantumrpg.manager.damage.DamageManager;
import su.nightexpress.quantumrpg.manager.interactions.InteractionManager;
import su.nightexpress.quantumrpg.manager.listener.ListenerManager;
import su.nightexpress.quantumrpg.manager.profile.ProfileManager;
import su.nightexpress.quantumrpg.manager.worth.WorthManager;
import su.nightexpress.quantumrpg.modules.ModuleCache;
import su.nightexpress.quantumrpg.nms.engine.PMS;
import su.nightexpress.quantumrpg.nms.engine.PMSManager;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.utils.actions.conditions.CEntityLevel;
import su.nightexpress.quantumrpg.utils.actions.executors.ActionDamage;
import su.nightexpress.quantumrpg.utils.actions.executors.ActionParticleLine;
import su.nightexpress.quantumrpg.utils.actions.executors.ActionParticlePulse;
import su.nightexpress.quantumrpg.utils.actions.executors.ActionTakeMana;
import su.nightexpress.quantumrpg.utils.actions.params.AttackableParam;
import su.nightexpress.quantumrpg.utils.actions.params.PartyMemberParam;

import java.io.File;
import java.sql.SQLException;

/**
 * @author © 2023 «Фогус-Мультимедиа»
 */
public class QuantumRPG extends NexDataPlugin<QuantumRPG, RPGUser> {

    public static QuantumRPG instance;

    private Config config;
    private Lang lang;
    private EngineCfg engineCfg;

    private RPGUserData dataHandler;

    private InteractionManager interactionManager;
    private WorthManager worthManager;
    private DamageManager dmgManager;
    private EntityManager entityManager;
    private ListenerManager listenerManager;
    private ProfileManager profileManager;
    private ModuleCache moduleCache;

    private PMSManager pms;

    public QuantumRPG() {
        instance = this;
    }

    public QuantumRPG(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        instance = this;
    }

    public static QuantumRPG getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        MimicHook.hook(this);
    }

    @Override
    public void enable() {
        this.addCustomActions();

        this.pms = new PMSManager(this);
        this.pms.setup();

        String coreVersion = NexEngine.getEngine().getDescription().getVersion();
        boolean minCoreVersionMet = DependencyRequirement.meetsVersion(DependencyRequirement.MIN_CORE_VERSION, coreVersion);

        if (this.pms.get() == null || !minCoreVersionMet) {
            if (!minCoreVersionMet) {
                warn("Missing required ProMCCore version. " + coreVersion + " installed. "
                        + DependencyRequirement.MIN_CORE_VERSION + " required. Disabling.");
            }
            this.getPluginManager().disablePlugin(this);
            return;
        }

        this.interactionManager = new InteractionManager(this);
        this.interactionManager.setup();

        this.dmgManager = new DamageManager();
        this.dmgManager.setup();

        this.entityManager = new EntityManager(this);
        this.entityManager.setup();

        this.listenerManager = new ListenerManager(this);
        this.listenerManager.setup();

        this.profileManager = new ProfileManager(this);
        this.profileManager.setup();

        this.moduleCache = new ModuleCache(this);
        this.moduleCache.initialize();

        this.worthManager = new WorthManager(this);
        this.worthManager.setup();
    }

    @Override
    public void disable() {
        if (this.interactionManager != null) {
            this.interactionManager.shutdown();
            this.interactionManager = null;
        }
        if (this.worthManager != null) {
            this.worthManager.shutdown();
            this.worthManager = null;
        }

        this.dmgManager.shutdown();
        this.entityManager.shutdown();
        this.listenerManager.shutdown();
        this.moduleCache.shutdown();

        if (this.profileManager != null) {
            this.profileManager.shutdown();
            this.profileManager = null;
        }

        this.pms.shutdown();

        ItemStats.clear();
        ItemRequirements.clear();
    }

    @Override
    public void setConfig() {
        this.config = new Config(this);
        this.config.setup();

        this.engineCfg = new EngineCfg(this);
        this.engineCfg.setup();

        this.lang = new Lang(this);
        this.lang.setup();

        this.cfg().setupAttributes();

        this.cfg().save();
    }

    @Override
    public void registerHooks() {
        this.registerHook(EHook.CRACK_SHOT, CrackShotHK.class);
        this.registerHook(EHook.LORINTHS_RPG_MOBS, LorinthsRpgMobsHK.class);
        this.registerHook(EHook.MAGIC, MagicHK.class);
        this.registerHook(EHook.MCMMO, McmmoHK.class);
        this.registerHook(EHook.JOBS, JobsRebornHK.class);
        this.registerHook(EHook.AURELIUM_SKILLS, AureliumSkillsHK.class);

        boolean mythic4 = true;
        try {
            Class.forName("io.lumine.xikage.mythicmobs.MythicMobs");
        } catch (ClassNotFoundException classNotFoundException) {
            mythic4 = false;
        }
        if (mythic4)
            this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHK.class);
        else
            this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHKv5.class);

        this.registerHook(EHook.MY_PET, MyPetHK.class);
        if (Hooks.hasPlaceholderAPI()) {
            this.registerHook(Hooks.PLACEHOLDER_API, PlaceholderAPIHK.class);
        }
        this.registerHook(EHook.PWING_RACES, PwingRacesHK.class);
        this.registerHook(EHook.SKILL_API, SkillAPIHK.class);
//		this.registerHook(EHook.RACES_OF_THANA, RacesOfThanaHK.class);
//		this.registerHook(EHook.SKILLS, SkillsProHK.class);
    }

    @Override
    public void registerCmds(@NotNull IGeneralCommand<QuantumRPG> mainCommand) {
        mainCommand.addSubCommand(new ModifyCommand(this));
        mainCommand.addSubCommand(new SetCommand(this));
        mainCommand.addSubCommand(new BuffCommand(this));
    }

    @Override
    public void registerEditor() {

    }

    @Override
    @NotNull
    public Config cfg() {
        return this.config;
    }

    @Override
    @NotNull
    public Lang lang() {
        return this.lang;
    }

    @Override
    protected boolean setupDataHandlers() {
        try {
            this.dataHandler = RPGUserData.getInstance(this);
            this.dataHandler.setup();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        this.userManager = new UserManager(this);
        this.userManager.setup();

        return true;
    }

    @Override
    public RPGUserData getData() {
        return this.dataHandler;
    }

    private void addCustomActions() {
        this.getActionsManager().registerParam(new PartyMemberParam());
        this.getActionsManager().registerParam(new AttackableParam());

        this.getActionsManager().registerCondition(new CEntityLevel(this));

        this.getActionsManager().registerExecutor(new ActionDamage(this));
        this.getActionsManager().registerExecutor(new ActionParticleLine(this));
        this.getActionsManager().registerExecutor(new ActionParticlePulse(this));
        this.getActionsManager().registerExecutor(new ActionTakeMana(this));
    }

    @NotNull
    public PMS getPMS() {
        return this.pms.get();
    }

    @NotNull
    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    @NotNull
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @NotNull
    public WorthManager getWorthManager() {
        return worthManager;
    }

    @NotNull
    public ModuleCache getModuleCache() {
        return moduleCache;
    }
}