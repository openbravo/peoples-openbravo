package org.openbravo.erpCommon.ad_process.buildStructure;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.betwixt.io.BeanWriter;
import org.xml.sax.InputSource;

public class BuildCreator {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {

    FileWriter outputWriter = new FileWriter("buildStructure.xml");

    outputWriter.write("<?xml version='1.0' ?>\n");

    BeanWriter beanWriter = new BeanWriter(outputWriter);

    beanWriter.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
    beanWriter.getBindingConfiguration().setMapIDs(false);
    beanWriter.enablePrettyPrint();
    beanWriter.getXMLIntrospector().register(
        new InputSource(new FileReader(new File(
            "src/org/openbravo/erpCommon/ad_process/buildStructure/mapping.xml"))));

    Build build = new Build();

    BuildMainStep bms1 = new BuildMainStep();
    bms1.setName("Initial Build Validation");
    bms1.setCode("RB10");
    bms1.setErrorMessage("The validation failed");
    bms1.setWarningMessage("The validation had warnings");
    build.addMainStep(bms1);
    bms1.setSuccessCode("RB60");

    BuildMainStep bms2 = new BuildMainStep();
    bms2.setName("Build");
    bms2.setCode("RB20");
    bms2.setErrorMessage("The build failed");
    bms2.setSuccessMessage("The build was succesfull");
    build.addMainStep(bms2);

    BuildStep bs1 = new BuildStep("RB21", "Database update");
    BuildStep bs2 = new BuildStep("RB22", "Compilation");

    bms2.addStep(bs1);
    bms2.addStep(bs2);

    beanWriter.write("Build", build);

    outputWriter.flush();
    outputWriter.close();

    FileWriter outputWriterT = new FileWriter("buildStructureTranslation.xml");

    outputWriterT.write("<?xml version='1.0' ?>\n");

    BeanWriter beanWriterT = new BeanWriter(outputWriterT);

    beanWriterT.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
    beanWriterT.getXMLIntrospector().register(
        new InputSource(new FileReader(new File(
            "src/org/openbravo/erpCommon/ad_process/buildStructure/mapping.xml"))));
    beanWriterT.getBindingConfiguration().setMapIDs(false);
    beanWriterT.enablePrettyPrint();

    BuildTranslation trl = build.generateBuildTranslation("es_ES");
    beanWriterT.write("BuildTranslation", trl);
    outputWriterT.flush();
    outputWriterT.close();

  }
}
