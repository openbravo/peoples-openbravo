/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2023 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.TableDomainType;

/**
 * Used by the {@link ModelProvider ModelProvider}, maps the AD_Ref_Table table in the application
 * dictionary.
 * 
 * @author iperdomo
 */
@Entity
@Table(name = "ad_ref_table")
public class RefTable extends ModelObject {
  private static final Logger log = LogManager.getLogger();

  private Reference reference;
  private Column column;
  private Column displayColumn;

  @javax.persistence.Column(name = "IsValueDisplayed", nullable = false)
  private boolean displayedValue;

  @Override
  @Id
  @javax.persistence.Column(name = "ad_reference_id")
  @GeneratedValue(generator = "DalUUIDGenerator")
  public String getId() {
    return super.getId();
  }

  @Override
  public void setId(String id) {
    super.setId(id);
  }

  @ManyToOne
  @JoinColumn(name = "ad_key", nullable = false)
  public Column getColumn() {
    return column;
  }

  public void setColumn(Column column) {
    this.column = column;
  }

  @ManyToOne
  @JoinColumn(name = "ad_reference_id", nullable = false, insertable = false, updatable = false)
  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
    final DomainType domainType = reference.getDomainType();
    if (!(domainType instanceof TableDomainType)) {
      log.error("Domain type of reference " + reference.getId() + " is not a TableDomainType but a "
          + domainType);
    } else {
      ((TableDomainType) domainType).setRefTable(this);
    }
  }

  @ManyToOne
  @JoinColumn(name = "ad_display", nullable = false)
  public Column getDisplayColumn() {
    return displayColumn;
  }

  public void setDisplayColumn(Column displayColumn) {
    this.displayColumn = displayColumn;
  }

  public boolean getDisplayedValue() {
    return this.displayedValue;
  }

  public void setDisplayedValue(boolean displayedValue) {
    this.displayedValue = displayedValue;
  }
}
