/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.dal.core;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.SessionFactoryController;

/**
 * This class is responsible for initializing the dal layer. It ensures that the
 * model is read in memory and that the mapping is generated in a two stage
 * process.
 * 
 * @author mtaal
 */

public class DalLayerInitializer {
  private static final Logger log = Logger.getLogger(DalLayerInitializer.class);
  
  private static DalLayerInitializer instance = new DalLayerInitializer();
  
  public static DalLayerInitializer getInstance() {
    return instance;
  }
  
  public static void setInstance(DalLayerInitializer dalLayerInitializer) {
    instance = dalLayerInitializer;
  }
  
  private boolean initialized = false;
  
  public void initialize() {
    log.info("Initializing in-memory model...");
    try {
      ModelProvider.getInstance().getModel();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new OBException(e);
    }
    
    log.info("Model read in-memory, generating mapping...");
    SessionFactoryController.setInstance(new DalSessionFactoryController());
    SessionFactoryController.getInstance().initialize();
    
    // reset the session
    SessionHandler.deleteSessionHandler();
    
    log.info("Dal layer initialized");
    initialized = true;
  }
  
  public boolean isInitialized() {
    return initialized;
  }
  
  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }
}
