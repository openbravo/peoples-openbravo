/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalConfigurationRequiredForCrossStore',
  kind: 'OB.UI.ModalInfo',
  header: OB.I18N.getLabel('OBPOS_configurationRequired'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_configurationNeededToCrossStore')
  },
  myId: 'modalConfigurationRequiredForCrossStore'
});