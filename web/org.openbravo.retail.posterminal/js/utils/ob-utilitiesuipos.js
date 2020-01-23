/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo */

OB.UTIL = window.OB.UTIL || {};

OB.UTIL.masterdataRefreshStatus = '';
OB.UTIL.backgroundMasterdataRefreshEnabled;

OB.UTIL.sendLastTerminalStatusValues = function(callback) {
  var process = new OB.DS.Process(
    'org.openbravo.retail.posterminal.process.LastTerminalStatusTimestamps'
  );
  process.exec(
    {
      posterminalId: OB.MobileApp.model.get('terminal').id,
      terminalLastfullrefresh: OB.UTIL.localStorage.getItem(
        'POSLastTotalRefresh'
      ),
      terminalLastincrefresh: OB.UTIL.localStorage.getItem('POSLastIncRefresh'),
      terminalLastcachegeneration: OB.UTIL.localStorage.getItem(
        'LastCacheGeneration'
      ),
      terminalLastjsgeneration: OB.UTIL.localStorage.getItem(
        'LastJSGeneration_' + OB.MobileApp.model.get('appName')
      ),
      terminalLastbenchmark: OB.UTIL.localStorage.getItem('benchmarkScore'),
      terminalLastlogindate: OB.UTIL.localStorage.getItem('lastLogInDate'),
      terminalLastloginuser: OB.UTIL.localStorage.getItem('lastUserIdLogin'),
      terminalLasttimeinoffline: OB.UTIL.localStorage.getItem(
        'lastTransitionToOffline'
      ),
      terminalLasttimeinonline: OB.UTIL.localStorage.getItem(
        'lastTransitionToOnline'
      ),
      terminalLasthwmversion: OB.UTIL.localStorage.getItem(
        'hardwareManagerVersion'
      ),
      terminalLasthwmrevision: OB.UTIL.localStorage.getItem(
        'hardwareManagerRevision'
      ),
      terminalLasthwmjavainfo: OB.UTIL.localStorage.getItem(
        'hardwareManagerJavaInfo'
      )
    },
    function(data, message) {
      if (callback instanceof Function) {
        callback();
      }
    },
    function(error) {
      if (callback instanceof Function) {
        callback();
      }
    }
  );
};

OB.UTIL.getImageURL = function(id) {
  var imageUrl = 'productImages/';
  var i;
  for (i = 0; i < id.length; i += 3) {
    if (i !== 0) {
      imageUrl += '/';
    }
    imageUrl += id.substring(i, i + 3 < id.length ? i + 3 : id.length);
  }
  imageUrl += '/' + id;
  return imageUrl;
};

OB.UTIL.getMinimizedImageURL = function(id) {
  return this.getImageURL(id) + '_min';
};

OB.UTIL.getNumberOfSequence = function(documentNo, isQuotation) {
  if (
    !OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal')) &&
    !OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal')).docNoPrefix
  ) {
    var posDocumentNoPrefix = OB.MobileApp.model.get('terminal').docNoPrefix;
    if (isQuotation) {
      posDocumentNoPrefix = OB.MobileApp.model.get('terminal')
        .quotationDocNoPrefix;
    }
    return parseInt(documentNo.substr(posDocumentNoPrefix.length + 1), 10);
  } else {
    return null;
  }
};

