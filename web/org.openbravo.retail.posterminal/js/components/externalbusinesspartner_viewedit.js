/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo, moment */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsDate',
  kind: 'OB.UI.DatePicker',
  classes: 'obObposPointOfSaleUiModalExternalBusinessPartnerControlsDate',
  processesToListen: ['obpos_extbp_validateAll'],
  processFinished: function(process, processExecution) {
    if (process.get('searchkey') === 'obpos_extbp_validateAll') {
      if (
        processExecution.get('propsNotValidated') &&
        processExecution.get('propsNotValidated').length > 0
      ) {
        let validationResult = processExecution
          .get('propsNotValidated')
          .find(
            propNotValidated =>
              this.bpProperty.apiKey === propNotValidated.property.apiKey
          );
        if (validationResult) {
          this.formElement.setMessage(validationResult.message, true);
          this.dialog.scrollToNode(this.hasNode());
        }
      }
    }
  },
  getCanNullify: function() {
    if (this.bpProperty.mandatory || this.getValue() === '') {
      return false;
    }
    return true;
  },
  validated: function(processExecution) {
    //for date additional validation is required
    try {
      this.bpProperty.value = this.getValue();
      this.formElement.setMessage('', false);
    } catch (error) {
      this.inValidated(
        OB.I18N.getLabel('OBPOS_DateConversionError', [this.getValue()]),
        processExecution
      );
    }
    OB.App.ExternalBusinessPartnerAPI.onPropertyValueChange(
      this.bp,
      this.bpProperty
    )
      .then(retBp => {
        if (
          JSON.stringify(retBp.getPropertiesList()) !==
          JSON.stringify(this.bp.getPropertiesList())
        ) {
          this.bp = retBp;
          processExecution.reDraw = true;
          processExecution.newBp = retBp;
        }
      })
      .catch(errorObj => {})
      .finally(() => {
        OB.UTIL.ProcessController.finish(
          'obpos_extbp_propertychanged',
          processExecution
        );
      });
  },
  blur: function(inSender, inEvent) {
    if (
      this.bpProperty.mandatory &&
      (OB.UTIL.isNullOrUndefined(inSender.value) || inSender.value === '')
    ) {
      this.formElement.setMessage(
        OB.I18N.getLabel('OBMOBC_LblMandatoryField'),
        true
      );
    }
    return true;
  },
  inValidated: function(validationResultMessage, processExecution) {
    this.formElement.setMessage(validationResultMessage, true);
    OB.UTIL.ProcessController.finish(
      'obpos_extbp_propertychanged',
      processExecution
    );
  },
  setValue: function(value, opts) {
    let me = this;
    if (!opts || (opts && opts.initializingComponent !== true)) {
      const execution = OB.UTIL.ProcessController.start(
        'obpos_extbp_propertychanged'
      );
      if (this.validator) {
        this.validator.validate(
          this.bp,
          this.bpProperty.apiKey,
          value,
          function(validationResult) {
            if (validationResult.status) {
              me.validated(execution);
            } else {
              me.inValidated(validationResult.message, execution);
            }
          }
        );
      } else {
        //This setTimeout is to ensure that this.getValue will work in validated function
        setTimeout(
          () => {
            this.validated(execution);
          },
          0,
          this
        );
      }
    }
    this.inherited(arguments);
  },
  initComponents: function() {
    this.readOnly = !this.bpProperty.editable;
    this.setLocale(OB.Format.date.toUpperCase());
    this.validator = OB.DQMController.getProviderForField(
      this.bpProperty.apiKey,
      OB.DQMController.Validate
    );
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
    if (this.bpProperty && this.bpProperty.value) {
      if (this.bpProperty.value === '') {
        this.setValue(null, { initializingComponent: true });
      } else {
        if (_.isString(this.bpProperty.value)) {
          //set value in YYYY-MM-DD
          this.setValue(this.bpProperty.value, { initializingComponent: true });
        } else {
          this.setValue(
            moment(this.bpProperty.value).format(this.internalDateFormat),
            { initializingComponent: true }
          );
        }
      }
    } else {
      this.setValue(null, { initializingComponent: true });
    }
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  }
});
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsCheckbox',
  kind: 'OB.UI.FormElement.Checkbox',
  classes: 'obObposPointOfSaleUiModalExternalBusinessPartnerControlsCheckbox',
  setChecked: function(checked) {
    if (!this.isInitializing) {
      const processExecution = OB.UTIL.ProcessController.start(
        'obpos_extbp_propertychanged'
      );
      this.bpProperty.value = checked;
      OB.App.ExternalBusinessPartnerAPI.onPropertyValueChange(
        this.bp,
        this.bpProperty
      )
        .then(retBp => {
          if (
            JSON.stringify(retBp.getPropertiesList()) !==
            JSON.stringify(this.bp.getPropertiesList())
          ) {
            this.bp = retBp;
            processExecution.reDraw = true;
            processExecution.newBp = retBp;
          }
        })
        .catch(errorObj => {})
        .finally(() => {
          OB.UTIL.ProcessController.finish(
            'obpos_extbp_propertychanged',
            processExecution
          );
        });
    }
    this.inherited(arguments);
  },
  getChecked: function() {
    return this.bpProperty.value;
  },
  getDisplayedValue: function() {
    return this.bpProperty.value;
  },
  getCanNullify: function() {
    return false;
  },
  initComponents: function() {
    this.isInitializing = true;
    this.inherited(arguments);
    this.isInitializing = false;
    if (OB.UTIL.isNotEmptyString(this.bpProperty.cssClass)) {
      this.formElement.addClass(this.bpProperty.cssClass);
    }
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement.Select',
  name: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsCombo',
  classes: 'obObposPointOfSaleUiModalExternalBusinessPartnerControlsCombo',
  components: [],
  processesToListen: ['obpos_extbp_validateAll'],
  processFinished: function(process, processExecution) {
    if (process.get('searchkey') === 'obpos_extbp_validateAll') {
      if (
        processExecution.get('propsNotValidated') &&
        processExecution.get('propsNotValidated').length > 0
      ) {
        let validationResult = processExecution
          .get('propsNotValidated')
          .find(
            propNotValidated =>
              this.bpProperty.apiKey === propNotValidated.property.apiKey
          );
        if (validationResult) {
          this.formElement.setMessage(validationResult.message, true);
          this.dialog.scrollToNode(this.hasNode());
        }
      }
    }
  },
  getDisplayedValue: function() {
    if (this.getSelected() === -1) {
      return '';
    }
    if (this.bpProperty.value) {
      return this.bpProperty.value;
    } else {
      return '';
    }
  },
  getCanNullify: function() {
    if (this.getSelected() === -1) {
      return false;
    }
    if (!this.bpProperty.mandatory && this.getSelected() !== -1) {
      return true;
    }
    return false;
  },
  nullifyValue: function() {
    this.inherited(arguments);
    this.bpProperty.value = '';
    this.setSelected(-1);
    return true;
  },
  change: function(inSender, inEvent) {
    const processExecution = OB.UTIL.ProcessController.start(
      'obpos_extbp_propertychanged'
    );
    this.inherited(arguments);
    if (
      this.bpProperty.options &&
      this.bpProperty.options[this.getSelected()]
    ) {
      this.bpProperty.value = this.bpProperty.options[
        this.getSelected()
      ].searchKey;
      this.formElement.removeClass('obUiFormElement_isEmpty');
    }
    OB.App.ExternalBusinessPartnerAPI.onPropertyValueChange(
      this.bp,
      this.bpProperty
    )
      .then(retBp => {
        if (
          JSON.stringify(retBp.getPropertiesList()) !==
          JSON.stringify(this.bp.getPropertiesList())
        ) {
          this.bp = retBp;
          processExecution.reDraw = true;
          processExecution.newBp = retBp;
        }
      })
      .catch(errorObj => {})
      .finally(() => {
        OB.UTIL.ProcessController.finish(
          'obpos_extbp_propertychanged',
          processExecution
        );
      });
    return true;
  },
  initComponents: function() {
    let components = [];
    let index = 0;
    let selectedIndex = -1;
    this.bpProperty.options = this.bpProperty.options.sort(
      (opta, optb) => opta.sequenceNumber - optb.sequenceNumber
    );
    this.bpProperty.options.map(opt => {
      let optContent = opt.text;
      if (opt.istranslatable && opt.message) {
        optContent = OB.I18N.getLabel(opt.message$_identifier);
      }
      if (
        this.bpProperty.value &&
        this.bpProperty.value !== '' &&
        opt.searchKey === this.bpProperty.value.toString()
      ) {
        selectedIndex = index;
      } else if (
        (OB.UTIL.isNullOrUndefined(this.bpProperty.value) ||
          this.bpProperty.value === '') &&
        opt.default
      ) {
        selectedIndex = index;
        this.bpProperty.value = opt.searchKey;
      }
      components.push({
        kind: 'OB.UI.FormElement.Select.Option',
        value: opt.searchKey,
        content: optContent
      });
      index += 1;
    });

    if (selectedIndex === -1 && this.bpProperty.mandatory) {
      OB.warn(
        'Value ' +
          this.bpProperty.value +
          ' for combo ' +
          this.bpProperty.apiKey +
          ' does not match with any option'
      );
    }

    this.readOnly = !this.bpProperty.editable;
    this.createComponents(components);
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
    this.setSelected(selectedIndex);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement.Input',
  name: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsInput',
  classes: 'obObposPointOfSaleUiModalExternalBusinessPartnerControlsInput',
  type: 'Text',
  processesToListen: ['obpos_extbp_validateAll'],
  processFinished: function(process, processExecution) {
    if (process.get('searchkey') === 'obpos_extbp_validateAll') {
      if (
        processExecution.get('propsNotValidated') &&
        processExecution.get('propsNotValidated').length > 0
      ) {
        let validationResult = processExecution
          .get('propsNotValidated')
          .find(
            propNotValidated =>
              this.bpProperty.apiKey === propNotValidated.property.apiKey
          );
        if (validationResult) {
          this.formElement.setMessage(validationResult.message, true);
          this.dialog.scrollToNode(this.hasNode());
        }
      }
    }
  },
  getDisplayedValue: function() {
    return this.getValue();
  },
  validated: function(processExecution) {
    this.formElement.setMessage('', false);
    this.bpProperty.value = this.getValue();
    OB.App.ExternalBusinessPartnerAPI.onPropertyValueChange(
      this.bp,
      this.bpProperty
    )
      .then(retBp => {
        if (
          JSON.stringify(retBp.getPropertiesList()) !==
          JSON.stringify(this.bp.getPropertiesList())
        ) {
          this.bp = retBp;
          processExecution.reDraw = true;
          processExecution.newBp = retBp;
        }
      })
      .catch(errorObj => {})
      .finally(() => {
        OB.UTIL.ProcessController.finish(
          'obpos_extbp_propertychanged',
          processExecution
        );
      });
  },
  inValidated: function(validationResultMessage, processExecution) {
    this.formElement.setMessage(validationResultMessage, true);
    OB.UTIL.ProcessController.finish(
      'obpos_extbp_propertychanged',
      processExecution
    );
  },
  focus: function(inSender, inEvent) {
    this.onFocusValue = this.getValue();
  },
  blur: function() {
    var me = this;
    if (this.onFocusValue === this.getValue()) {
      return;
    }
    if (
      this.bpProperty.mandatory &&
      (OB.UTIL.isNullOrUndefined(this.getValue()) || this.getValue() === '')
    ) {
      this.formElement.setMessage(
        OB.I18N.getLabel('OBMOBC_LblMandatoryField'),
        true
      );
      return;
    }
    const execution = OB.UTIL.ProcessController.start(
      'obpos_extbp_propertychanged'
    );
    if (this.validator) {
      this.validator.validate(
        this.bp,
        this.bpProperty.apiKey,
        this.getValue(),
        function(validationResult) {
          if (validationResult.status) {
            me.validated(execution);
          } else {
            me.inValidated(validationResult.message, execution);
          }
        }
      );
    } else {
      this.validated(execution);
    }
    this.inherited(arguments);
  },
  nullifyValue: function() {
    this.inherited(arguments);
    this.bpProperty.value = '';
    return true;
  },
  initComponents: function() {
    this.readOnly = !this.bpProperty.editable;
    this.validator = OB.DQMController.getProviderForField(
      this.bpProperty.apiKey,
      OB.DQMController.Validate
    );
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
    this.setValue(this.bpProperty.value, { initializingComponent: true });
    if (OB.UTIL.isNotEmptyString(this.bpProperty.cssClass)) {
      this.addClass(this.bpProperty.cssClass);
    }
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement.Input',
  name:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsReadOnlyInput',
  classes:
    'obObposPointOfSaleUiModalExternalBusinessPartnerControlsReadOnlyInput',
  type: 'Text',
  readOnly: true,
  getDisplayedValue: function() {
    if (
      this.bpProperty.reference === 'D' &&
      moment.isDate(this.bpProperty.value)
    ) {
      return moment(this.bpProperty.value).format(OB.Format.date.toUpperCase());
    }
    return this.bpProperty.value;
  },
  nullifyValue: function() {
    return false;
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setValue(this.getDisplayedValue());
  }
});

enyo.kind({
  kind:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsReadOnlyInput',
  name:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsReadOnlyCombo',
  classes:
    'obObposPointOfSaleUiModalExternalBusinessPartnerControlsReadOnlyCombo',
  getDisplayedValue: function() {
    if (this.bpProperty.valueLabel) {
      return this.bpProperty.valueLabel;
    }
    let selectedOption = this.bpProperty.options.find(
      option =>
        option.searchKey ===
        (this.bpProperty.value ? this.bpProperty.value.toString() : null)
    );
    if (selectedOption) {
      if (selectedOption.istranslatable && selectedOption.message) {
        return OB.I18N.getLabel(selectedOption.message$_identifier);
      }
      return selectedOption.text;
    }
    return this.bpProperty.value;
  }
});

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditBodyLoading',
  classes:
    'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBodyLoading',
  nrOfDisplayedSections: 3,
  nrOfItemsPerSection: 3,
  totalNrOfDisplayedItems: 0,
  initComponents: function() {
    this.inherited(arguments);
    if (this.totalNrOfDisplayedItems) {
      this.nrOfItemsPerSection = Math.floor(
        this.totalNrOfDisplayedItems / this.nrOfDisplayedSections
      );
    }
    for (let index = 0; index < this.nrOfDisplayedSections; index++) {
      this.createComponent({
        kind: 'OB.UI.FormSection.LoadingSection',
        classes:
          'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBodyLoading_loadingSection',
        nrOfItemsPerSection: this.nrOfItemsPerSection
      });
    }
  }
});
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditBody',
  classes: 'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody',
  processesToListen: [
    'obpos_extbp_propertychanged',
    'obpos_extbp_saveExternalBp',
    'obpos_extbp_validateAll'
  ],
  components: [
    {
      name: 'customerAttributes',
      kind: 'Scroller',
      classes:
        'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-customerAttributes'
    },
    {
      name: 'scrim',
      classes:
        'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-scrim u-hideFromUI'
    }
  ],
  processStarted: function(process) {
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' ||
      process.get('searchkey') === 'obpos_extbp_saveExternalBp' ||
      process.get('searchkey') === 'obpos_extbp_validateAll'
    ) {
      this.$.scrim.removeClass('u-hideFromUI');
    }
  },
  processFinished: function(process, processExecution) {
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' &&
      processExecution.reDraw
    ) {
      this.drawBp(processExecution.newBp);
      this.dialog.configDataManager.bp = processExecution.newBp;
    }
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' ||
      process.get('searchkey') === 'obpos_extbp_saveExternalBp' ||
      process.get('searchkey') === 'obpos_extbp_validateAll'
    ) {
      this.$.scrim.addClass('u-hideFromUI');
    }
  },
  drawSection: function(argSectionKey) {
    this.$.customerAttributes.createComponent({
      name: 'section_' + argSectionKey,
      sectionKey: argSectionKey,
      classes:
        'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-customerAttributes-section section_' +
        argSectionKey,
      components: [
        {
          kind: 'OB.UI.FormSection.Label',
          name: 'sectionTitle_' + argSectionKey,
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-customerAttributes-section-title sectionTitle_' +
            argSectionKey,
          content: OB.I18N.getLabel(argSectionKey)
        },
        {
          name: 'sectionContainer_' + argSectionKey,
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-customerAttributes-section-container sectionContainer_' +
            argSectionKey
        }
      ],
      addItem: function(component) {
        let newComponent = this.owner.$[
          'sectionContainer_' + this.sectionKey
        ].createComponent(component);
        newComponent.handleFormElementStyle();
        return newComponent;
      }
    });
  },
  drawPropertyInSection: function(item, bp, sectionKey) {
    let itemAdaptedToFormElement = {};
    itemAdaptedToFormElement.dialog = this.dialog;
    itemAdaptedToFormElement.bp = bp;
    itemAdaptedToFormElement.bpProperty = item;
    itemAdaptedToFormElement.label = item.translatable
      ? OB.I18N.getLabel(item.message$_identifier)
      : item.text;
    if (this.mode === 'viewDetails') {
      itemAdaptedToFormElement.mandatory = false;
      itemAdaptedToFormElement.readOnly = true;
      if (
        item.reference === 'S' ||
        item.reference === 'N' ||
        item.reference === 'D'
      ) {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsReadOnlyInput';
      } else if (item.reference === 'C') {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsReadOnlyCombo';
      } else if (item.reference === 'B') {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsCheckbox';
      }
    } else if (this.mode === 'edit' || this.mode === 'insert') {
      itemAdaptedToFormElement.mandatory = item.mandatory;
      itemAdaptedToFormElement.readOnly = !item.editable;
      if (item.reference === 'S' || item.reference === 'N') {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsInput';
      } else if (item.reference === 'C') {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsCombo';
      } else if (item.reference === 'B') {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsCheckbox';
      } else if (item.reference === 'D') {
        itemAdaptedToFormElement.kind =
          'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerControlsDate';
      }
    }
    this.$.customerAttributes.$['section_' + sectionKey].addItem({
      kind: 'OB.UI.CustomerPropertyLine',
      classes:
        'obUiFormElement_dataEntry obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-property property_' +
        item.apiKey,
      coreElement: itemAdaptedToFormElement
    });
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  drawBp: function(newBp) {
    let reDrawn = false;
    if (this.$.customerAttributes.hasNode()) {
      this.$.customerAttributes.destroy();
      this.createComponent({
        name: 'customerAttributes',
        kind: 'Scroller',
        classes:
          'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditBody-customerAttributes'
      });
      reDrawn = true;
    }
    if (newBp) {
      this.bp = newBp;
    }
    this.bpPropertiesToDraw = this.bp.getPropertiesForDetail();
    let lastSectionI18NLabel = null;
    this.bpPropertiesToDraw.forEach(item => {
      if (item.section$_identifier) {
        if (
          lastSectionI18NLabel === null ||
          lastSectionI18NLabel !== item.section$_identifier
        ) {
          lastSectionI18NLabel = item.section$_identifier;
          this.drawSection(lastSectionI18NLabel);
        }
      }
      this.drawPropertyInSection(item, this.bp, lastSectionI18NLabel);
    }, this);
    if (reDrawn) {
      this.render();
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
    if (this.args && this.args.bp) {
      this.mode = this.args.mode;
      this.addClass(this.args.mode);
      this.bp = this.args.bp;
      this.drawBp();
    }
  }
});

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditFooter_viewDetails',
  classes:
    'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_viewDetails',
  events: {
    onHideThisPopup: '',
    onSwitchToEdit: ''
  },
  components: [
    {
      kind: 'OB.UI.ActionButtonArea',
      name: 'abaExternalBPViewDetails',
      abaIdentifier: 'obpos_pointofsale_externalbp_view',
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_viewDetails_actionButtonArea',
      showing: false
    },
    {
      name: 'standardFooter',
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_viewDetails_standardFooter',
      components: [
        {
          kind: 'OB.UI.ModalDialogButton',
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_viewDetails_standardFooter_edit',
          i18nLabel: 'OBPOS_LblEdit',
          ontap: 'doSwitchToEdit'
        },
        {
          kind: 'OB.UI.ModalDialogButton',
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_viewDetails_standardFooter_close',
          i18nLabel: 'OBMOBC_Close',
          isDefaultAction: true,
          isFirstFocus: true,
          ontap: 'doHideThisPopup'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.abaExternalBPViewDetails.dialog = this.dialog;
  }
});

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditFooter_edit',
  events: {
    onHideThisPopup: '',
    onSaveExternalBP: ''
  },
  processesToListen: [
    'obpos_extbp_propertychanged',
    'obpos_extbp_saveExternalBp'
  ],
  processStarted: function(process) {
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' ||
      process.get('searchkey') === 'obpos_extbp_saveExternalBp'
    ) {
      this.$.actionButton.setDisabled(true);
    }
  },
  processFinished: function(process, processExecution) {
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' ||
      process.get('searchkey') === 'obpos_extbp_saveExternalBp'
    ) {
      this.$.actionButton.setDisabled(false);
    }
  },
  components: [
    {
      kind: 'OB.UI.ActionButtonArea',
      name: 'abaExternalBPEdit',
      abaIdentifier: 'obpos_pointofsale_externalbp_edit',
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_abaExternalBPEdit',
      showing: false
    },
    {
      name: 'standardFooter',
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_edit_standardFooter',
      components: [
        {
          kind: 'OB.UI.ModalDialogButton',
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_standardFooter_closeButton',
          name: 'closeButton',
          i18nLabel: 'OBMOBC_Close',
          ontap: 'doHideThisPopup'
        },
        {
          kind: 'OB.UI.ModalDialogButton',
          name: 'actionButton',
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_edit_standardFooter_actionButton',
          i18nLabel: 'OBPOS_LblSave',
          ontap: 'doSaveExternalBP',
          isDefaultAction: true
        }
      ]
    }
  ],
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.abaExternalBPEdit.dialog = this.dialog;
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  }
});

