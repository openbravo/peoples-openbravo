/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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
    constructor(resource) {
      this.resource = resource;
      this.resourcedata = null;
    }

    /**
     * Generates the data to be sent to the Hardware Manager for the printing according to this template
     *
     * @param data {string} - the data to be sent for the printing
     */
    async generate(params) {
      const templateData = await this.getData();
      const templateParams = this.prepareParams(params);
      const data = lodash.template(templateData)(templateParams);

      if (data.substr(0, 6) !== 'jrxml:') {
        return data;
      }

      // Template for printing a jasper PDF report
      const jasperParams = JSON.parse(data.substr(6));
      const newTemplate = new OB.App.Class.PrintTemplate(jasperParams.report);
      newTemplate.ispdf = true;
      newTemplate.printer = jasperParams.printer || 1;
      newTemplate.dateFormat = OB.Format.date;
      newTemplate.subreports = jasperParams.subreports.map(
        subReport => new OB.App.Class.PrintTemplate(subReport)
      );

      const templates = [newTemplate, ...newTemplate.subreports];
      const dataRetrievals = templates.map(async template => {
        await template.getData();
      });
      await Promise.all(dataRetrievals);

      return JSON.stringify({
        param: templateParams.order
          ? templateParams.order.serializeToJSON()
          : params.ticket,
        mainReport: newTemplate,
        subReports: newTemplate.subReports
      });
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

    prepareParams(params) {
      if (!this.isLegacyMode()) {
        return params;
      }
      const { ticket, ticketLine } = params;
      const newParams = { ...params };
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

    // eslint-disable-next-line class-methods-use-this
    isLegacyMode() {
      return OB.App.StateBackwardCompatibility != null;
    }
  };
})();
