/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBCommunityBrandingWidget =
//
// Implements the community branding widget.
//
isc.defineClass('OBCommunityBrandingWidget', isc.OBWidget).addProperties({
  bodyColor: '#e9e9e9',
  versionText: OB.Application.versionDescription,
  headerLabel: null,

  createWindowContents: function(){
    var layout = isc.VStack.create({height:'100%', width:'100%', styleName:''}),
        url, params = {};

    if(!OB.Application.brandingWidget) {
      // set a global pointer to ourselves
      OB.Application.brandingWidget = this;
    }
    
    this.versionLabel = isc.Label.create({contents: this.versionText,
                                         height: '36px',
                                         width:'100%',
                                         styleName: this.getPurposeStyleClass(),
                                         align: 'center'});

    layout.addMember(this.versionLabel);

    // note internetConnection is a global var set by a call to
    // the butler service
    if (typeof internetConnection !== 'undefined') {
      url = document.location.protocol + OB.Application.communityBrandingUrl;
    } else {
      url = OB.Application.contextUrl + OB.Application.communityBrandingStaticUrl;
      params = {'uimode': 'MyOB'};
    }

    layout.addMember(isc.HTMLFlow.create({
      contentsType: 'page',
      contentsURL: url,
      contentsURLParams: params,
      height: '324px',
      width: '100%'
    }));
    return layout;
  },
  
  update: function() {
    //FIXME: too expensive
    OB.MyOB.reloadWidgets();
//    this.versionLabel.clear();
//    this.versionLabel.contents = this.versionText;
//    this.versionLabel.styleName = this.getPurposeStyleClass();
//    this.versionLabel.draw();
  },
  
  getBrandingHtml: function() {
    var html = this.brandingHtml;
    
    html = html.replace('{versionText}', this.versionText);
    html = html.replace('{brandingUrl}', url);
    html = html.replace('{purposeClass}', this.getPurposeStyleClass());
    return html;
  },
  
  brandingHtml: '<DIV id="communityBranding" class="OBWidgetCommunityBranding" border="0">' +
  '<DIV class="OBWidgetCommunityBrandingTitle">' +
  '<DIV class="{purposeClass}" alt="" title=""></DIV>' +
  '<DIV class="OBWidgetCommunityBrandingVersion">{versionText}</DIV>' +
  '</DIV>' +
  '</DIV>',
  
  getPurposeStyleClass: function(){
    var purposeCode = OB.Application.purpose;
    if (purposeCode === 'D') {
      return 'OBWidgetCommunityBrandingDevelopment';
    } else if (purposeCode === 'P') {
      return 'OBWidgetCommunityBrandingProduction';
    } else if (purposeCode === 'T') {
      return 'OBWidgetCommunityBrandingTesting';
    } else if (purposeCode === 'E') {
      return 'OBWidgetCommunityBrandingEvaluation';
    } else {
      return 'OBWidgetCommunityBrandingUnknown';
    }
  },

  confirmedClosePortlet: function(ok) {
  var activateButton;

   if (!ok) {
     this.Super('confirmedClosePortlet', arguments);
     return;
   }

   if(OB.Application.brandingWidget !== this) {
     this.Super('confirmedClosePortlet', arguments);
     return;
   }
  
   activateButton = isc.addProperties({}, isc.Dialog.OK, {
          getTitle: function() {
            return OB.I18N.getLabel('OBKMO_ActivateLabel');
          },
          click: function() {
            this.topElement.cancelClick();
            window.open('http://www.openbravo.com/product/erp/get-basic/');
          }});

    if(OB.Application.licenseType === 'C') {
      isc.confirm(OB.I18N.getLabel('OBKMO_ActivateMessage'), {
          isModal: true,
          showModalMask: true,
          toolbarButtons: [activateButton, isc.Dialog.CANCEL]
      });
      return;
    }
    this.Super('confirmedClosePortlet', arguments);	
  }
});