enyo.kind({
  name:
    'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditFooter_insert',
  events: {
    onHideThisPopup: '',
    onSaveExternalBP: ''
  },
  processesToListen: [
    'obpos_extbp_propertychanged',
    'obpos_extbp_saveExternalBp'
  ],
  processStarted: function(process) {
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' ||
      process.get('searchkey') === 'obpos_extbp_saveExternalBp'
    ) {
      this.$.actionButton.setDisabled(true);
    }
  },
  processFinished: function(process, processExecution) {
    if (
      process.get('searchkey') === 'obpos_extbp_propertychanged' ||
      process.get('searchkey') === 'obpos_extbp_saveExternalBp'
    ) {
      this.$.actionButton.setDisabled(false);
    }
  },
  components: [
    {
      kind: 'OB.UI.ActionButtonArea',
      name: 'abaExternalBPInsert',
      abaIdentifier: 'obpos_pointofsale_externalbp_insert',
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_abaExternalBPInsert',
      showing: false
    },
    {
      name: 'standardFooter',
      classes:
        'obUiModal-footer-mainButtons obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_insert_standardFooter',
      components: [
        {
          kind: 'OB.UI.ModalDialogButton',
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_standardFooter_closeButton',
          name: 'closeButton',
          i18nLabel: 'OBMOBC_Close',
          ontap: 'doHideThisPopup'
        },
        {
          kind: 'OB.UI.ModalDialogButton',
          name: 'actionButton',
          classes:
            'obObposPointOfSaleUiModalExternalBusinessPartnerViewEditFooter_insert_standardFooter_actionButton',
          i18nLabel: 'OBPOS_LblSave',
          isDefaultAction: true,
          ontap: 'doSaveExternalBP'
        }
      ]
    }
  ],
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.abaExternalBPInsert.dialog = this.dialog;
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEdit',
  kind: 'OB.UI.Modal',
  classes:
    'obObPosPointOfSaleUiCustomersEditCustomer obUiModalExternalBusinessPartnerViewEdit',
  i18nHeader: '',
  handlers: {
    onSwitchToEdit: 'switchToEdit',
    onSaveExternalBP: 'saveExternalBP'
  },
  events: {
    onShowPopup: ''
  },
  body: {},
  footer: {},
  pressedButton: function() {
    this.pressedBtn = true;
    this.hide();
  },
  drawPreviewWhileLoading: function(popupClass) {
    this.setBody({
      kind:
        'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditBodyLoading',
      totalNrOfDisplayedItems: popupClass.getNumberOfPropertiesToBeDisplayedFromConfig(),
      nrOfDisplayedSections: 3
    });
  },
  drawHeader: function() {
    let bpIdentifier = '';
    if (
      this.configDataManager &&
      this.configDataManager.bp &&
      this.configDataManager.bp.getIdentifier
    ) {
      bpIdentifier = this.configDataManager.bp.getIdentifier();
    } else if (this.args.businessPartnerIdentifier) {
      bpIdentifier = this.args.businessPartnerIdentifier;
    }
    this.setHeader(bpIdentifier);
  },
  drawFooter: function(mode) {
    this.setFooter({
      kind:
        'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditFooter_' +
        mode,
      dialog: this
    });
  },
  drawBody: function() {
    if (
      this.args.mode === 'viewDetails' ||
      this.args.mode === 'edit' ||
      this.args.mode === 'insert'
    ) {
      this.setBody({
        kind: 'OB.OBPOSPointOfSale.UI.ModalExternalBusinessPartnerViewEditBody',
        args: { bp: this.configDataManager.bp, mode: this.args.mode },
        dialog: this
      });
    }
  },
  drawDialog: function(refreshBPFromApi) {
    let functNameToExecuteOnLoad =
      'on' +
      this.args.mode[0].toUpperCase() +
      this.args.mode.slice(1) +
      'BpViewLoad';
    this.drawHeader();
    this.drawFooter(this.args.mode);
    this.drawPreviewWhileLoading(this.configDataManager);
    this.render();
    if (refreshBPFromApi) {
      let functNameToExecuteFetch = 'fetchBusinessPartnerFromAPI';
      if (this.args.mode === 'insert') {
        functNameToExecuteFetch = 'fetchDefaultBusinessPartnerForNewFromAPI';
      }
      this.configDataManager[functNameToExecuteFetch](
        this.args.businessPartnerId
      )
        .then(bp => {
          OB.App.ExternalBusinessPartnerAPI[functNameToExecuteOnLoad](
            this.configDataManager.bp
          ).then(newBp => {
            this.configDataManager.bp = newBp;
            this.drawBody();
            this.render();
          });
        })
        .catch(error => {
          throw error;
        });
    } else {
      OB.App.ExternalBusinessPartnerAPI[functNameToExecuteOnLoad](
        this.configDataManager.bp
      ).then(newBp => {
        this.configDataManager.bp = newBp;
        this.drawBody();
        this.render();
      });
    }
  },
  switchToEdit: function(inSender, inEvent) {
    this.args.mode = 'edit';
    this.drawDialog(false);
    this.setDefaultActionButton();
    return true;
  },
  switchToView: function(inSender, inEvent) {
    this.args.mode = 'viewDetails';
    this.drawDialog(false);
    this.setDefaultActionButton();
    return true;
  },
  validateAll: function() {
    return this.configDataManager.bp.validate(OB.DQMController);
  },
  saveExternalBP: function(inSender, inEvent) {
    let executionValidate = OB.UTIL.ProcessController.start(
      'obpos_extbp_validateAll'
    );
    this.validateAll().then(propsNotValidated => {
      if (propsNotValidated.length > 0) {
        executionValidate.set('propsNotValidated', propsNotValidated);
        OB.UTIL.ProcessController.finish(
          'obpos_extbp_validateAll',
          executionValidate
        );
      } else {
        OB.UTIL.ProcessController.finish(
          'obpos_extbp_validateAll',
          executionValidate
        );
        const execution = OB.UTIL.ProcessController.start(
          'obpos_extbp_saveExternalBp'
        );
        let functionToExecute = null;
        if (this.args.mode === 'edit') {
          functionToExecute = this.configDataManager.updateBusinessPartnerIntoAPI.bind(
            this.configDataManager
          );
        } else {
          functionToExecute = this.configDataManager.insertBusinessPartnerIntoAPI.bind(
            this.configDataManager
          );
        }
        functionToExecute()
          .then(updatedBp => {
            this.modifyOrUpdateDone = true;
            this.modifyOrUpdateDoneIsNew =
              this.args.mode === 'insert' ? true : false;
            this.switchToView();
          })
          .catch(objError => {
            let reasonTitle = objError.reason
              ? objError.reason
              : OB.I18N.getLabel('OBPOS_BusinessPartnerUpdatedSavedErrorTitle');
            let reasonDetail = objError.reasonDetail
              ? objError.reasonDetail
              : OB.I18N.getLabel(
                  'OBPOS_BusinessPartnerUpdatedSavedErrorDetails'
                );
            objError;
            OB.UTIL.showConfirmation.display(reasonTitle, reasonDetail);
          })
          .finally(() => {
            OB.UTIL.ProcessController.finish(
              'obpos_extbp_saveExternalBp',
              execution
            );
          });
      }
    });
    return true;
  },
  executeOnShown: function() {
    setTimeout(
      () => {
        enyo.dispatcher.captureTarget = this;
      },
      0,
      this
    );
    return true;
  },
  executeOnShow: function() {
    this.modifyOrUpdateDone = false;
    this.modifyOrUpdateDoneIsNew = null;
    this.drawDialog(true);
    this.owner.model.set('externalBpOpenedData', this.configDataManager);
    this.owner.model.set('externalBpOpenedDialog', this);
    return true;
  },
  scrollToNode: function(domNode) {
    domNode.scrollIntoView();
  },
  executeOnHide: function() {
    this.doShowPopup({
      popup: this.args.navigationPath[this.args.navigationPath.length - 1],
      args: {
        target: this.args.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPop(
          this.args.navigationPath
        ),
        lastModifiedExtBp: this.modifyOrUpdateDone
          ? this.configDataManager.bp
          : null,
        lastModifiedExtBpIsNew: this.modifyOrUpdateDoneIsNew
      }
    });
    this.configDataManager.reset();
    this.owner.model.set('externalBpOpenedData', null);
    this.owner.model.set('externalBpOpenedDialog', null);
    return true;
  },
  initComponents: function() {
    this.configDataManager = new OB.App.Class.ExternalBusinessPartnerViewData();
    this.scrollToNode = _.debounce(this.scrollToNode, 500, true);
    this.inherited(arguments);
  }
});
