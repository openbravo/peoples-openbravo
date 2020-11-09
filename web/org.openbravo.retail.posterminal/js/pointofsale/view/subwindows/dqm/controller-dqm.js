/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function() {
  OB.DQMController = OB.DQMController || {};

  OB.DQMController.Suggest = 'SUGGEST';
  OB.DQMController.Validate = 'VALIDATE';
  OB.DQMController.OnSuggestSelected = 'ONSELECTED';

  OB.DQMController.providers = {};

  OB.DQMController.registerProvider = function(providerClass) {
    OB.DQMController.providers[
      providerClass.getImplementorSearchKey()
    ] = providerClass;
  };

  OB.DQMController.getProviderForField = function(property, type) {
    let selectedConfiguredProvider = OB.MobileApp.model
      .get('dataQualityProviders')
      .find(configuredProviders => {
        const provider =
          OB.DQMController.providers[configuredProviders.dataQualityProvider];
        if (type === OB.DQMController.Suggest) {
          return provider.getSuggestedFields().includes(property);
        } else if (type === OB.DQMController.Validate) {
          return provider.getValidatedFields().includes(property);
        } else if (type === OB.DQMController.OnSuggestSelected) {
          return provider.getOnSuggestSelectedFields().includes(property);
        }
      });
    if (selectedConfiguredProvider) {
      return OB.DQMController.providers[
        selectedConfiguredProvider.dataQualityProvider
      ];
    }
  };
})();
