/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
/** 
 * PeriodicGarbageCollector.  
 * @version 1.0.0 11/5/2004
 * @author Carlos Romero Herrero
*/
package org.openbravo.base;
import java.io.*;
import org.apache.log4j.Logger ;

/**
 * PeriodicGarbageCollector
 * @version 1.0.0 11/5/2004
 * @author Carlos Romero Herrero
*/
public class PeriodicGarbageCollector implements Runnable {
  static Logger log4j = Logger.getLogger(PeriodicGarbageCollector.class);
  private Thread runner;
  private long seconds;

  /**
   * Creates a new periodic garbage collector<br>
   * time:            Number in seconds between two garbage collections<br>
   */
  public PeriodicGarbageCollector(long seconds) throws IOException 
  {
    // Fire up the background housekeeping thread
    runner = new Thread(this);
    runner.start();
    this.seconds = seconds;
  }


  /**
   * Housekeeping thread.  Runs in the background with low CPU overhead.
   * This method acts as fault tolerance for bad connection/statement programming.
   */
  public void run()
  {
    boolean forever = true;

    while(forever) {
      try { 
        if (log4j.isDebugEnabled()) log4j.debug("Sleeping for "+seconds+" seconds.");
        Thread.sleep((seconds*1000)); 
        if (log4j.isDebugEnabled()) log4j.debug("gc begins");
        long inicio = System.currentTimeMillis();
        System.gc(); 
        long fin = System.currentTimeMillis();
        if (log4j.isDebugEnabled()) log4j.debug("gc ended: "+(fin-inicio)+" miliseconds");
        if (log4j.isDebugEnabled()) log4j.debug("Finalization begins");
        inicio = System.currentTimeMillis();
        System.runFinalization();
        fin = System.currentTimeMillis();
        if (log4j.isDebugEnabled()) log4j.debug("Finalization ended: "+(fin-inicio)+" miliseconds");
        if (log4j.isDebugEnabled()) log4j.debug(mostrarMemoria());
      }  // Wait time miliseconds for next cycle
      catch(InterruptedException e) {
        // Returning from the run method sets the internal 
        // flag referenced by Thread.isAlive() to false.
        // This is required because we don't use stop() to 
        // shutdown this thread.
        return;
      }
    }

  } // End run

  public String mostrarMemoria(String texto) {
    return (texto + "\n" + mostrarMemoria());
  }

  public String mostrarMemoria() {
    StringBuffer result = new StringBuffer();
    result.append("Maximum memory :").append(Runtime.getRuntime().maxMemory());
    result.append("Total memory  :").append(Runtime.getRuntime().totalMemory());
    result.append("Free memory  :").append(Runtime.getRuntime().freeMemory());
    result.append("% free memory:").append((100*Runtime.getRuntime().freeMemory()/Runtime.getRuntime().totalMemory())).append("%");
    return result.toString();
  }

}// End class

