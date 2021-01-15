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
        // ends with '/printer'
        this.mainURL = this.mainURL.substring(0, this.mainURL.length - 8);
      }

      this.setActiveURL(OB.UTIL.localStorage.getItem('hw_activeurl_id'));
      this.setActivePDFURL(OB.UTIL.localStorage.getItem('hw_activepdfurl_id'));

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
    }

    async selectPrinter(options) {
      const terminal = OB.App.TerminalProperty.get('terminal');
      const { isPdf, isRetry, skipSelectPrinters, forceSelect } = options;

      if (
        !forceSelect &&
        (!terminal.terminalType.selectprinteralways ||
          skipSelectPrinters ||
          !this.canSelectPrinter(isPdf))
      ) {
        // skip printer selection
        return;
      }

      const { printer } = await OB.App.View.DialogUIHandler.inputData(
        isPdf ? 'modalSelectPDFPrinters' : 'modalSelectPrinters',
        {
          title: isPdf
            ? OB.I18N.getLabel('OBPOS_SelectPDFPrintersTitle')
            : OB.I18N.getLabel('OBPOS_SelectPrintersTitle'),
          isRetry
        }
      );

      if (!printer) {
        return;
      }

      if (isPdf) {
        this.setActivePDFURL(printer);
      } else {
        this.setActiveURL(printer);
      }
    }

    // eslint-disable-next-line class-methods-use-this
    canSelectPrinter(isPdf) {
      if (!OB.App.Security.hasPermission('OBPOS_retail.selectprinter')) {
        return false;
      }
      const urlList = OB.App.TerminalProperty.get('hardwareURL') || [];
      return urlList.some(printer =>
        isPdf ? printer.hasPDFPrinter : printer.hasReceiptPrinter
      );
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
      const data = await this.print(template, params, this.devices.DISPLAY);
      return data;
    }

    async print(printTemplate, params = {}, device = 0) {
      try {
        const data = printTemplate.ispdf
          ? params
          : await printTemplate.generate(params);

        this.storeData(data, device);

        await this.executeHooks('OBPOS_HWServerSend', {
          device,
          time: new Date().getTime(),
          data
        });

        if (this.devices.PRINTER === device && data.mainReport) {
          await this.requestPDFPrint(data);
        } else {
          await this.requestPrint(data, device);
        }
        return data;
      } catch (error) {
        OB.error(`Error printing template: ${error}`);
        const data = await this.retryPrinting(printTemplate, params, device);
        return data;
      }
    }

    // eslint-disable-next-line class-methods-use-this
    async executeHooks(hookName, payload) {
      if (!OB.App.StateBackwardCompatibility) {
        // not in legacy mode: hooks are not supported
        return payload;
      }

      const order =
        payload.ticket instanceof Backbone.Model
          ? payload.ticket
          : OB.App.StateBackwardCompatibility.getInstance(
              'Ticket'
            ).toBackboneObject(payload.ticket);

      const finalPayload = await new Promise(resolve => {
        OB.UTIL.HookManager.executeHooks(
          hookName,
          { ...payload, order, receipt: order },
          args => resolve(args)
        );
      });

      return finalPayload;
    }

    async retryPrinting(printTemplate, params, device) {
      if (this.devices.PRINTER !== device) {
        return null;
      }

      const isPdf = printTemplate.ispdf;
      const selectPrinterButton = {
        label: isPdf
          ? 'OBPOS_SelectPDFPrintersTitle'
          : 'OBPOS_SelectPrintersTitle',
        action: async () => {
          const { printer } = await this.selectPrinter({
            isPdf,
            isRetry: true,
            forceSelect: true
          });
          return printer != null;
        }
      };

      const retry = await OB.App.View.DialogUIHandler.askConfirmation({
        title: 'OBPOS_MsgHardwareServerNotAvailable',
        message: isPdf ? 'OBPOS_MsgPDFPrintAgain' : 'OBPOS_MsgPrintAgain',
        messageParams: [
          isPdf ? this.activePDFURLIdentifier : this.activeIdentifier
        ],
        confirmLabel: 'OBPOS_LblRetry',
        additionalButtons: this.canSelectPrinter(isPdf)
          ? [selectPrinterButton]
          : []
      });

      const data = retry
        ? await this.print(printTemplate, params, device)
        : null;

      return data;
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
