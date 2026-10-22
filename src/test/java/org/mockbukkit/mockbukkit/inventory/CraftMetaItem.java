package org.mockbukkit.mockbukkit.inventory;

/**
 * Test-only shim. Codex's DataUT#itemPersistentDataContainer creates nested data containers by
 * reflectively reading {@code <server package>.inventory.CraftMetaItem.DATA_TYPE_REGISTRY}, which
 * exists on CraftBukkit but not on MockBukkit. Providing it lets item stats be written in tests.
 */
public class CraftMetaItem {
    static final Object DATA_TYPE_REGISTRY = new Object();
}
