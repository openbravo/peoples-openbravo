/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

/**
 * @fileoverview Define utility functions for the Add Product action
 */
(function AddProductUtilsDefinition() {
  const setLineAttributes = (line, attrs, productInfo) => {
    const lineAttrs = {
      ...attrs,
      hasMandatoryServices: attrs.splitline ? false : attrs.hasMandatoryServices
    };
    if (
      productInfo.product.productType === 'S' &&
      lineAttrs.relatedLines &&
      line.relatedLines
    ) {
      lineAttrs.relatedLines = OB.App.ArrayUtils.union(
        line.relatedLines,
        lineAttrs.relatedLines
      );
    }
    Object.assign(line, lineAttrs);
  };

  const updateServiceRelatedLines = (line, ticket, extraData) => {
    if (!line.product.productServiceLinked) {
      return ticket;
    }

    // Check if it is necessary to modify the tax category of related lines
    const linesToChange = line.product.productServiceLinked
      .map(psl => {
        return line.relatedLines
          .filter(rl => rl.productCategory === psl.productCategory)
          .map(rl => ticket.lines.find(l => l.id === rl.orderlineId))
          .filter(l => l !== undefined)
          .map(l => {
            const info = {
              id: l.id,
              product: {
                ...l.product,
                previousTaxCategory: l.product.taxCategory,
                taxCategory: psl.taxCategory
              },
              taxRate: l.taxRate,
              previousBaseGrossUnitPrice: l.priceIncludesTax
                ? l.baseGrossUnitPrice
                : undefined
            };
            return info;
          });
      })
      .flat();

    // Update the price of the related lines whose tax category has changed
    if (linesToChange.length > 0) {
      // can direclty change the lines property because this is an internal function that receives a clone of the ticket
      // eslint-disable-next-line no-param-reassign
      ticket.lines = ticket.lines.map(l => {
        const info = linesToChange.find(cl => cl.id === l.id);
        if (info) {
          return { ...l, ...info };
        }
        return l;
      });

      if (!ticket.priceIncludesTax) {
        return ticket;
      }

      const newTicket = OB.App.State.Ticket.Utils.calculateTotals(ticket, {
        discountRules: extraData.discountRules,
        taxRules: extraData.taxRules,
        bpSets: extraData.bpSets,
        qtyScale: extraData.qtyScale,
        payments: extraData.payments,
        paymentcash: extraData.paymentcash
      });

      newTicket.lines = newTicket.lines.map(l => {
        const info = linesToChange.find(cl => cl.id === l.id);
        if (info) {
          const newLine = { ...l };
          newLine.product = info.product;
          if (info.previousBaseGrossUnitPrice) {
            newLine.previousBaseGrossUnitPrice =
              info.previousBaseGrossUnitPrice;
            newLine.baseGrossUnitPrice = OB.DEC.mul(
              OB.DEC.div(info.previousBaseGrossUnitPrice, info.taxRate),
              newLine.taxRate
            );
          }
          return newLine;
        }
        return l;
      });
      return newTicket;
    }
    return ticket;
  };

  const createLine = (productInfo, ticket, extraData) => {
    const { product, qty, options, attrs } = productInfo;

    const lineQty =
      attrs.relatedLines &&
      attrs.relatedLines[0].deferred &&
      product.quantityRule === 'PP' &&
      qty > 0
        ? attrs.relatedLines[0].qty
        : qty;

    const createdData = OB.App.State.Ticket.Utils.createLine(ticket, {
      product,
      qty: lineQty,
      terminal: extraData.terminal,
      store: extraData.store,
      warehouses: extraData.warehouses,
      deliveryPaymentMode: extraData.deliveryPaymentMode
    });

    const newTicket = createdData.ticket;
    const newLine = createdData.line;

    if (attrs.organization) {
      newLine.organization = {
        id: attrs.organization.id,
        orgName: attrs.organization.name,
        country: attrs.organization.country,
        region: attrs.organization.region
      };
      newLine.country = newLine.organization.country;
      newLine.region = newLine.organization.region;
    }

    if (attrs.splitline != null && attrs.originalLine) {
      const originalLine = newTicket.lines.find(
        l => l.id === attrs.originalLine
      );
      if (originalLine) {
        newLine.warehouse = {
          id: originalLine.warehouse.id,
          warehousename: originalLine.warehouse.warehousename
        };
      }
    }

    if (lodash.has(options, 'isEditable')) {
      newLine.isEditable = options.isEditable;
    }

    if (lodash.has(options, 'isDeletable')) {
      newLine.isDeletable = options.isDeletable;
    }

    setLineAttributes(newLine, attrs, productInfo);

    if (!newLine.relatedLines) {
      return newTicket;
    }

    // update service information for the ticket
    newLine.groupService = newLine.product.groupProduct;
    if (!newTicket.hasServices) {
      newTicket.hasServices = true;
    }
    return updateServiceRelatedLines(newLine, newTicket, extraData);
  };

  const createLines = (productInfo, ticket, extraData) => {
    const { qty } = productInfo;
    let newTicket = ticket;
    const lineQty = Math.sign(qty) === -1 ? -1 : 1;

    for (let count = 0; count < Math.abs(qty); count += 1) {
      const newProductInfo = { ...productInfo, qty: lineQty };
      newTicket = createLine(newProductInfo, newTicket, extraData);
    }

    return newTicket;
  };

  OB.App.StateAPI.Ticket.registerUtilityFunctions({
    /**
     * Throws error in case ticket is not editable.
     */
    checkIsEditable(ticket) {
      if (ticket.isEditable === false) {
        throw new OB.App.Class.ActionCanceled({
          title: 'OBPOS_modalNoEditableHeader',
          errorConfirmation: 'OBPOS_modalNoEditableBody'
        });
      }
    },

    /**
     * Add products defined in the payload to the ticket sent as parameter
     */
    addProduct(ticket, payload) {
      let newTicket = { ...ticket };
      // the products property of the payload contains:
      // product: the product to add
      // qty: the qty of the product to be added
      // options: settings that allow to change the behavior of the action
      // attrs: additional properties to be included in the created lines
      const { products, extraData } = payload;
      delete newTicket.deferredOrder;

      const linesToEdit = products
        .map(productInfo =>
          OB.App.State.Ticket.Utils.getLineToEdit(newTicket, productInfo)
        )
        .filter(l => l !== undefined);

      newTicket.lines = newTicket.lines.map(line =>
        linesToEdit.some(lineToEdit => lineToEdit.id === line.id)
          ? { ...line }
          : line
      );

      products.forEach(productInfo => {
        const lineToEdit = OB.App.State.Ticket.Utils.getLineToEdit(
          newTicket,
          productInfo
        );
        const { product } = productInfo;
        if (lineToEdit) {
          // add product to an existing line
          lineToEdit.qty += productInfo.qty;
          setLineAttributes(lineToEdit, productInfo.attrs, productInfo);
        } else if (product.groupProduct || product.avoidSplitProduct) {
          // add product to a new line
          newTicket = createLine(productInfo, newTicket, extraData);
        } else {
          // add product creating multiple new lines with quantity 1 each
          newTicket = createLines(productInfo, newTicket, extraData);
        }
      });

      // delete the lines resulting with quantity zero
      newTicket.lines = newTicket.lines.filter(l => l.qty !== 0);

      newTicket = OB.App.State.Ticket.Utils.updateServicesInformation(
        newTicket,
        extraData
      );

      return newTicket;
    }
  });
})();
