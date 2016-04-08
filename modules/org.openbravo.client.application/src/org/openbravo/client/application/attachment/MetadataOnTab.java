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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.attachment;

import javax.servlet.ServletException;

import org.apache.axis.utils.StringUtils;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.AttachmentMethod;

public class MetadataOnTab extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strWindow = info.getWindowId();
    // Only execute callout if we are in Windows, Tabs and Fields window.
    if (!"102".equals(strWindow)) {
      return;
    }
    final String strMethodId = info.getStringParameter("inpcAttachmentMethodId",
        IsIDFilter.instance);
    final String strTabId = info.getStringParameter("inpadTabId", IsIDFilter.instance);
    if (StringUtils.isEmpty(strMethodId)) {
      info.addResult("inpseqno", "");
    } else {
      int seqNo = getNextSeqNo(strMethodId, strTabId);
      info.addResult("inpseqno", seqNo);
    }
  }

  private int getNextSeqNo(String strMethodId, String tabId) {
    ApplicationDictionaryCachedStructures adcs = WeldUtils
        .getInstanceFromStaticBeanManager(ApplicationDictionaryCachedStructures.class);

    AttachmentMethod attMethod = OBDal.getInstance().get(AttachmentMethod.class, strMethodId);
    Tab tab = adcs.getTab(tabId);
    OBCriteria<Parameter> critParam = OBDal.getInstance().createCriteria(Parameter.class);
    critParam.add(Restrictions.eq(Parameter.PROPERTY_ATTACHMENTMETHOD, attMethod));
    critParam.add(Restrictions.eq(Parameter.PROPERTY_TAB, tab));
    critParam.setProjection(Projections.max(Parameter.PROPERTY_SEQUENCENUMBER));

    if (critParam.uniqueResult() == null) {
      return 10;
    }
    long maxSeqNo = (Long) critParam.uniqueResult();

    return (int) (maxSeqNo + 10);
  }
}
