package org.openbravo.erpCommon.ad_process.buildStructure;

import java.util.ArrayList;
import java.util.List;

public class BuildMainStep {
  private String code;
  private String name;
  private String successMessage;
  private String warningMessage;
  private String errorMessage;
  private String successCode;
  private String warningCode;
  private String errorCode;
  private List<BuildStep> stepList;

  public BuildMainStep() {
    stepList = new ArrayList<BuildStep>();
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

  public String getSuccessMessage() {
    return successMessage;
  }

  public void setSuccessMessage(String successMessage) {
    this.successMessage = successMessage;
  }

  public String getWarningMessage() {
    return warningMessage;
  }

  public void setWarningMessage(String warningMessage) {
    this.warningMessage = warningMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getSuccessCode() {
    return successCode;
  }

  public void setSuccessCode(String successCode) {
    this.successCode = successCode;
  }

  public String getWarningCode() {
    return warningCode;
  }

  public void setWarningCode(String warningCode) {
    this.warningCode = warningCode;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public List<BuildStep> getStepList() {
    return stepList;
  }

  public void setStepList(List<BuildStep> internalSteps) {
    this.stepList = internalSteps;
  }

  public void addStep(BuildStep buildStep) {
    stepList.add(buildStep);
  }

  public BuildMainStepTranslation generateBuildMainStepTranslation() {
    BuildMainStepTranslation trl = new BuildMainStepTranslation();
    trl.setCode(code);
    trl.setOriginalName(name);
    trl.setTranslatedName(name);
    trl.setOriginalSuccessMessage(successMessage);
    trl.setOriginalWarningMessage(warningMessage);
    trl.setOriginalErrorMessage(errorMessage);
    trl.setTranslatedSuccessMessage(successMessage);
    trl.setTranslatedWarningMessage(warningMessage);
    trl.setTranslatedErrorMessage(errorMessage);
    for (BuildStep step : stepList) {
      trl.addStepTranslation(step.generateBuildStepTranslation());
    }

    return trl;
  }
}
