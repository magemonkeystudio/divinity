package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.manager.api.Loadable;
import mc.promcteam.engine.utils.FileUT;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.stats.tiers.Tier;

public class ResourceManager implements Loadable {

	private ItemGeneratorManager itemGen;
	private Map<ResourceCategory, Map<String, Map<ResourceType, List<String>>>> resources;

	ResourceManager(@NotNull ItemGeneratorManager itemGen) {
		this.itemGen = itemGen;
	}
	
	@Override
	public void setup() {
		this.resources = new HashMap<>();
		
		this.createMissingResources();
		
		this.load(ResourceCategory.MATERIAL, Config.getAllRegisteredMaterials()
					.stream().map(mat -> mat.name().toLowerCase()).collect(Collectors.toSet()));
		
		this.load(ResourceCategory.SUBTYPE, Config.getSubTypeIds());
		
		this.load(ResourceCategory.TIER, Config.getTiers()
					.stream().map(tier -> tier.getId()).collect(Collectors.toSet()));
	}

	@Override
	public void shutdown() {
		if (this.resources != null) {
			this.resources.clear();
			this.resources = null;
		}
	}

	private void createMissingResources() {
		for (ResourceType rt : ResourceType.values()) {
			String folder = rt.getFolder();
			
			for (ResourceCategory cat : ResourceCategory.values()) {
		        String path = itemGen.getFullPath() + "resources/names/" + folder + "/" + cat.getFolder();
				
		        itemGen.plugin.getConfigManager().extractFullPath(path, "txt", false);
		        
		        if (cat == ResourceCategory.TIER) {
					for (Tier tier : Config.getTiers()) {
						String tName = tier.getId() + ".txt";
						
			        	File f = new File(path, tName);
			        	FileUT.create(f);
			        }
		        }
		        else if (cat == ResourceCategory.MATERIAL) {
		    		for (Material m : Config.getAllRegisteredMaterials()) {
		            	File f = new File(path, m.name().toLowerCase() + ".txt");
		            	FileUT.create(f);
		    		}
		        }
		        else if (cat == ResourceCategory.SUBTYPE) {
		        	for (String sub : Config.getSubTypeIds()) {
		            	File f = new File(path, sub.toLowerCase() + ".txt");
		            	FileUT.create(f);
		    		}
		        }
			}
		}
	}
	
	private void load(@NotNull ResourceCategory cat, @NotNull Set<String> idList) {
		Map<String, Map<ResourceType, List<String>>> mapRes = new HashMap<>();
		for (String id : idList) {
			
			Map<ResourceType, List<String>> mapType = new HashMap<>();
			for (ResourceType resourceType : ResourceType.values()) {
				List<String> resList = this.getFileLines(resourceType, cat, id);
				mapType.put(resourceType, resList);
			}
			mapRes.put(id.toLowerCase(), mapType);
			
		}
		this.resources.put(cat, mapRes);
	}

	@NotNull
	private List<String> getFileLines(
			@NotNull ResourceType type, 
			@NotNull ResourceCategory cat, 
			@NotNull String file) {
		
		List<String> list = new ArrayList<>();
		
		String folder = type.getFolder();
	    String path = itemGen.getFullPath() + "resources/names/" + folder + "/" + cat.getFolder() + "/" + file + ".txt";
	
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				list.add(sCurrentLine);
			}	
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}

	@NotNull
	public List<String> getPrefix(@NotNull ResourceCategory cat, @NotNull String type) {
		return this.get(ResourceType.PREFIX, cat, type);
	}
	
	@NotNull
	public List<String> getSuffix(@NotNull ResourceCategory cat, @NotNull String type) {
		return this.get(ResourceType.SUFFIX, cat, type);
	}
	
	@NotNull
	public List<String> get(@NotNull ResourceType type, @NotNull ResourceCategory cat, @NotNull String id) {
		return this.resources
					.getOrDefault(cat, Collections.emptyMap())
					.getOrDefault(id.toLowerCase(), Collections.emptyMap())
					.getOrDefault(type, Collections.emptyList());
	}

	public static enum ResourceCategory {
	
		MATERIAL("materials"),
		SUBTYPE("types"),
		TIER("tiers");
		
		private String folder;
		
		private ResourceCategory(@NotNull String folder) {
			this.folder = folder;
		}
		
		@NotNull
		public String getFolder() {
			return this.folder;
		}
	}

	public static enum ResourceType {
	
		PREFIX("prefixes"),
		SUFFIX("suffixes");
		
		private String folder;
		
		private ResourceType(@NotNull String folder) {
			this.folder = folder;
		}
		
		@NotNull
		public String getFolder() {
			return this.folder;
		}
	}

}
