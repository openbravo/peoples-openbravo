/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * OB.Model.Executor provides a mechanism to execute actions synchronously even each of
 * these actions are not synchronous.
 */
OB.Model.Executor = Backbone.Model.extend({
  defaults: {
    executing: false
  },

  initialize: function() {
    var eventQueue = new Backbone.Collection();
    this.set('eventQueue', eventQueue);
    this.set('actionQueue', new Backbone.Collection());

    eventQueue.on('add', function() {

      if (!this.get('executing')) {
        // Adding an event to an empty queue, firing it
        this.nextEvent();
      }
    }, this);
  },

  addEvent: function(event, replaceExistent) {
    var evtQueue = this.get('eventQueue'),
        currentEvt, actionQueue;
    console.log('add event', evtQueue.length, this.get('executing'))
    if (replaceExistent && evtQueue) {
      currentEvt = this.get('currentEvent');
      evtQueue.where({
        id: event.get('id')
      }).forEach(function(evt) {
        if (currentEvt === evt) {
          this.set('eventQueue')
          actionQueue.remove(actionQueue.models);
          console.log('remove actions')
        }
        evtQueue.remove(evt);
        console.log('remove evt')
      }, this);
    }
    // TODO: replace if exists
    evtQueue.add(event);
  },

  nextEvent: function() {
    var evt = this.get('eventQueue').shift();
    if (evt) {
      this.set('executing', true);
      this.set('currentEvent', evt);
      evt.on('actionsCreated', function() {
        this.preAction(evt);
        this.nextAction(evt);
      }, this);
      this.createActions(evt);
    } else {
      this.set('executing', false);
      this.set('currentEvent', null);
    }
  },

  preAction: function(event) {
    // actions executed before the actions for the event
  },

  nextAction: function(event) {
    var action = this.get('actionQueue').shift();
    if (action) {
      action.get('action').call(this, action.get('args'), event);
    } else {
      // queue of action is empty
      this.postAction(event);
      this.nextEvent();
    }
  },

  postAction: function(event) {
    // actions executed after all actions for the event have been executed
  },

  createActions: function(event) {
    // To be implemented by subclasses. It should populate actionQueue with the
    // series of actions to be executed for this event. Note each of the actions
    // is in charge of synchronization by invoking nextAction method.
  }
});

OB.Model.DiscountsExecutor = OB.Model.Executor.extend({
  createActions: function(evt) {
    var line = evt.get('line'),
        receipt = evt.get('receipt'),
        bpId = receipt.get('bp').id,
        productId = line.get('product').id,
        actionQueue = this.get('actionQueue'),
        me = this,
        criteria;

    if (!receipt.shouldApplyPromotions()) {
      // Cannot apply promotions, leave actions empty
      evt.trigger('actionsCreated');
      return;
    }

    criteria = {
      '_whereClause': OB.Model.Discounts.standardFilter,
      params: [bpId, bpId, bpId, bpId, productId, productId, productId, productId]
    };

    OB.Dal.find(OB.Model.Discount, criteria, function(d) {
      d.forEach(function(disc) {
        actionQueue.add({
          action: me.applyRule,
          args: disc
        });
      })
      evt.trigger('actionsCreated');
    }, function() {});
  },

  applyRule: function(disc, evt) {
    var receipt = evt.get('receipt'),
        line = evt.get('line'),
        alerts = [];

    var rule = OB.Model.Discounts.discountRules[disc.get('discountType')],
        ds, ruleListener;
    if (rule && rule.implementation) {
      console.log('applying rule', rule);
      if (rule.async) {
        // waiting listener to trigger completed to move to next action
        ruleListener = new Backbone.Model();
        ruleListener.on('completed', function() {
          console.log('async action completed');
          this.nextAction(evt);
        }, this);
      }
      ds = rule.implementation(disc, receipt, line, ruleListener);
      if (ds && ds.alerts) {
        OB.UTIL.showAlert.display(ds.alerts);
      }

      if (!rule.async) {
        // done, move to next action
        this.nextAction(evt);
      }
    } else {
      console.warn('No POS implementation for discount ' + disc.get('discountType'));
      this.nextAction(evt);
    }
  },

  preAction: function(evt) {
    var line = evt.get('line'),
        receipt = evt.get('receipt'),
        silent;

    silent = receipt.shouldApplyPromotions();
    line.set({
      promotions: null,
      discountedLinePrice: null,
      promotionCandidates: null
    }, {
      silent: silent
    });
  },

  postAction: function(evt) {
    evt.get('receipt').calculateGross();
  }
});