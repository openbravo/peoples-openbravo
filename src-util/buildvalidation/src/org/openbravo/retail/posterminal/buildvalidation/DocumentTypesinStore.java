/*
************************************************************************************
* Copyright (C) 2019 Openbravo S.L.U.
* Licensed under the Openbravo Commercial License version 1.0
* You may obtain a copy of the License at
http://www.openbravo.com/legal/obcl.html
* or in the legal folder of this module distribution.
************************************************************************************
*/
package org.openbravo.retail.posterminal.buildvalidation;

import java.util.ArrayList;
import java.util.List;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * This validation is related to Assignation of document types refactor
 * 
 */
public class DocumentTypesinStore extends BuildValidation {

  private static final String RETAIL_POSTERMINAL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";

  @Override
  public List<String> execute() {
    try {
      List<String> errors = new ArrayList<String>();
      ConnectionProvider cp = getConnectionProvider();
      DocumentTypesinStoreData[] dataDocType = DocumentTypesinStoreData.validateDocumentType(cp);
      for (int i = 0; i < dataDocType.length; i++) {
        String msg = "\nPlease, review the configuration of Document Types for Orders in the store "
            + dataDocType[i].name
            + " . You have more that one Document Type for the same type in the store.";
        errors.add(msg);
      }
      DocumentTypesinStoreData[] dataDocTypeReturns = DocumentTypesinStoreData.validateDocumentTypeReturns(cp);
      for (int i = 0; i < dataDocTypeReturns.length; i++) {
        String msg = "\nPlease, review the configuration of Document Types for Returns in the store "
            + dataDocTypeReturns[i].name
            + " . You have more that one Document Type for the same type in the store.";
        errors.add(msg);
      }
      DocumentTypesinStoreData[] dataDocTypeRecon = DocumentTypesinStoreData.validateDocumentTypeReconciliation(cp);
      for (int i = 0; i < dataDocTypeRecon.length; i++) {
        String msg = "\nPlease, review the configuration of Document Types for Reconciliations in the store "
            + dataDocTypeRecon[i].name
            + " . You have more that one Document Type for the same type in the store.";
        errors.add(msg);
      }
      DocumentTypesinStoreData[] dataDocTypeQuo = DocumentTypesinStoreData.validateDocumentTypeQuotations(cp);
      for (int i = 0; i < dataDocTypeQuo.length; i++) {
        String msg = "\nPlease, review the configuration of Document Types for Quotations in the store "
            + dataDocTypeQuo[i].name
            + " . You have more that one Document Type for the same type in the store.";
        errors.add(msg);
      }
      return errors;
    } catch (Exception e) {
      return handleError(e);
    }
  }

  @Override
  protected boolean executeOnInstall() {
    return false;
  }

  @Override
  protected ExecutionLimits getBuildValidationLimits() {
    return new ExecutionLimits(RETAIL_POSTERMINAL_MODULE_ID, null, new OpenbravoVersion(1, 2, 6100));
  }
}