OB.UTIL.getPaymentByKey = function(key) {
  var i;
  var terminalPayments = OB.MobileApp.model.get('payments');
  for (i = 0; i < terminalPayments.length; i++) {
    if (terminalPayments[i].payment.searchKey === key) {
      return terminalPayments[i];
    }
  }
  return null;
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
  webPOSDefaultCurrencyId: function() {
    return OB.MobileApp.model.get('currency').id.toString();
  },
  isDefaultCurrencyId: function(currencyId) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      currencyId,
      "Missing required argument 'currencyId' in OB.UTIL.currency.isDefaultCurrencyId"
    );

    currencyId = currencyId.toString();

    return currencyId === OB.UTIL.currency.webPOSDefaultCurrencyId();
  },
  /**
   * add a conversion rate from the fromCurrencyId currency to the toCurrencyId currency into the conversions array
   * @param {currencyId}    fromCurrencyId    currencyId of the original amount
   * @param {currencyId}    toCurrencyId      currencyId of the resulting amount
   * @param {float}         rate              exchange rate to calculate the resulting amount
   */
  addConversion: function(fromCurrencyId, toCurrencyId, rate) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      fromCurrencyId,
      "Missing required argument 'fromCurrencyId' in OB.UTIL.currency.addConversion"
    );
    OB.UTIL.Debug.isDefined(
      toCurrencyId,
      "Missing required argument 'toCurrencyId' in OB.UTIL.currency.addConversion"
    );
    OB.UTIL.Debug.isDefined(
      rate,
      "Missing required argument 'rate' in OB.UTIL.currency.addConversion"
    );

    fromCurrencyId = fromCurrencyId.toString();
    toCurrencyId = toCurrencyId.toString();
    rate = parseFloat(rate, 10);

    if (fromCurrencyId === toCurrencyId) {
      OB.error('There is no point in converting a currencyId to itself');
      return;
    }

    var conversionAlreadyExists = this.findConverter(
      fromCurrencyId,
      toCurrencyId
    );
    if (conversionAlreadyExists) {
      if (conversionAlreadyExists.rate !== rate) {
        OB.error(
          'The rate for a currency is trying to be changed. If you are not trying to change the rate, something needs critical and inmediate fixing. If you really want to change the rate and know what you are doing, clean the OB.UTIL.currency.conversions array and fill it again.'
        );
      }
      return; // the conversor is already present. this is fine, unless a lot of calls are finishing here
    }
    this.conversions.push({
      fromCurrencyId: fromCurrencyId,
      toCurrencyId: toCurrencyId,
      rate: rate,
      toCurrencyIdPrecision: OB.DEC.getScale(),
      // TODO: get, from the backend, the precisions for the currency with the id = toCurrencyId
      isToCurrencyIdForeign:
        toCurrencyId !== OB.UTIL.currency.webPOSDefaultCurrencyId(),
      /**
       * Get a rounded exchanged amount that indicates the amount in the real world, say money, card tickets, etc
       *   e.g: the Avalanche Transceiver in sampledata cost 150.5€ or getTangibleOf(150.5) = 197.81$
       * @param  {float}     amountToRound  the amount to be converted to toCurrencyId
       * @return {float}     the converted amount using the exchange rate rounded to the precision set in preferences for toCurrencyId
       */
      getTangibleOf: function(amountToRound) {
        if (this.toCurrencyId === OB.UTIL.currency.webPOSDefaultCurrencyId()) {
          OB.error(
            'You cannot get a tangible of a foreign currency because it has already a value in local currency. If you are trying to get the amount for a financial account, use the getFinancialAmountOf function'
          );
          return;
        }
        return OB.DEC.mul(
          amountToRound,
          rate,
          OB.UTIL.currency.toCurrencyIdPrecision
        );
      },
      /**
       * Get a full precision converted amount which origin is real money and will and will be added to a local currency financial account
       *   e.g: when you deposit a 100$ bill in a bank account that is in euros -> getExchangeOfTangible(100) = 74.082€
       * @param  {float}     amountToRound  the amount to be converted to toCurrencyId
       * @return {float}     the converted amount using the exchange rate rounded to the precision set in preferences for toCurrencyId
       */
      getFinancialAmountOf: function(amount) {
        if (
          this.fromCurrencyId === OB.UTIL.currency.webPOSDefaultCurrencyId()
        ) {
          OB.error(
            'You are trying to get a financial amount value that is not from a foreign currency'
          );
          return;
        }
        return OB.DEC.mul(amount, rate);
      },
      toString: function() {
        return (
          this.fromCurrencyId +
          ' -> ' +
          this.toCurrencyId +
          '; rate:' +
          this.rate.toFixed(5)
        );
      }
    });
  },
  /**
   * get all the converters available in the internal converters array
   * @return {array of converters}  the converters available in the internal converters array
   */
  getConversions: function() {
    return this.conversions;
  },
  /**
   * Find the converter with the indicated fromCurrencyId and toCurrencyId in the internal converters array
   * Developer: you, most likely, won't need this function. If so, change this comment
   */
  findConverter: function(fromCurrencyId, toCurrencyId) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      fromCurrencyId,
      "Missing required argument 'fromCurrencyId' in OB.UTIL.currency.findConverter"
    );
    OB.UTIL.Debug.isDefined(
      toCurrencyId,
      "Missing required argument 'toCurrencyId' in OB.UTIL.currency.findConverter"
    );

    fromCurrencyId = fromCurrencyId.toString();
    toCurrencyId = toCurrencyId.toString();

    return _.find(this.conversions, function(c) {
      return (
        c.fromCurrencyId === fromCurrencyId && c.toCurrencyId === toCurrencyId
      );
    });
  },
  /**
   * Returns a converter to operate with amounts that will be converted from fromCurrencyId to toCurrencyId
   * @param  {currencyId} fromCurrencyId the original currencyId
   * @param  {currencyId} toCurrencyId   the destination currencyId
   * @return {converter}                 the converter to convert amounts from the fromCurrencyId currency to the toCurrencyId currency
   */
  getConverter: function(fromCurrencyId, toCurrencyId) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      fromCurrencyId,
      "Missing required argument 'fromCurrencyId' in OB.UTIL.currency.getConverter"
    );
    OB.UTIL.Debug.isDefined(
      toCurrencyId,
      "Missing required argument 'toCurrencyId' in OB.UTIL.currency.getConverter"
    );

    fromCurrencyId = fromCurrencyId.toString();
    toCurrencyId = toCurrencyId.toString();

    var found = this.findConverter(fromCurrencyId, toCurrencyId);
    if (!found) {
      OB.error(
        'Currency converter not added: ' +
          fromCurrencyId +
          ' -> ' +
          toCurrencyId
      );
    }
    return found;
  },
  /**
   * Returns a converter whose original currency is not the WebPOS currency. e.g: USD in sampledata
   * and whose destiny curency is the WebPOS default currency. i.e: OB.MobileApp.model.get('currency').id
    return this.getConverter(webPOSDefaultCurrencyId(), toCurrencyId);
   * @param  {currencyId} fromCurrencyId  the currencyId of the original currency
   * @return {converter}                  the converter to convert amounts from fromCurrencyId to the WebPOS default currency
   */
  getToLocalConverter: function(fromCurrencyId) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      fromCurrencyId,
      "Missing required argument 'fromCurrencyId' in OB.UTIL.currency.getToLocalConverter"
    );

    fromCurrencyId = fromCurrencyId.toString();

    return this.getConverter(fromCurrencyId, this.webPOSDefaultCurrencyId());
  },
  /**
   * Returns a converter whose destiny currency is not the WebPOS currency. e.g: USD in sampledata
   * and whose original curency is the WebPOS default currency. i.e: OB.MobileApp.model.get('currency').id
   * @param  {currencyId} toCurrencyId  the currencyId of the destiny currency
   * @return {converter}                the converter to convert amounts from WebPOS default currency to toCurrencyId
   */
  getFromLocalConverter: function(toCurrencyId) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      toCurrencyId,
      "Missing required argument 'toCurrencyId' in OB.UTIL.currency.getFromLocalConverter"
    );

    toCurrencyId = toCurrencyId.toString();

    return this.getConverter(this.webPOSDefaultCurrencyId(), toCurrencyId);
  },
  /**
   * converts an amount to the WebPOS amount currency
   * @param  {currencyId} fromCurrencyId    the currencyId of the amount to be converted
   * @param  {float}      amount            the amount to be converted
   * @return {float}                        the converted amount
   */
  toDefaultCurrency: function(fromCurrencyId, amount) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      fromCurrencyId,
      "Missing required argument 'fromCurrencyId' in OB.UTIL.currency.toDefaultCurrency"
    );
    OB.UTIL.Debug.isDefined(
      amount,
      "Missing required argument 'amount' in OB.UTIL.currency.toDefaultCurrency"
    );

    fromCurrencyId = fromCurrencyId.toString();

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
  toForeignCurrency: function(toCurrencyId, amount) {
    // argument checks
    OB.UTIL.Debug.isDefined(
      toCurrencyId,
      "Missing required argument 'toCurrencyId' in OB.UTIL.currency.toForeignCurrency"
    );
    OB.UTIL.Debug.isDefined(
      amount,
      "Missing required argument 'amount' in OB.UTIL.currency.toForeignCurrency"
    );

    toCurrencyId = toCurrencyId.toString();

    if (toCurrencyId === this.webPOSDefaultCurrencyId()) {
      return amount;
    }
    var converter = this.getFromLocalConverter(toCurrencyId);
    var foreignAmount = converter.getTangibleOf(amount);
    return foreignAmount;
  }
};

// Experimental method that could be introduced in ECMAScript 6. If this happens, this method should be removed and the calling methods should replace it with 'Math.sign'
// As of now, Nov 2014, Math.sign is supported by chrome v38 but not by Safari
OB.UTIL.Math = window.OB.UTIL.Math || {};
OB.UTIL.Math.sign = function(x) {
  x = +x; // convert to a number
  if (x === 0 || isNaN(x)) {
    return x;
  }
  return x > 0 ? 1 : -1;
};

