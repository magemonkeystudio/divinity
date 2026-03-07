# Custom Durability Implementation Plan (V2)

## Overview
Standardize durability management in Divinity by leveraging Minecraft's native `max_damage` Data Component (introduced in 1.20.6). This removes the need for complex manual syncing and allows external plugins like Fabled to interact with Divinity items natively while preserving Divinity's custom logic.

## Phase 1: Divinity Enhancements

### 1. ItemStats.java Refactoring
- **Centralize Syncing**: Update `updateVanillaAttributes` to handle all physical item properties.
- **Efficient Meta Updates**: Refactor `addAttribute` to accept an `ItemMeta` instance.
- **Durability Syncing Logic**:
    - **Proper Unbreakable**: If Divinity durability is `-1`, call `meta.setUnbreakable(true)`.
    - **Native Durability (1.20.6+)**:
        - Check `Version.CURRENT.isAtLeast(Version.V1_20_R4)`.
        - Set `max_damage` to `(int) (maxCustom + 1)`.
        - Set `damage` to `(int) (maxCustom - currentCustom)`.
        - *Why `+1`?* This prevents vanilla from breaking the item when it reaches 0 custom durability, allowing Divinity's `EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS` to decide the outcome.
    - **Legacy Durability (< 1.20.6)**:
        - Use `damageable.setDamage()` with the standard vanilla max to reflect the percentage of custom durability.

### 2. Performance: Dirty Check Example
To avoid redundant NBT/component writes:
```java
if (im instanceof Damageable damageable) {
    int targetMax = (int) (max + 1);
    int targetDmg = (int) (max - current);
    
    if (damageable.getMaxDamage() != targetMax) damageable.setMaxDamage(targetMax);
    if (damageable.getDamage() != targetDmg) damageable.setDamage(targetDmg);
}
```

### 3. ItemDurabilityListener.java Refinement
- **Support Native Damage**: Catch `PlayerItemDamageEvent`. If the item has a custom durability stat:
    - **Loop Prevention**: Ensure that calling `reduceDurability` does not trigger a recursive damage event. If Phase 2's `item.damage()` approach is used, use a `ThreadLocal` or metadata flag to ignore the second event.
    - Cancel the event.
    - Call `duraStat.reduceDurability(player, item, event.getDamage())`.
    - This allows plugins like Fabled to use `item.damage(amount, player)` without bypassing Divinity's events, break sounds, or unbreaking logic.
- **Support Mending natively**:
    - **1.20.6+**: MONITOR priority. Sync internal stat from vanilla damage: `current = maxCustom - vanillaDamage`.
    - **Legacy**: Cancel event, apply repair manually using vanilla `XP * 2` ratio, and consume XP orb by setting its experience to 0.

---

## Phase 2: Fabled Integration

### 1. DivinityHook.java (Fabled)
- Add `reduceDurability(LivingEntity user, ItemStack item, int amount)`.
- **Implementation**: `item.damage(amount, user)`.
- *Why?* By using the standard Bukkit damage method, Fabled stays decoupled from Divinity's internals, while Divinity's new `PlayerItemDamageEvent` listener ensures all custom logic is still triggered.

### 2. DurabilityMechanic.java (Fabled)
- Update to use the new hook:
    ```java
    if (DivinityHook.isDivinity(item)) {
        DivinityHook.reduceDurability(caster, item, amount);
    }
    ```

---

## Technical Details & Constraints

### 1. Version-Specific Technical Nuances
- **Data Components (1.20.6+)**: 
    - At `Version.V1_20_R4` and higher, Minecraft transitioned from NBT to Data Components.
    - `max_damage` and `damage` are components. While `Damageable#setMaxDamage` works, ensure the internal `unbreakable` state is set via `meta.setUnbreakable(true)` which now maps to the `minecraft:unbreakable` component.
- **ItemFlag Transitions**:
    - In 1.20.5+, many legacy flags were consolidated. 
    - Use `ItemFlag.HIDE_ADDITIONAL_TOOLTIP` if `HIDE_ATTRIBUTES` or `HIDE_ENCHANTS` is insufficient to hide the native durability text/bar when desired.
- **Paper API Attribute Hack**: 
    - In 1.20.4+, Paper requires at least one non-default attribute modifier for `HIDE_ATTRIBUTES` to function. Ensure the "0-modifier Movement Speed" hack (already in `ItemStats`) is preserved during the refactor.

### 2. Version Mapping
`Version.V1_20_R4` in Codex corresponds to Minecraft 1.20.6. This is the baseline for Data Component support.

### 3. Rounding
Since Divinity uses `double` and Vanilla uses `int`, all sync operations should use `Math.round()` to prevent "ghost" durability points or items breaking 1 point early.

### 4. Experience Orbs (Legacy Logic)
When cancelling `PlayerItemMendEvent`:
```java
int experience = event.getExperienceOrb().getExperience();
int repairAmount = experience * 2; // Standard vanilla ratio
// ... apply repair to custom stat ...
event.getExperienceOrb().setExperience(0); // Mark orb as consumed
```

## Testing Requirements
- **Unbreakable Test**: Ensure items with -1 durability show no bar and take no damage.
- **Near-Break Test**: Ensure items stay in inventory at 0 custom durability (empty bar) if `break-items-on-zero` is false.
- **Vanilla Compatibility Test**: Verify that standard Minecraft damage (cactus, fire) and Fabled mechanics correctly reduce the custom durability stat.
