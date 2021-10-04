/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global module*/

module.exports = [
  {
    id: 'CD0EA49B0941ADFD933081E34CF9BEB9',
    type: 'backend',
    modelName: 'OBPOS_CashUp',
    name: 'OBPOS_CashUp',
    service: 'org.openbravo.retail.posterminal.ProcessCashClose',
    time: 1593182307146,
    messageObj: {
      id: '83B5C6BE04037E1F85C92404F53433BD',
      terminal: 'VBS-1',
      cacheSessionId: 'DD9EBD1620664C8593FC1AD644F86821',
      data: [
        {
          id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
          netSales: 264.05,
          grossSales: 319.5,
          netReturns: 0,
          grossReturns: 0,
          totalRetailTransactions: 319.5,
          totalStartings: 200,
          creationDate: '2020-06-26T14:07:38.799Z',
          userId: '3073EDF96A3C42CC86C7069E379522D2',
          posterminal: '9104513C2D0741D4850AE8493998A7C8',
          isprocessed: 'Y',
          cashTaxInfo: [
            {
              id: '063EB8A372B29C3B1B1BB475B8F94F54',
              name: 'Entregas IVA 21%',
              amount: 55.45,
              orderType: '0'
            }
          ],
          cashCloseInfo: [
            {
              expected: 42.92,
              difference: 0,
              paymentTypeId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
              paymentMethod: {
                _identifier: 'Credit card',
                _entityName: 'OBPOS_App_Payment_Type',
                $ref: 'OBPOS_App_Payment_Type/4785B70E9C1048AB9E9E7B26CFEF6E31',
                id: '4785B70E9C1048AB9E9E7B26CFEF6E31',
                client: '39363B0921BB4293B48383844325E84C',
                client$_identifier: 'The White Valley Group',
                organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
                organization$_identifier: 'Vall Blanca Store',
                active: true,
                creationDate: '2013-07-04T23:01:13+02:00',
                createdBy: '0',
                createdBy$_identifier: 'System',
                updated: '2013-07-04T23:01:13+02:00',
                updatedBy: '0',
                updatedBy$_identifier: 'System',
                searchKey: 'OBPOS_creditcard',
                name: 'Credit card',
                paymentMethod: '5A741F883A31408CA3AC097D76AA6D32',
                paymentMethod$_identifier: 'Wire Transfer',
                currency: '102',
                currency$_identifier: 'EUR',
                obposTerminaltype: 'BD39916225594B32A88983899CF05F72',
                obposTerminaltype$_identifier: 'VBS POS Terminal Type',
                automatemovementtoother: true,
                keepfixedamount: false,
                amount: null,
                allowvariableamount: false,
                allowdontmove: false,
                allowmoveeverything: true,
                cashDifferences: '1EC5D1EA070F45BEB4C8023631DC4BBE',
                cashDifferences$_identifier: 'VBS Cash differences',
                allowdrops: false,
                gLItemForDrops: null,
                allowdeposits: false,
                gLItemForDeposits: null,
                glitemDropdep: '2C62436C613E4C398E7B34A4DED1B726',
                glitemDropdep$_identifier: 'VBS Mandatory G/L Item for Cash up',
                glitemWriteoff: '024D29298E63456A9104F1F672854DFB',
                glitemWriteoff$_identifier: 'VBS Overpayments',
                paymentProvider: null,
                refundProvider: null,
                openDrawer: false,
                iscash: false,
                allowopendrawer: true,
                printtwice: false,
                countcash: false,
                maxLimitAmount: null,
                showkeypad: true,
                defaultCashPaymentMethod: false,
                isshared: false,
                leaveascredit: false,
                image: null,
                paymentMethodCategory: null,
                allowoverpayment: true,
                overpaymentLimit: null,
                countDiffLimit: null,
                isreversable: true,
                availableReverseDelay: null,
                refundable: true,
                countpaymentincashup: true,
                obposPaymentgroup: null,
                obposPaymentmethodType: null,
                changeLessThan: null,
                changePaymentType: null,
                glitemRound: null,
                cashManagementProvider: null,
                isRounding: false,
                issafebox: false,
                recordTime: 1593171360026,
                amountToKeep: 0
              },
              id: '57E51B8FA1FABC2F58251F86C1AACDCE',
              foreignExpected: 42.92
            },
            {
              expected: 250.5,
              difference: 0,
              paymentTypeId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
              paymentMethod: {
                _identifier: 'Cash',
                _entityName: 'OBPOS_App_Payment_Type',
                $ref: 'OBPOS_App_Payment_Type/146569994AB34AC78FDC12AE15F307AF',
                id: '146569994AB34AC78FDC12AE15F307AF',
                client: '39363B0921BB4293B48383844325E84C',
                client$_identifier: 'The White Valley Group',
                organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
                organization$_identifier: 'Vall Blanca Store',
                active: true,
                creationDate: '2013-07-04T23:01:13+02:00',
                createdBy: '0',
                createdBy$_identifier: 'System',
                updated: '2014-04-22T12:58:54+02:00',
                updatedBy: '100',
                updatedBy$_identifier: 'Openbravo',
                searchKey: 'OBPOS_cash',
                name: 'Cash',
                paymentMethod: '45A202BF44884F05B8A1BF741E2063B6',
                paymentMethod$_identifier: 'Cash',
                currency: '102',
                currency$_identifier: 'EUR',
                obposTerminaltype: 'BD39916225594B32A88983899CF05F72',
                obposTerminaltype$_identifier: 'VBS POS Terminal Type',
                automatemovementtoother: true,
                keepfixedamount: true,
                amount: 200,
                allowvariableamount: true,
                allowdontmove: true,
                allowmoveeverything: true,
                cashDifferences: '1EC5D1EA070F45BEB4C8023631DC4BBE',
                cashDifferences$_identifier: 'VBS Cash differences',
                allowdrops: true,
                gLItemForDrops: 'FA646455DCC94D7C90CED9A6C00748E0',
                gLItemForDrops$_identifier: 'VBS Withdrawals',
                allowdeposits: true,
                gLItemForDeposits: '2C9F5E221F9E45F5B9A48462B01C1477',
                gLItemForDeposits$_identifier: 'VBS Deposits',
                glitemDropdep: '2C62436C613E4C398E7B34A4DED1B726',
                glitemDropdep$_identifier: 'VBS Mandatory G/L Item for Cash up',
                glitemWriteoff: '024D29298E63456A9104F1F672854DFB',
                glitemWriteoff$_identifier: 'VBS Overpayments',
                paymentProvider: null,
                refundProvider: null,
                openDrawer: false,
                iscash: true,
                allowopendrawer: true,
                printtwice: false,
                countcash: true,
                maxLimitAmount: null,
                showkeypad: true,
                defaultCashPaymentMethod: false,
                isshared: false,
                leaveascredit: false,
                image: null,
                paymentMethodCategory: null,
                allowoverpayment: true,
                overpaymentLimit: null,
                countDiffLimit: null,
                isreversable: true,
                availableReverseDelay: null,
                refundable: true,
                countpaymentincashup: true,
                obposPaymentgroup: null,
                obposPaymentmethodType: null,
                changeLessThan: null,
                changePaymentType: null,
                glitemRound: null,
                cashManagementProvider: null,
                isRounding: false,
                issafebox: false,
                recordTime: 1593171360028,
                amountToKeep: 200
              },
              id: 'FC02DC8DDA80D32370AC7D75F82D3583',
              foreignDifference: 0,
              foreignExpected: 250.5
            },
            {
              expected: 38.04,
              difference: 0,
              paymentTypeId: 'E11EBCB5CF0442618B72B903DCB6A036',
              paymentMethod: {
                _identifier: 'USA Cash',
                _entityName: 'OBPOS_App_Payment_Type',
                $ref: 'OBPOS_App_Payment_Type/EF09DDF5E0534014B525F87B63285392',
                id: 'EF09DDF5E0534014B525F87B63285392',
                client: '39363B0921BB4293B48383844325E84C',
                client$_identifier: 'The White Valley Group',
                organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
                organization$_identifier: 'Vall Blanca Store',
                active: true,
                creationDate: '2013-07-04T23:01:13+02:00',
                createdBy: '0',
                createdBy$_identifier: 'System',
                updated: '2013-11-27T00:51:37+01:00',
                updatedBy: '100',
                updatedBy$_identifier: 'Openbravo',
                searchKey: 'OBPOS_USA_cash',
                name: 'USA Cash',
                paymentMethod: '45A202BF44884F05B8A1BF741E2063B6',
                paymentMethod$_identifier: 'Cash',
                currency: '100',
                currency$_identifier: 'USD',
                obposTerminaltype: 'BD39916225594B32A88983899CF05F72',
                obposTerminaltype$_identifier: 'VBS POS Terminal Type',
                automatemovementtoother: true,
                keepfixedamount: true,
                amount: 200,
                allowvariableamount: true,
                allowdontmove: true,
                allowmoveeverything: true,
                cashDifferences: '1EC5D1EA070F45BEB4C8023631DC4BBE',
                cashDifferences$_identifier: 'VBS Cash differences',
                allowdrops: true,
                gLItemForDrops: 'FA646455DCC94D7C90CED9A6C00748E0',
                gLItemForDrops$_identifier: 'VBS Withdrawals',
                allowdeposits: true,
                gLItemForDeposits: '2C9F5E221F9E45F5B9A48462B01C1477',
                gLItemForDeposits$_identifier: 'VBS Deposits',
                glitemDropdep: '2C62436C613E4C398E7B34A4DED1B726',
                glitemDropdep$_identifier: 'VBS Mandatory G/L Item for Cash up',
                glitemWriteoff: '024D29298E63456A9104F1F672854DFB',
                glitemWriteoff$_identifier: 'VBS Overpayments',
                paymentProvider: null,
                refundProvider: null,
                openDrawer: false,
                iscash: true,
                allowopendrawer: true,
                printtwice: false,
                countcash: false,
                maxLimitAmount: null,
                showkeypad: true,
                defaultCashPaymentMethod: false,
                isshared: false,
                leaveascredit: false,
                image: null,
                paymentMethodCategory: null,
                allowoverpayment: true,
                overpaymentLimit: null,
                countDiffLimit: null,
                isreversable: true,
                availableReverseDelay: null,
                refundable: true,
                countpaymentincashup: true,
                obposPaymentgroup: null,
                obposPaymentmethodType: null,
                changeLessThan: null,
                changePaymentType: null,
                glitemRound: null,
                cashManagementProvider: null,
                isRounding: false,
                issafebox: false,
                recordTime: 1593171360030,
                amountToKeep: 50
              },
              id: '3185C85C8B1CDB287FBBA7BC5319A1AD',
              foreignExpected: 50
            },
            {
              expected: 50,
              difference: 0,
              paymentTypeId: '6E98C4DE459748BE997693E9ED956D21',
              paymentMethod: {
                _identifier: 'Voucher',
                _entityName: 'OBPOS_App_Payment_Type',
                $ref: 'OBPOS_App_Payment_Type/5A2A5B8DABCF49759C1D0EE674B2CD04',
                id: '5A2A5B8DABCF49759C1D0EE674B2CD04',
                client: '39363B0921BB4293B48383844325E84C',
                client$_identifier: 'The White Valley Group',
                organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
                organization$_identifier: 'Vall Blanca Store',
                active: true,
                creationDate: '2013-07-04T23:01:13+02:00',
                createdBy: '0',
                createdBy$_identifier: 'System',
                updated: '2013-07-04T23:01:13+02:00',
                updatedBy: '0',
                updatedBy$_identifier: 'System',
                searchKey: 'OBPOS_voucher',
                name: 'Voucher',
                paymentMethod: 'CAE4A5CFD6EA485AAA5D7D5859341DB0',
                paymentMethod$_identifier: 'Check',
                currency: '102',
                currency$_identifier: 'EUR',
                obposTerminaltype: 'BD39916225594B32A88983899CF05F72',
                obposTerminaltype$_identifier: 'VBS POS Terminal Type',
                automatemovementtoother: true,
                keepfixedamount: false,
                amount: null,
                allowvariableamount: true,
                allowdontmove: true,
                allowmoveeverything: true,
                cashDifferences: '1EC5D1EA070F45BEB4C8023631DC4BBE',
                cashDifferences$_identifier: 'VBS Cash differences',
                allowdrops: false,
                gLItemForDrops: null,
                allowdeposits: false,
                gLItemForDeposits: null,
                glitemDropdep: '2C62436C613E4C398E7B34A4DED1B726',
                glitemDropdep$_identifier: 'VBS Mandatory G/L Item for Cash up',
                glitemWriteoff: '024D29298E63456A9104F1F672854DFB',
                glitemWriteoff$_identifier: 'VBS Overpayments',
                paymentProvider: null,
                refundProvider: null,
                openDrawer: false,
                iscash: false,
                allowopendrawer: true,
                printtwice: false,
                countcash: false,
                maxLimitAmount: null,
                showkeypad: true,
                defaultCashPaymentMethod: false,
                isshared: false,
                leaveascredit: false,
                image: null,
                paymentMethodCategory: null,
                allowoverpayment: true,
                overpaymentLimit: null,
                countDiffLimit: null,
                isreversable: true,
                availableReverseDelay: null,
                refundable: true,
                countpaymentincashup: true,
                obposPaymentgroup: null,
                obposPaymentmethodType: null,
                changeLessThan: null,
                changePaymentType: null,
                glitemRound: null,
                cashManagementProvider: null,
                isRounding: false,
                issafebox: false,
                recordTime: 1593171360033,
                amountToKeep: 0
              },
              id: '5DBF470B4F11B85E35E87A4C90351CF4',
              foreignExpected: 50
            }
          ],
          cashPaymentMethodInfo: [
            {
              id: '87F12657F37F3BD411BB307314799C4F',
              paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
              searchKey: 'OBPOS_payment.card',
              name: 'Card',
              startingCash: 0,
              totalSales: 42.92,
              totalReturns: 0,
              totalDeposits: 0,
              totalDrops: 0,
              rate: '1',
              isocode: 'EUR',
              cashManagements: []
            },
            {
              id: 'F6E9A7CA1FA859664873F770C0156B1D',
              paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
              searchKey: 'OBPOS_payment.cash',
              name: 'Cash',
              startingCash: 200,
              totalSales: 150.5,
              totalReturns: 0,
              totalDeposits: 100,
              totalDrops: 200,
              rate: '1',
              isocode: 'EUR',
              cashManagements: [
                {
                  id: '355EEFF0868771961EC5C1471498671C',
                  description: 'Cash - Backoffice transfer to VBS',
                  amount: 100,
                  origAmount: 100,
                  type: 'deposit',
                  reasonId: '65D62A9F2F2F433BA55BB41D8F514117',
                  paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                  user: 'Vall Blanca Store User',
                  userId: '3073EDF96A3C42CC86C7069E379522D2',
                  creationDate: '2020-06-26T14:08:48.294Z',
                  timezoneOffset: -120,
                  isocode: 'EUR',
                  glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                  cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                  posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                  isbeingprocessed: 'N',
                  defaultProcess: 'Y',
                  extendedType: '',
                  cashUpReportInformation: {
                    id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                    netSales: 264.05,
                    grossSales: 319.5,
                    netReturns: 0,
                    grossReturns: 0,
                    totalRetailTransactions: 319.5,
                    totalStartings: 200,
                    creationDate: '2020-06-26T14:07:38.799Z',
                    userId: '3073EDF96A3C42CC86C7069E379522D2',
                    posterminal: '9104513C2D0741D4850AE8493998A7C8',
                    isprocessed: false,
                    cashTaxInfo: [
                      {
                        id: '063EB8A372B29C3B1B1BB475B8F94F54',
                        name: 'Entregas IVA 21%',
                        amount: 55.45,
                        orderType: '0'
                      }
                    ],
                    cashCloseInfo: [],
                    cashPaymentMethodInfo: [
                      {
                        id: '87F12657F37F3BD411BB307314799C4F',
                        paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                        searchKey: 'OBPOS_payment.card',
                        name: 'Card',
                        startingCash: 0,
                        totalSales: 42.92,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        newPaymentMethod: false,
                        cashManagements: [],
                        usedInCurrentTrx: true
                      },
                      {
                        id: 'F6E9A7CA1FA859664873F770C0156B1D',
                        paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                        searchKey: 'OBPOS_payment.cash',
                        name: 'Cash',
                        startingCash: 200,
                        totalSales: 150.5,
                        totalReturns: 0,
                        totalDeposits: 100,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        newPaymentMethod: false,
                        cashManagements: [
                          {
                            id: '355EEFF0868771961EC5C1471498671C',
                            description: 'Cash - Backoffice transfer to VBS',
                            amount: 100,
                            origAmount: 100,
                            type: 'deposit',
                            reasonId: '65D62A9F2F2F433BA55BB41D8F514117',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:08:48.294Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            isDraft: true
                          }
                        ],
                        usedInCurrentTrx: true
                      },
                      {
                        id: 'F5E63E5C9BE2B3854D236726836F80DE',
                        paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                        searchKey: 'OBPOS.payment.usacash',
                        name: 'USA Cash',
                        startingCash: 0,
                        totalSales: 100,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '0.76082',
                        isocode: 'USD',
                        newPaymentMethod: false,
                        cashManagements: [],
                        usedInCurrentTrx: true
                      },
                      {
                        id: '624C1AB9D2D97166091DA970BE2E8F43',
                        paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
                        searchKey: 'OBPOS_payment.voucher',
                        name: 'Voucher',
                        startingCash: 0,
                        totalSales: 50,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        newPaymentMethod: false,
                        cashManagements: [],
                        usedInCurrentTrx: true
                      }
                    ]
                  }
                },
                {
                  id: 'E5D7F0D3C1F600B9C9661D17C077DD9E',
                  description:
                    'Cash - Cashier shift (transfer to VBS backoffice)',
                  amount: 200,
                  origAmount: 200,
                  type: 'drop',
                  reasonId: '84CB7407F7834C05BD6E2ADF7A4AFC25',
                  paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                  user: 'Vall Blanca Store User',
                  userId: '3073EDF96A3C42CC86C7069E379522D2',
                  creationDate: '2020-06-26T14:09:05.754Z',
                  timezoneOffset: -120,
                  isocode: 'EUR',
                  glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                  cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                  posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                  isbeingprocessed: 'N',
                  defaultProcess: 'Y',
                  extendedType: '',
                  cashUpReportInformation: {
                    id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                    netSales: 264.05,
                    grossSales: 319.5,
                    netReturns: 0,
                    grossReturns: 0,
                    totalRetailTransactions: 319.5,
                    totalStartings: 200,
                    creationDate: '2020-06-26T14:07:38.799Z',
                    userId: '3073EDF96A3C42CC86C7069E379522D2',
                    posterminal: '9104513C2D0741D4850AE8493998A7C8',
                    isprocessed: false,
                    cashTaxInfo: [
                      {
                        id: '063EB8A372B29C3B1B1BB475B8F94F54',
                        name: 'Entregas IVA 21%',
                        amount: 55.45,
                        orderType: '0'
                      }
                    ],
                    cashCloseInfo: [],
                    cashPaymentMethodInfo: [
                      {
                        id: '87F12657F37F3BD411BB307314799C4F',
                        paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                        searchKey: 'OBPOS_payment.card',
                        name: 'Card',
                        startingCash: 0,
                        totalSales: 42.92,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: []
                      },
                      {
                        id: 'F6E9A7CA1FA859664873F770C0156B1D',
                        paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                        searchKey: 'OBPOS_payment.cash',
                        name: 'Cash',
                        startingCash: 200,
                        totalSales: 150.5,
                        totalReturns: 0,
                        totalDeposits: 100,
                        totalDrops: 200,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: [
                          {
                            id: '355EEFF0868771961EC5C1471498671C',
                            description: 'Cash - Backoffice transfer to VBS',
                            amount: 100,
                            origAmount: 100,
                            type: 'deposit',
                            reasonId: '65D62A9F2F2F433BA55BB41D8F514117',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:08:48.294Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            cashUpReportInformation: {
                              id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                              netSales: 264.05,
                              grossSales: 319.5,
                              netReturns: 0,
                              grossReturns: 0,
                              totalRetailTransactions: 319.5,
                              totalStartings: 200,
                              creationDate: '2020-06-26T14:07:38.799Z',
                              userId: '3073EDF96A3C42CC86C7069E379522D2',
                              posterminal: '9104513C2D0741D4850AE8493998A7C8',
                              isprocessed: false,
                              cashTaxInfo: [
                                {
                                  id: '063EB8A372B29C3B1B1BB475B8F94F54',
                                  name: 'Entregas IVA 21%',
                                  amount: 55.45,
                                  orderType: '0'
                                }
                              ],
                              cashCloseInfo: [],
                              cashPaymentMethodInfo: [
                                {
                                  id: '87F12657F37F3BD411BB307314799C4F',
                                  paymentMethodId:
                                    '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                  searchKey: 'OBPOS_payment.card',
                                  name: 'Card',
                                  startingCash: 0,
                                  totalSales: 42.92,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F6E9A7CA1FA859664873F770C0156B1D',
                                  paymentMethodId:
                                    '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                  searchKey: 'OBPOS_payment.cash',
                                  name: 'Cash',
                                  startingCash: 200,
                                  totalSales: 150.5,
                                  totalReturns: 0,
                                  totalDeposits: 100,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [
                                    {
                                      id: '355EEFF0868771961EC5C1471498671C',
                                      description:
                                        'Cash - Backoffice transfer to VBS',
                                      amount: 100,
                                      origAmount: 100,
                                      type: 'deposit',
                                      reasonId:
                                        '65D62A9F2F2F433BA55BB41D8F514117',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:08:48.294Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        '2C9F5E221F9E45F5B9A48462B01C1477',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      isDraft: true
                                    }
                                  ],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F5E63E5C9BE2B3854D236726836F80DE',
                                  paymentMethodId:
                                    'E11EBCB5CF0442618B72B903DCB6A036',
                                  searchKey: 'OBPOS.payment.usacash',
                                  name: 'USA Cash',
                                  startingCash: 0,
                                  totalSales: 100,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '0.76082',
                                  isocode: 'USD',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: '624C1AB9D2D97166091DA970BE2E8F43',
                                  paymentMethodId:
                                    '6E98C4DE459748BE997693E9ED956D21',
                                  searchKey: 'OBPOS_payment.voucher',
                                  name: 'Voucher',
                                  startingCash: 0,
                                  totalSales: 50,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                }
                              ]
                            }
                          },
                          {
                            id: 'E5D7F0D3C1F600B9C9661D17C077DD9E',
                            description:
                              'Cash - Cashier shift (transfer to VBS backoffice)',
                            amount: 200,
                            origAmount: 200,
                            type: 'drop',
                            reasonId: '84CB7407F7834C05BD6E2ADF7A4AFC25',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:05.754Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            isDraft: true
                          }
                        ],
                        usedInCurrentTrx: true
                      },
                      {
                        id: 'F5E63E5C9BE2B3854D236726836F80DE',
                        paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                        searchKey: 'OBPOS.payment.usacash',
                        name: 'USA Cash',
                        startingCash: 0,
                        totalSales: 100,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '0.76082',
                        isocode: 'USD',
                        cashManagements: []
                      },
                      {
                        id: '624C1AB9D2D97166091DA970BE2E8F43',
                        paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
                        searchKey: 'OBPOS_payment.voucher',
                        name: 'Voucher',
                        startingCash: 0,
                        totalSales: 50,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: []
                      }
                    ]
                  }
                }
              ]
            },
            {
              id: 'F5E63E5C9BE2B3854D236726836F80DE',
              paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
              searchKey: 'OBPOS.payment.usacash',
              name: 'USA Cash',
              startingCash: 0,
              totalSales: 100,
              totalReturns: 0,
              totalDeposits: 250,
              totalDrops: 300,
              rate: '0.76082',
              isocode: 'USD',
              cashManagements: [
                {
                  id: '4133CE5C159D6F5561579E04F3CBCE60',
                  description: 'USA Cash - Backoffice transfer to VBS - USD',
                  amount: 250,
                  origAmount: 190.21,
                  type: 'deposit',
                  reasonId: '8C436C5AA7A446A58C47E0837C1B0475',
                  paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                  user: 'Vall Blanca Store User',
                  userId: '3073EDF96A3C42CC86C7069E379522D2',
                  creationDate: '2020-06-26T14:09:24.222Z',
                  timezoneOffset: -120,
                  isocode: 'USD',
                  glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                  cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                  posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                  isbeingprocessed: 'N',
                  defaultProcess: 'Y',
                  extendedType: '',
                  cashUpReportInformation: {
                    id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                    netSales: 264.05,
                    grossSales: 319.5,
                    netReturns: 0,
                    grossReturns: 0,
                    totalRetailTransactions: 319.5,
                    totalStartings: 200,
                    creationDate: '2020-06-26T14:07:38.799Z',
                    userId: '3073EDF96A3C42CC86C7069E379522D2',
                    posterminal: '9104513C2D0741D4850AE8493998A7C8',
                    isprocessed: false,
                    cashTaxInfo: [
                      {
                        id: '063EB8A372B29C3B1B1BB475B8F94F54',
                        name: 'Entregas IVA 21%',
                        amount: 55.45,
                        orderType: '0'
                      }
                    ],
                    cashCloseInfo: [],
                    cashPaymentMethodInfo: [
                      {
                        id: '87F12657F37F3BD411BB307314799C4F',
                        paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                        searchKey: 'OBPOS_payment.card',
                        name: 'Card',
                        startingCash: 0,
                        totalSales: 42.92,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: []
                      },
                      {
                        id: 'F6E9A7CA1FA859664873F770C0156B1D',
                        paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                        searchKey: 'OBPOS_payment.cash',
                        name: 'Cash',
                        startingCash: 200,
                        totalSales: 150.5,
                        totalReturns: 0,
                        totalDeposits: 100,
                        totalDrops: 200,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: [
                          {
                            id: '355EEFF0868771961EC5C1471498671C',
                            description: 'Cash - Backoffice transfer to VBS',
                            amount: 100,
                            origAmount: 100,
                            type: 'deposit',
                            reasonId: '65D62A9F2F2F433BA55BB41D8F514117',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:08:48.294Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            cashUpReportInformation: {
                              id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                              netSales: 264.05,
                              grossSales: 319.5,
                              netReturns: 0,
                              grossReturns: 0,
                              totalRetailTransactions: 319.5,
                              totalStartings: 200,
                              creationDate: '2020-06-26T14:07:38.799Z',
                              userId: '3073EDF96A3C42CC86C7069E379522D2',
                              posterminal: '9104513C2D0741D4850AE8493998A7C8',
                              isprocessed: false,
                              cashTaxInfo: [
                                {
                                  id: '063EB8A372B29C3B1B1BB475B8F94F54',
                                  name: 'Entregas IVA 21%',
                                  amount: 55.45,
                                  orderType: '0'
                                }
                              ],
                              cashCloseInfo: [],
                              cashPaymentMethodInfo: [
                                {
                                  id: '87F12657F37F3BD411BB307314799C4F',
                                  paymentMethodId:
                                    '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                  searchKey: 'OBPOS_payment.card',
                                  name: 'Card',
                                  startingCash: 0,
                                  totalSales: 42.92,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F6E9A7CA1FA859664873F770C0156B1D',
                                  paymentMethodId:
                                    '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                  searchKey: 'OBPOS_payment.cash',
                                  name: 'Cash',
                                  startingCash: 200,
                                  totalSales: 150.5,
                                  totalReturns: 0,
                                  totalDeposits: 100,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [
                                    {
                                      id: '355EEFF0868771961EC5C1471498671C',
                                      description:
                                        'Cash - Backoffice transfer to VBS',
                                      amount: 100,
                                      origAmount: 100,
                                      type: 'deposit',
                                      reasonId:
                                        '65D62A9F2F2F433BA55BB41D8F514117',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:08:48.294Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        '2C9F5E221F9E45F5B9A48462B01C1477',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      isDraft: true
                                    }
                                  ],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F5E63E5C9BE2B3854D236726836F80DE',
                                  paymentMethodId:
                                    'E11EBCB5CF0442618B72B903DCB6A036',
                                  searchKey: 'OBPOS.payment.usacash',
                                  name: 'USA Cash',
                                  startingCash: 0,
                                  totalSales: 100,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '0.76082',
                                  isocode: 'USD',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: '624C1AB9D2D97166091DA970BE2E8F43',
                                  paymentMethodId:
                                    '6E98C4DE459748BE997693E9ED956D21',
                                  searchKey: 'OBPOS_payment.voucher',
                                  name: 'Voucher',
                                  startingCash: 0,
                                  totalSales: 50,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                }
                              ]
                            }
                          },
                          {
                            id: 'E5D7F0D3C1F600B9C9661D17C077DD9E',
                            description:
                              'Cash - Cashier shift (transfer to VBS backoffice)',
                            amount: 200,
                            origAmount: 200,
                            type: 'drop',
                            reasonId: '84CB7407F7834C05BD6E2ADF7A4AFC25',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:05.754Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            cashUpReportInformation: {
                              id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                              netSales: 264.05,
                              grossSales: 319.5,
                              netReturns: 0,
                              grossReturns: 0,
                              totalRetailTransactions: 319.5,
                              totalStartings: 200,
                              creationDate: '2020-06-26T14:07:38.799Z',
                              userId: '3073EDF96A3C42CC86C7069E379522D2',
                              posterminal: '9104513C2D0741D4850AE8493998A7C8',
                              isprocessed: false,
                              cashTaxInfo: [
                                {
                                  id: '063EB8A372B29C3B1B1BB475B8F94F54',
                                  name: 'Entregas IVA 21%',
                                  amount: 55.45,
                                  orderType: '0'
                                }
                              ],
                              cashCloseInfo: [],
                              cashPaymentMethodInfo: [
                                {
                                  id: '87F12657F37F3BD411BB307314799C4F',
                                  paymentMethodId:
                                    '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                  searchKey: 'OBPOS_payment.card',
                                  name: 'Card',
                                  startingCash: 0,
                                  totalSales: 42.92,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  cashManagements: []
                                },
                                {
                                  id: 'F6E9A7CA1FA859664873F770C0156B1D',
                                  paymentMethodId:
                                    '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                  searchKey: 'OBPOS_payment.cash',
                                  name: 'Cash',
                                  startingCash: 200,
                                  totalSales: 150.5,
                                  totalReturns: 0,
                                  totalDeposits: 100,
                                  totalDrops: 200,
                                  rate: '1',
                                  isocode: 'EUR',
                                  cashManagements: [
                                    {
                                      id: '355EEFF0868771961EC5C1471498671C',
                                      description:
                                        'Cash - Backoffice transfer to VBS',
                                      amount: 100,
                                      origAmount: 100,
                                      type: 'deposit',
                                      reasonId:
                                        '65D62A9F2F2F433BA55BB41D8F514117',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:08:48.294Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        '2C9F5E221F9E45F5B9A48462B01C1477',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      cashUpReportInformation: {
                                        id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                        netSales: 264.05,
                                        grossSales: 319.5,
                                        netReturns: 0,
                                        grossReturns: 0,
                                        totalRetailTransactions: 319.5,
                                        totalStartings: 200,
                                        creationDate:
                                          '2020-06-26T14:07:38.799Z',
                                        userId:
                                          '3073EDF96A3C42CC86C7069E379522D2',
                                        posterminal:
                                          '9104513C2D0741D4850AE8493998A7C8',
                                        isprocessed: false,
                                        cashTaxInfo: [
                                          {
                                            id:
                                              '063EB8A372B29C3B1B1BB475B8F94F54',
                                            name: 'Entregas IVA 21%',
                                            amount: 55.45,
                                            orderType: '0'
                                          }
                                        ],
                                        cashCloseInfo: [],
                                        cashPaymentMethodInfo: [
                                          {
                                            id:
                                              '87F12657F37F3BD411BB307314799C4F',
                                            paymentMethodId:
                                              '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                            searchKey: 'OBPOS_payment.card',
                                            name: 'Card',
                                            startingCash: 0,
                                            totalSales: 42.92,
                                            totalReturns: 0,
                                            totalDeposits: 0,
                                            totalDrops: 0,
                                            rate: '1',
                                            isocode: 'EUR',
                                            newPaymentMethod: false,
                                            cashManagements: [],
                                            usedInCurrentTrx: true
                                          },
                                          {
                                            id:
                                              'F6E9A7CA1FA859664873F770C0156B1D',
                                            paymentMethodId:
                                              '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                            searchKey: 'OBPOS_payment.cash',
                                            name: 'Cash',
                                            startingCash: 200,
                                            totalSales: 150.5,
                                            totalReturns: 0,
                                            totalDeposits: 100,
                                            totalDrops: 0,
                                            rate: '1',
                                            isocode: 'EUR',
                                            newPaymentMethod: false,
                                            cashManagements: [
                                              {
                                                id:
                                                  '355EEFF0868771961EC5C1471498671C',
                                                description:
                                                  'Cash - Backoffice transfer to VBS',
                                                amount: 100,
                                                origAmount: 100,
                                                type: 'deposit',
                                                reasonId:
                                                  '65D62A9F2F2F433BA55BB41D8F514117',
                                                paymentMethodId:
                                                  '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                                user: 'Vall Blanca Store User',
                                                userId:
                                                  '3073EDF96A3C42CC86C7069E379522D2',
                                                creationDate:
                                                  '2020-06-26T14:08:48.294Z',
                                                timezoneOffset: -120,
                                                isocode: 'EUR',
                                                glItem:
                                                  '2C9F5E221F9E45F5B9A48462B01C1477',
                                                cashup_id:
                                                  'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                                posTerminal:
                                                  '9104513C2D0741D4850AE8493998A7C8',
                                                isbeingprocessed: 'N',
                                                defaultProcess: 'Y',
                                                extendedType: '',
                                                isDraft: true
                                              }
                                            ],
                                            usedInCurrentTrx: true
                                          },
                                          {
                                            id:
                                              'F5E63E5C9BE2B3854D236726836F80DE',
                                            paymentMethodId:
                                              'E11EBCB5CF0442618B72B903DCB6A036',
                                            searchKey: 'OBPOS.payment.usacash',
                                            name: 'USA Cash',
                                            startingCash: 0,
                                            totalSales: 100,
                                            totalReturns: 0,
                                            totalDeposits: 0,
                                            totalDrops: 0,
                                            rate: '0.76082',
                                            isocode: 'USD',
                                            newPaymentMethod: false,
                                            cashManagements: [],
                                            usedInCurrentTrx: true
                                          },
                                          {
                                            id:
                                              '624C1AB9D2D97166091DA970BE2E8F43',
                                            paymentMethodId:
                                              '6E98C4DE459748BE997693E9ED956D21',
                                            searchKey: 'OBPOS_payment.voucher',
                                            name: 'Voucher',
                                            startingCash: 0,
                                            totalSales: 50,
                                            totalReturns: 0,
                                            totalDeposits: 0,
                                            totalDrops: 0,
                                            rate: '1',
                                            isocode: 'EUR',
                                            newPaymentMethod: false,
                                            cashManagements: [],
                                            usedInCurrentTrx: true
                                          }
                                        ]
                                      }
                                    },
                                    {
                                      id: 'E5D7F0D3C1F600B9C9661D17C077DD9E',
                                      description:
                                        'Cash - Cashier shift (transfer to VBS backoffice)',
                                      amount: 200,
                                      origAmount: 200,
                                      type: 'drop',
                                      reasonId:
                                        '84CB7407F7834C05BD6E2ADF7A4AFC25',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:09:05.754Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        'FA646455DCC94D7C90CED9A6C00748E0',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      isDraft: true
                                    }
                                  ],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F5E63E5C9BE2B3854D236726836F80DE',
                                  paymentMethodId:
                                    'E11EBCB5CF0442618B72B903DCB6A036',
                                  searchKey: 'OBPOS.payment.usacash',
                                  name: 'USA Cash',
                                  startingCash: 0,
                                  totalSales: 100,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '0.76082',
                                  isocode: 'USD',
                                  cashManagements: []
                                },
                                {
                                  id: '624C1AB9D2D97166091DA970BE2E8F43',
                                  paymentMethodId:
                                    '6E98C4DE459748BE997693E9ED956D21',
                                  searchKey: 'OBPOS_payment.voucher',
                                  name: 'Voucher',
                                  startingCash: 0,
                                  totalSales: 50,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  cashManagements: []
                                }
                              ]
                            }
                          }
                        ]
                      },
                      {
                        id: 'F5E63E5C9BE2B3854D236726836F80DE',
                        paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                        searchKey: 'OBPOS.payment.usacash',
                        name: 'USA Cash',
                        startingCash: 0,
                        totalSales: 100,
                        totalReturns: 0,
                        totalDeposits: 250,
                        totalDrops: 0,
                        rate: '0.76082',
                        isocode: 'USD',
                        cashManagements: [
                          {
                            id: '4133CE5C159D6F5561579E04F3CBCE60',
                            description:
                              'USA Cash - Backoffice transfer to VBS - USD',
                            amount: 250,
                            origAmount: 190.21,
                            type: 'deposit',
                            reasonId: '8C436C5AA7A446A58C47E0837C1B0475',
                            paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:24.222Z',
                            timezoneOffset: -120,
                            isocode: 'USD',
                            glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            isDraft: true
                          },
                          {
                            id: '0786608FE51D9C38B4E6A8DEBE4C5046',
                            description:
                              'USA Cash - Cashier shift (transfer to VBS backoffice) - USD',
                            amount: 300,
                            origAmount: 228.25,
                            type: 'drop',
                            reasonId: '9A90A1DAF459446895A6E6E4C21768C5',
                            paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:32.367Z',
                            timezoneOffset: -120,
                            isocode: 'USD',
                            glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            isDraft: true
                          }
                        ],
                        usedInCurrentTrx: true
                      },
                      {
                        id: '624C1AB9D2D97166091DA970BE2E8F43',
                        paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
                        searchKey: 'OBPOS_payment.voucher',
                        name: 'Voucher',
                        startingCash: 0,
                        totalSales: 50,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: []
                      }
                    ]
                  }
                },
                {
                  id: '0786608FE51D9C38B4E6A8DEBE4C5046',
                  description:
                    'USA Cash - Cashier shift (transfer to VBS backoffice) - USD',
                  amount: 300,
                  origAmount: 228.25,
                  type: 'drop',
                  reasonId: '9A90A1DAF459446895A6E6E4C21768C5',
                  paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                  user: 'Vall Blanca Store User',
                  userId: '3073EDF96A3C42CC86C7069E379522D2',
                  creationDate: '2020-06-26T14:09:32.367Z',
                  timezoneOffset: -120,
                  isocode: 'USD',
                  glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                  cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                  posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                  isbeingprocessed: 'N',
                  defaultProcess: 'Y',
                  extendedType: '',
                  cashUpReportInformation: {
                    id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                    netSales: 264.05,
                    grossSales: 319.5,
                    netReturns: 0,
                    grossReturns: 0,
                    totalRetailTransactions: 319.5,
                    totalStartings: 200,
                    creationDate: '2020-06-26T14:07:38.799Z',
                    userId: '3073EDF96A3C42CC86C7069E379522D2',
                    posterminal: '9104513C2D0741D4850AE8493998A7C8',
                    isprocessed: false,
                    cashTaxInfo: [
                      {
                        id: '063EB8A372B29C3B1B1BB475B8F94F54',
                        name: 'Entregas IVA 21%',
                        amount: 55.45,
                        orderType: '0'
                      }
                    ],
                    cashCloseInfo: [],
                    cashPaymentMethodInfo: [
                      {
                        id: '87F12657F37F3BD411BB307314799C4F',
                        paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                        searchKey: 'OBPOS_payment.card',
                        name: 'Card',
                        startingCash: 0,
                        totalSales: 42.92,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: []
                      },
                      {
                        id: 'F6E9A7CA1FA859664873F770C0156B1D',
                        paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                        searchKey: 'OBPOS_payment.cash',
                        name: 'Cash',
                        startingCash: 200,
                        totalSales: 150.5,
                        totalReturns: 0,
                        totalDeposits: 100,
                        totalDrops: 200,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: [
                          {
                            id: '355EEFF0868771961EC5C1471498671C',
                            description: 'Cash - Backoffice transfer to VBS',
                            amount: 100,
                            origAmount: 100,
                            type: 'deposit',
                            reasonId: '65D62A9F2F2F433BA55BB41D8F514117',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:08:48.294Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            cashUpReportInformation: {
                              id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                              netSales: 264.05,
                              grossSales: 319.5,
                              netReturns: 0,
                              grossReturns: 0,
                              totalRetailTransactions: 319.5,
                              totalStartings: 200,
                              creationDate: '2020-06-26T14:07:38.799Z',
                              userId: '3073EDF96A3C42CC86C7069E379522D2',
                              posterminal: '9104513C2D0741D4850AE8493998A7C8',
                              isprocessed: false,
                              cashTaxInfo: [
                                {
                                  id: '063EB8A372B29C3B1B1BB475B8F94F54',
                                  name: 'Entregas IVA 21%',
                                  amount: 55.45,
                                  orderType: '0'
                                }
                              ],
                              cashCloseInfo: [],
                              cashPaymentMethodInfo: [
                                {
                                  id: '87F12657F37F3BD411BB307314799C4F',
                                  paymentMethodId:
                                    '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                  searchKey: 'OBPOS_payment.card',
                                  name: 'Card',
                                  startingCash: 0,
                                  totalSales: 42.92,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F6E9A7CA1FA859664873F770C0156B1D',
                                  paymentMethodId:
                                    '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                  searchKey: 'OBPOS_payment.cash',
                                  name: 'Cash',
                                  startingCash: 200,
                                  totalSales: 150.5,
                                  totalReturns: 0,
                                  totalDeposits: 100,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [
                                    {
                                      id: '355EEFF0868771961EC5C1471498671C',
                                      description:
                                        'Cash - Backoffice transfer to VBS',
                                      amount: 100,
                                      origAmount: 100,
                                      type: 'deposit',
                                      reasonId:
                                        '65D62A9F2F2F433BA55BB41D8F514117',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:08:48.294Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        '2C9F5E221F9E45F5B9A48462B01C1477',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      isDraft: true
                                    }
                                  ],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F5E63E5C9BE2B3854D236726836F80DE',
                                  paymentMethodId:
                                    'E11EBCB5CF0442618B72B903DCB6A036',
                                  searchKey: 'OBPOS.payment.usacash',
                                  name: 'USA Cash',
                                  startingCash: 0,
                                  totalSales: 100,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '0.76082',
                                  isocode: 'USD',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: '624C1AB9D2D97166091DA970BE2E8F43',
                                  paymentMethodId:
                                    '6E98C4DE459748BE997693E9ED956D21',
                                  searchKey: 'OBPOS_payment.voucher',
                                  name: 'Voucher',
                                  startingCash: 0,
                                  totalSales: 50,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  newPaymentMethod: false,
                                  cashManagements: [],
                                  usedInCurrentTrx: true
                                }
                              ]
                            }
                          },
                          {
                            id: 'E5D7F0D3C1F600B9C9661D17C077DD9E',
                            description:
                              'Cash - Cashier shift (transfer to VBS backoffice)',
                            amount: 200,
                            origAmount: 200,
                            type: 'drop',
                            reasonId: '84CB7407F7834C05BD6E2ADF7A4AFC25',
                            paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:05.754Z',
                            timezoneOffset: -120,
                            isocode: 'EUR',
                            glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            cashUpReportInformation: {
                              id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                              netSales: 264.05,
                              grossSales: 319.5,
                              netReturns: 0,
                              grossReturns: 0,
                              totalRetailTransactions: 319.5,
                              totalStartings: 200,
                              creationDate: '2020-06-26T14:07:38.799Z',
                              userId: '3073EDF96A3C42CC86C7069E379522D2',
                              posterminal: '9104513C2D0741D4850AE8493998A7C8',
                              isprocessed: false,
                              cashTaxInfo: [
                                {
                                  id: '063EB8A372B29C3B1B1BB475B8F94F54',
                                  name: 'Entregas IVA 21%',
                                  amount: 55.45,
                                  orderType: '0'
                                }
                              ],
                              cashCloseInfo: [],
                              cashPaymentMethodInfo: [
                                {
                                  id: '87F12657F37F3BD411BB307314799C4F',
                                  paymentMethodId:
                                    '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                  searchKey: 'OBPOS_payment.card',
                                  name: 'Card',
                                  startingCash: 0,
                                  totalSales: 42.92,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  cashManagements: []
                                },
                                {
                                  id: 'F6E9A7CA1FA859664873F770C0156B1D',
                                  paymentMethodId:
                                    '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                  searchKey: 'OBPOS_payment.cash',
                                  name: 'Cash',
                                  startingCash: 200,
                                  totalSales: 150.5,
                                  totalReturns: 0,
                                  totalDeposits: 100,
                                  totalDrops: 200,
                                  rate: '1',
                                  isocode: 'EUR',
                                  cashManagements: [
                                    {
                                      id: '355EEFF0868771961EC5C1471498671C',
                                      description:
                                        'Cash - Backoffice transfer to VBS',
                                      amount: 100,
                                      origAmount: 100,
                                      type: 'deposit',
                                      reasonId:
                                        '65D62A9F2F2F433BA55BB41D8F514117',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:08:48.294Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        '2C9F5E221F9E45F5B9A48462B01C1477',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      cashUpReportInformation: {
                                        id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                        netSales: 264.05,
                                        grossSales: 319.5,
                                        netReturns: 0,
                                        grossReturns: 0,
                                        totalRetailTransactions: 319.5,
                                        totalStartings: 200,
                                        creationDate:
                                          '2020-06-26T14:07:38.799Z',
                                        userId:
                                          '3073EDF96A3C42CC86C7069E379522D2',
                                        posterminal:
                                          '9104513C2D0741D4850AE8493998A7C8',
                                        isprocessed: false,
                                        cashTaxInfo: [
                                          {
                                            id:
                                              '063EB8A372B29C3B1B1BB475B8F94F54',
                                            name: 'Entregas IVA 21%',
                                            amount: 55.45,
                                            orderType: '0'
                                          }
                                        ],
                                        cashCloseInfo: [],
                                        cashPaymentMethodInfo: [
                                          {
                                            id:
                                              '87F12657F37F3BD411BB307314799C4F',
                                            paymentMethodId:
                                              '5EA2A7DEBB2A49A69550C7E3D8899ED5',
                                            searchKey: 'OBPOS_payment.card',
                                            name: 'Card',
                                            startingCash: 0,
                                            totalSales: 42.92,
                                            totalReturns: 0,
                                            totalDeposits: 0,
                                            totalDrops: 0,
                                            rate: '1',
                                            isocode: 'EUR',
                                            newPaymentMethod: false,
                                            cashManagements: [],
                                            usedInCurrentTrx: true
                                          },
                                          {
                                            id:
                                              'F6E9A7CA1FA859664873F770C0156B1D',
                                            paymentMethodId:
                                              '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                            searchKey: 'OBPOS_payment.cash',
                                            name: 'Cash',
                                            startingCash: 200,
                                            totalSales: 150.5,
                                            totalReturns: 0,
                                            totalDeposits: 100,
                                            totalDrops: 0,
                                            rate: '1',
                                            isocode: 'EUR',
                                            newPaymentMethod: false,
                                            cashManagements: [
                                              {
                                                id:
                                                  '355EEFF0868771961EC5C1471498671C',
                                                description:
                                                  'Cash - Backoffice transfer to VBS',
                                                amount: 100,
                                                origAmount: 100,
                                                type: 'deposit',
                                                reasonId:
                                                  '65D62A9F2F2F433BA55BB41D8F514117',
                                                paymentMethodId:
                                                  '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                                user: 'Vall Blanca Store User',
                                                userId:
                                                  '3073EDF96A3C42CC86C7069E379522D2',
                                                creationDate:
                                                  '2020-06-26T14:08:48.294Z',
                                                timezoneOffset: -120,
                                                isocode: 'EUR',
                                                glItem:
                                                  '2C9F5E221F9E45F5B9A48462B01C1477',
                                                cashup_id:
                                                  'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                                posTerminal:
                                                  '9104513C2D0741D4850AE8493998A7C8',
                                                isbeingprocessed: 'N',
                                                defaultProcess: 'Y',
                                                extendedType: '',
                                                isDraft: true
                                              }
                                            ],
                                            usedInCurrentTrx: true
                                          },
                                          {
                                            id:
                                              'F5E63E5C9BE2B3854D236726836F80DE',
                                            paymentMethodId:
                                              'E11EBCB5CF0442618B72B903DCB6A036',
                                            searchKey: 'OBPOS.payment.usacash',
                                            name: 'USA Cash',
                                            startingCash: 0,
                                            totalSales: 100,
                                            totalReturns: 0,
                                            totalDeposits: 0,
                                            totalDrops: 0,
                                            rate: '0.76082',
                                            isocode: 'USD',
                                            newPaymentMethod: false,
                                            cashManagements: [],
                                            usedInCurrentTrx: true
                                          },
                                          {
                                            id:
                                              '624C1AB9D2D97166091DA970BE2E8F43',
                                            paymentMethodId:
                                              '6E98C4DE459748BE997693E9ED956D21',
                                            searchKey: 'OBPOS_payment.voucher',
                                            name: 'Voucher',
                                            startingCash: 0,
                                            totalSales: 50,
                                            totalReturns: 0,
                                            totalDeposits: 0,
                                            totalDrops: 0,
                                            rate: '1',
                                            isocode: 'EUR',
                                            newPaymentMethod: false,
                                            cashManagements: [],
                                            usedInCurrentTrx: true
                                          }
                                        ]
                                      }
                                    },
                                    {
                                      id: 'E5D7F0D3C1F600B9C9661D17C077DD9E',
                                      description:
                                        'Cash - Cashier shift (transfer to VBS backoffice)',
                                      amount: 200,
                                      origAmount: 200,
                                      type: 'drop',
                                      reasonId:
                                        '84CB7407F7834C05BD6E2ADF7A4AFC25',
                                      paymentMethodId:
                                        '63339A82A49A4AE0BCD9AC5929B0EA3B',
                                      user: 'Vall Blanca Store User',
                                      userId:
                                        '3073EDF96A3C42CC86C7069E379522D2',
                                      creationDate: '2020-06-26T14:09:05.754Z',
                                      timezoneOffset: -120,
                                      isocode: 'EUR',
                                      glItem:
                                        'FA646455DCC94D7C90CED9A6C00748E0',
                                      cashup_id:
                                        'EF694A2C8FD3F9A4990A74DD87ED8D10',
                                      posTerminal:
                                        '9104513C2D0741D4850AE8493998A7C8',
                                      isbeingprocessed: 'N',
                                      defaultProcess: 'Y',
                                      extendedType: '',
                                      isDraft: true
                                    }
                                  ],
                                  usedInCurrentTrx: true
                                },
                                {
                                  id: 'F5E63E5C9BE2B3854D236726836F80DE',
                                  paymentMethodId:
                                    'E11EBCB5CF0442618B72B903DCB6A036',
                                  searchKey: 'OBPOS.payment.usacash',
                                  name: 'USA Cash',
                                  startingCash: 0,
                                  totalSales: 100,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '0.76082',
                                  isocode: 'USD',
                                  cashManagements: []
                                },
                                {
                                  id: '624C1AB9D2D97166091DA970BE2E8F43',
                                  paymentMethodId:
                                    '6E98C4DE459748BE997693E9ED956D21',
                                  searchKey: 'OBPOS_payment.voucher',
                                  name: 'Voucher',
                                  startingCash: 0,
                                  totalSales: 50,
                                  totalReturns: 0,
                                  totalDeposits: 0,
                                  totalDrops: 0,
                                  rate: '1',
                                  isocode: 'EUR',
                                  cashManagements: []
                                }
                              ]
                            }
                          }
                        ]
                      },
                      {
                        id: 'F5E63E5C9BE2B3854D236726836F80DE',
                        paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                        searchKey: 'OBPOS.payment.usacash',
                        name: 'USA Cash',
                        startingCash: 0,
                        totalSales: 100,
                        totalReturns: 0,
                        totalDeposits: 250,
                        totalDrops: 300,
                        rate: '0.76082',
                        isocode: 'USD',
                        cashManagements: [
                          {
                            id: '4133CE5C159D6F5561579E04F3CBCE60',
                            description:
                              'USA Cash - Backoffice transfer to VBS - USD',
                            amount: 250,
                            origAmount: 190.21,
                            type: 'deposit',
                            reasonId: '8C436C5AA7A446A58C47E0837C1B0475',
                            paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:24.222Z',
                            timezoneOffset: -120,
                            isocode: 'USD',
                            glItem: '2C9F5E221F9E45F5B9A48462B01C1477',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            isDraft: true
                          },
                          {
                            id: '0786608FE51D9C38B4E6A8DEBE4C5046',
                            description:
                              'USA Cash - Cashier shift (transfer to VBS backoffice) - USD',
                            amount: 300,
                            origAmount: 228.25,
                            type: 'drop',
                            reasonId: '9A90A1DAF459446895A6E6E4C21768C5',
                            paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
                            user: 'Vall Blanca Store User',
                            userId: '3073EDF96A3C42CC86C7069E379522D2',
                            creationDate: '2020-06-26T14:09:32.367Z',
                            timezoneOffset: -120,
                            isocode: 'USD',
                            glItem: 'FA646455DCC94D7C90CED9A6C00748E0',
                            cashup_id: 'EF694A2C8FD3F9A4990A74DD87ED8D10',
                            posTerminal: '9104513C2D0741D4850AE8493998A7C8',
                            isbeingprocessed: 'N',
                            defaultProcess: 'Y',
                            extendedType: '',
                            isDraft: true
                          }
                        ],
                        usedInCurrentTrx: true
                      },
                      {
                        id: '624C1AB9D2D97166091DA970BE2E8F43',
                        paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
                        searchKey: 'OBPOS_payment.voucher',
                        name: 'Voucher',
                        startingCash: 0,
                        totalSales: 50,
                        totalReturns: 0,
                        totalDeposits: 0,
                        totalDrops: 0,
                        rate: '1',
                        isocode: 'EUR',
                        cashManagements: []
                      }
                    ]
                  }
                }
              ]
            },
            {
              id: '624C1AB9D2D97166091DA970BE2E8F43',
              paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
              searchKey: 'OBPOS_payment.voucher',
              name: 'Voucher',
              startingCash: 0,
              totalSales: 50,
              totalReturns: 0,
              totalDeposits: 0,
              totalDrops: 0,
              rate: '1',
              isocode: 'EUR',
              cashManagements: []
            }
          ],
          cashUpDate: '2020-06-26T14:36:53.170Z',
          lastcashupeportdate: '2020-06-26T14:36:53.170Z',
          timezoneOffset: -120,
          cashMgmtIds: [
            '355EEFF0868771961EC5C1471498671C',
            'E5D7F0D3C1F600B9C9661D17C077DD9E',
            '4133CE5C159D6F5561579E04F3CBCE60',
            '0786608FE51D9C38B4E6A8DEBE4C5046'
          ]
        }
      ]
    }
  },
  {
    id: '254D0D182905168F76FB9ADCB7C7EC81',
    type: 'backend',
    modelName: 'OBPOS_CashUp',
    name: 'OBPOS_CashUp',
    service: 'org.openbravo.retail.posterminal.ProcessCashClose',
    time: 1593184229010,
    messageObj: {
      id: '3CF92A24A63491CFCD674FC032AD9CE5',
      terminal: 'VBS-1',
      cacheSessionId: 'DD9EBD1620664C8593FC1AD644F86821',
      data: [
        {
          id: 'FDAB894083D7A22D971A42AF3C63EBF3',
          netSales: 0,
          grossSales: 0,
          netReturns: 0,
          grossReturns: 0,
          totalRetailTransactions: 0,
          totalStartings: 250,
          creationDate: '2020-06-26T14:36:53.173Z',
          userId: '3073EDF96A3C42CC86C7069E379522D2',
          posterminal: '9104513C2D0741D4850AE8493998A7C8',
          isprocessed: false,
          cashTaxInfo: [],
          cashCloseInfo: [],
          cashPaymentMethodInfo: [
            {
              id: '21A3F0739E4DA84D1F31FEF0281B1C23',
              paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
              searchKey: 'OBPOS_payment.card',
              name: 'Card',
              startingCash: 0,
              initialCounted: 0,
              totalSales: 0,
              totalReturns: 0,
              totalDeposits: 0,
              totalDrops: 0,
              rate: '1',
              isocode: 'EUR',
              newPaymentMethod: true,
              cashManagements: []
            },
            {
              id: '9CC37F3EDAE5F37D55D37F783A2AE5E1',
              paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
              searchKey: 'OBPOS_payment.cash',
              name: 'Cash',
              startingCash: 200,
              initialCounted: 200,
              totalSales: 0,
              totalReturns: 0,
              totalDeposits: 0,
              totalDrops: 0,
              rate: '1',
              isocode: 'EUR',
              newPaymentMethod: true,
              cashManagements: []
            },
            {
              id: '582637FC4A84DBC7F3E3BCBEA385BF30',
              paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
              searchKey: 'OBPOS.payment.usacash',
              name: 'USA Cash',
              startingCash: 50,
              initialCounted: 50,
              totalSales: 0,
              totalReturns: 0,
              totalDeposits: 0,
              totalDrops: 0,
              rate: '0.76082',
              isocode: 'USD',
              newPaymentMethod: true,
              cashManagements: []
            },
            {
              id: '718C1EC40B8A629F4DA732C46DB2709E',
              paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
              searchKey: 'OBPOS_payment.voucher',
              name: 'Voucher',
              startingCash: 0,
              initialCounted: 0,
              totalSales: 0,
              totalReturns: 0,
              totalDeposits: 0,
              totalDrops: 0,
              rate: '1',
              isocode: 'EUR',
              newPaymentMethod: true,
              cashManagements: []
            }
          ]
        }
      ]
    }
  }
];
