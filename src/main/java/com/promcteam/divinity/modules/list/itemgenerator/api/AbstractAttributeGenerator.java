package com.promcteam.divinity.modules.list.itemgenerator.api;

import com.promcteam.divinity.Divinity;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;

import java.util.List;

public abstract class AbstractAttributeGenerator implements IAttributeGenerator {

    protected Divinity      plugin;
    protected GeneratorItem generatorItem;

    protected       int          minAmount;
    protected       int          maxAmount;
    protected       List<String> loreFormat;
    protected final String       placeholder;

    public AbstractAttributeGenerator(
            @NotNull Divinity plugin,
            @NotNull GeneratorItem generatorItem,
            @NotNull String placeholder) {
        this.plugin = plugin;
        this.generatorItem = generatorItem;
        this.placeholder = placeholder;
    }

    @Override
    public final int getMaxAmount() {
        return this.maxAmount;
    }

    @Override
    public final int getMinAmount() {
        return this.minAmount;
    }

    @Override
    @NotNull
    public final List<String> getLoreFormat() {
        return this.loreFormat;
    }

    @Override
    @NotNull
    public final String getPlaceholder() {
        return this.placeholder;
    }
}