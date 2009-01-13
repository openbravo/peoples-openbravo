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
package org.openbravo.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.ParallelPort;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class CommPortsOpen {
    /** How long to wait for the open to finish up. */
    public static final int TIMEOUTSECONDS = 30;
    /** The baud rate to use. */
    public static final int BAUD = 9600;
    /** The input stream */
    protected DataInputStream is;
    /** The output stream */
    protected PrintStream os;
    /** The chosen Port Identifier */
    CommPortIdentifier thePortID;
    /** The chosen Port itself */
    CommPort thePort;

    public static void main(String[] args) throws Exception, IOException,
            NoSuchPortException, PortInUseException,
            UnsupportedCommOperationException {
        if (args.length != 1) {
            System.err.println("Usage: CommPortsOpen filename");
            System.exit(1);
        }
        String inputFileName = args[0];
        CommPortsOpen cpo = new CommPortsOpen();
        cpo.transmit_data(inputFileName);
        cpo.close();
        System.exit(0);
    }

    public CommPortsOpen() throws Exception, IOException, NoSuchPortException,
            PortInUseException, UnsupportedCommOperationException {
        choosePort(CommPortIdentifier.PORT_PARALLEL);

        System.out.println("Trying to open " + thePortID.getName() + "...");
        switch (thePortID.getPortType()) {
        case CommPortIdentifier.PORT_SERIAL:
            thePort = thePortID.open("Openbravo CommData",
                    TIMEOUTSECONDS * 1000);
            SerialPort myPort = (SerialPort) thePort;
            // set up the serial port
            myPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            break;
        case CommPortIdentifier.PORT_PARALLEL:
            thePort = thePortID.open("Openbravo Printing",
                    TIMEOUTSECONDS * 1000);
            ParallelPort pPort = (ParallelPort) thePort;
            int mode = pPort.getMode();
            switch (mode) {
            case ParallelPort.LPT_MODE_ECP:
                System.out.println("Mode is: ECP");
                break;
            case ParallelPort.LPT_MODE_EPP:
                System.out.println("Mode is: EPP");
                break;
            case ParallelPort.LPT_MODE_NIBBLE:
                System.out.println("Mode is: Nibble Mode.");
                break;
            case ParallelPort.LPT_MODE_PS2:
                System.out.println("Mode is: Byte mode.");
                break;
            case ParallelPort.LPT_MODE_SPP:
                System.out.println("Mode is: Compatibility mode.");
                break;
            // ParallelPort.LPT_MODE_ANY is a "set only" mode;
            // tells the API to pick "best mode"; will report the
            // actual mode it selected.
            default:
                throw new IllegalStateException("Parallel mode " + mode
                        + " invalid.");
            }
            break;
        default:// Neither parallel nor serial??
            throw new IllegalStateException("Unknown port type " + thePortID);
        }
        // Get the input and output streams
        // Printers can be write-only
        try {
            is = new DataInputStream(thePort.getInputStream());
        } catch (IOException e) {
            System.err.println("Can't open input stream: write-only");
            is = null;
        }
        os = new PrintStream(thePort.getOutputStream(), true);
    }

    private void choosePort(int portType) throws Exception {
        Enumeration<?> listaPort = CommPortIdentifier.getPortIdentifiers();
        if (portType == 0)
            portType = CommPortIdentifier.PORT_PARALLEL;
        while (listaPort.hasMoreElements()) {
            thePortID = (CommPortIdentifier) listaPort.nextElement();
            if (thePortID.getPortType() == portType) {
                if (thePortID.isCurrentlyOwned())
                    throw new Exception("The port is use now by: "
                            + thePortID.getCurrentOwner());
                return;
            }
        }
        thePortID = null;
        throw new Exception("port not found for type: " + portType);
    }

    public void transmit(String data) throws IOException {
        os.write(data.getBytes());
    }

    public void close() throws IOException {
        System.out.println("Closing port.");
        if (is != null)
            is.close();
        os.close();
        thePort.close();
        thePort = null;
        thePortID = null;
    }

    protected void transmit_data(String inputFileName) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(inputFileName));
        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = file.readLine()) != null)
            sb.append(line).append("\r\n");
        System.out.println(sb.toString());
        transmit(sb.toString());
        // Finally, clean up.
        file.close();
    }
}
