/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the ExternalDeviceController class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function ExternalDeviceControllerDefinition() {
  // used to send POST requests to the HardwareManager
  async function post(url, data, options = {}) {
    const response = await OB.App.Request.post(url, data, {
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
    checkSuccessfulResponse(response);
    return response;
  }

  // used to send GET requests to the HardwareManager
  async function get(url, options = {}) {
    const response = await OB.App.Request.get(url, null, {
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
    checkSuccessfulResponse(response);
    return response;
  }

  // throws an error in case the provided HardwareManager response was not successful
  function checkSuccessfulResponse(response) {
    if (response && response.exception) {
      throw new Error(
        `Hardware Manager request responded with error: ${response.exception}`
      );
    }
  }

  /**
   * This is an internal class used by the HardwareManagerEndpoint when consumming its messages.
   * It handles the communication with the HardwareManager and other devices (web printers).
   *
   * @see HardwareManagerEndpoint
   */
  OB.App.Class.ExternalDeviceController = class ExternalDeviceController {
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
        // ends with '/printer'
        this.mainURL = this.mainURL.substring(0, this.mainURL.length - 8);
      }

      this.setActiveURL(OB.UTIL.localStorage.getItem('hw_activeurl_id'));
      this.setActivePDFURL(OB.UTIL.localStorage.getItem('hw_activepdfurl_id'));

      // WebPrinter
      const printerTypeInfo =
        OB.PRINTERTYPES && OB.PRINTERTYPES[terminal.printertype];
      this.webPrinter = printerTypeInfo
        ? new OB.WEBPrinter(printerTypeInfo, OB.PRINTERIMAGES.getImagesMap())
        : null;

      this.storeDataKey = terminal.searchKey;
      this.storeData(null, this.devices.PRINTER);
      this.storeData(null, this.devices.DISPLAY);
      this.storeData(null, this.devices.DRAWER);
    }

    setActiveURL(hardwareId) {
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

    setActivePDFURL(hardwareId) {
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
      const urlList = OB.App.TerminalProperty.get('hardwareURL') || [];
      const validPrinter =
        hardwareId != null
          ? urlList.find(url => filter(url) && url.id === hardwareId)
          : null;

      return {
        url: validPrinter ? validPrinter.hardwareURL : this.mainURL,
        identifier: validPrinter
          ? // eslint-disable-next-line no-underscore-dangle
            validPrinter._identifier
          : OB.I18N.getLabel('OBPOS_MainPrinter'),
        id: validPrinter
          ? validPrinter.id
          : OB.App.TerminalProperty.get('terminal').id
      };
    }

    storeData(data, device) {
      const time = new Date().getTime();
      OB.UTIL.localStorage.setItem(
        `HWM.${this.storeDataKey}.${device || this.devices.PRINTER}`,
        JSON.stringify({ time, data })
      );
    }

    getActiveURLIdentifier(isPdf) {
      return isPdf ? this.activePDFURLIdentifier : this.activeIdentifier;
    }

    hasAvailablePrinter(isPdf) {
      const urlList = OB.App.TerminalProperty.get('hardwareURL') || [];
      return urlList.some(printer =>
        isPdf ? printer.hasPDFPrinter : printer.hasReceiptPrinter
      );
    }

    async getHardwareManagerStatus() {
      if (!this.activeURL) {
        return { notConfigured: true };
      }
      try {
        const data = (await get(`${this.activeURL}/status.json`)) || {};
        return data;
      } catch (error) {
        OB.App.UserNotifier.notifyError({
          message: 'OBPOS_MsgHardwareServerNotAvailable'
        });
        throw error;
      }
    }

    async isHardwareManagerReady() {
      if (!this.activeURL) {
        return false;
      }
      try {
        await get(
          `${this.activeURL}/status.json?${new URLSearchParams({
            ignoreForConnectionStatus: true
          })}`
        );
      } catch (error) {
        return false;
      }
      return true;
    }

    async openDrawer() {
      const template = OB.App.PrintTemplateStore.get('openDrawerTemplate');
      await this.print(template, {}, this.devices.DRAWER);
    }

    async display(template, params) {
      const data = await this.print(template, params, this.devices.DISPLAY);
      return data;
    }

    async print(printTemplate, params = {}, device = 0) {
      const data = await printTemplate.generate(params);

      await this.send(data, device, { isPdf: printTemplate.ispdf });
      return data;
    }

    async send(dataToSend, device, options = {}) {
      const { isPdf } = options;
      const data = isPdf ? JSON.stringify(dataToSend) : dataToSend.data;

      this.storeData(data, device);

      await this.executeHooks('OBPOS_HWServerSend', {
        device,
        time: new Date().getTime(),
        data
      });

      switch (device) {
        case this.devices.DISPLAY:
          await this.requestPrint(data, device);
          break;
        case this.devices.DRAWER:
          if (this.webPrinter) {
            await this.requestWebPrinter(data);
          } else {
            await this.requestPrint(data, device);
          }
          break;
        case this.devices.PRINTER:
          if (this.webPrinter && this.isMainURLActive()) {
            await this.requestWebPrinter(data);
          } else if (isPdf) {
            await this.requestPDFPrint(data);
          } else {
            await this.requestPrint(data, device);
          }
          break;
        default:
          throw new Error(`Unkwnown device: ${device}`);
      }
    }

    isMainURLActive() {
      return this.activeURL === this.mainURL;
    }

    async executeHooks(hookName, payload) {
      if (!OB.App.StateBackwardCompatibility) {
        // not in legacy mode: printing hooks are not supported
        return payload;
      }

      let payloadForHooks = payload;
      if (payload.ticket) {
        const order =
          payload.ticket instanceof Backbone.Model
            ? payload.ticket
            : OB.UTIL.TicketUtils.toOrder(payload.ticket);
        // some hooks (OBPRINT_PrePrint) expect "order" and others (OBPRINT_PostPrint) "receipt"...
        payloadForHooks = { ...payload, order, receipt: order };
      }

      const afterHooksPayload = await new Promise(resolve => {
        OB.UTIL.HookManager.executeHooks(hookName, payloadForHooks, args =>
          resolve(args || {})
        );
      });

      if (afterHooksPayload.order) {
        // transform to state ticket again, just in case any hook modified the backbone order...
        afterHooksPayload.ticket = OB.UTIL.TicketUtils.toTicket(
          afterHooksPayload.order
        );
      }

      return afterHooksPayload;
    }

    async requestPrint(data, device) {
      const url =
        this.devices.PRINTER === device ? this.activeURL : this.mainURL;

      if (!url) {
        return;
      }

      try {
        const options = {
          contentType: 'application/xml; charset=utf-8'
        };
        await post(`${url}/printer`, data, options);
      } catch (error) {
        OB.App.UserNotifier.notifyError({
          message: 'OBPOS_MsgHardwareServerNotAvailable'
        });
        throw error;
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
        throw error;
      }
    }

    async requestWebPrinter(data) {
      if (!this.webPrinter.connected()) {
        const confirmation = await OB.App.View.DialogUIHandler.askConfirmation({
          title: 'OBPOS_WebPrinter',
          message: 'OBPOS_WebPrinterPair'
        });

        if (!confirmation) {
          return;
        }

        await this.webPrinter.request();
      }
      await this.webPrinter.print(data);
    }
  };
})();
