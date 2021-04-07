/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

(function PrintTemplateDefinition() {
  /**
   * Builds the data that is sent to the Hardware Manager to be printed in a particular format.
   */
  OB.App.Class.PrintTemplate = class PrintTemplate {
    /**
     * Creates a new PrintTemplate instance
     *
     * @param name {string} - the name that identifies the template
     * @param resource {string} - the resource relative path used to retrieve the template data
     * @param options {object} - additional configuration options:
     *                           - isLegacy: whether this is a legacy template or not
     */
    constructor(name, resource, options = {}) {
      this.name = name;
      this.resource = resource.startsWith('res/')
        ? `../org.openbravo.retail.posterminal/${resource}`
        : resource;
      this.resourcedata = null;
      this.isLegacy = options.isLegacy === true;
    }

    /**
     * Processes the print template, generating the result to be printed
     *
     * @param params {object} - the parameters to be provided to the template
     * @return {object} - the result of processing the template. It is an object that may contain:
     *                  - data: a string with the template processing result (regular templates)
     *                  - param: if provided, the ticket information (PDF templates)
     *                  - mainReport: the main report definition (PDF templates)
     *                  - subReports: an array with the subreports (PDF templates)
     */
    async generate(params) {
      const terminal = OB.App.TerminalProperty.get('terminal');
      if (terminal[`${this.name}IsPdf`] === true && !this.ispdf) {
        await this.processPDFTemplate({
          printer: terminal[`${this.name}Printer`],
          subreports: Object.keys(terminal)
            .filter(key => key.startsWith(`${this.name}Subrep`))
            .map(key => terminal[key])
        });
      }

      const templateParams = await this.prepareParams(params);

      if (this.ispdf) {
        // Template for printing a jasper PDF report
        return {
          param: this.getTicketForPDFReport(templateParams),
          mainReport: this,
          subReports: this.subreports
        };
      }

      const templateData = await this.getData();
      const data = lodash.template(templateData)(templateParams);

      if (data.substr(0, 6) !== 'jrxml:') {
        // Standard XML template
        return { data };
      }

      // Template for printing a jasper PDF report
      const jasperParams = JSON.parse(data.substr(6));
      const newTemplate = new OB.App.Class.PrintTemplate(
        null,
        jasperParams.report
      );
      await newTemplate.processPDFTemplate(jasperParams);

      return {
        param: this.getTicketForPDFReport(templateParams),
        mainReport: newTemplate,
        subReports: newTemplate.subreports
      };
    }

    async processPDFTemplate(params) {
      const { printer, subreports } = params;

      this.ispdf = true;
      this.printer = printer || 1;
      this.dateFormat = OB.Format.date;
      this.subreports = subreports.map(
        (s, index) =>
          new OB.App.Class.PrintTemplate(`${this.name}Subrep${index}`, s)
      );

      const templates = [this, ...this.subreports];
      const dataRetrievals = templates.map(async template => {
        await template.getData();
      });
      await Promise.all(dataRetrievals);
    }

    async getData() {
      if (!this.resourcedata) {
        // Retrieve the data from backend
        this.resourcedata = await OB.App.Request.get(
          this.resource,
          `hash=${OB.UTIL.localStorage.getItem('templateVersion')}`,
          {
            timeout: 20000,
            type: 'text',
            options: {
              headers: {
                'Content-Type':
                  'application/x-www-form-urlencoded; charset=utf-8'
              }
            }
          }
        );
      }
      return this.resourcedata;
    }

    async prepareParams(params) {
      const newParams = { ...params };
      if (!this.isLegacyTemplate()) {
        const orgVariables = await OB.App.OrgVariables.getAll();
        const defaultLanguage = OB.App.TerminalProperty.get('terminal')
          .language_string;

        newParams.getOrgVariable = (searchKey, language) => {
          const lang = language || defaultLanguage;

          const orgVariable = orgVariables.find(
            p =>
              p.variable === searchKey &&
              (!p.translatable || p.language === lang)
          );

          return orgVariable && orgVariable.value;
        };

        return newParams;
      }
      const { ticket, ticketLine } = params;

      if (ticket) {
        newParams.order = ticket.multiOrdersList
          ? OB.UTIL.TicketUtils.toMultiOrder(ticket)
          : OB.UTIL.TicketUtils.toOrder(ticket);
      }
      if (ticketLine) {
        newParams.line = OB.UTIL.TicketUtils.toOrderLine(ticketLine);
      }
      return newParams;
    }

    getTicketForPDFReport(params) {
      return this.isLegacyTemplate() && params.order
        ? params.order.serializeToJSON()
        : params.ticket;
    }

    isLegacyTemplate() {
      const terminal = OB.App.TerminalProperty.get('terminal');
      return terminal[`${this.name}IsLegacy`] === true || this.isLegacy;
    }
  };
})();
