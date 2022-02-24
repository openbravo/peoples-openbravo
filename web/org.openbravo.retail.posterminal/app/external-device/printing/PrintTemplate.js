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
  let templateInitializer;

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
      this.initialized = false;
      this.isLegacy = options.isLegacy === true;

      this.resource = resource.startsWith('res/')
        ? `../org.openbravo.retail.posterminal/${resource}`
        : resource;
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
      await this.initialize();

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

    async initialize() {
      if (!this.initialized) {
        await (templateInitializer || defaultTemplateInitializer)(this);
        this.initialized = true;
      }
    }

    /** Reset current template so that next time is used, its initializer will be called. */
    reset() {
      this.initialized = false;
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

      const dataRetrievals = this.subreports.map(template =>
        template.initialize()
      );
      await Promise.all(dataRetrievals);
    }

    getData() {
      return this.resourcedata;
    }

    async prepareParams(params) {
      const newParams = { ...params };
      if (!this.isLegacyTemplate()) {
        const orgVariables = await OB.App.OrgVariables.getAll();

        newParams.getOrgVariable = (
          searchKey,
          language,
          currentDate = new Date()
        ) => {
          return OB.App.OrgVariables.getOrgVariable(
            searchKey,
            language,
            currentDate,
            orgVariables
          );
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
      return this.isLegacy;
    }
  };

  /** By default template is fetched from the resource URL */
  async function defaultTemplateInitializer(printTemplate) {
    const terminal = OB.App.TerminalProperty.get('terminal');
    const templateName = printTemplate.name;

    // eslint-disable-next-line no-param-reassign
    printTemplate.ispdf = terminal[`${templateName}IsPdf`] === true;
    // eslint-disable-next-line no-param-reassign
    printTemplate.isLegacy =
      terminal[`${templateName}IsLegacy`] === true || printTemplate.isLegacy;

    if (printTemplate.ispdf) {
      await printTemplate.processPDFTemplate({
        printer: terminal[`${templateName}Printer`],
        subreports: Object.keys(terminal)
          .filter(key => key.startsWith(`${templateName}Subrep`))
          .map(key => terminal[key])
      });
    }

    // eslint-disable-next-line no-param-reassign
    printTemplate.resourcedata = await OB.App.Request.get(
      printTemplate.resource,
      `hash=${OB.UTIL.localStorage.getItem('templateVersion')}`,
      {
        timeout: 20000,
        type: 'text',
        options: {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'
          }
        }
      }
    );
  }

  OB.App.PrintTemplate = {
    /**
     * Registers a function to get template data. Different modules can define their own function
     * to retrieve templates, the actual template will be the first result returning data. This allows
     * to overwrite the behavior in modules deeper in the dependency tree.
     *
     * The default function to get the template performs a rquest to the 'resource'.
     *
     * @param {function} templateSelector Asynchronous function that, receiving as a parameter a
     *   PrintTemplate instance, returns its template or a nullish value to execute the next getter in
     *   the chain.
     */
    registerInitializer: newTemplateInitializer => {
      templateInitializer = newTemplateInitializer;
    }
  };
})();
