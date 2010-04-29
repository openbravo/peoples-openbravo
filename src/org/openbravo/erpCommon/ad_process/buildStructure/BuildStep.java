package org.openbravo.erpCommon.ad_process.buildStructure;

public class BuildStep {
  private String code;
  private String name;

  public BuildStep() {

  }

  public BuildStep(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BuildStepTranslation generateBuildStepTranslation() {
    BuildStepTranslation trl = new BuildStepTranslation();
    trl.setCode(code);
    trl.setOriginalName(name);
    trl.setTranslatedName(name);
    return trl;
  }
}
