package com.promcteam.divinity.testutil;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.mccore.commands.CommandManager;
import com.promcteam.codex.nms.NMS;
import com.promcteam.codex.nms.TEST;
import com.promcteam.codex.util.ItemUT;
import com.promcteam.codex.util.reflection.ReflectionManager;
import com.promcteam.codex.util.reflection.ReflectionUtil;
import com.promcteam.divinity.Divinity;
import com.promcteam.fabled.api.player.PlayerData;
import org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MockedTest {
    protected ServerMock                      server;
    protected CodexEngine                     engine;
    protected Divinity                        plugin;
    protected List<PlayerMock>                players          = new ArrayList<>();
    protected Map<UUID, PlayerData>           activePlayerData = new HashMap<>();
    protected MockedStatic<ReflectionManager> reflectionManager;
    protected ReflectionUtil                  reflectionUtil;
    protected MockedStatic<CodexEngine>       codexEngine;
    protected NMS                             nms;

    @BeforeAll
    public void setupServer() {
        server = spy(MockBukkit.mock());
        String coreVersion  = System.getProperty("CODEX_VERSION");
        String itemsVersion = System.getProperty("ITEMS_VERSION");

        try {
            File itemsJar =
                    new File(server.getPluginsFolder().getAbsolutePath(), "Divinity-" + itemsVersion + ".jar");
            if (!itemsJar.exists()) itemsJar.createNewFile();
            createZipArchive(itemsJar, "target/classes");

            File core = DependencyResolver.resolve("com.promcteam:codex:" + coreVersion);
            File dest = new File(server.getPluginsFolder().getAbsolutePath(), "Codex-" + coreVersion + ".jar");
            FileUtils.copyFile(core, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        reflectionUtil = mock(ReflectionUtil.class);
        reflectionManager = mockStatic(ReflectionManager.class);
        reflectionManager.when(ReflectionManager::getReflectionUtil)
                .thenReturn(reflectionUtil);
        when(reflectionUtil.getDefaultDamage(any(ItemStack.class)))
                .thenAnswer(args -> {
                    switch (((ItemStack) args.getArgument(0)).getType()) {
                        case DIAMOND_SWORD:
                            return 7.0;
                        case IRON_SWORD:
                            return 6.0;
                        case WOODEN_SWORD:
                            return 4.0;
                        case TRIDENT:
                            return 9.0;
                        default:
                            return 1.0;
                    }
                });

        engine = MockBukkit.load(CodexEngine.class);
        nms = ((TEST) engine.getNMS()).getTestNms();
        when(nms.fixColors(anyString()))
                .thenAnswer(args -> args.getArgument(0));
        when(nms.toBase64(any())).thenReturn("");
        codexEngine = mockStatic(CodexEngine.class);
        codexEngine.when(CodexEngine::get).thenReturn(engine);

        ItemUT.setEngine(engine);

        plugin = MockBukkit.load(Divinity.class);
        server.getScheduler().performOneTick();
//        server.getScheduler().waitAsyncTasksFinished();
    }

    @AfterAll
    public void destroy() {
        CommandManager.unregisterAll();
        MockBukkit.unmock();
    }

    @AfterEach
    public void clearData() {
        activePlayerData.clear();
        clearEvents();
        players.clear();
    }

    public PlayerData generatePlayerData(Player player) {
        PlayerData pd = mock(PlayerData.class);
        activePlayerData.put(player.getUniqueId(), pd);

        when(pd.getPlayer()).thenReturn(player);
        return pd;
    }

    public PlayerMock genPlayer(String name) {
        return genPlayer(name, true);
    }

    public PlayerMock genPlayer(String name, boolean op) {
        PlayerMock pm = new PlayerMock(server, name, UUID.randomUUID());
        server.addPlayer(pm);
        players.add(pm);
        pm.setOp(op);

        return pm;
    }

    public <T extends Event> void assertEventFired(Class<T> clazz) {
        server.getPluginManager().assertEventFired(clazz);
    }

    public <T extends Event> void assertEventFired(Class<T> clazz, Predicate<T> predicate) {
        server.getPluginManager().assertEventFired(clazz, predicate);
    }

    public void clearEvents() {
        server.getPluginManager().clearEvents();
    }

    private final static int BUFFER = 2048;

    public boolean createZipArchive(File destFile, String srcFolder) {
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)))) {
            addFolder(srcFolder, "", out);
        } catch (Exception e) {
            System.out.println("createZipArchive threw exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void addFolder(String srcFolder, String baseFolder, ZipOutputStream out) throws IOException {
        File   subDir       = new File(srcFolder);
        String subdirList[] = subDir.list();
        for (String sd : subdirList) {
            // get a list of files from current directory
            File f = new File(srcFolder + "/" + sd);
            if (f.isDirectory())
                addFolder(f.getAbsolutePath(), baseFolder + "/" + sd, out);
            else {//it is just a file
                addFile(new FileInputStream(f), baseFolder + "/" + sd, out);
            }
        }
    }

    @NotNull
    private void addFile(FileInputStream f, String sd, ZipOutputStream out) throws IOException {
        byte            data[] = new byte[BUFFER];
        FileInputStream fi     = f;
        try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)) {
            ZipEntry entry = new ZipEntry(sd);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
                out.flush();
            }
        }
    }
}
