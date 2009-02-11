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

package org.openbravo.test.dal;

import java.util.List;

import org.hibernate.criterion.Expression;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.DynamicOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.CategoryAccounts;
import org.openbravo.test.base.BaseTest;

/**
 * Tests access on the basis of window and table definitions. Also tests derived read access.
 * 
 * @author mtaal
 */

public class DynamicEntityTest extends BaseTest {
  public void testCreateBPGroup() {
    setErrorOccured(true);
    setUserContext("1000001");
    final DynamicOBObject bpGroup = new DynamicOBObject();
    bpGroup.setEntityName(Category.ENTITY_NAME);
    bpGroup.set(Category.PROPERTY_DEFAULT, true);
    bpGroup.set(Category.PROPERTY_DESCRIPTION, "hello world");
    bpGroup.set(Category.PROPERTY_NAME, "hello world");
    bpGroup.set(Category.PROPERTY_SEARCHKEY, "hello world");
    OBDal.getInstance().save(bpGroup);
    printXML(bpGroup);
    setErrorOccured(false);
  }

  // query for the BPGroup again and remove it
  public void testRemoveBPGroup() {
    setErrorOccured(true);
    setUserContext("1000001");
    final OBCriteria<Category> obc = OBDal.getInstance().createCriteria(Category.class);
    obc.add(Expression.eq(Category.PROPERTY_NAME, "hello world"));
    final List<Category> bpgs = obc.list();
    assertEquals(1, bpgs.size());
    final Category bog = bpgs.get(0);
    final OBContext obContext = OBContext.getOBContext();
    assertEquals(obContext.getUser().getId(), bog.getCreatedBy().getId());
    assertEquals(obContext.getUser().getId(), bog.getUpdatedBy().getId());
    // update and create have occured less than one second ago
    // note that if the delete fails for other reasons that you will have a
    // currency in the database which has for sure a created/updated time
    // longer in the past, You need to manually delete the currency record
    if (true) {
      assertTrue("Created time not updated", (System.currentTimeMillis() - bog.getCreationDate()
          .getTime()) < 2000);
      assertTrue("Updated time not updated", (System.currentTimeMillis() - bog.getUpdated()
          .getTime()) < 2000);
    }

    // first delete the related accounts
    final OBCriteria<CategoryAccounts> obc2 = OBDal.getInstance().createCriteria(
        CategoryAccounts.class);
    obc2.add(Expression.eq(CategoryAccounts.PROPERTY_BUSINESSPARTNERCATEGORY, bpgs.get(0)));
    final List<CategoryAccounts> bogas = obc2.list();
    for (final CategoryAccounts bga : bogas) {
      OBDal.getInstance().remove(bga);
    }
    OBDal.getInstance().remove(bpgs.get(0));
    setErrorOccured(false);
  }

  // check if the BPGroup was removed
  public void testCheckBPGroupRemoved() {
    setErrorOccured(true);
    setUserContext("1000001");
    final OBCriteria<Category> obc = OBDal.getInstance().createCriteria(Category.class);
    obc.add(Expression.eq(Category.PROPERTY_NAME, "hello world"));
    final List<Category> bpgs = obc.list();
    assertEquals(0, bpgs.size());
    setErrorOccured(false);
  }

  private void printXML(BaseOBObject bob) {
    // used to print a bit nicer xml
    final String indent = "\t ";

    // get the entity from the runtime model using the entity name of the
    // object
    final String entityName = bob.getEntityName();
    final Entity e = ModelProvider.getInstance().getEntity(entityName);
    // Note: bob.getEntity() also gives the entity of the object

    // print the opening tag
    System.err.println("<" + e.getName() + ">");

    // iterate through the properties of the entity
    for (final Property p : e.getProperties()) {

      // and get the value through the dynamic api offered by the
      // BaseOBObject
      final Object value = bob.get(p.getName());

      // handle null, just create an empty tag for that
      if (value == null) {
        System.err.println(indent + "<" + p.getName() + "/>");
        continue;
      }

      // make a difference between a primitive and a reference type
      if (p.isPrimitive()) {
        // in reality some form of xml conversion/encoding should take
        // place...
        System.err.println(indent + "<" + p.getName() + ">" + value + "</" + p.getName() + ">");
      } else {
        // cast to the parent of all openbravo objects
        final BaseOBObject referencedObject = (BaseOBObject) value;
        // assumes that the id is always a primitive type
        System.err.println(indent + "<" + p.getName() + ">" + referencedObject.getId() + "</"
            + p.getName() + ">");
      }
    }

    // and the closing tag
    System.err.println("</" + e.getName() + ">");
  }
}