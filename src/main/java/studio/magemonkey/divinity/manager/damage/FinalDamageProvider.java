package studio.magemonkey.divinity.manager.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.damage.DamageContexts;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.FinalDamageEvent;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.DivinityHitEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;

/**
 * Divinity as a provider of Codex's {@link FinalDamageEvent}: it fires one, as {@link DivinityHitEvent.Result},
 * for most hits. {@link #willFire(EntityDamageEvent)} is the predicate Divinity registers, so that a plugin
 * that cannot see Divinity (Fabled) can ask {@link FinalDamageEvent#willFire(EntityDamageEvent)} whether to
 * change the Bukkit event's damage itself or to wait for the event.
 * <p>
 * The predicate follows the early returns of {@code VanillaWrapperListener#onVanillaDamage} that come before
 * any event is fired. Keep the two in step: a new early return there needs a check here, and a test in
 * {@code FinalDamageProviderTest}.
 */
public final class FinalDamageProvider {

    private FinalDamageProvider() {}

    public static void register(@NotNull Divinity plugin) {
        FinalDamageEvent.registerProvider(plugin, FinalDamageProvider::willFire);
    }

    public static void unregister(@NotNull Divinity plugin) {
        FinalDamageEvent.unregisterProvider(plugin);
    }

    /**
     * Whether Divinity's damage handler will reach {@link DivinityHitEvent.Result}, and so fire a
     * {@link FinalDamageEvent}, for this hit, should nothing cancel it first. It changes nothing and claims no
     * damage request (it peeks), so it may be asked from any damage listener, before or after Divinity's.
     * <p>
     * False when:
     * <ul>
     *     <li>Divinity is set to vanilla-only damage handling;</li>
     *     <li>the target is not a living entity;</li>
     *     <li>the event is one of Fabled's throwaway events that ask whether an attack is allowed;</li>
     *     <li>the hit is described by a request with {@link WeaponStatsMode#RAW}, read with
     *     {@link DamageContexts#peek(EntityDamageEvent)}, which does not claim it;</li>
     *     <li>it is a vanilla sweep attack and vanilla sweeping is disabled (Divinity cancels it).</li>
     * </ul>
     * True otherwise, including for some hits that then fire no event, because what decides them is not
     * known until Divinity's handler runs. In every such case the Bukkit event ends up cancelled, so a plugin
     * that skipped its own changes because of the answer loses nothing:
     * <ul>
     *     <li>a dodged hit (the roll happens in the handler);</li>
     *     <li>a hit the attacker may not deal to the target (protection plugins, NPCs), which Divinity
     *     cancels. That check asks WorldGuard and Citizens, so it is not repeated here;</li>
     *     <li>a hit cancelled by a listener of one of Divinity's own events before {@code Result}.</li>
     * </ul>
     */
    public static boolean willFire(@NotNull EntityDamageEvent event) {
        if (EngineCfg.VANILLA_ONLY_DAMAGE_HANDLING) return false;
        if (!(event.getEntity() instanceof LivingEntity)) return false;

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) event;
            if (isFabledFakeDamage(ede)) return false;
        }

        DamageRequest request = DamageContexts.peek(event);
        if (request != null && request.getWeaponStats() == WeaponStatsMode.RAW) return false;

        if (event instanceof EntityDamageByEntityEvent
                && event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK
                && EngineCfg.COMBAT_DISABLE_VANILLA_SWEEP
                && ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity) return false;

        return true;
    }

    private static boolean isFabledFakeDamage(@NotNull EntityDamageByEntityEvent event) {
        Divinity plugin = Divinity.getInstance();
        if (plugin == null || !plugin.getPluginManager().isPluginEnabled("Fabled")) return false;
        FabledHook hook = (FabledHook) plugin.getHook(EHook.SKILL_API);
        return hook != null && hook.isFakeDamage(event);
    }
}
