/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _, Backbone, OB */

(function () {

  // Register global Event Bus
  OB.POS.EventBus = OB.POS.EventBus || _.extend({
    callscounter: 0,
    startProcess: function () {
      if (this.callscounter === 0) {
        this.trigger('UI_Enabled', false);
      }
      this.callscounter++;
    },
    endProcess: function () {
      this.callscounter--;
      if (this.callscounter === 0) {
        this.trigger('UI_Enabled', true);
      }
    },
    isProcessEnabled: function () {
      return this.callscounter === 0;
    }

  }, Backbone.Events);

}());