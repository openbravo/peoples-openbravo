package org.openbravo.erpCommon.ad_process;

public class ApplyModulesResponse {
  int state;
  String statusofstate;
  String[] warnings;
  String[] errors;
  String lastmessage;

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getStatusofstate() {
    return statusofstate;
  }

  public void setStatusofstate(String statusofstate) {
    this.statusofstate = statusofstate;
  }

  public String[] getWarnings() {
    return warnings;
  }

  public void setWarnings(String[] warnings) {
    this.warnings = warnings;
  }

  public String[] getErrors() {
    return errors;
  }

  public void setErrors(String[] errors) {
    this.errors = errors;
  }

  public String getLastmessage() {
    return lastmessage;
  }

  public void setLastmessage(String lastmessage) {
    this.lastmessage = lastmessage;
  }
}
