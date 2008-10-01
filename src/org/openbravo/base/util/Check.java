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
 * Collection of static utility methods for checking variable state and
 * arguments.
 * 
 * @author mtaal
 */
public class Check {
  
  public static void fail(String message) {
    throw new CheckException(message);
  }
  
  public static void isTrue(boolean value, String message) {
    if (!value) {
      throw new CheckException(message);
    }
  }
  
  public static void isFalse(boolean value, String message) {
    if (value) {
      throw new CheckException(message);
    }
  }
  
  public static void isNotNull(Object value, String message) {
    if (value == null) {
      throw new CheckException(message);
    }
  }
  
  public static void isNull(Object value, String message) {
    if (value != null) {
      throw new CheckException(message);
    }
  }
  
  public static void notEmpty(String value, String message) {
    if (value == null || value.trim().length() == 0) {
      throw new ArgumentException(message);
    }
  }
  
  public static void notEmpty(Object[] array, String message) {
    if (array == null || array.length == 0) {
      throw new CheckException(message);
    }
  }
  
  // Checks if the passed object is of the class specified, null values are
  // ignored
  public static void isInstanceOf(Object obj, Class<?> expClass) {
    if (obj == null) {
      return;
    }
    if (!(expClass.isAssignableFrom(obj.getClass()))) {
      throw new CheckException("Expected class: " + expClass.getName() + " but object has class: " + obj.getClass().getName());
    }
  }
  
  // Checks object memory equality
  public static void isSameObject(Object obj1, Object obj2) {
    if (obj1 != obj2) {
      throw new CheckException("Objects are not the same");
    }
  }
  
  // Checks object memory inequality
  public static void isNotSameObject(Object obj1, Object obj2) {
    if (obj1 == obj2) {
      throw new CheckException("Objects are not the same");
    }
  }
  
  private Check() {
  }
}
