/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global */

OB.UTIL = window.OB.UTIL || {};

/**
 * Facilitates to work reliably with currency conversions
 *   in the easiest way, you will just need to do like this:
 *     add the conversor:
 *       OB.UTIL.currency.addConversion(fromCurrencyId, toCurrencyId)
 *
 *     get the conversor, depending on what you want:
 *       let cD = OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, amount)
 *       let cF = OB.UTIL.currency.toForeignCurrency(toCurrencyId, amount)
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
  defaultCurrencyId: null,
  setDefaultCurrencyId(currencyId) {
    this.defaultCurrencyId = currencyId.toString();
  },
  webPOSDefaultCurrencyId() {
    return this.defaultCurrencyId;
  },
  isDefaultCurrencyId(currencyId) {
    // argument checks
    if (!currencyId) {
      OB.error('Missing arguments in OB.UTIL.currency.isDefaultCurrencyId');
      return false;
    }
    return currencyId.toString() === this.defaultCurrencyId;
  },
  /**
   * add a conversion rate from the fromCurrencyId currency to the toCurrencyId currency into the conversions array
   * @param {currencyId}    fromCurrencyId    currencyId of the original amount
   * @param {currencyId}    toCurrencyId      currencyId of the resulting amount
   * @param {float}         rate              exchange rate to calculate the resulting amount
   */
  addConversion(fromCurrencyId, toCurrencyId, rate) {
    // argument checks
    if (!fromCurrencyId || !toCurrencyId || rate == null) {
      OB.error('Missing arguments in OB.UTIL.currency.addConversion');
      return;
    }

    const fromCurId = fromCurrencyId.toString();
    const toCurId = toCurrencyId.toString();
    const rateValue = parseFloat(rate, 10);

    if (fromCurId === toCurId) {
      OB.error('There is no point in converting a currencyId to itself');
      return;
    }

    const conversionAlreadyExists = this.findConverter(fromCurId, toCurId);
    if (conversionAlreadyExists) {
      if (conversionAlreadyExists.rate !== rateValue) {
        OB.error(
          'The rate for a currency is trying to be changed. If you are not trying to change the rate, something needs critical and inmediate fixing. If you really want to change the rate and know what you are doing, clean the OB.UTIL.currency.conversions array and fill it again.'
        );
      }
      return; // the conversor is already present. this is fine, unless a lot of calls are finishing here
    }
    this.conversions.push({
      fromCurrencyId: fromCurId,
      toCurrencyId: toCurId,
      rate: rateValue,
      toCurrencyIdPrecision: OB.DEC.getScale(),
      // TODO: get, from the backend, the precisions for the currency with the id = toCurrencyId
      isToCurrencyIdForeign: toCurrencyId !== this.defaultCurrencyId,
      /**
       * Get a rounded exchanged amount that indicates the amount in the real world, say money, card tickets, etc
       *   e.g: the Avalanche Transceiver in sampledata cost 150.5€ or getTangibleOf(150.5) = 197.81$
       * @param  {float}     amountToRound  the amount to be converted to toCurrencyId
       * @return {float}     the converted amount using the exchange rate rounded to the precision set in preferences for toCurrencyId
       */
      getTangibleOf(amountToRound) {
        if (this.toCurrencyId === OB.UTIL.currency.webPOSDefaultCurrencyId()) {
          OB.error(
            'You cannot get a tangible of a foreign currency because it has already a value in local currency. If you are trying to get the amount for a financial account, use the getFinancialAmountOf function'
          );
          return false;
        }
        return OB.DEC.mul(
          amountToRound,
          this.rate,
          OB.UTIL.currency.toCurrencyIdPrecision
        );
      },
      /**
       * Get a full precision converted amount which origin is real money and will and will be added to a local currency financial account
       *   e.g: when you deposit a 100$ bill in a bank account that is in euros -> getExchangeOfTangible(100) = 74.082€
       * @param  {float}     amountToRound  the amount to be converted to toCurrencyId
       * @return {float}     the converted amount using the exchange rate rounded to the precision set in preferences for toCurrencyId
       */
      getFinancialAmountOf(amountToRound) {
        if (
          this.fromCurrencyId === OB.UTIL.currency.webPOSDefaultCurrencyId()
        ) {
          OB.error(
            'You are trying to get a financial amount value that is not from a foreign currency'
          );
          return false;
        }
        return OB.DEC.mul(
          amountToRound,
          this.rate,
          OB.UTIL.currency.toCurrencyIdPrecision
        );
      },
      toString() {
        return `${this.fromCurrencyId} -> ${
          this.toCurrencyId
        }; rate:${this.rate.toFixed(5)}`;
      }
    });
  },
  /**
   * get all the converters available in the internal converters array
   * @return {array of converters}  the converters available in the internal converters array
   */
  getConversions() {
    return this.conversions;
  },
  /**
   * Find the converter with the indicated fromCurrencyId and toCurrencyId in the internal converters array
   * Developer: you, most likely, won't need this function. If so, change this comment
   */
  findConverter(fromCurrencyId, toCurrencyId) {
    // argument checks
    if (!fromCurrencyId || !toCurrencyId) {
      OB.error('Missing arguments in OB.UTIL.currency.findConverter');
      return false;
    }
    const fromCurId = fromCurrencyId.toString();
    const toCurId = toCurrencyId.toString();
    return this.conversions.find(c => {
      return c.fromCurrencyId === fromCurId && c.toCurrencyId === toCurId;
    });
  },
  /**
   * Returns a converter to operate with amounts that will be converted from fromCurrencyId to toCurrencyId
   * @param  {currencyId} fromCurrencyId the original currencyId
   * @param  {currencyId} toCurrencyId   the destination currencyId
   * @return {converter}                 the converter to convert amounts from the fromCurrencyId currency to the toCurrencyId currency
   */
  getConverter(fromCurrencyId, toCurrencyId) {
    // argument checks
    if (!fromCurrencyId || !toCurrencyId) {
      OB.error('Missing arguments in OB.UTIL.currency.getConverter');
      return false;
    }
    const fromCurId = fromCurrencyId.toString();
    const toCurId = toCurrencyId.toString();
    const found = this.findConverter(fromCurId, toCurId);
    if (!found) {
      OB.error(`Currency converter not added: ${fromCurId} -> ${toCurId}`);
      throw new Error(
        `Currency converter not added: ${fromCurId} -> ${toCurId}`
      );
    }
    return found;
  },
  /**
   * Returns a converter whose original currency is not the WebPOS currency. e.g: USD in sampledata
   * and whose destiny curency is the WebPOS default currency.
    return this.getConverter(defaultCurrencyId, toCurrencyId);
   * @param  {currencyId} fromCurrencyId  the currencyId of the original currency
   * @return {converter}                  the converter to convert amounts from fromCurrencyId to the WebPOS default currency
   */
  getToLocalConverter(fromCurrencyId) {
    // argument checks
    if (!fromCurrencyId) {
      OB.error('Missing arguments in OB.UTIL.currency.getToLocalConverter');
      return false;
    }
    return this.getConverter(fromCurrencyId.toString(), this.defaultCurrencyId);
  },
  /**
   * Returns a converter whose destiny currency is not the WebPOS currency. e.g: USD in sampledata
   * and whose original curency is the WebPOS default currency.
   * @param  {currencyId} toCurrencyId  the currencyId of the destiny currency
   * @return {converter}                the converter to convert amounts from WebPOS default currency to toCurrencyId
   */
  getFromLocalConverter(toCurrencyId) {
    // argument checks
    if (!toCurrencyId) {
      OB.error('Missing arguments in OB.UTIL.currency.getFromLocalConverter');
      return false;
    }
    return this.getConverter(this.defaultCurrencyId, toCurrencyId.toString());
  },
  /**
   * converts an amount to the WebPOS amount currency
   * @param  {currencyId} fromCurrencyId    the currencyId of the amount to be converted
   * @param  {float}      amount            the amount to be converted
   * @return {float}                        the converted amount
   */
  toDefaultCurrency(fromCurrencyId, amount) {
    // argument checks
    if (!fromCurrencyId || amount == null) {
      OB.error('Missing arguments in OB.UTIL.currency.toDefaultCurrency');
      return false;
    }
    const fromCurId = fromCurrencyId.toString();
    if (
      fromCurId != null &&
      this.defaultCurrencyId != null &&
      fromCurId === this.defaultCurrencyId
    ) {
      return amount;
    }
    const converter = this.getToLocalConverter(fromCurId);
    if (converter) {
      return converter.getFinancialAmountOf(amount);
    }
    return false;
  },
  /**
   * converts an amount from the WebPOS currency to the toCurrencyId currency
   * @param  {currencyId} toCurrencyId      the currencyId of the final amount
   * @param  {float}      amount            the amount to be converted
   * @return {float}                        the converted amount
   */
  toForeignCurrency(toCurrencyId, amount) {
    // argument checks
    if (!toCurrencyId || amount == null) {
      OB.error('Missing arguments in OB.UTIL.currency.toForeignCurrency');
      return false;
    }
    const toCurId = toCurrencyId.toString();
    if (toCurId === this.defaultCurrencyId) {
      return amount;
    }
    const converter = this.getFromLocalConverter(toCurId);
    if (converter) {
      return converter.getTangibleOf(amount);
    }
    return false;
  }
};