OB.UTIL.getPriceListName = function(priceListId, callback) {
  if (priceListId) {
    if (OB.MobileApp.model.get('pricelist').id === priceListId) {
      callback(OB.MobileApp.model.get('pricelist').name);
    } else {
      OB.Dal.findUsingCache(
        'PriceListName',
        OB.Model.PriceList,
        { m_pricelist_id: priceListId },
        function(pList) {
          if (pList.length > 0) {
            callback(pList.at(0).get('name'));
          } else {
            callback();
          }
        },
        function() {
          callback();
        },
        { modelsAffectedByCache: ['PriceList'] }
      );
    }
  } else {
    callback('');
  }
};

/**
 * Generic approval checker. It validates user/password can approve the approvalType.
 * It can work online in case that user has done at least once the same approvalType
 * in this same browser. Data regarding privileged users is stored in supervisor table
 */
OB.UTIL.checkApproval = function(
  approvalType,
  username,
  password,
  callback,
  windowModel,
  attrs
) {
  OB.Dal.initCache(OB.Model.Supervisor, [], null, null);
  var approvalList = [];
  approvalType.forEach(function(approvalType) {
    approvalList.push(
      typeof approvalType === 'object' ? approvalType.approval : approvalType
    );
  });
  var execution = OB.UTIL.ProcessController.start('checkApproval');

  var rr,
    checkApprovalRequest = new enyo.Ajax({
      url: '../../org.openbravo.retail.posterminal.utility.CheckApproval',
      cacheBust: false,
      method: 'GET',
      handleAs: 'json',
      timeout: 20000,
      data: {
        terminal: OB.MobileApp.model.get('terminalName'),
        user: username,
        password: password,
        approvalType: JSON.stringify(approvalList),
        attributes: JSON.stringify(attrs)
      },
      contentType: 'application/json;charset=utf-8',
      success: function(inSender, inResponse) {
        OB.UTIL.ProcessController.finish('checkApproval', execution);
        var approved = false;
        if (inResponse.error) {
          callback(false, null, null, true, inResponse.error.message);
        } else {
          approved = inResponse.data.canApprove;
          if (!approved) {
            callback(
              false,
              null,
              null,
              false,
              OB.I18N.getLabel('OBPOS_UserCannotApprove', [username])
            );
          }
          // saving supervisor in local so next time it is possible to approve offline
          OB.Dal.find(
            OB.Model.Supervisor,
            {
              id: inResponse.data.userId
            },
            enyo.bind(this, function(users) {
              var supervisor,
                permissions = [];
              if (users.models.length === 0) {
                // new user
                if (inResponse.data.canApprove) {
                  // insert in local db only in case it is supervisor for current type
                  supervisor = new OB.Model.Supervisor();
                  supervisor.set('id', inResponse.data.userId);
                  supervisor.set('name', username);
                  OB.Model.PasswordHash.updatePasswordIfNeeded(
                    supervisor,
                    password
                  );
                  supervisor.set('created', new Date().toString());
                  // Set all permissions
                  if (inResponse.data.preference) {
                    _.each(
                      inResponse.data.preference,
                      function(perm) {
                        permissions.push(perm);
                      },
                      this
                    );
                    supervisor.set('permissions', JSON.stringify(permissions));
                  } else {
                    supervisor.set('permissions', JSON.stringify(approvalType));
                  }
                  OB.Dal.save(supervisor, null, null, true);
                }
              } else {
                // update existent user granting or revoking permission
                supervisor = users.models[0];
                OB.Model.PasswordHash.updatePasswordIfNeeded(
                  supervisor,
                  password
                );
                if (supervisor.get('permissions')) {
                  permissions = JSON.parse(supervisor.get('permissions'));
                }
                if (inResponse.data.canApprove) {
                  // grant permission if it does not exist
                  _.each(
                    approvalType,
                    function(perm) {
                      if (!_.contains(permissions, perm)) {
                        permissions.push(perm);
                      }
                    },
                    this
                  );
                } else {
                  // revoke permission if it exists
                  _.each(
                    approvalType,
                    function(perm) {
                      if (_.contains(permissions, perm)) {
                        permissions = _.without(permissions, perm);
                      }
                    },
                    this
                  );
                }
                supervisor.set('permissions', JSON.stringify(permissions));
                OB.Dal.save(supervisor);
              }
              callback(approved, supervisor, approvalType, true, null);
            })
          );
        }
      },
      fail: function(inSender, inResponse) {
        // offline
        OB.UTIL.ProcessController.finish('checkApproval', execution);
        OB.Dal.find(
          OB.Model.Supervisor,
          {
            name: username
          },
          enyo.bind(this, function(users) {
            var supervisor,
              countApprovals = 0,
              approved = false;
            if (users.models.length === 0) {
              countApprovals = 0;
              OB.Dal.find(
                OB.Model.User,
                null,
                enyo.bind(this, function(users) {
                  _.each(users.models, function(user) {
                    if (
                      username === user.get('name') &&
                      OB.Model.PasswordHash.checkPassword(user, password)
                    ) {
                      _.each(
                        approvalType,
                        function(perm) {
                          var approvalToCheck =
                            typeof perm === 'object' ? perm.approval : perm;
                          if (
                            JSON.parse(user.get('terminalinfo')).permissions[
                              approvalToCheck
                            ]
                          ) {
                            countApprovals += 1;
                            supervisor = user;
                          }
                        },
                        this
                      );
                    }
                  });
                  if (countApprovals === approvalType.length) {
                    approved = true;
                    callback(approved, supervisor, approvalType, true, null);
                  } else {
                    callback(
                      false,
                      null,
                      null,
                      false,
                      OB.I18N.getLabel('OBPOS_UserCannotApprove', [username])
                    );
                  }
                }),
                function() {}
              );
            } else {
              supervisor = users.models[0];
              if (OB.Model.PasswordHash.checkPassword(supervisor, password)) {
                _.each(
                  approvalType,
                  function(perm) {
                    var approvalToCheck =
                      typeof perm === 'object' ? perm.approval : perm;
                    if (
                      _.contains(
                        JSON.parse(supervisor.get('permissions')),
                        approvalToCheck
                      )
                    ) {
                      countApprovals += 1;
                    }
                  },
                  this
                );
                if (countApprovals === approvalType.length) {
                  approved = true;
                  callback(approved, supervisor, approvalType, true, null);
                } else {
                  countApprovals = 0;
                  OB.Dal.find(
                    OB.Model.User,
                    null,
                    enyo.bind(this, function(users) {
                      _.each(users.models, function(user) {
                        if (
                          username === user.get('name') &&
                          OB.Model.PasswordHash.checkPassword(user, password)
                        ) {
                          _.each(
                            approvalType,
                            function(perm) {
                              var approvalToCheck =
                                typeof perm === 'object' ? perm.approval : perm;
                              if (
                                JSON.parse(user.get('terminalinfo'))
                                  .permissions[approvalToCheck]
                              ) {
                                countApprovals += 1;
                                supervisor = user;
                              }
                            },
                            this
                          );
                        }
                      });
                      if (countApprovals === approvalType.length) {
                        approved = true;
                        callback(
                          approved,
                          supervisor,
                          approvalType,
                          true,
                          null
                        );
                      } else {
                        callback(
                          false,
                          null,
                          null,
                          false,
                          OB.I18N.getLabel('OBPOS_UserCannotApprove', [
                            username
                          ])
                        );
                      }
                    }),
                    function() {}
                  );
                }
              } else {
                callback(
                  false,
                  null,
                  null,
                  false,
                  OB.I18N.getLabel('OBPOS_InvalidUserPassword')
                );
              }
            }
          }),
          function() {}
        );
      }
    });

  rr = new OB.RR.Request({
    ajaxRequest: checkApprovalRequest
  });
  rr.exec(checkApprovalRequest.url);
};

