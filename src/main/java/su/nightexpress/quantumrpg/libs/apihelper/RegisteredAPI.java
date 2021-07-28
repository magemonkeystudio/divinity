package su.nightexpress.quantumrpg.libs.apihelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.libs.apihelper.exception.HostRegistrationException;
import su.nightexpress.quantumrpg.libs.apihelper.exception.MissingHostException;

public class RegisteredAPI {
  protected final API api;
  
  protected final Set<Plugin> hosts = new HashSet<>();
  
  protected boolean initialized = false;
  
  protected Plugin initializerHost;
  
  protected boolean eventsRegistered = false;
  
  public RegisteredAPI(API api) {
    this.api = api;
  }
  
  public void registerHost(Plugin host) throws HostRegistrationException {
    this.hosts.add(host);
  }
  
  public Plugin getNextHost() throws MissingHostException {
    if (this.api instanceof Plugin && ((Plugin)this.api).isEnabled())
      return (Plugin)this.api; 
    if (this.hosts.isEmpty())
      throw new MissingHostException("API '" + this.api.getClass().getName() + "' is disabled, but no other Hosts have been registered"); 
    for (Iterator<Plugin> iterator = this.hosts.iterator(); iterator.hasNext(); ) {
      Plugin host = iterator.next();
      if (host.isEnabled())
        return host; 
    } 
    throw new MissingHostException("API '" + this.api.getClass().getName() + "' is disabled and all registered Hosts are as well");
  }
  
  public void init() {
    if (this.initialized)
      return; 
    this.api.init(this.initializerHost = getNextHost());
    this.initialized = true;
  }
  
  public void disable() {
    if (!this.initialized)
      return; 
    this.api.disable(this.initializerHost);
    this.initialized = false;
  }
}
