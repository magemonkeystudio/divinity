package org.mockbukkit.mockbukkit.persistence;

/**
 * Test-only shim. Codex's DataUT#itemPersistentDataContainer instantiates
 * {@code <server package>.persistence.CraftPersistentDataContainer(registry)}, which exists on
 * CraftBukkit but not on MockBukkit. Backed by MockBukkit's own container implementation.
 */
public class CraftPersistentDataContainer extends PersistentDataContainerMock {
    public CraftPersistentDataContainer(Object registry) {
        super();
    }
}
