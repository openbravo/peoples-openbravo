/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

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

OB.UTIL.getPriceList = async function(priceListId, callback) {
  if (priceListId) {
    if (OB.MobileApp.model.get('pricelist').id === priceListId) {
      callback(
        OB.Dal.transform(OB.Model.PriceList, {
          id: priceListId,
          name: OB.MobileApp.model.get('pricelist').name,
          priceIncludesTax: OB.MobileApp.model.get('pricelist')
            .priceIncludesTax,
          c_currency_id: OB.MobileApp.model.get('pricelist').currency
        })
      );
    } else {
      try {
        const priceList = await OB.App.MasterdataModels.PriceList.withId(
          priceListId
        );
        if (priceList) {
          callback(OB.Dal.transform(OB.Model.PriceList, priceList));
        } else {
          callback();
        }
      } catch (error) {
        OB.UTIL.showError(error);
        callback();
      }
    }
  } else {
    callback();
  }
};

/**
 * Generic approval checker. It validates user/password can approve the approvalType.
 * It can work offline in case that user has done at least once the same approvalType
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
      method: 'POST',
      handleAs: 'json',
      timeout: 20000,
      data: {
        terminal: OB.MobileApp.model.get('terminalName'),
        user: username,
        password: password,
        approvalType: JSON.stringify(approvalList),
        attributes: JSON.stringify(attrs)
      },
      success: async function(inSender, inResponse) {
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
          const supervisor = await OB.App.OfflineSession.upsertSupervisor(
            {
              id: inResponse.data.userId,
              name: username
            },
            password,
            approvalType,
            inResponse.data.approvableTypes
          );

          // TODO: using backbone model to keep compatibilty in callbacks
          supervisor.permissions = JSON.stringify(
            supervisor.approvePermissions
          );
          delete supervisor.approvePermissions;
          const backboneSupervisor = new OB.Model.Supervisor(supervisor);

          callback(approved, backboneSupervisor, approvalType, true, null);
        }
      },
      fail: async function(inSender, inResponse) {
        // offline
        OB.UTIL.ProcessController.finish('checkApproval', execution);

        const supervisor = await OB.App.OfflineSession.login(
          username,
          password
        );
        if (!supervisor) {
          callback(
            false,
            null,
            null,
            false,
            OB.I18N.getLabel('OBPOS_InvalidUserPassword')
          );
          return;
        }

        const approved = OB.App.OfflineSession.canApprove(
          supervisor,
          approvalType
        );

        if (approved) {
          // TODO: using backbone model to keep compatibilty in callbacks
          supervisor.permissions = JSON.stringify(
            supervisor.approvePermissions
          );
          delete supervisor.approvePermissions;
          const backboneSupervisor = new OB.Model.Supervisor(supervisor);

          callback(approved, backboneSupervisor, approvalType, true, null);
        } else {
          callback(
            false,
            null,
            null,
            false,
            OB.I18N.getLabel('OBPOS_UserCannotApprove', [username])
          );
        }
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
  if (OB.MobileApp.model.get('loggedOffline')) {
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
        if (
          (OB.DS.masterdataBackgroundModels.totalLength || 0) +
            OB.App.MasterdataController.modifiedMasterdataModels.length >
          0
        ) {
          OB.UTIL.refreshMasterDataInBackgroundSave();
        } else {
          OB.UTIL.masterdataRefreshStatus = '';
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
      OB.App.MasterdataController.requestIncrementalMasterdata().then(
        function() {
          OB.UTIL.masterdataRefreshStatus = 'background-request-finished';
          callback();
        }
      );
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
      OB.App.MasterdataController.putIncrementalMasterdata().then(function() {
        OB.Taxes.Pos.initCache(function() {
          OB.Discounts.Pos.initCache(function() {
            OB.UTIL.masterdataRefreshStatus = '';
            OB.DS.masterdataBackgroundModels = {};
            OB.UTIL.showLoading(false);
            OB.MobileApp.view.scanningFocus(true);
            OB.MobileApp.model.set('isLoggingIn', false);
          });
        });
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
        OB.App.MasterdataController.incrementalMasterdataRefresh().then(
          function() {
            OB.UTIL.showLoading(false);
            OB.MobileApp.view.scanningFocus(true);
            OB.MobileApp.model.set('isLoggingIn', false);
            OB.UTIL.localStorage.removeItem(
              'neededForeGroundMasterDataRefresh'
            );
            OB.UTIL.masterdataRefreshStatus = '';
          }
        );
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
  if (OB.MobileApp.model.get('loggedOffline')) {
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

  async function getPriceRuleVersionByValidDate(
    product,
    relatedLineMap,
    callback
  ) {
    let includedProducts = product.get('includeProducts'),
      includedProductCategories = product.get('includeProductCategories'),
      getColumnCriteria = function(column, value) {
        let columnCriteria = new OB.App.Class.Criteria().multiCriterion(
          [
            new OB.App.Class.Criterion(column, value),
            new OB.App.Class.Criterion(column, null, '')
          ],
          'or'
        );
        return columnCriteria;
      };
    const criteria = new OB.App.Class.Criteria()
      .criterion('product', product.get('id'))
      .criterion(
        'validFromDate',
        OB.I18N.normalizeDate(new Date()),
        'lowerOrEqualThan'
      )
      .orderBy('validFromDate', 'desc')
      .limit(1);
    if (includedProducts === 'N') {
      criteria.innerCriteria(
        getColumnCriteria('relatedProduct', relatedLineMap.product)
      );
    }
    if (includedProductCategories === 'N') {
      criteria.innerCriteria(
        getColumnCriteria(
          'relatedProductCategory',
          relatedLineMap.productCategory
        )
      );
    }
    try {
      const priceRuleVersion = await OB.App.MasterdataModels.ServicePriceRuleVersion.find(
        criteria.build()
      );
      if (priceRuleVersion && priceRuleVersion.length > 0) {
        callback(
          OB.Dal.transform(
            OB.Model.ServicePriceRuleVersion,
            priceRuleVersion[0]
          )
        );
      } else {
        callback(null);
      }
    } catch (error) {
      OB.error(error.message);
      callback(null);
    }
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
    } else {
      const criteria = new OB.App.Class.Criteria()
        .criterion('product', product.get('id'))
        .orderBy('validFromDate', 'desc');
      let getPriceRuleVersionData = async function() {
        try {
          const priceRuleVersion = await OB.App.MasterdataModels.ServicePriceRuleVersion.find(
            criteria.build()
          );
          if (priceRuleVersion && priceRuleVersion.length > 0) {
            priceRuleVersion.sort(function(a, b) {
              if (
                a.relatedProduct ||
                (a.relatedProductCategory && !b.relatedProduct) ||
                (!a.relatedProductCategory &&
                  !b.relatedProductCategory &&
                  !b.relatedProduct)
              ) {
                return -1;
              }
              return 1;
            });
            callback(
              OB.Dal.transform(
                OB.Model.ServicePriceRuleVersion,
                priceRuleVersion[0]
              )
            );
          } else {
            errorCallback('OBPOS_ErrorPriceRuleVersionNotFound');
          }
        } catch (error) {
          OB.error(error.message);
          errorCallback('OBPOS_ErrorGettingPriceRuleVersion');
        }
      };
      if (relatedLine) {
        relatedLineMap = relatedLinesMap[relatedLine.orderlineId];
        relatedAmt = OB.DEC.div(relatedLineMap.linePrice, relatedLineMap.qty);
        let includedProducts = product.get('includeProducts'),
          includedProductCategories = product.get('includeProductCategories'),
          getColumnCriteria = function(column, value) {
            let columnCriteria = new OB.App.Class.Criteria().multiCriterion(
              [
                new OB.App.Class.Criterion(column, value),
                new OB.App.Class.Criterion(column, null, '')
              ],
              'or'
            );
            return columnCriteria;
          };

        getPriceRuleVersionByValidDate(product, relatedLineMap, function(
          priceRuleVersionByValidDate
        ) {
          if (priceRuleVersionByValidDate) {
            criteria.criterion(
              'validFromDate',
              priceRuleVersionByValidDate.get('validFromDate')
            );
            let minCriteria = new OB.App.Class.Criteria().multiCriterion(
              [
                new OB.App.Class.Criterion('minimum', null),
                new OB.App.Class.Criterion(
                  'minimum',
                  isUniqueQuantity ? totalRelatedAmount : relatedAmt,
                  'lowerOrEqualThan'
                )
              ],
              'or'
            );
            let maxCriteria = new OB.App.Class.Criteria().multiCriterion(
              [
                new OB.App.Class.Criterion('maximum', null),
                new OB.App.Class.Criterion(
                  'maximum',
                  isUniqueQuantity ? totalRelatedAmount : relatedAmt,
                  'greaterOrEqualThan'
                )
              ],
              'or'
            );
            criteria.innerCriteria(minCriteria);
            criteria.innerCriteria(maxCriteria);

            if (includedProducts === false) {
              criteria.innerCriteria(
                getColumnCriteria('relatedProduct', relatedLineMap.product)
              );
            }
            if (includedProductCategories === false) {
              criteria.innerCriteria(
                getColumnCriteria(
                  'relatedProductCategory',
                  relatedLineMap.productCategory
                )
              );
            }
            getPriceRuleVersionData();
          } else {
            errorCallback('OBPOS_ErrorPriceRuleVersionNotFound');
            return;
          }
        });
      } else {
        criteria.criterion(
          'validFromDate',
          OB.I18N.normalizeDate(new Date()),
          'lowerOrEqualThan'
        );
        getPriceRuleVersionData();
      }
    }
  }

  async function getPriceRule(
    servicePriceRuleVersion,
    callback,
    errorCallback
  ) {
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
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
    } else {
      try {
        const priceRule = await OB.App.MasterdataModels.ServicePriceRule.withId(
          servicePriceRuleVersion.get('servicePriceRule')
        );
        if (priceRule) {
          callback(OB.Dal.transform(OB.Model.ServicePriceRule, priceRule));
        } else {
          errorCallback('OBPOS_ErrorGettingPriceRule');
        }
      } catch (error) {
        OB.error(error.message);
        errorCallback('OBPOS_ErrorGettingPriceRule');
      }
    }
  }

  async function getPriceRuleRange(
    servicePriceRule,
    rangeAmountBeforeDiscounts,
    rangeAmountAfterDiscounts,
    callback,
    errorCallback
  ) {
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      var rangeCriteria = {};
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
    } else {
      const criteria = new OB.App.Class.Criteria()
        .criterion('servicepricerule', servicePriceRule.get('id'))
        .orderBy('amountUpTo', 'asc')
        .limit(1);
      const amount = servicePriceRule.get('afterdiscounts')
        ? rangeAmountAfterDiscounts
        : rangeAmountBeforeDiscounts;
      const amountCriteria = new OB.App.Class.Criteria()
        .multiCriterion(
          [
            new OB.App.Class.Criterion(
              'amountUpTo',
              amount,
              'greaterOrEqualThan'
            ),
            new OB.App.Class.Criterion('amountUpTo', null, '')
          ],
          'or'
        )
        .operator('and');
      criteria.innerCriteria(amountCriteria);
      try {
        const priceRuleRange = await OB.App.MasterdataModels.ServicePriceRuleRange.find(
          criteria.build()
        );
        if (priceRuleRange && priceRuleRange.length === 1) {
          callback(
            OB.Dal.transform(OB.Model.ServicePriceRuleRange, priceRuleRange[0])
          );
        } else {
          errorCallback('OBPOS_ErrorPriceRuleRangeNotFound');
        }
      } catch (error) {
        OB.error(error.message);
        errorCallback('OBPOS_ErrorGettingPriceRuleRange');
      }
    }
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

  async function calculateRangePriceAmount(
    product,
    range,
    partialPrice,
    callback,
    errorCallback
  ) {
    var finalCallback = function(priceRuleRangePrice) {
      if (priceRuleRangePrice) {
        let oldPrice = partialPrice ? 0 : product.get('listPrice'),
          newPrice = OB.Utilities.Number.roundJSNumber(
            OB.DEC.add(oldPrice, priceRuleRangePrice.get('listPrice')),
            2
          );
        callback(newPrice);
      } else {
        errorCallback('OBPOS_ErrorPriceRuleRangePriceNotFound');
      }
    };
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
      var priceCriteria = {};
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
      OB.Dal.find(
        OB.Model.ServicePriceRuleRangePrices,
        priceCriteria,
        function(price) {
          if (price && price.length > 0) {
            finalCallback(price.at(0));
          }
        },
        function() {
          errorCallback('OBPOS_ErrorGettingPriceRuleRangePrice');
        }
      );
    } else {
      const criteria = new OB.App.Class.Criteria()
        .criterion('product', product.get('id'))
        .criterion('priceList', range.get('priceList'));
      try {
        const priceRuleRangePrice = await OB.App.MasterdataModels.ServicePriceRuleRangePrices.find(
          criteria.build()
        );
        if (priceRuleRangePrice && priceRuleRangePrice.length > 0) {
          finalCallback(
            OB.Dal.transform(
              OB.Model.ServicePriceRuleRangePrices,
              priceRuleRangePrice[0]
            )
          );
        } else {
          finalCallback();
        }
      } catch (error) {
        OB.error(error.message);
        errorCallback('OBPOS_ErrorGettingPriceRuleRangePrice');
      }
    }
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

OB.UTIL.getChangeLabelFromReceipt = function(receipt) {
  const getChangeLabelFromChangePayments = changePayments => {
    return changePayments
      .map(function(item) {
        return item.label;
      })
      .join(' + ');
  };

  const getChangeLabelFromPayments = payments => {
    return payments
      .filter(function(payment) {
        return (
          payment.get('paymentData') &&
          payment.get('paymentData').label &&
          payment.get('paymentData').key === payment.get('kind')
        );
      })
      .map(function(payment) {
        return payment.get('paymentData').label;
      })
      .join(' + ');
  };

  if (receipt.get('changePayments')) {
    return getChangeLabelFromChangePayments(receipt.get('changePayments'));
  } else {
    return getChangeLabelFromPayments(receipt.get('payments'));
  }
};
