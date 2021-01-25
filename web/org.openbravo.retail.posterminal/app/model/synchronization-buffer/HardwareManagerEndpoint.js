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
    }

    /**
     * Initializes the hardware manager information and displays the welcome message
     *
     * @see printWelcome
     */
    async initHardwareManager() {
      try {
        const data = await this.controller.getHardwareManagerStatus();

        if (Object.keys(data).length > 0) {
          await this.printWelcome();
        }

        // Save hardware manager information
        const { version, revision, javaInfo } = data;
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
        const template = await OB.App.PrintTemplateStore.get(
          'printWelcomeTemplate'
        );
        await this.controller.display(template);
      } catch (error) {
        OB.error(`Error displaying welcome message: ${error}`);
      }
    }
  }

  OB.App.Class.HardwareManagerEndpoint = HardwareManagerEndpoint;
})();
