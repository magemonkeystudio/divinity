package studio.magemonkey.divinity.stats.items.attributes;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.divinity.stats.items.ItemTags;
import studio.magemonkey.divinity.stats.items.api.ItemLoreStat;
import studio.magemonkey.divinity.utils.ItemUtils;

public class AmmoAttribute extends ItemLoreStat<String> {

    private AmmoAttribute.Type type;

    public AmmoAttribute(
            @NotNull Type type,
            @NotNull String name,
            @NotNull String format
    ) {
        super(type.name(),
                name,
                format,
                ItemTags.PLACEHOLDER_ITEM_AMMO,
                ItemTags.TAG_ITEM_AMMO,
                PersistentDataType.STRING);
        this.type = type;

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:item_ammo_" + this.getId()));
        this.keys.add(NamespacedKey.fromString("prorpgitems:item_ammo" + this.getId()));
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_item_ammo" + this.getId()));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_item_ammo" + this.getId()));
    }

    @Override
    @NotNull
    public Class<String> getParameterClass() {
        return String.class;
    }

    public static enum Type {

        ARROW(Arrow.class, EntityType.ARROW, true),
        SNOWBALL(Snowball.class, EntityType.SNOWBALL, false),
        EGG(Egg.class, EntityType.EGG, false),
        FIREBALL(Fireball.class, EntityType.FIREBALL, true),
        WITHER_SKULL(WitherSkull.class, EntityType.WITHER_SKULL, true),
        SHULKER_BULLET(ShulkerBullet.class, EntityType.SHULKER_BULLET, true),
        LLAMA_SPIT(LlamaSpit.class, EntityType.LLAMA_SPIT, true),
        ENDER_PEARL(EnderPearl.class, EntityType.ENDER_PEARL, false),
        EXP_POTION(ThrownExpBottle.class,
                EntityType.fromName("experience_bottle"), false), // THROWN_EXP_BOTTLE/EXPERIENCE_BOTTLE
        ;

        private Class<? extends Projectile> clazz;
        private EntityType                  eType;
        private boolean                     dealsDamage;

        private Type(@NotNull Class<? extends Projectile> clazz, @NotNull EntityType eType, boolean dealsDamage) {
            this.clazz = clazz;
            this.eType = eType;
            this.dealsDamage = dealsDamage;
        }

        /**
         * Whether this kind of projectile hurts what it hits. Pearls, snowballs, eggs and bottles are thrown
         * for their other effects, and the damage they cause on impact is not what the ammo's stats are for.
         */
        public boolean dealsDamage() {
            return this.dealsDamage;
        }

        @NotNull
        public Class<? extends Projectile> getProjectileClass() {
            return this.clazz;
        }
    }

    @Override
    protected boolean isSingle() {
        return true;
    }

    @NotNull
    public AmmoAttribute.Type getType() {
        return this.type;
    }

    @Override
    public boolean add(@NotNull ItemStack item, @NotNull String value, int line) {
        return this.add(item, line);
    }

    public boolean add(@NotNull ItemStack item, int line) {
        return super.add(item, this.getType().name(), line);
    }

    @NotNull
    public Projectile getProjectile(@NotNull LivingEntity shooter) {
        switch (this.type) {
            case ARROW:
            case SNOWBALL:
            case EGG:
            case ENDER_PEARL:
            case FIREBALL:
            case WITHER_SKULL: {
                return shooter.launchProjectile(this.getType().clazz);
            }

            case EXP_POTION:
            case LLAMA_SPIT:
            case SHULKER_BULLET: {
                Location eye = shooter.getEyeLocation();
                Projectile sb =
                        (Projectile) shooter.getWorld().spawnEntity(eye.add(eye.getDirection()), this.getType().eType);
                return sb;
            }
        }
        return shooter.launchProjectile(this.type.clazz);
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String value) {
        if (!ItemUtils.isBow(item)) return "";

        return value;
    }
}
