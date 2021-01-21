/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function PrintTemplateStoreDefinition() {
  const templates = {};

  OB.App.PrintTemplateStore = {
    /**
     * Registers a print template
     *
     * @param name {string} - the name that identifies the print template
     * @param name {string} - the default template resource to be requested when retrieving the template data
     */
    register: (name, defaultTemplate) => {
      if (templates[name]) {
        throw new Error(`A template with name ${name} already exists`);
      }
      templates[name] = { defaultTemplate, printTemplate: null };
    },

    /**
     * Retrieves a print template. The template resource is retrieved as follows:
     *   If there is a terminal property equal to the provided name, the resource is taken from that property value.
     *   If not, the template default resource is used.
     *
     * @param name {string} - the name that identifies the print template.
     * @return {PrintTemplate} - the PrintTemplate identified with the provided name
     * @throws {Error} in case the template can not be retrieved
     */
    get: async name => {
      if (!templates[name]) {
        throw new Error(`Unknown template with name ${name}`);
      }
      if (!templates[name].printTemplate) {
        const terminal = OB.App.TerminalProperty.get('terminal');
        if (!terminal) {
          throw new Error('Missing terminal information');
        }

        templates[name].printTemplate = new OB.App.Class.PrintTemplate(
          terminal[name] ? terminal[name] : templates[name].defaultTemplate
        );

        if (terminal[`${name}IsPdf`] === true) {
          const params = {
            printer: terminal[`${name}Printer`],
            subreports: Object.keys(terminal)
              .filter(key => key.startsWith(`${name}Subrep`))
              .map(key => terminal[key])
          };

          await templates[name].printTemplate.processPDFTemplate(params);
        }
      }
      return templates[name].printTemplate;
    },

    /**
     * Selects the correct print template to be used to print the provided ticket.
     *
     * @param ticket {ticket} - the ticket to be printed
     * @param options {string} - options that are used in the template selection. It may contain:
     *                           - forcedtemplate: is used to forcibly select this template
     * @return {PrintTemplate} - the PrintTemplate for printing the provided ticket
     */
    selectTicketPrintTemplate: async (ticket, options) => {
      const name = OB.App.PrintTemplateStore.selectTicketPrintTemplateName(
        ticket,
        options
      );
      const template = await OB.App.PrintTemplateStore.get(name);
      return template;
    },

    selectTicketPrintTemplateName: (ticket, options = {}) => {
      const { forcedtemplate } = options;
      const negativeLines = ticket.lines.filter(line => line.qty < 0);
      const hasNegativeLines =
        negativeLines.length === ticket.lines.length ||
        (negativeLines.length > 0 &&
          OB.App.Security.hasPermission(
            'OBPOS_SalesWithOneLineNegativeAsReturns'
          ));

      if (forcedtemplate) {
        return forcedtemplate;
      }
      if (ticket.ordercanceled) {
        return 'printCanceledReceiptTemplate';
      }
      if (ticket.cancelLayaway) {
        return 'printCanceledLayawayTemplate';
      }
      if (ticket.isInvoice) {
        if (ticket.orderType === 1 || hasNegativeLines) {
          if (ticket.fullInvoice) {
            return 'printReturnInvoiceTemplate';
          }
          return 'printSimplifiedReturnInvoiceTemplate';
        }
        if (ticket.isQuotation) {
          return 'printQuotationTemplate';
        }
        if (ticket.isPaid) {
          if (ticket.fullInvoice) {
            return 'printClosedInvoiceTemplate';
          }
          return 'printSimplifiedClosedInvoiceTemplate';
        }
        if (ticket.fullInvoice) {
          return 'printInvoiceTemplate';
        }
        return 'printSimplifiedInvoiceTemplate';
      }
      if (ticket.isPaid) {
        if (ticket.orderType === 1 || hasNegativeLines) {
          return 'printReturnTemplate';
        }
        if (ticket.isQuotation) {
          return 'printQuotationTemplate';
        }
        return 'printClosedReceiptTemplate';
      }
      if (ticket.orderType === 2 || ticket.orderType === 3) {
        return 'printLayawayTemplate';
      }
      if (
        (ticket.orderType === 1 || hasNegativeLines) &&
        ticket.lines.length > 0
      ) {
        return 'printReturnTemplate';
      }
      if (ticket.isQuotation) {
        return 'printQuotationTemplate';
      }
      return 'printTicketTemplate';
    }
  };

  // register default templates

  OB.App.PrintTemplateStore.register(
    'printDisplayTotalTemplate',
    '../org.openbravo.retail.posterminal/res/displaytotal.xml'
  );

  OB.App.PrintTemplateStore.register(
    'displayReceiptTemplate',
    '../org.openbravo.retail.posterminal/res/displayreceipt.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printWelcomeTemplate',
    '../org.openbravo.retail.posterminal/res/welcome.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printReceiptLineTemplate',
    '../org.openbravo.retail.posterminal/res/printline.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printTicketTemplate',
    '../org.openbravo.retail.posterminal/res/printreceipt.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printCanceledReceiptTemplate',
    '../org.openbravo.retail.posterminal/res/printcanceledreceipt.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printLayawayTemplate',
    '../org.openbravo.retail.posterminal/res/printlayaway.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printCanceledLayawayTemplate',
    '../org.openbravo.retail.posterminal/res/printcanceledlayaway.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printClosedReceiptTemplate',
    '../org.openbravo.retail.posterminal/res/printclosedreceipt.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printInvoiceTemplate',
    '../org.openbravo.retail.posterminal/res/printinvoice.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printSimplifiedInvoiceTemplate',
    '../org.openbravo.retail.posterminal/res/printsimplifiedinvoice.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printClosedInvoiceTemplate',
    '../org.openbravo.retail.posterminal/res/printclosedinvoice.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printSimplifiedClosedInvoiceTemplate',
    '../org.openbravo.retail.posterminal/res/printsimplifiedclosedinvoice.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printReturnInvoiceTemplate',
    '../org.openbravo.retail.posterminal/res/printreturninvoice.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printSimplifiedReturnInvoiceTemplate',
    '../org.openbravo.retail.posterminal/res/printsimplifiedreturninvoice.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printQuotationTemplate',
    '../org.openbravo.retail.posterminal/res/printquotation.xml'
  );

  OB.App.PrintTemplateStore.register(
    'printReturnTemplate',
    '../org.openbravo.retail.posterminal/res/printreturn.xml'
  );
})();
