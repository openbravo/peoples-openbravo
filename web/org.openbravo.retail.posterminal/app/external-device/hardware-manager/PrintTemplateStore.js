/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function PrintTemplateStoreDefinition() {
  // TODO -- should this component be placed under 'hardware-manager' ?
  const templates = {}; // TODO -- check in old implementation if templates are cached

  OB.App.PrintTemplateStore = {
    // TODO: implement extendHWResource for templates

    // Retrieves the template to display the total of a ticket
    getDisplayTotalTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printDisplayTotalTemplate',
        '../org.openbravo.retail.posterminal/res/displaytotal.xml'
      );
    },

    getDisplayTicketTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'displayReceiptTemplate',
        '../org.openbravo.retail.posterminal/res/displayreceipt.xml'
      );
    },

    // Retrieves the template to display the welcome message
    getWelcomeTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printWelcomeTemplate',
        '../org.openbravo.retail.posterminal/res/welcome.xml'
      );
    },

    // Retrieves the template to display the information of a ticket line
    getTicketLineTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printReceiptLineTemplate',
        '../org.openbravo.retail.posterminal/res/printline.xml'
      );
    },

    getTicketTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printTicketTemplate',
        '../org.openbravo.retail.posterminal/res/printreceipt.xml'
      );
    },

    getCanceledTicketTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printCanceledReceiptTemplate',
        '../org.openbravo.retail.posterminal/res/printcanceledreceipt.xml'
      );
    },

    getLayawayTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printLayawayTemplate',
        '../org.openbravo.retail.posterminal/res/printlayaway.xml'
      );
    },

    getCanceledLayawayTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printCanceledLayawayTemplate',
        '../org.openbravo.retail.posterminal/res/printcanceledlayaway.xml'
      );
    },

    getClosedTicketTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printClosedReceiptTemplate',
        '../org.openbravo.retail.posterminal/res/printclosedreceipt.xml'
      );
    },

    getInvoiceTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printInvoiceTemplate',
        '../org.openbravo.retail.posterminal/res/printinvoice.xml'
      );
    },

    getSimplifiedInvoiceTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printSimplifiedInvoiceTemplate',
        '../org.openbravo.retail.posterminal/res/printsimplifiedinvoice.xml'
      );
    },

    getClosedInvoiceTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printClosedInvoiceTemplate',
        '../org.openbravo.retail.posterminal/res/printclosedinvoice.xml'
      );
    },

    getSimplifiedClosedInvoiceTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printSimplifiedClosedInvoiceTemplate',
        '../org.openbravo.retail.posterminal/res/printsimplifiedclosedinvoice.xml'
      );
    },

    getReturnedInvoiceTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printReturnInvoiceTemplate',
        '../org.openbravo.retail.posterminal/res/printreturninvoice.xml'
      );
    },

    getSimplifiedReturnedInvoiceTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printSimplifiedReturnInvoiceTemplate',
        '../org.openbravo.retail.posterminal/res/printsimplifiedreturninvoice.xml'
      );
    },

    getQuotationTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printQuotationTemplate',
        '../org.openbravo.retail.posterminal/res/printquotation.xml'
      );
    },

    getReturnTemplate: () => {
      return OB.App.PrintTemplateStore.getOrDefault(
        'printReturnTemplate',
        '../org.openbravo.retail.posterminal/res/printreturn.xml'
      );
    },

    selectTicketPrintTemplate: (ticket, options) => {
      const { forcedtemplate } = options;
      const negativeLines = ticket.lines.filter(line => line.qty < 0);
      const hasNegativeLines =
        negativeLines.length === ticket.lines.length ||
        (negativeLines.length > 0 &&
          OB.App.TerminalProperty.get('permissions')
            .OBPOS_SalesWithOneLineNegativeAsReturns);

      if (forcedtemplate) {
        return forcedtemplate;
      }
      if (ticket.ordercanceled) {
        return OB.App.PrintTemplateStore.getCanceledTicketTemplate();
      }
      if (ticket.cancelLayaway) {
        return OB.App.PrintTemplateStore.getCanceledLayawayTemplate();
      }
      if (ticket.isInvoice) {
        if (ticket.orderType === 1 || hasNegativeLines) {
          if (ticket.fullInvoice) {
            return OB.App.PrintTemplateStore.getReturnedInvoiceTemplate();
          }
          return OB.App.PrintTemplateStore.getSimplifiedReturnedInvoiceTemplate();
        }
        if (ticket.isQuotation) {
          return OB.App.PrintTemplateStore.getQuotationTemplate();
        }
        if (ticket.isPaid) {
          if (ticket.fullInvoice) {
            return OB.App.PrintTemplateStore.getClosedInvoiceTemplate();
          }
          return OB.App.PrintTemplateStore.getSimplifiedClosedInvoiceTemplate();
        }
        if (ticket.fullInvoice) {
          return OB.App.PrintTemplateStore.getInvoiceTemplate();
        }
        return OB.App.PrintTemplateStore.getSimplifiedInvoiceTemplate();
      }
      if (ticket.isPaid) {
        if (ticket.orderType === 1 || hasNegativeLines) {
          return OB.App.PrintTemplateStore.getReturnTemplate();
        }
        if (ticket.isQuotation) {
          return OB.App.PrintTemplateStore.getQuotationTemplate();
        }
        return OB.App.PrintTemplateStore.getClosedTicketTemplate();
      }
      if (ticket.orderType === 2 || ticket.orderType === 3) {
        return OB.App.PrintTemplateStore.getLayawayTemplate();
      }
      if (
        (ticket.orderType === 1 || hasNegativeLines) &&
        ticket.lines.length > 0
      ) {
        return OB.App.PrintTemplateStore.getReturnTemplate();
      }
      if (ticket.isQuotation) {
        return OB.App.PrintTemplateStore.getQuotationTemplate();
      }
      return OB.App.PrintTemplateStore.getTicketTemplate();
    },

    getOrDefault: (name, defaultTemplate) => {
      if (!templates[name]) {
        const terminal = OB.App.TerminalProperty.get('terminal');
        if (!terminal) {
          throw new Error('Missing terminal information');
        }
        const template = terminal[name] ? terminal[name] : defaultTemplate;
        templates[name] = new OB.App.Class.PrintTemplate(template);
      }
      return templates[name];
    }
  };
})();
