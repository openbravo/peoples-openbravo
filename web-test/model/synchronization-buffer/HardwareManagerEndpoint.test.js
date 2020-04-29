/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB = { debug: jest.fn() };
OB.App = {
  Class: {}
};
OB.POS = {
  hwserver: {}
};

require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/synchronization-buffer/SynchronizationBuffer');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/synchronization-buffer/SynchronizationEndpoint');
require('../../../web/org.openbravo.retail.posterminal/app/model/synchronization-buffer/HardwareManagerEndpoint');

describe('Harware Manager Synchronization Endpoint', () => {
  let syncBuffer;
  let hwManagerEndpoint;
  const hwManagerEndpointClass =
    OB.App.SynchronizationBuffer.endpoints[0].constructor;

  // initializes the sync buffer with just the hw manager synchronization endpoint
  const initSingleEndpointSyncBuffer = () => {
    syncBuffer = new OB.App.Class.SynchronizationBuffer();
    syncBuffer.registerEndpoint(hwManagerEndpointClass);
    hwManagerEndpoint = syncBuffer.endpoints[0];
  };

  beforeEach(() => {
    jest.resetAllMocks();
    // by default execute the tests with a unique synchronization endpoint
    initSingleEndpointSyncBuffer();
  });

  describe('online/offline endpoint status', () => {
    it('Correct initial endpoint status', async () => {
      hwManagerEndpoint.hardwareManagerStatus = jest
        .fn()
        .mockResolvedValue(true);
      expect(await hwManagerEndpoint.isOnline()).toBeTruthy();
    });

    it('The endpoint is offline if the HW Manager is not available', async () => {
      hwManagerEndpoint.hardwareManagerStatus = jest
        .fn()
        .mockRejectedValue(new Error('Printer and display are not available'));
      await syncBuffer.goOnline('HardwareManager');
      expect(await hwManagerEndpoint.isOnline()).toBeFalsy();
    });

    it('SynchronizationBuffer can switch the endpoint status', async () => {
      hwManagerEndpoint.hardwareManagerStatus = jest
        .fn()
        .mockResolvedValue(true);
      syncBuffer.goOffline('HardwareManager');
      expect(await hwManagerEndpoint.isOnline()).toBeFalsy();
      await syncBuffer.goOnline('HardwareManager');
      expect(await hwManagerEndpoint.isOnline()).toBeTruthy();
    });
  });
});
