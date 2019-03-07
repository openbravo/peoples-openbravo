/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _ */

/**
 * OB.Model.Executor provides a mechanism to execute actions synchronously even each of
 * these actions are not synchronous. It is managed with two queues: one for events and
 * another one for actions. Each event has a series of actions to be executed synchronously,
 * when all actions in the event are finished, next event is started.
 */

OB.Model.Executor = Backbone.Model.extend({
  defaults: {
    executing: false
  },

  initialize: function () {
    var eventQueue = new Backbone.Collection();
    this.set('eventQueue', eventQueue);
    this.set('actionQueue', new Backbone.Collection());

    eventQueue.on('add', function (event) {
      if (!this.get('executing') && !event.get('avoidTrigger')) {
        // Adding an event to an empty queue, firing it
        this.nextEvent();
      }
      event.set('avoidTrigger', false);
    }, this);
  },
  removeGroup: function (groupId) {
    var evtQueue = this.get('eventQueue');
    evtQueue.where({
      groupId: groupId
    }).forEach(function (evt) {
      evtQueue.remove(evt);
    }, this);
    this.set('eventQueue', evtQueue);
  },
  addEvent: function (event, replaceExistent) {
    var evtQueue = this.get('eventQueue'),
        currentEvt, actionQueue, currentExecutionQueue;
    if (replaceExistent && evtQueue) {
      currentEvt = this.get('currentEvent');
      evtQueue.where({
        id: event.get('id')
      }).forEach(function (evt) {
        if (currentEvt === evt) {
          this.set('eventQueue');
          actionQueue.remove(actionQueue.models);
        }
        evtQueue.remove(evt);
        currentExecutionQueue = (this.get('exec') || 0) - 1;
        this.set('exec', currentExecutionQueue);
      }, this);
    }

    this.set('exec', (this.get('exec') || 0) + 1);
    event.on('finish', function () {
      var currentExecutionQueue = (this.get('exec') || 0) - 1;
      this.set('exec', currentExecutionQueue);
      OB.debug('event execution time', (new Date().getTime()) - event.get('start'), currentExecutionQueue);
      if (currentExecutionQueue === 0 && event.get('receipt')) {
        event.get('receipt').trigger('eventExecutionDone');
      }
    }, this);

    evtQueue.add(event);
  },
  preEvent: function () {
    // Logic to implement before the event is created
  },
  postEvent: function () {
    // Logic to implement after the event is created
  },
  nextEvent: function () {
    var evt = this.get('eventQueue').shift(),
        previousEvt = this.get('currentEvent');
    if (previousEvt) {
      previousEvt.trigger('finish');
      this.postEvent();
    }
    if (evt) {
      this.preEvent();
      this.set('executing', true);
      this.set('currentEvent', evt);
      evt.set('start', new Date().getTime());
      evt.on('actionsCreated', function () {
        this.preAction(evt);
        this.nextAction(evt);
      }, this);
      this.createActions(evt);
    } else {
      this.set('executing', false);
      this.set('currentEvent', null);
    }
  },

  preAction: function (event) {
    // actions executed before the actions for the event
  },

  nextAction: function (event) {
    var action = this.get('actionQueue').shift();
    if (action) {
      action.get('action').call(this, action.get('args'), event, action.get('promCandidates'));
    } else {
      // queue of action is empty
      this.postAction(event);
      this.nextEvent();
    }
  },

  postAction: function (event) {
    // actions executed after all actions for the event have been executed
  },

  createActions: function (event) {
    // To be implemented by subclasses. It should populate actionQueue with the
    // series of actions to be executed for this event. Note each of the actions
    // is in charge of synchronization by invoking nextAction method.
  }
});

