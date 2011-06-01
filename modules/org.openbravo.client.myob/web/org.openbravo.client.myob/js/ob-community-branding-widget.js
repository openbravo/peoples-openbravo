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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
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
    var layout = isc.VStack.create({height:'100%', width:'100%', styleName:''});

    if(!OB.Application.brandingWidget) {
      // set a global pointer to ourselves
      OB.Application.brandingWidget = this;
    }

    layout.addMember(OB.Utilities.createLoadingLayout());

    var post = {
      'eventType': 'GET_COMMUNITY_BRANDING_URL',
      'context' : {
        'adminMode' : 'false'
      },
      'widgets' : []
    };

    var me = this;
    var haveInternet = false;
    /*
     * The following LAB.wait(callback) call does not reliably call the callout in case no
     * internet connection is present (so schedule timeout to use local fallback content after 10s)
     */
    var timerNoInternet = setTimeout(function() {
      me.setOBContent(false);
    }, 10000);
    $LAB.script(document.location.protocol + OB.Application.butlerUtilsUrl).wait(function() {
      haveInternet = (typeof internetConnection !== 'undefined');
      // callback did fire so clear timer as its no longer needed
      clearTimeout(timerNoInternet);

      if (haveInternet) {
        OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function(response, data, request) {
          var communityBrandingUrl = data.url;
          me.setOBContent(haveInternet, communityBrandingUrl);
        });
      } else {
          me.setOBContent(false);
        }
    });

    return layout;
  },

  setOBContent: function(haveInternet, communityBrandingUrl) {
    var url, params = {};

    if (haveInternet) {
      url = document.location.protocol + communityBrandingUrl;
    } else {
      url = OB.Application.contextUrl + OB.Application.communityBrandingStaticUrl;
      params = {'uimode': 'MyOB'};
    }

    var layout = this.windowContents;

    // remove Loading...
    var loadingBar = layout.members[this.windowContents.members.length-1];

    this.versionLabel = isc.Label.create({contents: this.versionText,
      height: '36px',
      width:'100%',
      styleName: this.getPurposeStyleClass(),
      align: 'center'
    });

    var content = isc.HTMLFlow.create({
        contentsType: 'page',
        contentsURL: url,
        contentsURLParams: params,
        height: '324px',
        width: '100%'
      });

    layout.removeMember(loadingBar);
    layout.addMember(this.versionLabel);
    layout.addMember(content);
  },

  update: function() {
    //FIXME: too expensive
    OB.MyOB.reloadWidgets();
//    this.versionLabel.clear();
//    this.versionLabel.contents = this.versionText;
//    this.versionLabel.styleName = this.getPurposeStyleClass();
//    this.versionLabel.draw();
  },

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
            return OB.I18N.getLabel('OBKMO_LearnMore');
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
