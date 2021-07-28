package su.nightexpress.quantumrpg.libs.apihelper.exception;

public class HostRegistrationException extends RuntimeException {
  private static final long serialVersionUID = 1204727076469488682L;
  
  public HostRegistrationException() {}
  
  public HostRegistrationException(String message) {
    super(message);
  }
  
  public HostRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public HostRegistrationException(Throwable cause) {
    super(cause);
  }
  
  public HostRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
