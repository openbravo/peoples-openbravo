/*
 ************************************************************************************
 * Copyright (C) 2001-2007 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/

package org.openbravo.utils;

import java.io.IOException;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.servlet.ServletException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoUtility {
  private static Cipher s_cipher = null;
  private static SecretKey s_key = null;

  public CryptoUtility() {
  }

  public static void main(String argv[]) throws Exception {
    System.out.println("Enter encryption password:  ");
    System.out.flush();
    String clave = argv[0];
    System.out.println("************* " + clave);
    String strEnc = CryptoUtility.encrypt(clave);
    System.out.println("ENCRYPTED TEXT: " + strEnc);
    System.out.println("DECRYPTED TEXT: " + CryptoUtility.decrypt(strEnc));
  }

  private static void initCipher() {
    try {
      s_key = new SecretKeySpec(new byte[] {
        100, 25, 28, -122, -26, 94, -3, -72
      }, "DES");
      s_cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public static String encrypt(String value) throws ServletException {
    byte encString[];
    String clearText;
    clearText = value;
    if(clearText == null) clearText = "";
    if(s_cipher == null) initCipher();
    if(s_cipher == null) throw new ServletException("CryptoUtility.encrypt() - Can't load cipher");
    String result = "";
    try {
      s_cipher.init(Cipher.ENCRYPT_MODE, s_key);
      encString = s_cipher.doFinal(clearText.getBytes());
      result = new BASE64Encoder().encode(encString);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ServletException("CryptoUtility.encrypt() - Can't init cipher");
    }
    return result;
  }

  public static String decrypt(String value) throws ServletException {
    if(value == null) return null;
    if(value.length() == 0) return value;
    if(s_cipher == null) initCipher();
    if(s_cipher == null || value == null || value.length() <= 0) throw new ServletException("CryptoUtility.decrypt() - Can't load cipher");
    byte out[];
    byte decode[];
    try {
      decode = new BASE64Decoder().decodeBuffer(value);
      s_cipher.init(Cipher.DECRYPT_MODE, s_key, s_cipher.getParameters());
      out = s_cipher.doFinal(decode);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ServletException("CryptoUtility.decrypt() - Can't init cipher");
    }
    return new String(out);
  }
}
