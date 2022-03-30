/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the HardwareManagerEndpoint class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function HardwareManagerEndpointDefinition() {
  /**
   * A synchronization endpoint in charge of the messages for communicating with external devices.
   */
  class HardwareManagerEndpoint extends OB.App.Class.SynchronizationEndpoint {
    /**
     * Base constructor, it can be used once the terminal information is loaded.
     * That means that this endpoint should not be registered until that information is ready.
     */
    constructor(name) {
      super(name || 'HardwareManager');

      const remoteServerName = 'HardwareManagerServer';
      OB.App.RemoteServerController.subscribe(this.name, remoteServerName);

      this.controller = new OB.App.Class.ExternalDeviceController();
      this.addMessageSynchronization('initHardwareManager', async () => {
        await this.initHardwareManager();
      });
      this.addMessageSynchronization('printWelcome', async () => {
        await this.printWelcome();
      });
      this.addMessageSynchronization('openDrawer', async () => {
        await this.openDrawer();
      });
      this.addMessageSynchronization('display', async message => {
        await this.display(message);
      });
    }

    /**
     * Initializes the hardware manager information and displays the welcome message
     *
     * @see printWelcome
     */
    async initHardwareManager() {
      let status = {};

      try {
        status = await this.controller.getHardwareManagerStatus();

        // Save hardware manager information
        const { version, revision, javaInfo } = status;
        if (version) {
          // Max database string size: 10
          const hwmVersion =
            version.length > 10 ? version.substring(0, 9) : version;
          OB.UTIL.localStorage.setItem('hardwareManagerVersion', hwmVersion);
        }
        if (revision) {
          // Max database string size: 15
          const hwmRevision =
            revision.length > 15 ? revision.substring(0, 14) : revision;
          OB.UTIL.localStorage.setItem('hardwareManagerRevision', hwmRevision);
        }
        if (javaInfo) {
          OB.UTIL.localStorage.setItem('hardwareManagerJavaInfo', javaInfo);
        }
      } catch (error) {
        OB.error(`Error initializing hardware manager: ${error}`);
      }

      if (!status.notConfigured) {
        // HWM is configured, display the welcome message
        await this.printWelcome();
      }
    }

    /**
     * Opens the drawer device
     */
    async openDrawer() {
      try {
        await this.controller.openDrawer();
      } catch (error) {
        OB.error(`Error opening the drawer: ${error}`);
      }
    }

    /**
     * Prints the welcome message into the display device
     */
    async printWelcome() {
      try {
        const template = OB.App.PrintTemplateStore.get('printWelcomeTemplate');
        await this.controller.display(template);
      } catch (error) {
        OB.error(`Error displaying welcome message: ${error}`);
      }
    }

    /**
     * Displays information into the display device using a given template
     */
    async display(message) {
      const messageData = message.messageObj;
      try {
        const { template, displayData } = messageData.data;
        const printTemplate = OB.App.PrintTemplateStore.get(template);
        await this.controller.display(printTemplate, displayData);
      } catch (error) {
        OB.error(`Error displaying information in display device: ${error}`);
      }
    }
  }

  OB.App.Class.HardwareManagerEndpoint = HardwareManagerEndpoint;
})();