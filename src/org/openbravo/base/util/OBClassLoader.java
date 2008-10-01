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
package org.openbravo.base.util;

/**
 * The OBClassLoader which can be from the outside. Two classloaders are
 * supported: the context (the default) and the class classloader.
 * 
 * @author mtaal
 */

public class OBClassLoader {
  
  private static OBClassLoader instance = new OBClassLoader();
  
  public static void setClassClassLoader() {
    setInstance(new OBClassLoader() {
      @Override
      public Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
      }
    });
  }
  
  public static void setSpecificClassClassLoader(Class<?> clz) {
    setInstance(new SpecificOBClassLoader(clz));
  }
  
  public static OBClassLoader getInstance() {
    return instance;
  }
  
  public static void setInstance(OBClassLoader instance) {
    OBClassLoader.instance = instance;
  }
  
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return Thread.currentThread().getContextClassLoader().loadClass(className);
  }
  
  private static class SpecificOBClassLoader extends OBClassLoader {
    private final Class<?> clz;
    
    SpecificOBClassLoader(Class<?> clz) {
      this.clz = clz;
    }
    
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
      return clz.getClassLoader().loadClass(className);
    }
  }
}