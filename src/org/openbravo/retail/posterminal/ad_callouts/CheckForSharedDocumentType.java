/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;

public class CheckForSharedDocumentType extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String selectedDocTypeId;
    DocumentType selectedDocType;
    final String orgId = info.getStringParameter("inpadOrgId");
    final String lastFieldChanged = info.getLastFieldChanged();
    if (StringUtils.equals(lastFieldChanged, "inpemObposCDoctypeId")) {
      // Order
      selectedDocTypeId = info.getStringParameter("inpemObposCDoctypeId");
      checkOrderDocumentTypeRef(orgId, selectedDocTypeId, info, false);
    } else if (StringUtils.equals(lastFieldChanged, "inpemObposCDoctyperetId")) {
      // Return
      selectedDocTypeId = info.getStringParameter("inpemObposCDoctyperetId");
      checkOrderDocumentTypeRef(orgId, selectedDocTypeId, info, true);
    } else if (StringUtils.equals(lastFieldChanged, "inpemObposCDoctypequotId")) {
      // Quotations - Check Quotation DocType in other stores
      selectedDocTypeId = info.getStringParameter("inpemObposCDoctypequotId");
      selectedDocType = OBDal.getInstance().get(DocumentType.class, selectedDocTypeId);
      String referredStores = checkQuotationDocTypeRefInOtherStores(orgId, selectedDocTypeId);
      if (!StringUtils.isEmpty(referredStores)) {
        info.addResult("WARNING",
            String.format(Utility.messageBD(this, "OBPOS_SharedDocType", info.vars.getLanguage()),
                selectedDocType.getName(), referredStores));
      }
    } else if (StringUtils.equals(lastFieldChanged, "inpemObposCDoctypereconId")) {
      // Reconcillations - Check Reconcillations DocType in other stores
      selectedDocTypeId = info.getStringParameter("inpemObposCDoctypereconId");
      selectedDocType = OBDal.getInstance().get(DocumentType.class, selectedDocTypeId);
      String referredStores = checkReconcileDocTypeRefInOtherStores(orgId, selectedDocTypeId);
      if (!StringUtils.isEmpty(referredStores)) {
        info.addResult("WARNING",
            String.format(Utility.messageBD(this, "OBPOS_SharedDocType", info.vars.getLanguage()),
                selectedDocType.getName(), referredStores));
      }
    }
  }

  private String checkOrderDocTypeRefInOtherStores(String orgId, String docTypeId) {
    String referredStores = "";
    OBQuery<Organization> orgQuery = OBDal.getInstance()
        .createQuery(Organization.class,
            " as org where org.id<>:orgId and (org.obposCDoctype.id = :docTypeId or org.obposCDoctyperet.id = :docTypeId)");
    orgQuery.setFilterOnActive(false);
    orgQuery.setNamedParameter("orgId", orgId);
    orgQuery.setNamedParameter("docTypeId", docTypeId);
    orgQuery.setNamedParameter("docTypeId", docTypeId);
    for (Organization org : orgQuery.list()) {
      if (StringUtils.isEmpty(referredStores)) {
        referredStores = org.getName();
      } else {
        referredStores += ", " + org.getName();
      }
    }
    return referredStores;
  }

  private boolean checkOrderDocTypeRefInSameStore(String orgId, String docTypeId,
      boolean isReturn) {
    String query = " as org where org.id= :orgId ";
    if (isReturn) {
      query += "and org.obposCDoctype.id = :docTypeId";
    } else {
      query += "and org.obposCDoctyperet.id = :docTypeId";
    }
    OBQuery<Organization> orgQuery = OBDal.getInstance().createQuery(Organization.class, query);
    orgQuery.setFilterOnActive(false);
    orgQuery.setNamedParameter("orgId", orgId);
    orgQuery.setNamedParameter("docTypeId", docTypeId);
    if (orgQuery.list().size() > 0) {
      return true;
    } else {
      return false;
    }
  }

  private String checkDocTypeRefInOtherDocType(String selectedDocTypeId, String docTypeId) {
    String referredDocType = "";
    OBQuery<DocumentType> docTypeQry = OBDal.getInstance()
        .createQuery(DocumentType.class,
            " as dt where dt.id<>:docTypeId and (dt.documentTypeForInvoice.id = :invDocTypeId or dt.doctypesimpinvoice.id = :invDocTypeId or dt.doctypeaggrinvoice.id = :invDocTypeId)");
    docTypeQry.setFilterOnActive(false);
    docTypeQry.setNamedParameter("docTypeId", selectedDocTypeId);
    docTypeQry.setNamedParameter("invDocTypeId", docTypeId);
    docTypeQry.setNamedParameter("invDocTypeId", docTypeId);
    docTypeQry.setNamedParameter("invDocTypeId", docTypeId);
    for (DocumentType doctype : docTypeQry.list()) {
      if (StringUtils.isEmpty(referredDocType)) {
        referredDocType = doctype.getName();
      } else {
        referredDocType += ", " + doctype.getName();
      }
    }
    return referredDocType;
  }

  private String checkShipDocTypeRefInOtherDocType(String selectedDocTypeId, String docTypeId) {
    String referredDocType = "";
    OBQuery<DocumentType> docTypeQry = OBDal.getInstance()
        .createQuery(DocumentType.class,
            " as dt where dt.id<>:docTypeId and dt.documentTypeForShipment.id = :shipDocTypeId");
    docTypeQry.setFilterOnActive(false);
    docTypeQry.setNamedParameter("docTypeId", selectedDocTypeId);
    docTypeQry.setNamedParameter("shipDocTypeId", docTypeId);
    for (DocumentType doctype : docTypeQry.list()) {
      if (StringUtils.isEmpty(referredDocType)) {
        referredDocType = doctype.getName();
      } else {
        referredDocType += ", " + doctype.getName();
      }
    }
    return referredDocType;
  }

  private boolean checkOrderDocumentTypeRef(String orgId, String selectedDocTypeId,
      CalloutInfo info, boolean isReturn) {
    DocumentType selectedDocType = OBDal.getInstance().get(DocumentType.class, selectedDocTypeId);
    // Check Order and Return DocType in other stores
    String referredStores = checkOrderDocTypeRefInOtherStores(orgId, selectedDocTypeId);
    if (!StringUtils.isEmpty(referredStores)) {
      info.addResult("WARNING",
          String.format(Utility.messageBD(this, "OBPOS_SharedDocType", info.vars.getLanguage()),
              selectedDocType.getName(), referredStores));
      return false;
    } else {
      // Check DocType Reference in same store
      if (checkOrderDocTypeRefInSameStore(orgId, selectedDocTypeId, isReturn)) {
        if (isReturn) {
          info.addResult("WARNING",
              String.format(
                  Utility.messageBD(this, "OBPOS_DocTypeUsedInOrder", info.vars.getLanguage()),
                  selectedDocType.getName()));
        } else {
          info.addResult("WARNING",
              String.format(
                  Utility.messageBD(this, "OBPOS_DocTypeUsedInReturn", info.vars.getLanguage()),
                  selectedDocType.getName()));
        }
        return false;
      } else {
        // Check Invoice DocType in other document types
        String referredInvoiceDocType = checkDocTypeRefInOtherDocType(selectedDocTypeId,
            selectedDocType.getDocumentTypeForInvoice().getId());
        if (!StringUtils.isEmpty(referredInvoiceDocType)) {
          info.addResult("WARNING",
              String.format(
                  Utility.messageBD(this, "OBPOS_SharedInvoiceDocType", info.vars.getLanguage()),
                  selectedDocType.getDocumentTypeForInvoice().getName(), referredInvoiceDocType));
          return false;
        } else {
          // Check Simplified Invoice DocType in other document types
          String referredSimpInvoiceDocType = checkDocTypeRefInOtherDocType(selectedDocTypeId,
              selectedDocType.getDoctypesimpinvoice().getId());
          if (!StringUtils.isEmpty(referredSimpInvoiceDocType)) {
            info.addResult("WARNING",
                String.format(
                    Utility.messageBD(this, "OBPOS_SharedSimplifedInvoiceDocType",
                        info.vars.getLanguage()),
                    selectedDocType.getDoctypesimpinvoice().getName(), referredSimpInvoiceDocType));
            return false;
          } else {
            // Check Aggregate Invoice DocTypes in other document types
            String referredAggrInvoiceDocType = checkDocTypeRefInOtherDocType(selectedDocTypeId,
                selectedDocType.getDoctypeaggrinvoice().getId());
            if (!StringUtils.isEmpty(referredAggrInvoiceDocType)) {
              info.addResult("WARNING", String.format(
                  Utility.messageBD(this, "OBPOS_SharedAggregateInvoiceDocType",
                      info.vars.getLanguage()),
                  selectedDocType.getDoctypeaggrinvoice().getName(), referredAggrInvoiceDocType));
              return false;
            } else {
              // Check Shipment DocType in other document types
              String referredShipDocType = checkShipDocTypeRefInOtherDocType(selectedDocTypeId,
                  selectedDocType.getDocumentTypeForShipment().getId());
              if (!StringUtils.isEmpty(referredShipDocType)) {
                info.addResult("WARNING", String.format(
                    Utility.messageBD(this, "OBPOS_SharedShipmentDocType", info.vars.getLanguage()),
                    selectedDocType.getDocumentTypeForShipment().getName(), referredShipDocType));
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

  private String checkQuotationDocTypeRefInOtherStores(String orgId, String docTypeId) {
    String referredStores = "";
    OBQuery<Organization> orgQuery = OBDal.getInstance()
        .createQuery(Organization.class,
            " as org where org.id<>:orgId and (org.obposCDoctypequot.id = :docTypeId)");
    orgQuery.setFilterOnActive(false);
    orgQuery.setNamedParameter("orgId", orgId);
    orgQuery.setNamedParameter("docTypeId", docTypeId);
    for (Organization org : orgQuery.list()) {
      if (StringUtils.isEmpty(referredStores)) {
        referredStores = org.getName();
      } else {
        referredStores += ", " + org.getName();
      }
    }
    return referredStores;
  }

  private String checkReconcileDocTypeRefInOtherStores(String orgId, String docTypeId) {
    String referredStores = "";
    OBQuery<Organization> orgQuery = OBDal.getInstance()
        .createQuery(Organization.class,
            " as org where org.id<>:orgId and (org.obposCDoctyperecon.id = :docTypeId)");
    orgQuery.setFilterOnActive(false);
    orgQuery.setNamedParameter("orgId", orgId);
    orgQuery.setNamedParameter("docTypeId", docTypeId);
    for (Organization org : orgQuery.list()) {
      if (StringUtils.isEmpty(referredStores)) {
        referredStores = org.getName();
      } else {
        referredStores += ", " + org.getName();
      }
    }
    return referredStores;
  }

}
