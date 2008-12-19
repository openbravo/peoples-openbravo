/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.process;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductUOM;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.project.Project;
import org.openbravo.model.project.ProjectVendor;

/**
 * Implements the c_create_pinvoice_from_outs Function.
 * 
 * @author Martin Taal
 */
public class CreatePurchaseInvoiceFromShipmentProcess {
    private static final Logger log = Logger
            .getLogger(CreatePurchaseInvoiceFromShipmentProcess.class);

    private Date dateFrom;
    private Date dateTo;
    private Warehouse warehouse;
    private BusinessPartner businessPartner;
    private String referenceNo;
    private Date dateInvoiced;

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BusinessPartner getBusinessPartner() {
        return businessPartner;
    }

    public void setBusinessPartner(BusinessPartner businessPartner) {
        this.businessPartner = businessPartner;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public Date getDateInvoiced() {
        return dateInvoiced;
    }

    public void setDateInvoiced(Date dateInvoiced) {
        this.dateInvoiced = dateInvoiced;
    }

    private class BaseDate {

        private Client client;
        private Organization organization;
        private Project project;
        private PriceList priceList;
        private Product product;
        private BigDecimal movementQty;
        private UOM uom;
        private AttributeSetInstance attributeSetInstance;
        private BigDecimal quantityOrder;
        private ProductUOM productUom;

        void setFromShipment(ShipmentInOut shipmentInOut,
                ShipmentInOutLine shipmentInOutLine, Project project,
                ProjectVendor projectVendor, Locator locator) {
            client = shipmentInOut.getClient();
            organization = shipmentInOut.getOrganization();
            this.project = project;
            priceList = projectVendor.getPriceList();
            product = shipmentInOutLine.getProduct();
            movementQty = shipmentInOutLine.getMovementQuantity();
            uom = shipmentInOutLine.getUom();
            attributeSetInstance = shipmentInOutLine.getAttributeSetInstance();
            quantityOrder = shipmentInOutLine.getQuantityOrder();
            productUom = shipmentInOutLine.getProductUOM();
        }

        /*
         * SELECT s.AD_Client_ID, s.AD_Org_ID, p.C_Project_ID,
         * pv.M_PriceList_ID, sl.M_Product_ID, sl.MovementQty, sl.C_UOM_ID,
         * sl.M_AttributeSetInstance_ID, sl.quantityOrder, sl.M_Product_UOM_ID
         */
    }

}
