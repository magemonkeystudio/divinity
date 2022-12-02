package su.nightexpress.quantumrpg.data.api.serialize;

import com.google.gson.*;
import su.nightexpress.quantumrpg.modules.list.classes.ComboManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserSkillData;

import java.lang.reflect.Type;

public class SkillDataDeserializer implements JsonDeserializer<UserSkillData> {
    public UserSkillData deserialize(JsonElement json, Type type, JsonDeserializationContext contex) throws JsonParseException {
        JsonObject              o      = json.getAsJsonObject();
        String                  id     = o.get("id").getAsString();
        int                     lvl    = o.get("lvl").getAsInt();
        ComboManager.ComboKey[] combo  = new ComboManager.ComboKey[(ComboManager.ComboKey.values()).length];
        JsonElement             eCombo = o.get("combo");
        if (eCombo != null) {
            JsonArray jCombo = eCombo.getAsJsonArray();
            combo = new ComboManager.ComboKey[jCombo.size()];
            int i = 0;
            for (JsonElement e : jCombo)
                combo[i++] = (ComboManager.ComboKey) contex.deserialize(e, ComboManager.ComboKey.class);
        }
        return new UserSkillData(id, lvl, combo);
    }
}
