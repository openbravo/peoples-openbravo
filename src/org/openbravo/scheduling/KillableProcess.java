package org.openbravo.scheduling;

/**
 * KillableProcess needs to be implemented in any process you want to be able to kill from the
 * Process Monitor
 */
public interface KillableProcess {

  public void kill() throws Exception;

}
