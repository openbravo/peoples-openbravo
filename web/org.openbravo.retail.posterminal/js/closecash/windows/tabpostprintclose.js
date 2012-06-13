/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.PostPrintClose = OB.COMP.CustomView.extend({
  _id: 'postprintclose',
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'postprintclose', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                      OB.I18N.getLabel('OBPOS_LblStep3of3')
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; text-align:center;'}, content: [
                       {kind: B.KindJQuery('img'), attr: {'style': 'padding: 20px 20px 20px 10px;', 'src':'http://www.openbravo.com/img-corp/logotypes/ob-logo.gif'}, content:[]},
                       {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px; text-align:center;'}, content:[
                             'User: Salvador'
                         ]},
                         {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 5px 15px 5px; text-align:center;'}, content:[
                            'Time: '+ new Date().toString().substring(3,24)
                        ]}
                     ]}
                  ]}
                ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Net Sales'
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '15.000,00'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Tax 18%*12500'
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '2.250,00'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Tax 6%*2500'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                         '150,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                         'Gross Sales'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                         '17.400,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
             {kind: B.KindJQuery('div')}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                         {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                    ]}
                ]},
                {kind: B.KindJQuery('div')}
               ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                     {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                        'Net Returns'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                            '100,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
             ]},
             {kind: B.KindJQuery('div')}
           ]},
           {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                     {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                        'Tax 18%*100'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                            '18,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
             ]},
             {kind: B.KindJQuery('div')}
           ]},
           {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
               {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                        'Gross Returns'
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                        '118,00'
               ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
             ]},
             {kind: B.KindJQuery('div')}
           ]},
           {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                         {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                    ]}
                ]},
                {kind: B.KindJQuery('div')}
               ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
 {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
 'TOTAL RETAIL TRANSACTIONS'
  ]},
  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
 '17282,00'
  ]},
  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
  ]}
 ]},
 {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
 {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                ]}
            ]},
            {kind: B.KindJQuery('div')}
           ]},
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      'Cash drop env 01'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          '500,00'
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]},
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      'Cash drop env 02'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          '750,00'
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]},
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      'Cash drop env 03'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          '200,00'
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]},
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      'Voucher drop'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                          '100,00'
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]},
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                      'Cash returns'
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                      '118,00'
             ]},
             {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
           {kind: B.KindJQuery('div')}
         ]},
         {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
             {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                      'Total Drops'
               ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                      '1668,00'
             ]},
             {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
           ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
 {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                  ]}
              ]},
             {kind: B.KindJQuery('div')}
             ]},
             {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                       'Cash sales'
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                           '7608,00'
               ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]},
  
            {kind: B.KindJQuery('div')}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                       'Voucher sales'
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                           '200,00'
               ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]},
            {kind: B.KindJQuery('div')}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                       'Card sales'
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                           '9582,00'
               ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]},
  
            {kind: B.KindJQuery('div')}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                       'Starting cash'
                 ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                           '250,00'
               ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]},
            {kind: B.KindJQuery('div')}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                       'Cash deposit'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                       '300,00'
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]},
  
            {kind: B.KindJQuery('div')}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                       'Total Deposits'
                ]},
               {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                       '17940,00'
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
            ]},
               {kind: B.KindJQuery('div')}
             ]},
             {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
             
                {kind: B.KindJQuery('div')}
                ]},    {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                   ]}
               ]},
               {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                             'Cash expected'
                       ]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                                 '6.590,00'
                     ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                  {kind: B.KindJQuery('div')}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                             'Card expected'
                       ]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                                 '9.582,00'
                     ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                  {kind: B.KindJQuery('div')}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                             'Voucher expected'
                      ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '100,00'
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                  {kind: B.KindJQuery('div')}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                             'Expected amount'
                      ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                             '16.272,00'
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                 {kind: B.KindJQuery('div')}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                        {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                             {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                        ]}
                    ]},
                    {kind: B.KindJQuery('div')}
                    ]},
                 {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                             'Cash actual'
                       ]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                                 '6.590,00'
                     ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                  {kind: B.KindJQuery('div')}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                             'Card actual'
                       ]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                                 '9.582,00'
                     ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                  {kind: B.KindJQuery('div')}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                             'Voucher actual'
                      ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '104,00'
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                  {kind: B.KindJQuery('div')}
                ]},
                {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                             'Expected amount'
                      ]},
                     {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                             '16.266,00'
                    ]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
                  ]},
                 {kind: B.KindJQuery('div')}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                        {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                        {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                             {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                        ]}
                    ]},
                {kind: B.KindJQuery('div')}
              ]},
             {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Cash difference'
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '-10,00'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                      {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                      {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Card difference'
                   ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                             '0,00'
                 ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%'}, content: [
                         'Voucher difference'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;'}, content: [
                         '4,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
              {kind: B.KindJQuery('div')}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;'}, content: [
                         'Balancing difference'
                  ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;'}, content: [
                         '-6,00'
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]}
              ]},
             {kind: B.KindJQuery('div')}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                         {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                    ]}
                ]},
          {kind: B.KindJQuery('div')}
        ]}
              
          ]}
        ]}
       ]}
      );
    }
  });
});