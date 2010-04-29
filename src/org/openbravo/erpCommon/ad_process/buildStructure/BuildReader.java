package org.openbravo.erpCommon.ad_process.buildStructure;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.betwixt.io.BeanReader;
import org.openbravo.base.session.OBPropertiesProvider;
import org.xml.sax.InputSource;

public class BuildReader {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    String source = OBPropertiesProvider.getInstance().getOpenbravoProperties().get("source.path")
        .toString();
    FileReader xmlReader = new FileReader(
        "/home/openbravo/workspaces/pi-drw/openbravo/buildStructure.xml");
    // + "/src/org/openbravo/erpCommon/ad_process/buildStructure/buildStructure.xml");

    BeanReader beanReader = new BeanReader();

    beanReader.getBindingConfiguration().setMapIDs(false);

    beanReader.getXMLIntrospector().register(
        new InputSource(new FileReader(new File(source,
            "/src/org/openbravo/erpCommon/ad_process/buildStructure/mapping.xml"))));

    beanReader.registerBeanClass("Build", Build.class);

    Build build = (Build) beanReader.parse(xmlReader);
    for (BuildMainStep mainStep : build.getMainSteps()) {
      System.out.println(mainStep.getCode() + "- " + mainStep.getName());
      for (BuildStep step : mainStep.getStepList()) {
        System.out.println("   " + step.getCode() + "- " + step.getName());
      }
    }
  }

}