OB.UTIL.setScanningFocus = function(focus) {
  OB.MobileApp.view.scanningFocus(focus);
};

OB.UTIL.clearFlagAndTimersRefreshMasterData = function() {
  OB.MobileApp.model.set('refreshMasterdataShowPopup', true);
  OB.MobileApp.model.set('refreshMasterdata', false);
};

OB.UTIL.checkRefreshMasterData = function() {
  // this code is called on:
  // - ticket close
  // - timeout of force refresh time
  // - window navigation
  if (OB.MobileApp.view.applicationLocked === true) {
    return;
  }
  if (
    OB.MobileApp.model.get('refreshMasterdata') === true &&
    OB.UTIL.refreshMasterDataGetProperty('allowedIncrementalRefresh')
  ) {
    if (
      OB.UTIL.backgroundMasterdataIsEnabled() &&
      !OB.UTIL.localStorage.getItem('neededForeGroundMasterDataRefresh')
    ) {
      // background
      // this is the save, the request was called in OB.UTIL.loadModelsIncFunc
      if (OB.UTIL.masterdataRefreshStatus !== 'background-request-finished') {
        OB.info(
          "Cannot start masterdata save because the espected status was 'background-request-finished', but it was: " +
            OB.UTIL.masterdataRefreshStatus
        );
      } else {
        OB.UTIL.clearFlagAndTimersRefreshMasterData();
        if (OB.DS.masterdataBackgroundModels.totalLength > 0) {
          OB.UTIL.refreshMasterDataInBackgroundSave();
        } else {
          OB.info('No updates in the masterdata.');
        }
      }
    } else {
      // foreground
      if (OB.UTIL.localStorage.getItem('neededForeGroundMasterDataRefresh')) {
        OB.info(
          'Detected that previous masterdata refresh in background has fail because execeeds the limit of data. Trying normal refresh.'
        );
      }
      if (
        OB.UTIL.masterdataRefreshStatus !== '' &&
        OB.UTIL.masterdataRefreshStatus !== 'background-request-finished'
      ) {
        OB.info(
          "Cannot start masterdata refresh in foreground because the espected status was '' or 'background-request-finished', but it was: " +
            OB.UTIL.masterdataRefreshStatus
        );
      } else {
        OB.UTIL.clearFlagAndTimersRefreshMasterData();
        OB.UTIL.refreshMasterDataForeground();
      }
    }
  }
};

OB.UTIL.checkRefreshMasterDataOnNavigate = function() {
  if (
    OB.MobileApp.model.get('refreshMasterdata') === true &&
    OB.UTIL.refreshMasterDataGetProperty('incrementalRefreshOnNavigate')
  ) {
    OB.UTIL.checkRefreshMasterData();
  }
};

OB.UTIL.refreshMasterDataInBackgroundRequest = function(callback) {
  if (
    OB.UTIL.masterdataRefreshStatus !== '' &&
    OB.UTIL.masterdataRefreshStatus !== 'background-request-finished'
  ) {
    OB.info(
      'Cannot start the masterdata requests because the expected status was "" or "background-request-finished" but it was: ' +
        OB.UTIL.masterdataRefreshStatus
    );
    callback();
    return;
  }
  OB.UTIL.masterdataRefreshStatus = 'background-request-started';
  OB.DS.masterdataBackgroundModels = {};
  OB.MobileApp.model.loadModels(
    null,
    true,
    function() {
      OB.UTIL.masterdataRefreshStatus = 'background-request-finished';
      callback();
    },
    'background-request'
  );
};

OB.UTIL.refreshMasterDataInBackgroundSave = function() {
  OB.UTIL.masterdataRefreshStatus = 'background-save-started';
  if (OB.UTIL.RfidController.isRfidConfigured()) {
    OB.UTIL.RfidController.disconnectRFIDDevice();
  }
  OB.UTIL.startLoadingSteps();
  OB.MobileApp.model.set('isLoggingIn', true);
  OB.UTIL.showLoading(true);
  OB.MobileApp.model.loadModels(
    null,
    true,
    function() {
      OB.Discounts.Pos.initCache(function() {
        OB.UTIL.masterdataRefreshStatus = '';
        OB.DS.masterdataBackgroundModels = {};
        OB.UTIL.showLoading(false);
        OB.MobileApp.view.scanningFocus(true);
        OB.MobileApp.model.set('isLoggingIn', false);
      });
    },
    'background-save'
  );
};

