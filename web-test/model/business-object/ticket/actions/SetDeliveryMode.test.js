/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SetDeliveryMode');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('Ticket.setDeliveryMode action', () => {
  test.each`
    ticket                                                                 | payload
    ${{ lines: [] }}                                                       | ${{}}
    ${{ lines: [] }}                                                       | ${{ obrdmDeliveryModeProperty: 'PickAndCarry' }}
    ${{ lines: [] }}                                                       | ${{ obrdmDeliveryModeProperty: 'PickupInStore' }}
    ${{ lines: [] }}                                                       | ${{ obrdmDeliveryModeProperty: 'PickupInStoreWithDate', obrdmDeliveryDateProperty: new Date(), obrdmDeliveryTimeProperty: new Date() }}
    ${{ lines: [] }}                                                       | ${{ obrdmDeliveryModeProperty: 'HomeDelivery', obrdmDeliveryDateProperty: new Date(), obrdmDeliveryTimeProperty: new Date() }}
    ${{ businessPartner: {}, lines: [{ product: {}, organization: {} }] }} | ${{}}
    ${{ businessPartner: {}, lines: [{ product: {}, organization: {} }] }} | ${{ obrdmDeliveryModeProperty: 'PickAndCarry' }}
    ${{ businessPartner: {}, lines: [{ product: {}, organization: {} }] }} | ${{ obrdmDeliveryModeProperty: 'PickupInStore' }}
    ${{ businessPartner: {}, lines: [{ product: {}, organization: {} }] }} | ${{ obrdmDeliveryModeProperty: 'PickupInStoreWithDate', obrdmDeliveryDateProperty: new Date(), obrdmDeliveryTimeProperty: new Date() }}
    ${{ businessPartner: {}, lines: [{ product: {}, organization: {} }] }} | ${{ obrdmDeliveryModeProperty: 'HomeDelivery', obrdmDeliveryDateProperty: new Date(), obrdmDeliveryTimeProperty: new Date() }}
  `('should set delivery mode', ({ ticket, payload }) => {
    const newTicket = OB.App.StateAPI.Ticket.setDeliveryMode(
      deepfreeze(ticket),
      deepfreeze(payload)
    );
    expect(newTicket).toMatchObject({
      ...ticket,
      ...payload,
      lines: ticket.lines.map(line => ({
        ...line,
        obrdmDeliveryMode: payload.obrdmDeliveryModeProperty || 'PickAndCarry',
        obrdmDeliveryDate: payload.obrdmDeliveryDateProperty,
        obrdmDeliveryTime: payload.obrdmDeliveryTimeProperty
      }))
    });
  });

  test.each`
    product                                                                                    | payload                                                                                                                 | result
    ${{}}                                                                                      | ${{}}                                                                                                                   | ${{ obrdmDeliveryMode: 'PickAndCarry', obrdmDeliveryDate: undefined, obrdmDeliveryTime: undefined }}
    ${{ obrdmDeliveryMode: 'HomeDelivery', obrdmDeliveryDate: 'LD', obrdmDeliveryTime: 'LT' }} | ${{}}                                                                                                                   | ${{ obrdmDeliveryMode: 'HomeDelivery', obrdmDeliveryDate: 'LD', obrdmDeliveryTime: 'LT' }}
    ${{}}                                                                                      | ${{ obrdmDeliveryModeProperty: 'PickupInStore' }}                                                                       | ${{ obrdmDeliveryMode: 'PickupInStore', obrdmDeliveryDate: undefined, obrdmDeliveryTime: undefined }}
    ${{}}                                                                                      | ${{ obrdmDeliveryModeProperty: 'PickupInStoreDate', obrdmDeliveryDateProperty: 'TD', obrdmDeliveryTimeProperty: 'TT' }} | ${{ obrdmDeliveryMode: 'PickupInStoreDate', obrdmDeliveryDate: 'TD', obrdmDeliveryTime: 'TT' }}
    ${{ obrdmDeliveryMode: 'HomeDelivery', obrdmDeliveryDate: 'LD', obrdmDeliveryTime: 'LT' }} | ${{ obrdmDeliveryModeProperty: 'PickupInStore' }}                                                                       | ${{ obrdmDeliveryMode: 'PickupInStore', obrdmDeliveryDate: undefined, obrdmDeliveryTime: undefined }}
    ${{ obrdmDeliveryMode: 'HomeDelivery', obrdmDeliveryDate: 'LD', obrdmDeliveryTime: 'LT' }} | ${{ obrdmDeliveryModeProperty: 'PickupInStoreDate', obrdmDeliveryDateProperty: 'TD', obrdmDeliveryTimeProperty: 'TT' }} | ${{ obrdmDeliveryMode: 'PickupInStoreDate', obrdmDeliveryDate: 'TD', obrdmDeliveryTime: 'TT' }}
  `('should override product delivery mode', ({ product, payload, result }) => {
    const newTicket = OB.App.StateAPI.Ticket.setDeliveryMode(
      deepfreeze({
        businessPartner: {},
        lines: [{ product, organization: {} }]
      }),
      deepfreeze(payload)
    );
    newTicket.lines.forEach(line => expect(line).toMatchObject(result));
  });

  test.each`
    payload                                               | result
    ${{}}                                                 | ${{ country: 'SC', region: 'SR' }}
    ${{ obrdmDeliveryModeProperty: 'PickAndCarry' }}      | ${{ country: 'SC', region: 'SR' }}
    ${{ obrdmDeliveryModeProperty: 'PickupInStore' }}     | ${{ country: 'SC', region: 'SR' }}
    ${{ obrdmDeliveryModeProperty: 'PickupInStoreDate' }} | ${{ country: 'SC', region: 'SR' }}
    ${{ obrdmDeliveryModeProperty: 'HomeDelivery' }}      | ${{ country: 'CC', region: 'CR' }}
  `('should add delivery location', ({ payload, result }) => {
    const newTicket = OB.App.StateAPI.Ticket.setDeliveryMode(
      deepfreeze({
        businessPartner: {
          shipLocId: 'C',
          locationModel: { countryId: 'CC', regionId: 'CR' }
        },
        lines: [{ product: {}, organization: { country: 'SC', region: 'SR' } }]
      }),
      deepfreeze(payload)
    );
    newTicket.lines.forEach(line => expect(line).toMatchObject(result));
  });
});