OB.Model.DiscountsExecutor = OB.Model.Executor.extend({
  // parameters that will be used in the SQL to get promotions, in case this SQL is extended,
  // these parameters might be required to be extended too
  criteriaParams: ['date', 'bpId', 'bpId', 'bpId', 'bpId', 'productId', 'productId', 'categoryId', 'categoryId', 'productId', 'productId', 'priceListId', 'priceListId'],

  // defines the property each of the parameters in criteriaParams is translated to, in case of
  // different parameters than standard ones this should be extended
  paramsTranslation: {
    bpId: {
      model: 'receipt',
      property: 'bp'
    },
    productId: {
      model: 'line',
      property: 'product'
    },
    categoryId: {
      model: 'line',
      property: 'product.productCategory'
    },
    priceListId: {
      model: 'receipt',
      property: 'priceList'
    },
    date: {
      model: 'receipt',
      property: 'orderDate'
    }
  },

  convertParams: function (evt, line, receipt, pTrl) {
    var translatedParams = [];
    _.forEach(this.criteriaParams, function (param) {
      var paraTrl, model;

      paraTrl = pTrl[param];
      if (!paraTrl) {
        OB.error('Not found param to calculate discounts', param);
        return;
      }

      if (paraTrl.model === 'receipt') {
        model = receipt;
      } else if (paraTrl.model === 'line') {
        if (paraTrl.originalProperty) {
          paraTrl.property = paraTrl.originalProperty;
        }

        if (paraTrl.property.indexOf('.') > 0) {
          var path = paraTrl.property.split('.');
          model = line.get(path[0]);
          paraTrl.originalProperty = paraTrl.property;
          paraTrl.property = path[1];
        } else {
          model = line;
        }
      } else {
        model = evt.get(paraTrl.model);
      }

      if (param === 'date') {
        translatedParams.push(OB.Utilities.Date.JSToOB(new Date(), 'yyyy-MM-dd') + ' 00:00:00.000');
      } else {
        translatedParams.push(model.get(paraTrl.property).id ? model.get(paraTrl.property).id : model.get(paraTrl.property));
      }
    });
    return translatedParams;
  },

  createActions: function (evt) {
    var line = evt.get('line'),
        receipt = evt.get('receipt'),
        actionQueue = this.get('actionQueue'),
        me = this,
        criteria, whereClause = "WHERE ( " + OB.Model.Discounts.computeStandardFilter(receipt) // 
         + " AND M_OFFER_TYPE_ID NOT IN (" + OB.Model.Discounts.getManualPromotions() + ")" //
         + " AND ((EM_OBDISC_ROLE_SELECTION = 'Y' AND NOT EXISTS (SELECT 1 FROM OBDISC_OFFER_ROLE WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID " + " AND AD_ROLE_ID = '" + OB.MobileApp.model.get('context').role.id + "')) OR (EM_OBDISC_ROLE_SELECTION = 'N' " //
         + " AND EXISTS (SELECT 1 FROM OBDISC_OFFER_ROLE WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID " + " AND AD_ROLE_ID = '" + OB.MobileApp.model.get('context').role.id + "')))" //
         + " ) OR M_OFFER_TYPE_ID IN (" + OB.Model.Discounts.getAutoCalculatedPromotions() + ")";

    if (!receipt.shouldApplyPromotions() || line.get('product').get('ignorePromotions')) {
      // Cannot apply promotions, leave actions empty
      evt.trigger('actionsCreated');
      return;
    }

    criteria = {
      '_whereClause': whereClause,
      '_orderByClause': 'priority is null, priority, _idx',
      params: this.convertParams(evt, line, receipt, this.paramsTranslation)
    };

    OB.Dal.findUsingCache('discountsCache', OB.Model.Discount, criteria, function (d) {
      OB.UTIL.HookManager.executeHooks('OBPOS_PreApplyAutomaticDiscount', {
        context: me,
        discountList: d
      }, function (args) {
        if (args.cancelation !== true) {
          // Set real _idx value for new list of promotions to apply and save the original _idx
          args.discountList.forEach(function (disc, index) {
            disc.set('orig_idx', disc.get('_idx'));
            disc.set('_idx', index);
          });
          line.set('promotionCandidates', []);
          args.discountList.forEach(function (disc) {
            line.get('promotionCandidates').push(disc.id);
          });
          args.discountList.forEach(function (disc) {
            actionQueue.add({
              action: me.applyRule,
              args: disc,
              promCandidates: args.discountList,
              avoidTrigger: true
            });
          });
        }
        evt.trigger('actionsCreated');
      });
    }, function () {
      OB.error('Error getting promotions', arguments);
    }, {
      modelsAffectedByCache: ['BusinessPartner', 'Product', 'ProductCharacteristicValue', 'Discount', 'DiscountFilterBusinessPartner', 'PricingAdjustmentBusinessPartnerGroup', 'DiscountFilterProductCategory', 'DiscountFilterCharacteristic', 'DiscountFilterProduct', 'OfferPriceList']
    });
  },

  applyRule: function (disc, evt, promCandidates) {
    var receipt = evt.get('receipt'),
        line = evt.get('line'),
        rule = OB.Model.Discounts.discountRules[disc.get('discountType')],
        ds, ruleListener, autoCalculatedPromotions = OB.Model.Discounts.getAutoCalculatedPromotions();
    if (line.stopApplyingPromotions() && (autoCalculatedPromotions.indexOf(disc.get('discountType')) === -1)) {
      this.nextAction(evt);
      return;
    }

    if (rule && rule.implementation) {
      if (rule.async) {
        // waiting listener to trigger completed to move to next action
        ruleListener = new Backbone.Model();
        ruleListener.on('completed', function (obj) {
          if (obj && obj.alerts) {
            // in the new flow discount, the messages are stored in array, so only will be displayed the first time
            var localArrayMessages = line.get('promotionMessages') || [];
            localArrayMessages.push(obj.alerts);
            line.set('promotionMessages', localArrayMessages);
          }
          ruleListener.off('completed');
          this.nextAction(evt);
        }, this);
      }
      ds = rule.implementation(disc, receipt, line, ruleListener, line.get('promotionCandidates'));
      if (ds && ds.alerts) {
        // in the new flow discount, the messages are stored in array, so only will be displayed the first time
        var localArrayMessages = line.get('promotionMessages') || [];
        localArrayMessages.push(ds.alerts);
        line.set('promotionMessages', localArrayMessages);
      }

      if (!rule.async) {
        // done, move to next action
        this.nextAction(evt);
      }
    } else {
      OB.warn('No POS implementation for discount ' + disc.get('discountType'));
      this.nextAction(evt);
    }
  },
  preEvent: function () {
    // Logic to implement before the event is created
  },
  postEvent: function () {
    // Logic to implement after the event is created
  },
  preAction: function (evt) {
    var line = evt.get('line'),
        originalManualPromotions = line.get('manualPromotions') || [],
        manualPromotions = [],
        order = evt.get('receipt'),
        beforeManualPromo = [],
        appliedPack, appliedPromotions;

    appliedPack = line.isAffectedByPack();
    if (appliedPack) {
      // we need to remove this pack from other lines in order to warranty consistency
      order.get('lines').forEach(function (l) {
        var promos = l.get('promotions'),
            newPromos = [];
        if (!promos) {
          return;
        }

        promos.forEach(function (p) {
          if (p.ruleId !== appliedPack.ruleId) {
            newPromos.push(p);
          }
        });

        l.set('promotions', newPromos);
      });
    }

    if (!line.get('originalOrderLineId')) {
      // Keep only manual discounts in promotions array of the line
      var keepManual = [],
          i;
      if (line.get('promotions')) {
        for (i = 0; i < line.get('promotions').length; i++) {
          if (line.get('promotions')[i].manual) {
            keepManual.push(line.get('promotions')[i]);
          }
        }
      }
      line.set('promotions', keepManual.length > 0 ? keepManual : null);
      line.set('discountedLinePrice', null);
    }

    appliedPromotions = line.get('promotions');
    if (appliedPromotions && appliedPromotions.length > 0) {
      _.forEach(originalManualPromotions, function (promotion) {
        if (appliedPromotions.indexOf(promotion) === -1) {
          manualPromotions.push(promotion);
        }
      });
    } else {
      manualPromotions = originalManualPromotions;
    }
    // Apply regular manual promotions
    beforeManualPromo = _.filter(manualPromotions, function (promo) {
      return !promo.obdiscApplyafter;
    });

    _.forEach(beforeManualPromo, function (promo) {
      promo.qtyOffer = undefined;
      var promotion = {
        rule: new Backbone.Model(promo),

        definition: {
          userAmt: promo.userAmt,
          applyNext: promo.applyNext,
          lastApplied: promo.lastApplied,
          discountinstance: promo.discountinstance
        },
        alreadyCalculated: true // to prevent loops
      };
      OB.Model.Discounts.addManualPromotion(order, [line], promotion);
    });
  },

  postAction: function (evt) {
    if (this.get('eventQueue').filter(function (p) {
      return p.get('receipt') === evt.get('receipt');
    }).length === 0) {
      var order = evt.get('receipt'),
          manualPromotions = [],
          afterManualPromo = [];
      _.each(order.get('lines').models, function (line) {
        manualPromotions = line.get('manualPromotions') || [];
        afterManualPromo = _.filter(manualPromotions, function (promo) {
          return promo.obdiscApplyafter;
        });
        _.forEach(afterManualPromo, function (promo) {
          promo.qtyOffer = undefined;
          var promotion = {
            rule: new Backbone.Model(promo),
            definition: {
              userAmt: promo.userAmt,
              applyNext: promo.applyNext,
              lastApplied: promo.lastApplied
            },
            alreadyCalculated: true
          };
          OB.Model.Discounts.addManualPromotion(order, [line], promotion);
        });
        line.set('manualPromotions', []);
      });
      evt.get('receipt').trigger('discountsApplied');
    }
    // Forcing local db save. Rule implementations could (should!) do modifications
    // without persisting them improving performance in this manner.
    if (!evt.get('skipSave') && evt.get('receipt') && evt.get('receipt').get('lines') && evt.get('receipt').get('lines').length > 0) {
      evt.get('receipt').save();
    }
  }
});