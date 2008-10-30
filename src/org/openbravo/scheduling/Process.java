package org.openbravo.scheduling;

public interface Process {
  
public static final String EXECUTION_ID = "param.execution.id";
  
  public static final String SCHEDULED = "Scheduled";
  
  public static final String UNSCHEDULED = "Unscheduled";
  
  public static final String MISFIRED = "Misfired";
  
  public static final String PROCESSING = "Processing";
  
  public static final String SUCCESS = "Success";
  
  public static final String ERROR = "Error";
  
  public static final String COMPLETE = "Complete";
  
  public void initialize(ProcessBundle bundle);
  
  public void execute(ProcessBundle bundle) throws Exception;
  
}