OB.UTIL.refreshMasterDataForeground = function() {
  OB.DS.masterdataBackgroundModels = {};
  OB.MobileApp.model.set('secondsToRefreshMasterdata', 3);
  var counterIntervalId = null;
  counterIntervalId = setInterval(function() {
    OB.MobileApp.model.set(
      'secondsToRefreshMasterdata',
      OB.MobileApp.model.get('secondsToRefreshMasterdata') - 1
    );
    if (OB.MobileApp.model.get('secondsToRefreshMasterdata') === 0) {
      OB.UTIL.masterdataRefreshStatus = 'foreground-started';
      OB.MobileApp.model.set('refreshMasterdataShowPopup', false);
      clearInterval(counterIntervalId);
      if (OB.UTIL.RfidController.isRfidConfigured()) {
        OB.UTIL.RfidController.disconnectRFIDDevice();
      }
      OB.UTIL.startLoadingSteps();
      OB.MobileApp.model.set('isLoggingIn', true);
      OB.UTIL.showLoading(true);
      OB.MobileApp.model.loadModels(null, true, function() {
        OB.UTIL.showLoading(false);
        OB.MobileApp.view.scanningFocus(true);
        OB.MobileApp.model.set('isLoggingIn', false);
        OB.UTIL.localStorage.removeItem('neededForeGroundMasterDataRefresh');
        OB.UTIL.masterdataRefreshStatus = '';
      });
    }
  }, 1000);

  OB.MobileApp.view.$.dialogsContainer
    .createComponent({
      kind: 'OB.UI.ModalAction',
      header: OB.I18N.getLabel('OBMOBC_MasterdataNeedsToBeRefreshed'),
      bodyContent: {
        content: OB.I18N.getLabel(
          'OBMOBC_MasterdataNeedsToBeRefreshedMessage',
          [OB.MobileApp.model.get('secondsToRefreshMasterdata')]
        )
      },
      bodyButtons: {
        kind: 'OB.UI.ModalDialogButton',
        content: OB.I18N.getLabel('OBMOBC_LblCancel'),
        tap: function() {
          OB.MobileApp.model.set('refreshMasterdataShowPopup', false);
          OB.MobileApp.model.off('change:secondsToRefreshMasterdata');
          clearInterval(counterIntervalId);
          this.doHideThisPopup();
        }
      },
      autoDismiss: false,
      hideCloseButton: true,
      executeOnShow: function() {
        var reloadPopup = this;
        OB.MobileApp.model.on('change:secondsToRefreshMasterdata', function() {
          reloadPopup.$.bodyContent.$.control.setContent(
            OB.I18N.getLabel('OBMOBC_MasterdataNeedsToBeRefreshedMessage', [
              OB.MobileApp.model.get('secondsToRefreshMasterdata')
            ])
          );
          if (OB.MobileApp.model.get('secondsToRefreshMasterdata') === 0) {
            reloadPopup.hide();
            OB.MobileApp.model.off('change:secondsToRefreshMasterdata');
          }
        });
      }
    })
    .show();

  OB.info(OB.I18N.getLabel('OBMOBC_MasterdataNeedsToBeRefreshed'));
  clearInterval(OB.MobileApp.model.get('refreshMasterdataIntervalHandler'));
  OB.MobileApp.model.set(
    'refreshMasterdataIntervalHandler',
    setInterval(
      OB.UTIL.loadModelsIncFunc,
      OB.MobileApp.model.get('refreshMasterdataInterval')
    )
  );
};

OB.UTIL.refreshMasterDataGetProperty = function(prop) {
  var currentWindow = _.find(OB.MobileApp.model.windows.models, function(win) {
    return win.get('route') === OB.MobileApp.view.currentWindow;
  });
  if (currentWindow) {
    var windowClass = currentWindow.get('windowClass');
    if (windowClass && typeof windowClass === 'function') {
      return windowClass.prototype[prop];
    }
  }
  return false;
};

OB.UTIL.loadModelsIncFunc = function() {
  if (OB.MobileApp.view.applicationLocked === true) {
    return;
  }
  var msg = OB.I18N.getLabel(
      OB.MobileApp.view.currentWindow === 'retail.pointofsale'
        ? 'OBPOS_MasterdataWillHappenOnCloseTicket'
        : 'OBPOS_MasterdataWillHappenOnReturnToWebPOS'
    ),
    refreshMasterData,
    minutesToShowRefreshDataInc = OB.MobileApp.model.get('terminal')
      .terminalType.minutesToShowRefreshDataInc,
    minShowIncRefresh = OB.UTIL.isNullOrUndefined(minutesToShowRefreshDataInc)
      ? undefined
      : minutesToShowRefreshDataInc * 60 * 1000;
  refreshMasterData = function() {
    if (
      !OB.UTIL.isNullOrUndefined(minShowIncRefresh) &&
      minShowIncRefresh >= 0
    ) {
      // Forced refresh by timeout, ticket close also calls to checkRefreshMasterData
      var noActivityTimeout = OB.MobileApp.model.get(
        'refreshMasterdataNoActivityTimeout'
      );
      if (OB.UTIL.isNullOrUndefined(noActivityTimeout)) {
        OB.MobileApp.model.set('refreshMasterdataNoActivityTimeout', true);
        setTimeout(function() {
          // Refresh Master Data
          OB.MobileApp.model.unset('refreshMasterdataNoActivityTimeout');
          OB.UTIL.checkRefreshMasterData();
        }, minShowIncRefresh);
      }
    }
  };
  if (OB.UTIL.isNullOrUndefined(minShowIncRefresh) || minShowIncRefresh > 0) {
    OB.info(msg);
    OB.UTIL.showWarning(msg);
  }
  OB.MobileApp.model.set('refreshMasterdata', true);
  if (
    OB.UTIL.backgroundMasterdataIsEnabled() &&
    !OB.UTIL.localStorage.getItem('neededForeGroundMasterDataRefresh')
  ) {
    OB.UTIL.refreshMasterDataInBackgroundRequest(refreshMasterData);
  } else {
    refreshMasterData();
  }
};

OB.UTIL.backgroundMasterdataIsEnabled = function() {
  if (OB.UTIL.backgroundMasterdataRefreshEnabled === undefined) {
    let preferenceValue = OB.MobileApp.model.hasPermission(
      'OBMOBC_BackgroundMasterdataMaxSize',
      true
    );
    if (
      !preferenceValue ||
      (preferenceValue && !isNaN(preferenceValue) && preferenceValue >= 1)
    ) {
      OB.UTIL.backgroundMasterdataRefreshEnabled = true;
    } else {
      OB.UTIL.backgroundMasterdataRefreshEnabled = false;
      if (isNaN(preferenceValue)) {
        OB.warn(
          'OBMOBC_BackgroundMasterdataMaxSize has a value not numeric: ' +
            preferenceValue
        );
      }
    }
  }
  return OB.UTIL.backgroundMasterdataRefreshEnabled;
};

