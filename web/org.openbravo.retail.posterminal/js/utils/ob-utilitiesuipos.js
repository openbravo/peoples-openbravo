/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, console, _ */

OB = window.OB || {};
OB.UTIL = window.OB.UTIL || {};

OB.UTIL.isDisableDiscount = function (receipt) {
  if (receipt.get('lines').length > 0) {
    return OB.POS.modelterminal.get('isDisableDiscount');
  } else {
    return true;
  }
};

OB.UTIL.getImageURL = function (id) {
  var imageUrl = 'productImages/';
  var i;
  for (i = 0; i < id.length; i += 3) {
    if (i !== 0) {
      imageUrl += "/";
    }
    imageUrl += id.substring(i, ((i + 3) < id.length) ? (i + 3) : id.length);
  }
  imageUrl += "/" + id;
  return imageUrl;
};

OB.UTIL.getNumberOfSequence = function (documentNo, isQuotation) {
  if (!OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal')) && !OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal')).docNoPrefix) {
    var posDocumentNoPrefix = OB.MobileApp.model.get('terminal').docNoPrefix;
    if (isQuotation) {
      posDocumentNoPrefix = OB.MobileApp.model.get('terminal').quotationDocNoPrefix;
    }
    return parseInt(documentNo.substr(posDocumentNoPrefix.length + 1), 10);
  } else {
    return null;
  }
};


/**
 * Facilitates to work reliably with currency conversions
 *   in the easiest way, you will just need to do like this:
 *     add the conversor:
 *       OB.UTIL.currency.addConversion(fromCurrencyId, toCurrencyId)
 *
 *     get the conversor, depending on what you want:
 *       var cD = OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, amount)
 *       var cF = OB.UTIL.currency.toForeignCurrency(toCurrencyId, amount)
 *
 *
 *   expert use:
 *   case 1: retail
 *     when selling, to get the converted amount of a good, you should use getTangibleOf(amount)
 *     e.g: the Avalanche Transceiver in sampledata cost 150.5€ or getTangibleOf(150.5) = 197.81$
 *
 *   case 2: doble conversion
 *     when you have already converted one currency to another and want to convert the resulted amount again you will want to convert it back with full precision. use getFinancialAmountOf(amount)
 *     e.g: when showing a foreign value to the user
 *
 *   case 3: financial accounts (a doble conversion with sensitive data)
 *     when you deposit foreign money in a local financial account you should use getFinancialAmountOf(amount)
 *     e.g: when you deposit a 100$ bill in a bank account that is in euros -> getFinancialAmountOf(100) = 74.082324€
 *
 */
