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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Image;

/**
 * This ActionHandler implements the DELETE and GETSIZE actions which are used by the ImageBLOB
 * reference components in the OB3 windows.
 * 
 * The DELETE action deletes an image from the database, and its reference from the referencing
 * table
 * 
 * The GETSIZE action gets the size of an image
 * 
 */
public class ImagesActionHandler extends BaseActionHandler {

  private static final Logger log = Logger.getLogger(ImagesActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    if (parameters.get("command").equals("DELETE")) {
      OBContext.setAdminMode();
      String imageID = (String) parameters.get("inpimageId");
      String tabId = (String) parameters.get("inpTabId");
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String tableId = tab.getTable().getId();

      String columnName = (String) parameters.get("inpColumnName");
      String parentObjectId = (String) parameters.get("parentObjectId");
      try {
        Image image = OBDal.getInstance().get(Image.class, imageID);
        Table table = OBDal.getInstance().get(Table.class, tableId);
        Entity entity = ModelProvider.getInstance().getEntityByTableName(table.getDBTableName());
        String propertyName = entity.getPropertyByColumnName(columnName).getName();
        BaseOBObject parentObject = (BaseOBObject) OBDal.getInstance().get(entity.getName(),
            parentObjectId);
        parentObject.set(propertyName, null);
        OBDal.getInstance().flush();
        OBDal.getInstance().remove(image);
        return new JSONObject();
      } finally {
        OBContext.restorePreviousMode();
      }
    } else if (parameters.get("command").equals("GETSIZE")) {
      try {
        OBContext.setAdminMode();
        String imageID = (String) parameters.get("inpimageId");
        Image image = OBDal.getInstance().get(Image.class, imageID);
        ByteArrayInputStream bis = new ByteArrayInputStream(image.getBindaryData());
        BufferedImage rImage = ImageIO.read(bis);
        int width = rImage.getWidth();
        int height = rImage.getHeight();
        JSONObject obj = new JSONObject();
        obj.put("width", width);
        obj.put("height", height);
        return obj;
      } catch (Exception e) {
        log.error("Error while calculating image size", e);
        return new JSONObject();
      } finally {
        OBContext.restorePreviousMode();
      }
    } else {
      return new JSONObject();
    }
  }
}
