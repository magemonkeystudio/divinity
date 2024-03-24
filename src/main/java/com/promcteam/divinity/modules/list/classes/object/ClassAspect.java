package com.promcteam.divinity.modules.list.classes.object;

import com.promcteam.codex.utils.StringUT;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class ClassAspect {

    private String   id;
    private String   name;
    private Material material;

    public ClassAspect(
            @NotNull String id,
            @NotNull String name,
            @NotNull Material material
    ) {
        this.id = id.toLowerCase();
        this.name = StringUT.color(name);
        this.material = material;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public Material getMaterial() {
        return this.material;
    }
}