OB.UTIL.currency = {
  conversions: [],
  webPOSDefaultCurrencyId: function () {
    return parseInt(OB.POS.modelterminal.get('currency').id, 10);
  },
  isDefaultCurrencyId: function (currencyId) {
    currencyId = parseInt(currencyId, 10);
    return currencyId === OB.UTIL.currency.webPOSDefaultCurrencyId();
  },
  /**
   * add a conversion rate from the fromCurrencyId currency to the toCurrencyId currency into the conversions array
   * @param {currencyId}    fromCurrencyId    currencyId of the original amount
   * @param {currencyId}    toCurrencyId      currencyId of the resulting amount
   * @param {float}         rate              exchange rate to calculate the resulting amount
   */
  addConversion: function (fromCurrencyId, toCurrencyId, rate) {
    fromCurrencyId = parseInt(fromCurrencyId, 10);
    toCurrencyId = parseInt(toCurrencyId, 10);
    rate = parseFloat(rate, 10);

    if (fromCurrencyId === toCurrencyId) {
      this.showError('DEVELOPER: there is no point in converting a currencyId to itself');
      return;
    }

    var conversionAlreadyExists = this.findConverter(fromCurrencyId, toCurrencyId);
    if (conversionAlreadyExists) {
      if (conversionAlreadyExists.rate !== rate) {
        this.showError('DEVELOPER: The rate for a currency is trying to be changed. If you are not trying to change the rate, something needs critical and inmediate fixing. If you really want to change the rate and know what you are doing, clean the OB.UTIL.currency.conversions array and fill it again.');
      }
      return; // the conversor is already present. this is fine, unless a lot of calls are finishing here
    }
    this.conversions.push({
      fromCurrencyId: fromCurrencyId,
      toCurrencyId: toCurrencyId,
      rate: rate,
      toCurrencyIdPrecision: OB.DEC.getScale(),
      // TODO: get, from the backend, the precisions for the currency with the id = toCurrencyId
      isToCurrencyIdForeign: toCurrencyId !== OB.UTIL.currency.webPOSDefaultCurrencyId(),
      /**
       * Get a rounded exchanged amount that indicates the amount in the real world, say money, card tickets, etc
       *   e.g: the Avalanche Transceiver in sampledata cost 150.5€ or getTangibleOf(150.5) = 197.81$
       * @param  {float}     amountToRound  the amount to be converted to toCurrencyId
       * @return {float}     the converted amount using the exchange rate rounded to the precision set in preferences for toCurrencyId
       */
      getTangibleOf: function (amountToRound) {
        if (this.toCurrencyId === OB.UTIL.currency.webPOSDefaultCurrencyId()) {
          OB.UTIL.currency.showError('DEVELOPER: You cannot get a tangible of a foreign currency because it has already a value in local currency. If you are trying to get the amount for a financial account, use the getFinancialAmountOf function');
          return;
        }
        return OB.DEC.mul(amountToRound, rate, OB.UTIL.currency.toCurrencyIdPrecision);
      },
      /**
       * Get a full precision converted amount which origin is real money and will and will be added to a local currency financial account
       *   e.g: when you deposit a 100$ bill in a bank account that is in euros -> getExchangeOfTangible(100) = 74.082€
       * @param  {float}     amountToRound  the amount to be converted to toCurrencyId
       * @return {float}     the converted amount using the exchange rate rounded to the precision set in preferences for toCurrencyId
       */
      getFinancialAmountOf: function (amount) {
        if (this.fromCurrencyId === OB.UTIL.currency.webPOSDefaultCurrencyId()) {
          OB.UTIL.currency.showError('DEVELOPER: You are trying to get a financial amount value that is not from a foreign currency');
          return;
        }
        return OB.DEC.mulFullPrecision(amount, rate);
      },
      toString: function () {
        return this.fromCurrencyId + ' -> ' + this.toCurrencyId + '; rate:' + this.rate.toFixed(5);
      }
    });
  },
  /**
   * get all the converters available in the internal converters array
   * @return {array of converters}  the converters available in the internal converters array
   */
  getConversions: function () {
    return this.conversions;
  },
  /**
   * Find the converter with the indicated fromCurrencyId and toCurrencyId in the internal converters array
   * Developer: you, most likely, won't need this function. If so, change this comment
   */
  findConverter: function (fromCurrencyId, toCurrencyId) {
    return _.find(this.conversions, function (c) {
      return (c.fromCurrencyId === fromCurrencyId) && (c.toCurrencyId === toCurrencyId);
    });
  },
  /**
   * Returns a converter to operate with amounts that will be converted from fromCurrencyId to toCurrencyId
   * @param  {currencyId} fromCurrencyId the original currencyId
   * @param  {currencyId} toCurrencyId   the destination currencyId
   * @return {converter}                 the converter to convert amounts from the fromCurrencyId currency to the toCurrencyId currency
   */
  getConverter: function (fromCurrencyId, toCurrencyId) {
    fromCurrencyId = parseInt(fromCurrencyId, 10);
    toCurrencyId = parseInt(toCurrencyId, 10);
    var found = this.findConverter(fromCurrencyId, toCurrencyId);
    if (!found) {
      this.showError('DEVELOPER: Currency converter not added: ' + fromCurrencyId + ' -> ' + toCurrencyId);
    }
    return found;
  },
/**
   * Returns a converter whose original currency is not the WebPOS currency. e.g: USD in sampledata
   * and whose destiny curency is the WebPOS default currency. i.e: OB.POS.modelterminal.get('currency').id
    return this.getConverter(webPOSDefaultCurrencyId(), toCurrencyId);
   * @param  {currencyId} fromCurrencyId  the currencyId of the original currency
   * @return {converter}                  the converter to convert amounts from fromCurrencyId to the WebPOS default currency
   */
  getToLocalConverter: function (fromCurrencyId) {
    fromCurrencyId = parseInt(fromCurrencyId, 10);
    return this.getConverter(fromCurrencyId, this.webPOSDefaultCurrencyId());
  },
  /**
   * Returns a converter whose destiny currency is not the WebPOS currency. e.g: USD in sampledata
   * and whose original curency is the WebPOS default currency. i.e: OB.POS.modelterminal.get('currency').id
   * @param  {currencyId} toCurrencyId  the currencyId of the destiny currency
   * @return {converter}                the converter to convert amounts from WebPOS default currency to toCurrencyId
   */
  getFromLocalConverter: function (toCurrencyId) {
    toCurrencyId = parseInt(toCurrencyId, 10);
    return this.getConverter(this.webPOSDefaultCurrencyId(), toCurrencyId);
  },
  /**
   * converts an amount to the WebPOS amount currency
   * @param  {currencyId} fromCurrencyId    the currencyId of the amount to be converted
   * @param  {float}      amount            the amount to be converted
   * @return {float}                        the converted amount
   */
  toDefaultCurrency: function (fromCurrencyId, amount) {
    if (OB.UTIL.isNullOrUndefined(amount)) {
      this.showError('DEVELOPER: you are missing one parameter');
    }
    fromCurrencyId = parseInt(fromCurrencyId, 10);
    if (fromCurrencyId === this.webPOSDefaultCurrencyId()) {
      return amount;
    }
    var converter = this.getToLocalConverter(fromCurrencyId);
    var foreignAmount = converter.getFinancialAmountOf(amount);
    return foreignAmount;
  },
  /**
   * converts an amount from the WebPOS currency to the toCurrencyId currency
   * @param  {currencyId} toCurrencyId      the currencyId of the final amount
   * @param  {float}      amount            the amount to be converted
   * @return {float}                        the converted amount
   */
  toForeignCurrency: function (toCurrencyId, amount) {
    if (OB.UTIL.isNullOrUndefined(amount)) {
      this.showError('DEVELOPER: you are missing one parameter');
    }
    toCurrencyId = parseInt(toCurrencyId, 10);
    if (toCurrencyId === this.webPOSDefaultCurrencyId()) {
      return amount;
    }
    var converter = this.getFromLocalConverter(toCurrencyId);
    var foreignAmount = converter.getTangibleOf(amount);
    return foreignAmount;
  },
  showError: function (msg) {
    console.error(msg);
  }
};