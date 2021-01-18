/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function CreateTicketFromQuotation() {
  OB.App.StateAPI.Ticket.registerAction(
    'createTicketFromQuotation',
    (ticket, payload) => {
      const oldIdMap = [];
      const newTicket = {
        ...ticket,
        oldId: ticket.id,
        id: OB.UTIL.get_UUID(),
        documentNo: '',
        isQuotation: false,
        orderType: payload.terminal.terminalType.layawayorder ? 2 : 0,
        documentType: payload.documentType,
        createdBy: payload.user,
        hasbeenpaid: 'N',
        skipApplyPromotions: false,
        isPaid: false,
        isEditable: true,
        orderDate: payload.date,
        posTerminal: payload.terminal.id,
        session: payload.session,
        creationDate: null
      };

      delete newTicket.deletedLines;
      if (payload.isSalesRepresentative) {
        newTicket.salesRepresentative = payload.salesRepresentative;
      } else {
        delete newTicket.salesRepresentative;
      }
      const payloadLines = payload.lines || [];
      newTicket.lines = newTicket.lines.map(line => {
        const newLine = {
          ...line,
          id: OB.App.UUID.generate(),
          priceIncludesTax: newTicket.priceIncludesTax,
          skipApplyPromotions: payload.firmQuotation
        };
        // Update line extra properties
        const payloadLine = payloadLines.find(l => l.lineId === line.id);
        if (payloadLine) {
          const lineProps = payloadLine.lineProperties;
          /* eslint-disable no-restricted-syntax */
          for (const key in lineProps) {
            if (lineProps[key]) {
              newLine[key] = lineProps[key];
            }
          }
          const productProps = payloadLine.productProperties;
          /* eslint-disable no-restricted-syntax */
          for (const key in productProps) {
            if (productProps[key]) {
              newLine.product[key] = productProps[key];
            }
          }
        }
        if (line.hasRelatedServices) {
          oldIdMap[line.id] = newLine.id;
        }
        // Promotions will be recalculated
        if (payload.firmQuotation === false) {
          delete newLine.promotions;
          if (newLine.priceIncludesTax) {
            newLine.grossListPrice = newLine.product.listPrice;
            newLine.baseGrossUnitPrice = newLine.product.listPrice;
          } else {
            newLine.netListPrice = newLine.product.listPrice;
            newLine.baseNetUnitPrice = newLine.product.listPrice;
          }
        }
        delete newLine.grossUnitPrice;
        delete newLine.lineGrossAmount;
        delete newLine.netFull;
        delete newLine.obposQtytodeliver;
        return newLine;
      });
      newTicket.lines = newTicket.lines.map(line => {
        const newLine = {
          ...line
        };
        if (newLine.relatedLines) {
          newLine.relatedLines = newLine.relatedLines.map(relatedLine => {
            const newRelatedLine = {
              ...relatedLine,
              orderId: newTicket.id,
              orderDocumentNo: newTicket.documentNo
            };
            if (oldIdMap[relatedLine.orderlineId]) {
              newRelatedLine.orderlineId = oldIdMap[relatedLine.orderlineId];
            }
            return newRelatedLine;
          });
        }
        return newLine;
      });

      return newTicket;
    }
  );

  // Checking stock before creating ticket from quotation
  OB.App.StateAPI.Ticket.createTicketFromQuotation.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };
      newPayload.lines = ticket.lines.map(line => {
        return {
          lineId: line.id,
          productId: line.product.id,
          lineProperties: {},
          productProperties: {}
        };
      });
      for (let i = 0; i < ticket.lines.length; i += 1) {
        const { product, qty, options, attrs } = ticket.lines[i];
        const line = OB.App.State.Ticket.Utils.getLineToEdit(ticket, {
          ...ticket.lines[i],
          options: ticket.lines[i].options || {},
          attrs: ticket.lines[i].attrs || {}
        });
        const lineId = line ? line.id : payload.line;
        const settings = { ticket, lineId, options, attrs };

        // eslint-disable-next-line no-await-in-loop
        const hasStock = await OB.App.StockChecker.hasStock(
          product,
          qty,
          settings
        );

        if (!hasStock) {
          throw new OB.App.Class.ActionCanceled(
            `Create Ticket From Quotation canceled: there is no stock of product ${product.id}`
          );
        }
      }

      return newPayload;
    },
    async (ticket, payload) => payload
  );

  // Updating Line Price
  OB.App.StateAPI.Ticket.createTicketFromQuotation.addActionPreparation(
    async (ticket, payload) => {
      if (payload.firmQuotation === true) {
        return payload;
      }
      const newPayload = { ...payload };

      const linePrices = newPayload.lines.map(async line => {
        const newLine = { ...line };
        let product = null;
        if (OB.App.Security.hasPermission('OBPOS_remote.product')) {
          try {
            const [dataProduct] = await OB.App.DAL.remoteSearch('Product', {
              remoteFilters: [
                {
                  columns: ['id'],
                  operator: 'equals',
                  value: line.productId,
                  isId: true
                }
              ]
            });
            if (dataProduct) {
              product = dataProduct;
            }
          } catch (error) {
            OB.error(error.message);
          }
        } else {
          try {
            product = await OB.App.MasterdataModels.Product.withId(
              line.productId
            );
          } catch (error) {
            OB.error(error.message);
          }
        }
        if (product) {
          newLine.productProperties.standardPrice = product.standardPrice;
          newLine.productProperties.listPrice = product.listPrice;
          newLine.lineProperties.price = product.standardPrice;
        }
        return newLine;
      });
      newPayload.lines = await Promise.all(linePrices);

      return newPayload;
    },
    async (ticket, payload) => payload
  );
})();
