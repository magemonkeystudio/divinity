package su.nightexpress.quantumrpg.libs.apihelper.exception;

public class MissingHostException extends RuntimeException {
  private static final long serialVersionUID = 542397736184222384L;
  
  public MissingHostException() {}
  
  public MissingHostException(String message) {
    super(message);
  }
  
  public MissingHostException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public MissingHostException(Throwable cause) {
    super(cause);
  }
  
  public MissingHostException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
