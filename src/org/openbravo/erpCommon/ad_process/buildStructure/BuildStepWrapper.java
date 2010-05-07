package org.openbravo.erpCommon.ad_process.buildStructure;

import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;

public class BuildStepWrapper {

  private String node;
  private String name;
  private String level;

  public BuildStepWrapper(BuildMainStep step) {
    name = step.getName();
    level = "0";
    node = generateNode(step.getCode());
  }

  public BuildStepWrapper(BuildStep step) {
    name = step.getName();
    level = "1";
    node = generateNode(step.getCode());
  }

  public BuildStepWrapper(BuildMainStepTranslation step) {
    name = step.getTranslatedName();
    level = "0";
    node = generateNode(step.getCode());
  }

  public BuildStepWrapper(BuildStepTranslation step) {
    name = step.getTranslatedName();
    level = "1";
    node = generateNode(step.getCode());
  }

  public FieldProvider getFieldProvider() {
    return new FieldProviderFactory(this);
  }

  private String generateNode(String code) {
    String numCode = code.replace("RB", "");
    String postfix = "." + numCode.substring(1);
    if (postfix.equals(".0"))
      postfix = "";
    return numCode.substring(0, 1) + postfix;
  }

  public String getTitleLabel() {
    return name;
  }

  public String getPaddingLevel() {
    return level;
  }

  public String getNode() {
    return node;
  }

  public String getPadding() {
    return node;
  }

  public String getIcon() {
    return node;
  }

  public String getTitle() {
    return node;
  }

  public String getProcessing() {
    return node;
  }

  public String getError() {
    return node;
  }

  public String getException() {
    return node;
  }

  public String getWarning() {
    return node;
  }

}
