/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the HardwareManagerController class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function HardwareManagerControllerDefinition() {
  // used to send POST requests to the HardwareManager
  async function post(url, data, options = {}) {
    await OB.App.Request.post(url, data, {
      timeout: 20000,
      type: 'json',
      cache: 'no-cache',
      options: {
        headers: {
          'Content-Type':
            options.contentType || 'application/json; charset=utf-8'
        }
      }
    });
  }

  // used to send GET requests to the HardwareManager
  async function get(url, options = {}) {
    const response = await OB.App.Request.get(url, {
      timeout: 5000,
      type: 'json',
      cache: 'no-cache',
      options: {
        headers: {
          'Content-Type':
            options.contentType || 'application/json; charset=utf-8'
        }
      }
    });
    return response;
  }

  /**
   * This is an internal class used by the HardwareManagerEndpoint when consumming its messages.
   * It handles the communication with the HardwareManager.
   *
   * @see HardwareManagerEndpoint
   */
  OB.App.Class.HardwareManagerController = class HardwareManagerController {
    constructor() {
      this.devices = { PRINTER: 0, DISPLAY: 1, DRAWER: 2 };
      this.initialize();
    }

    initialize() {
      const terminal = OB.App.TerminalProperty.get('terminal');
      this.mainURL = terminal.hardwareurl;
      this.scaleURL = terminal.scaleurl;

      // Remove suffix if needed
      if (
        this.mainURL &&
        this.mainURL.indexOf('/printer', this.mainURL.length - 8) !== -1
      ) {
        // endswith '/printer'
        this.mainURL = this.mainURL.substring(0, this.mainURL.length - 8);
      }

      this.setActiveURL();
      this.setActivePDFURL();

      this.storeDataKey = terminal.searchKey;
      this.storeData(null, this.devices.PRINTER);
      this.storeData(null, this.devices.DISPLAY);
      this.storeData(null, this.devices.DRAWER);
    }

    setActiveURL() {
      const hardwareId = OB.UTIL.localStorage.getItem('hw_activeurl_id');
      const data = this.getPrinterData(
        hardwareId,
        url => url.hasReceiptPrinter
      );
      this.activeURL = data.url;
      this.activeIdentifier = data.identifier;
      this.activeURLId = data.id;

      // save
      if (this.activeURL) {
        OB.UTIL.localStorage.setItem('hw_activeurl_id', this.activeURLId);
        OB.UTIL.localStorage.setItem(
          'hw_activeidentifier',
          this.activeIdentifier
        );
      } else {
        OB.UTIL.localStorage.removeItem('hw_activeurl_id');
        OB.UTIL.localStorage.removeItem('hw_activeidentifier');
      }
    }

    setActivePDFURL() {
      const hardwareId = OB.UTIL.localStorage.getItem('hw_activeurl_id');
      const data = this.getPrinterData(hardwareId, url => url.hasPDFPrinter);
      this.activePDFURL = data.url;
      this.activePDFURLIdentifier = data.identifier;
      this.activePDFURLId = data.id;

      // save
      if (this.activePDFURL) {
        OB.UTIL.localStorage.setItem('hw_activepdfurl_id', this.activePDFURLId);
        OB.UTIL.localStorage.setItem(
          'hw_activepdfidentifier',
          this.activePDFURLIdentifier
        );
      } else {
        OB.UTIL.localStorage.removeItem('hw_activepdfurl_id');
        OB.UTIL.localStorage.removeItem('hw_activepdfidentifier');
      }
    }

    getPrinterData(hardwareId, filter) {
      const urlList = OB.App.TerminalProperty.get('hardwareURL');
      const validPrinter =
        hardwareId == null && this.mainURL == null
          ? urlList.find(url => filter(url) && url.hardwareURL === this.mainURL)
          : urlList.find(url => filter(url) && url.id === hardwareId);

      return validPrinter
        ? {
            url: validPrinter.hardwareURL,
            // eslint-disable-next-line no-underscore-dangle
            identifier: validPrinter._identifier,
            id: validPrinter.id
          }
        : {
            url: this.mainURL,
            identifier: OB.I18N.getLabel('OBPOS_MainPrinter'),
            id: OB.App.TerminalProperty.get('terminal').id
          };
    }

    storeData(data, device) {
      const time = new Date().getTime();
      OB.UTIL.localStorage.setItem(
        `HWM.${this.storeDataKey}.${device || this.devices.PRINTER}`,
        JSON.stringify({ time, data })
      );

      // TODO -- check how to implement this
      /* OB.UTIL.HookManager.executeHooks('OBPOS_HWServerSend', {
        device: device || OB.DS.HWServer.PRINTER,
        time,
        data
      }); */
    }

    async getStatus() {
      let data = {};
      if (!this.activeURL) {
        return data;
      }
      try {
        data = (await get(`${this.activeURL}/status.json`)) || {};
        OB.App.SynchronizationBuffer.goOnline('HardwareManager');
      } catch (error) {
        OB.App.SynchronizationBuffer.goOffline('HardwareManager');
        OB.App.UserNotifier.notifyError({
          message: 'OBPOS_MsgHardwareServerNotAvailable'
        });
      }
      return data;
    }

    async display(template, params) {
      await this.print(template, params, this.devices.DISPLAY);
    }

    async print(printTemplate, params = {}, device = 0) {
      try {
        const data = await printTemplate.generate(params);
        this.storeData(data, device);
        if (this.devices.PRINTER === device && data.mainReport) {
          this.requestPDFPrint(data);
        } else {
          this.requestPrint(data, device);
        }
      } catch (error) {
        OB.error(`Error printing template: ${error}`);
      }
    }

    async requestPrint(data, device) {
      const url =
        this.devices.PRINTER === device ? this.activeURL : this.mainURL;

      if (!url) {
        OB.App.SynchronizationBuffer.goOnline('HardwareManager');
        return;
      }

      try {
        const options = {
          contentType: 'application/xml; charset=utf-8'
        };
        await post(`${url}/printer`, data, options);
        OB.App.SynchronizationBuffer.goOnline('HardwareManager');
      } catch (error) {
        OB.App.SynchronizationBuffer.goOffline('HardwareManager');
        OB.App.UserNotifier.notifyError({
          message: 'OBPOS_MsgHardwareServerNotAvailable'
        });
      }
    }

    async requestPDFPrint(data) {
      if (!this.activePDFURL) {
        return;
      }

      try {
        await post(`${this.activePDFURL}/printerpdf`, data);
      } catch (error) {
        OB.App.UserNotifier.notifyError({
          message: 'OBPOS_MsgHardwareServerNotAvailable'
        });
      }
    }
  };
})();