OB.UTIL.getCalculatedPriceForService = function(
  line,
  product,
  relatedLines,
  serviceLineQty,
  callback,
  errorCallback
) {
  var amountBeforeDiscounts = 0,
    amountAfterDiscounts = 0,
    rangeAmountBeforeDiscounts = 0,
    rangeAmountAfterDiscounts = 0,
    relatedQuantity = 0,
    relatedLinesMap = {},
    execution,
    finishExecution = _.once(function() {
      OB.UTIL.ProcessController.finish('servicePriceCalculation', execution);
    });

  function genericError(errorCode) {
    errorCallback(line, errorCode);
    finishExecution();
  }

  function getPriceRuleVersion(
    product,
    relatedLine,
    totalRelatedAmount,
    callback,
    errorCallback
  ) {
    var relatedLineMap,
      relatedAmt,
      criteria = {},
      isUniqueQuantity = product.get('quantityRule') === 'UQ';
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      criteria.remoteFilters = [];
      criteria.remoteFilters.push({
        columns: ['product'],
        operator: 'equals',
        value: product.get('id'),
        isId: true
      });
      if (relatedLine) {
        relatedLineMap = relatedLinesMap[relatedLine.orderlineId];
        criteria.remoteFilters.push({
          columns: [],
          operator: 'filter',
          value: 'ServicePriceRuleVersion_RangeFilter',
          params: [
            isUniqueQuantity
              ? totalRelatedAmount
              : OB.DEC.div(relatedLineMap.linePrice, relatedLineMap.qty)
          ]
        });
        criteria.remoteFilters.push({
          columns: [],
          operator: 'filter',
          value: 'ServicePriceRuleVersion_DateFilter',
          params: [relatedLineMap.product, relatedLineMap.productCategory]
        });
      } else {
        criteria.remoteFilters.push({
          columns: [],
          operator: 'filter',
          value: 'ServicePriceRuleVersion_DateFilter',
          params: []
        });
      }
    } else {
      if (relatedLine) {
        relatedLineMap = relatedLinesMap[relatedLine.orderlineId];
        relatedAmt = OB.DEC.div(relatedLineMap.linePrice, relatedLineMap.qty);
        var includedProducts = product.get('includeProducts'),
          includedProductCategories = product.get('includeProductCategories');
        criteria._whereClause = "where product = '" + product.get('id');
        criteria._whereClause +=
          "' and validFromDate = (select max(validFromDate)" +
          ' from m_servicepricerule_version sprv ' +
          " where sprv.product = '" +
          product.get('id') +
          "'";
        criteria._whereClause += " and sprv.validFromDate <= date('now')";
        if (includedProducts === 'N') {
          criteria._whereClause +=
            " and (sprv.relatedProduct is null or sprv.relatedProduct = '" +
            relatedLineMap.product +
            "')";
        }
        if (includedProductCategories === 'N') {
          criteria._whereClause +=
            " and (sprv.relatedProductCategory is null or sprv.relatedProductCategory = '" +
            relatedLineMap.productCategory +
            "')";
        }
        criteria._whereClause += ')';
        if (isUniqueQuantity) {
          criteria._whereClause +=
            ' and (minimum is null or minimum <= ' +
            totalRelatedAmount +
            ') and (maximum is null or maximum >= ' +
            totalRelatedAmount +
            ')';
        } else {
          criteria._whereClause +=
            ' and (minimum is null or minimum <= ' +
            relatedAmt +
            ') and (maximum is null or maximum >= ' +
            relatedAmt +
            ')';
        }
        if (includedProducts === 'N') {
          criteria._whereClause +=
            " and (relatedProduct is null or relatedProduct = '" +
            relatedLineMap.product +
            "')";
        }
        if (includedProductCategories === 'N') {
          criteria._whereClause +=
            " and (relatedProductCategory is null or relatedProductCategory = '" +
            relatedLineMap.productCategory +
            "')";
        }
      } else {
        criteria._whereClause =
          "where product = '" +
          product.get('id') +
          "' and validFromDate <= date('now')";
      }
      criteria._orderByClause = 'validFromDate desc';
    }
    OB.Dal.find(
      OB.Model.ServicePriceRuleVersion,
      criteria,
      function(sprvs) {
        if (sprvs && sprvs.length > 0) {
          sprvs.comparator = function(a, b) {
            if (
              a.get('relatedProduct') ||
              (a.get('relatedProductCategory') && !b.get('relatedProduct')) ||
              (!a.get('relatedProductCategory') &&
                !b.get('relatedProductCategory') &&
                !b.get('relatedProduct'))
            ) {
              return -1;
            } else {
              return 1;
            }
          };
          sprvs.sort();
          callback(sprvs.at(0));
        } else {
          errorCallback('OBPOS_ErrorPriceRuleVersionNotFound');
        }
      },
      function() {
        errorCallback('OBPOS_ErrorGettingPriceRuleVersion');
      }
    );
  }

  function getPriceRule(servicePriceRuleVersion, callback, errorCallback) {
    OB.Dal.get(
      OB.Model.ServicePriceRule,
      servicePriceRuleVersion.get('servicePriceRule'),
      function(spr) {
        callback(spr);
      },
      function() {
        errorCallback('OBPOS_ErrorGettingPriceRule');
      },
      function() {
        errorCallback('OBPOS_ErrorGettingPriceRule');
      }
    );
  }

  function getPriceRuleRange(
    servicePriceRule,
    rangeAmountBeforeDiscounts,
    rangeAmountAfterDiscounts,
    callback,
    errorCallback
  ) {
    var rangeCriteria = {};
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      rangeCriteria.remoteFilters = [];
      rangeCriteria.remoteFilters.push({
        columns: ['servicepricerule'],
        operator: 'equals',
        value: servicePriceRule.get('id'),
        isId: true
      });
      rangeCriteria.remoteFilters.push({
        columns: [],
        operator: 'filter',
        value: 'ServicePriceRuleRange_AmountFilter',
        params: [
          servicePriceRule.get('afterdiscounts')
            ? rangeAmountAfterDiscounts
            : rangeAmountBeforeDiscounts
        ]
      });
    } else {
      rangeCriteria._whereClause =
        "where servicepricerule = '" +
        servicePriceRule.get('id') +
        "' and (( amountUpTo >= " +
        (servicePriceRule.get('afterdiscounts')
          ? rangeAmountAfterDiscounts
          : rangeAmountBeforeDiscounts) +
        ') or (amountUpTo is null))';
      rangeCriteria._orderByClause = 'amountUpTo is null, amountUpTo';
      rangeCriteria._limit = 1;
    }
    OB.Dal.find(
      OB.Model.ServicePriceRuleRange,
      rangeCriteria,
      function(sprr) {
        if (sprr && sprr.length > 0) {
          callback(sprr.at(0));
        } else {
          errorCallback('OBPOS_ErrorPriceRuleRangeNotFound');
        }
      },
      function() {
        errorCallback('OBPOS_ErrorGettingPriceRuleRange');
      }
    );
  }

  function calculatePercentageAmount(
    product,
    amount,
    percentage,
    partialPrice,
    callback
  ) {
    var oldprice = partialPrice ? 0 : product.get('listPrice'),
      newprice = OB.DEC.add(
        oldprice,
        OB.DEC.toBigDecimal(amount).multiply(
          OB.DEC.toBigDecimal(percentage).divide(
            new BigDecimal('100'),
            20,
            OB.DEC.getRoundingMode()
          )
        )
      );
    callback(newprice);
  }

  function calculateRangePriceAmount(
    product,
    range,
    partialPrice,
    callback,
    errorCallback
  ) {
    var priceCriteria = {};
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      priceCriteria.remoteFilters = [];
      priceCriteria.remoteFilters.push({
        columns: ['product'],
        operator: 'equals',
        value: product.get('id'),
        isId: true
      });
      priceCriteria.remoteFilters.push({
        columns: ['priceList'],
        operator: 'equals',
        value: range.get('priceList'),
        isId: true
      });
    } else {
      priceCriteria.product = product.get('id');
      priceCriteria.priceList = range.get('priceList');
    }
    OB.Dal.find(
      OB.Model.ServicePriceRuleRangePrices,
      priceCriteria,
      function(price) {
        var oldprice = partialPrice ? 0 : product.get('listPrice'),
          newprice;
        if (price && price.length > 0) {
          newprice = OB.Utilities.Number.roundJSNumber(
            OB.DEC.add(oldprice, price.at(0).get('listPrice')),
            2
          );
          callback(newprice);
        } else {
          errorCallback('OBPOS_ErrorPriceRuleRangePriceNotFound');
        }
      },
      function() {
        errorCallback('OBPOS_ErrorGettingPriceRuleRangePrice');
      }
    );
  }

  if (
    product.get('productType') === 'S' &&
    product.get('isPriceRuleBased') &&
    (!line || !line.get('originalOrderLineId'))
  ) {
    relatedLines.forEach(function(rl) {
      var partialAmtAfterDiscounts,
        partialAmtBeforeDiscounts,
        lineMap = {},
        l = OB.MobileApp.model.receipt.get('lines').get(rl.orderlineId);
      if (l) {
        relatedQuantity += l.get('qty');
        lineMap.qty = l.get('qty');
        lineMap.product = l.get('product').get('id');
        lineMap.productCategory = l.get('product').get('productCategory');
        lineMap.deferred = false;
      } else {
        relatedQuantity += rl.qty;
        lineMap.qty = rl.qty;
        lineMap.deferred = true;
        lineMap.product = rl.productId;
        lineMap.productCategory = rl.productCategory;
      }
      if (OB.MobileApp.model.receipt.get('priceIncludesTax')) {
        if (l) {
          partialAmtBeforeDiscounts = Math.abs(l.get('gross'));
          partialAmtAfterDiscounts = Math.abs(
            OB.DEC.sub(
              l.get('gross'),
              _.reduce(
                l.get('promotions'),
                function(memo, promo) {
                  return OB.DEC.add(memo, promo.amt);
                },
                0
              )
            )
          );
          amountBeforeDiscounts = OB.DEC.add(
            amountBeforeDiscounts,
            partialAmtBeforeDiscounts
          );
          amountAfterDiscounts = OB.DEC.add(
            amountAfterDiscounts,
            partialAmtAfterDiscounts
          );
          lineMap.linePriceBeforeDiscounts = partialAmtBeforeDiscounts;
          lineMap.linePrice = partialAmtAfterDiscounts;
          if (product.get('quantityRule') === 'PP') {
            partialAmtBeforeDiscounts = Math.abs(
              OB.DEC.div(l.get('gross'), l.get('qty'))
            );
            partialAmtAfterDiscounts = Math.abs(
              OB.DEC.div(
                OB.DEC.sub(
                  l.get('gross'),
                  _.reduce(
                    l.get('promotions'),
                    function(memo, promo) {
                      return OB.DEC.add(memo, promo.amt);
                    },
                    0
                  )
                ),
                l.get('qty')
              )
            );
            rangeAmountBeforeDiscounts = OB.DEC.add(
              rangeAmountBeforeDiscounts,
              partialAmtBeforeDiscounts
            );
            rangeAmountAfterDiscounts = OB.DEC.add(
              rangeAmountAfterDiscounts,
              partialAmtAfterDiscounts
            );
          }
        } else {
          partialAmtBeforeDiscounts = Math.abs(rl.gross);
          partialAmtAfterDiscounts = Math.abs(
            OB.DEC.sub(
              rl.gross,
              _.reduce(
                rl.promotions,
                function(memo, promo) {
                  return OB.DEC.add(memo, promo.amt);
                },
                0
              )
            )
          );
          amountBeforeDiscounts = OB.DEC.add(
            amountBeforeDiscounts,
            partialAmtBeforeDiscounts
          );
          amountAfterDiscounts = OB.DEC.add(
            amountAfterDiscounts,
            partialAmtAfterDiscounts
          );
          lineMap.linePriceBeforeDiscounts = partialAmtBeforeDiscounts;
          lineMap.linePrice = partialAmtAfterDiscounts;
          if (product.get('quantityRule') === 'PP') {
            partialAmtBeforeDiscounts = Math.abs(OB.DEC.div(rl.gross, rl.qty));
            partialAmtAfterDiscounts = Math.abs(
              OB.DEC.div(
                OB.DEC.sub(
                  rl.gross,
                  _.reduce(
                    rl.promotions,
                    function(memo, promo) {
                      return OB.DEC.add(memo, promo.amt);
                    },
                    0
                  )
                ),
                rl.qty
              )
            );
            rangeAmountBeforeDiscounts = OB.DEC.add(
              rangeAmountBeforeDiscounts,
              partialAmtBeforeDiscounts
            );
            rangeAmountAfterDiscounts = OB.DEC.add(
              rangeAmountAfterDiscounts,
              partialAmtAfterDiscounts
            );
          }
        }
      } else {
        if (l) {
          partialAmtBeforeDiscounts = Math.abs(l.get('net'));
          partialAmtAfterDiscounts = Math.abs(
            OB.DEC.sub(
              l.get('net'),
              _.reduce(
                l.get('promotions'),
                function(memo, promo) {
                  return memo + promo.amt;
                },
                0
              )
            )
          );
          amountBeforeDiscounts = OB.DEC.add(
            amountBeforeDiscounts,
            partialAmtBeforeDiscounts
          );
          amountAfterDiscounts = OB.DEC.add(
            amountAfterDiscounts,
            partialAmtAfterDiscounts
          );
          lineMap.linePriceBeforeDiscounts = partialAmtBeforeDiscounts;
          lineMap.linePrice = partialAmtAfterDiscounts;
          if (product.get('quantityRule') === 'PP') {
            partialAmtBeforeDiscounts = Math.abs(
              OB.DEC.div(l.get('net'), l.get('qty'))
            );
            partialAmtAfterDiscounts = Math.abs(
              OB.DEC.div(
                OB.DEC.sub(
                  l.get('net'),
                  _.reduce(
                    l.get('promotions'),
                    function(memo, promo) {
                      return OB.DEC.add(memo, promo.amt);
                    },
                    0
                  )
                ),
                l.get('qty')
              )
            );
            rangeAmountBeforeDiscounts = OB.DEC.add(
              rangeAmountBeforeDiscounts,
              partialAmtBeforeDiscounts
            );
            rangeAmountAfterDiscounts = OB.DEC.add(
              rangeAmountAfterDiscounts,
              partialAmtAfterDiscounts
            );
          }
        } else {
          partialAmtBeforeDiscounts = Math.abs(rl.net);
          partialAmtAfterDiscounts = Math.abs(
            OB.DEC.div(
              OB.DEC.sub(
                rl.net,
                _.reduce(
                  rl.promotions,
                  function(memo, promo) {
                    return OB.DEC.add(memo, promo.amt);
                  },
                  0
                )
              ),
              rl.qty
            )
          );
          amountBeforeDiscounts = OB.DEC.add(
            amountBeforeDiscounts,
            partialAmtBeforeDiscounts
          );
          amountAfterDiscounts = OB.DEC.add(
            amountAfterDiscounts,
            partialAmtAfterDiscounts
          );
          lineMap.linePriceBeforeDiscounts = partialAmtBeforeDiscounts;
          lineMap.linePrice = partialAmtAfterDiscounts;
          if (product.get('quantityRule') === 'PP') {
            partialAmtBeforeDiscounts = Math.abs(OB.DEC.div(rl.net, rl.qty));
            partialAmtAfterDiscounts = Math.abs(
              OB.DEC.div(
                OB.DEC.sub(
                  rl.net,
                  _.reduce(
                    rl.promotions,
                    function(memo, promo) {
                      return OB.DEC.add(memo, promo.amt);
                    },
                    0
                  )
                ),
                rl.qty
              )
            );
            rangeAmountBeforeDiscounts = OB.DEC.add(
              rangeAmountBeforeDiscounts,
              partialAmtBeforeDiscounts
            );
            rangeAmountAfterDiscounts = OB.DEC.add(
              rangeAmountAfterDiscounts,
              partialAmtAfterDiscounts
            );
          }
        }
      }
      relatedLinesMap[rl.orderlineId] = lineMap;
    });

    serviceLineQty = Math.abs(serviceLineQty);
    if (product.get('quantityRule') === 'UQ') {
      rangeAmountBeforeDiscounts = amountBeforeDiscounts;
      rangeAmountAfterDiscounts = amountAfterDiscounts;
    }
    var aggregatedNewPrice = 0,
      finalCallback = _.after(relatedLines.length, function() {
        if (product.get('quantityRule') === 'PP') {
          callback(
            line,
            OB.Utilities.Number.roundJSNumber(
              OB.DEC.add(
                OB.DEC.div(aggregatedNewPrice, relatedQuantity),
                product.get('listPrice')
              ),
              2
            )
          );
        } else {
          callback(
            line,
            OB.Utilities.Number.roundJSNumber(
              OB.DEC.add(aggregatedNewPrice, product.get('listPrice')),
              2
            )
          );
        }
        finishExecution();
      });
    execution = OB.UTIL.ProcessController.start('servicePriceCalculation');
    relatedLines.forEach(function(rl) {
      var amountToCheck;
      getPriceRuleVersion(
        product,
        rl,
        amountAfterDiscounts,
        function(servicePriceRuleVersion) {
          if (line) {
            line.set(
              'serviceTrancheMaximum',
              servicePriceRuleVersion.get('maximum')
            );
            line.set(
              'serviceTrancheMinimum',
              servicePriceRuleVersion.get('minimum')
            );
          }
          getPriceRule(
            servicePriceRuleVersion,
            function(spr) {
              if (spr.get('ruletype') === 'P') {
                if (spr.get('afterdiscounts')) {
                  amountToCheck = relatedLinesMap[rl.orderlineId].linePrice;
                } else {
                  amountToCheck =
                    relatedLinesMap[rl.orderlineId].linePriceBeforeDiscounts;
                }
                calculatePercentageAmount(
                  product,
                  amountToCheck,
                  spr.get('percentage'),
                  true,
                  function(newprice) {
                    aggregatedNewPrice = OB.DEC.add(
                      aggregatedNewPrice,
                      newprice
                    );
                    finalCallback();
                  }
                );
              } else {
                //ruletype = 'R'
                getPriceRuleRange(
                  spr,
                  rangeAmountBeforeDiscounts,
                  rangeAmountAfterDiscounts,
                  function(range) {
                    if (range.get('ruleType') === 'P') {
                      if (spr.get('afterdiscounts')) {
                        amountToCheck =
                          relatedLinesMap[rl.orderlineId].linePrice;
                      } else {
                        amountToCheck =
                          relatedLinesMap[rl.orderlineId]
                            .linePriceBeforeDiscounts;
                      }
                      calculatePercentageAmount(
                        product,
                        amountToCheck,
                        range.get('percentage'),
                        true,
                        function(newprice) {
                          aggregatedNewPrice = OB.DEC.add(
                            aggregatedNewPrice,
                            newprice
                          );
                          finalCallback();
                        }
                      );
                    } else {
                      calculateRangePriceAmount(
                        product,
                        range,
                        true,
                        function(newprice) {
                          aggregatedNewPrice = OB.DEC.add(
                            aggregatedNewPrice,
                            OB.DEC.mul(
                              newprice,
                              relatedLinesMap[rl.orderlineId].qty
                            )
                          );
                          finalCallback();
                        },
                        genericError
                      );
                    }
                  },
                  genericError
                );
              }
            },
            genericError
          );
        },
        genericError
      );
    });
  }
};

OB.UTIL.hideStoreFilter = function(filterOptions) {
  _.each(
    filterOptions,
    function(prop) {
      if (prop.name === 'store') {
        prop.filter = OB.UTIL.isCrossStoreEnabled();
      }
    },
    this
  );
};

OB.UTIL.isCrossStoreEnabled = function() {
  return OB.MobileApp.model.attributes.store.length !== 0;
};

OB.UTIL.isCrossStoreReceipt = function(receipt) {
  return (
    OB.MobileApp.model.get('terminal').organization !==
    (receipt.organization ? receipt.organization : receipt.get('organization'))
  );
};

OB.UTIL.isCrossStoreProduct = function(product) {
  return product.get('crossStore');
};

OB.UTIL.isCrossStoreLine = function(line) {
  return (
    line instanceof OB.Model.OrderLine &&
    line.has('organization') &&
    OB.UTIL.isCrossStoreOrganization(line.get('organization'))
  );
};

OB.UTIL.isCrossStoreOrganization = function(organization) {
  return organization.id !== OB.MobileApp.model.get('terminal').organization;
};
