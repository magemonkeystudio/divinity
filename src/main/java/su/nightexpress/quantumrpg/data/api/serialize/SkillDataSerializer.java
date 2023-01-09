package su.nightexpress.quantumrpg.data.api.serialize;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import su.nightexpress.quantumrpg.modules.list.classes.ComboManager.ComboKey;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserSkillData;

public class SkillDataSerializer implements JsonSerializer<UserSkillData>, JsonDeserializer<UserSkillData> {

	@Override
	public JsonElement serialize(UserSkillData data, Type type, JsonSerializationContext contex) {
		
		JsonObject o = new JsonObject();
		o.addProperty("id", data.getId());
		o.addProperty("lvl", data.getLevel());
		o.add("combo", contex.serialize(data.getCombo()));
		
		return o;
	}
	
	@Override
	public UserSkillData deserialize(JsonElement json, Type type, JsonDeserializationContext contex)
			throws JsonParseException {
		
		JsonObject o = json.getAsJsonObject();
		String id = o.get("id").getAsString();
		int lvl = o.get("lvl").getAsInt();
		ComboKey[] combo = new ComboKey[ComboKey.values().length];
		
		JsonElement eCombo = o.get("combo");
		if (eCombo != null) {
			JsonArray jCombo = eCombo.getAsJsonArray();
			combo = new ComboKey[jCombo.size()];
		
			int i = 0;
			for (JsonElement e : jCombo) {
				combo[i++] = (contex.deserialize(e, ComboKey.class));
			}
		}
		
		return new UserSkillData(id, lvl, combo);
	}

}
