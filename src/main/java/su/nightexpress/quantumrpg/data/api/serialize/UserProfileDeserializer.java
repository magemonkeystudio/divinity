package su.nightexpress.quantumrpg.data.api.serialize;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.data.api.UserEntityNamesMode;
import su.nightexpress.quantumrpg.data.api.UserProfile;
import su.nightexpress.quantumrpg.manager.effects.buffs.SavedBuff;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class UserProfileDeserializer implements JsonDeserializer<UserProfile> {
    public UserProfile deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject j = json.getAsJsonObject();
        String id = j.get("name").getAsString();
        boolean isDefault = j.get("isDefault").getAsBoolean();
        Set<SavedBuff> buffDamage = new HashSet<>();
        JsonElement jBuffsElem = j.get("buffDamage");
        JsonArray jBuffs = null;
        if (jBuffsElem != null) {
            jBuffs = jBuffsElem.getAsJsonArray();
            for (JsonElement e : jBuffs)
                buffDamage.add(context.deserialize(e, SavedBuff.class));
        }
        Set<SavedBuff> buffDefense = new HashSet<>();
        jBuffsElem = j.get("buffDefense");
        jBuffs = null;
        if (jBuffsElem != null) {
            jBuffs = jBuffsElem.getAsJsonArray();
            for (JsonElement e : jBuffs)
                buffDefense.add(context.deserialize(e, SavedBuff.class));
        }
        Set<SavedBuff> buffStats = new HashSet<>();
        jBuffsElem = j.get("buffStats");
        jBuffs = null;
        if (jBuffsElem != null) {
            jBuffs = jBuffsElem.getAsJsonArray();
            for (JsonElement e : jBuffs)
                buffStats.add(context.deserialize(e, SavedBuff.class));
        }
        JsonElement eInventory = j.get("inventory");
        ItemStack[] inventory = new ItemStack[41];
        if (eInventory != null) {
            int count = 0;
            for (JsonElement item : eInventory.getAsJsonArray())
                inventory[count++] = ItemUT.fromBase64(item.getAsString());
        }
        JsonElement eNames = j.get("namesMode");
        String namesModeRaw = (eNames != null) ? eNames.getAsString() : null;
        UserEntityNamesMode namesMode = (namesModeRaw != null) ? CollectionsUT.getEnum(namesModeRaw, UserEntityNamesMode.class) : UserEntityNamesMode.DEFAULT;
        JsonElement eHideHelmet = j.get("hideHelmet");
        boolean hideHelmet = eHideHelmet != null && eHideHelmet.getAsBoolean();
        UserClassData cData = null;
        JsonElement jData = j.get("cData");
        if (jData != null && QuantumRPG.getInstance().cfg().isModuleEnabled("classes")) {
            JsonObject jClass = jData.getAsJsonObject();
            cData = context.deserialize(jClass, UserClassData.class);
            String clazzId = cData.getClassId();
            RPGClass clazz = QuantumAPI.getModuleManager().getClassManager().getClassById(clazzId);
            if (clazz == null) {
                System.out.println("[QuantumRPG] Player class '" + clazzId + "' no more exists.");
                cData = null;
            } else {
                cData.setPlayerClass(clazz);
            }
        }
        JsonElement jCooldown = j.get("cCooldown");
        long cCooldown = 0L;
        if (jCooldown != null)
            cCooldown = jCooldown.getAsLong();
        return new UserProfile(
                id,
                isDefault,

                buffDamage,
                buffDefense,
                buffStats,

                inventory,
                (namesMode == null) ? UserEntityNamesMode.DEFAULT : namesMode,
                hideHelmet,

                cData,
                cCooldown);
    }
}
