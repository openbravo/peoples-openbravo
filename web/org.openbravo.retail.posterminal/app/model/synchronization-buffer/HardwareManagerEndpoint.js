/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the HardwareMaangerEndpoint class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function HardwareManagerEndpointDefinition() {
  /**
   * A synchronization endpoint in charge of synchronizing the messages for communicating with the Hardware Manager.
   */
  class HardwareManagerEndpoint extends OB.App.Class.SynchronizationEndpoint {
    constructor() {
      super();
      this.name = 'HardwareManager';
      this.online = true;
    }

    // eslint-disable-next-line class-methods-use-this
    async synchronizeMessage(message) {
      //TODO
    }

    async isOnline() {
      if (!this.online) {
        return this.online;
      }
      // check HW Manager status only if the endpoint has not been set as offline
      const status = await this.hardwareManagerStatus().catch(() => false);
      return status && !status.exception;
    }

    // eslint-disable-next-line class-methods-use-this
    async hardwareManagerStatus() {
      const status = await new Promise((resolve, reject) => {
        OB.POS.hwserver.status(data => {
          if (data && data.exception) {
            reject(new Error(data.exception.message));
          }
          resolve(data);
        });
      });
      return status;
    }
  }
  OB.App.SynchronizationBuffer.registerEndpoint(HardwareManagerEndpoint);
})();
