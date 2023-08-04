/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.CORE = OB.CORE || {};
OB.CORE.ClientSideEventHandlers = OB.CORE.ClientSideEventHandlers || {};
OB.CORE.DISCOUNTS_WINDOW_TAB = '800079';

OB.CORE.ClientSideEventHandlers.discountAmountValidation = function(
  view,
  form,
  grid,
  extraParameters,
  actions
) {
  const data = extraParameters.data;

  if (data.discountAmount < 0) {
    view.messageBar.setMessage(
      isc.OBMessageBar.TYPE_ERROR,
      OB.I18N.getLabel('OBUIAPP_Error'),
      OB.I18N.getLabel('OBUIAPP_NegativeDiscountAmtError')
    );
    return;
  }
  OB.EventHandlerRegistry.callbackExecutor(
    view,
    form,
    grid,
    extraParameters,
    actions
  );
};

OB.EventHandlerRegistry.register(
  OB.CORE.DISCOUNTS_WINDOW_TAB,
  OB.EventHandlerRegistry.PRESAVE,
  OB.CORE.ClientSideEventHandlers.discountAmountValidation,
  'CORE_discountAmountValidation'
);
