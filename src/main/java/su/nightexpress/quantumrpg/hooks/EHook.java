package su.nightexpress.quantumrpg.hooks;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.external.*;
import su.nightexpress.quantumrpg.hooks.placeholders.PapiHook;

public enum EHook {
    CITIZENS("Citizens", CitizensHook.class),
    BATTLE_LEVELS("BattleLevels", BattleLevelsHook.class),
    HEROES("Heroes", HeroesHook.class),
    HOLOGRAPHIC_DISPLAYS("HolographicDisplays", HDHook.class),
    MCMMO("mcMMO", McmmoHook.class),
    MYTHIC_MOBS("MythicMobs", MythicMobsHook.class),
    NONE("None", DefaultHook.class),
    PLACEHOLDER_API("PlaceholderAPI", PapiHook.class),
    PVP_LEVELS("PvPLevels", PvPLevelsHook.class),
//    PLAYER_LEVELS("PlayerLevels", PlayerLevelsHK.class),
//    RACES_OF_THANA("RacesOfThana", RoTHook.class),
    RPG_INVENTORY("RPGInventory", RPGInvHook.class),
    SKILL_API("SkillAPI", SkillAPIHook.class),
//    SKILLS("Skills", SkillsHook.class),
    VAULT("Vault", VaultHook.class),
    WORLD_GUARD("WorldGuard", WorldGuardHook.class),
    RESIDENCE("Residence", ResidenceHook.class);

    private final String plugin;
    private final Class<? extends Hook> cl;
    private boolean e = false;
    private Hook h = null;

    EHook(String plugin, Class<? extends Hook> cl) {
        this.plugin = plugin;
        this.cl = cl;
    }

    public boolean isEnabled() {
        return this.e;
    }

    public void enable() {
        this.e = true;
        setHook();
        this.h.setup();
    }

    public void disable() {
        this.e = false;
        this.h.shutdown();
        this.h = null;
    }

    public String getPluginName() {
        return this.plugin;
    }

    public boolean isLevel() {
        return this.h instanceof HookLevel;
    }

    public boolean isClass() {
        return this.h instanceof HookClass;
    }

    public void setHook() {
        try {
            this.h = this.cl.getConstructor(new Class[]{EHook.class, QuantumRPG.class}).newInstance(this, QuantumRPG.instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hook getHook() {
        return this.h;
    }
}
