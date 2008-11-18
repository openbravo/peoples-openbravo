package org.openbravo.scheduling;

public interface Process {
  
  public static final String EXECUTION_ID = "param.execution.id";
  
  public static final String SCHEDULED = "SCH";
  
  public static final String UNSCHEDULED = "UNS";
  
  public static final String MISFIRED = "MIS";
  
  public static final String PROCESSING = "PRC";
  
  public static final String SUCCESS = "SUC";
  
  public static final String ERROR = "ERR";
  
  public static final String COMPLETE = "COM";
  
  public void execute(ProcessBundle bundle) throws Exception;
  
}
