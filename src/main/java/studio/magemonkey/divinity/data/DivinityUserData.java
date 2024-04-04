package studio.magemonkey.divinity.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import studio.magemonkey.codex.data.DataTypes;
import studio.magemonkey.codex.data.IDataHandler;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.data.api.DivinityUser;
import studio.magemonkey.divinity.data.api.UserProfile;
import studio.magemonkey.divinity.data.api.serialize.SkillDataSerializer;
import studio.magemonkey.divinity.data.api.serialize.UserProfileSerializer;
import studio.magemonkey.divinity.modules.list.classes.api.UserSkillData;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Function;

public class DivinityUserData extends IDataHandler<Divinity, DivinityUser> {

    private static DivinityUserData instance;

    private final Function<ResultSet, DivinityUser> FUNC_USER;

    protected DivinityUserData(@NotNull Divinity plugin) throws SQLException {
        super(plugin);

        this.FUNC_USER = (rs) -> {
            try {
                UUID   uuid       = UUID.fromString(rs.getString(COL_USER_UUID));
                String name       = rs.getString(COL_USER_NAME);
                long   lastOnline = rs.getLong(COL_USER_LAST_ONLINE);

                LinkedHashMap<String, UserProfile> profiles =
                        gson.fromJson(rs.getString("profiles"), new TypeToken<LinkedHashMap<String, UserProfile>>() {
                        }.getType());

                return new DivinityUser(plugin, uuid, name, lastOnline, profiles);
            } catch (SQLException ex) {
                ex.printStackTrace();
                return null;
            }
        };
    }

    public static DivinityUserData getInstance(@NotNull Divinity plugin) throws SQLException {
        if (instance == null) {
            instance = new DivinityUserData(plugin);
        }
        return instance;
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return super.registerAdapters(builder
                .registerTypeAdapter(UserProfile.class, new UserProfileSerializer())
                .registerTypeAdapter(UserSkillData.class, new SkillDataSerializer())
        );
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToCreate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("profiles", DataTypes.STRING.build(this.dataType));
        return map;
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToSave(@NotNull DivinityUser user) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("profiles", this.gson.toJson(user.getProfileMap()));
        return map;
    }

    @Override
    @NotNull
    protected Function<ResultSet, DivinityUser> getFunctionToUser() {
        return this.FUNC_USER;
    }

}
