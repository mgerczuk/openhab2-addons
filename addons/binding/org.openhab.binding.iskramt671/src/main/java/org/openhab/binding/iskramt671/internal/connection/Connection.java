/*
 * Copyright 2013-14 Fraunhofer ISE
 *
 * This file is part of j62056.
 * For more information visit http://www.openmuc.org
 *
 * j62056 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * j62056 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with j62056.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openhab.binding.iskramt671.internal.connection;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final String serialPortName;
    private SerialPort serialPort;

    private int timeout = 5000;

    private DataOutputStream os;
    private DataInputStream is;

    private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final Charset charset = Charset.forName("US-ASCII");

    /**
     * Creates a Connection object. You must call <code>open()</code> before
     * calling <code>read()</code> in order to read data. The timeout is set by
     * default to 5s.
     * 
     * @param serialPort
     *            examples for serial port identifiers are on Linux "/dev/ttyS0"
     *            or "/dev/ttyUSB0" and on Windows "COM1"
     * @param initMessage
     *            extra pre init bytes
     * @param handleEcho
     *            tells the connection to throw away echos of outgoing messages.
     *            Echos are caused by some optical transceivers.
     * @param baudRateChangeDelay
     *            tells the connection the time in ms to wait before changing
     *            the baud rate during message exchange. This parameter can
     *            usually be set to zero for regular serial ports. If a USB to
     *            serial converter is used, you might have to use a delay of
     *            around 250ms because otherwise the baud rate is changed before
     *            the previous message (i.e. the acknowledgment) has been
     *            completely sent.
     */
    public Connection(String serialPort, byte[] initMessage, boolean handleEcho, int baudRateChangeDelay) {
        if (serialPort == null) {
            throw new IllegalArgumentException("serialPort may not be NULL");
        }

        serialPortName = serialPort;
    }

    /**
     * Creates a Connection object. The option handleEcho is set to false and
     * the baudRateChangeDelay is set to 0.
     * 
     * @param serialPort
     *            examples for serial port identifiers on Linux are "/dev/ttyS0"
     *            or "/dev/ttyUSB0" and on Windows "COM1"
     */
    public Connection(String serialPort) {
        this(serialPort, null, false, 0);
    }

    /**
     * Sets the maximum time in ms to wait for new data from the remote device.
     * A timeout of zero is interpreted as an infinite timeout.
     *
     * @param timeout
     *            the maximum time in ms to wait for new data.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the timeout in ms.
     *
     * @return the timeout in ms.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Opens the serial port associated with this connection.
     *
     * @throws IOException
     *             if any kind of error occurs opening the serial port.
     */
    public void open() throws IOException {

        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(new File(serialPortName).getCanonicalPath());
        } catch (NoSuchPortException e) {
            throw new IOException("Serial port with given name does not exist", e);
        }

        if (portIdentifier.isCurrentlyOwned()) {
            throw new IOException("Serial port is currently in use.");
        }

        // fixed issue as rxtx library originally used in j62056 does use
        // different version of rxtx
        // com port in their version is using gnu.io.CommPort
        RXTXPort commPort;
        try {
            commPort = portIdentifier.open(this.getClass().getName(), 2000);
        } catch (PortInUseException e) {
            throw new IOException("Serial port is currently in use.", e);
        }

        if (!(commPort instanceof SerialPort)) {
            commPort.close();
            throw new IOException("The specified CommPort is not a serial port");
        }

        serialPort = commPort;

        try {
            os = new DataOutputStream(serialPort.getOutputStream());
            is = new DataInputStream(serialPort.getInputStream());
        } catch (IOException e) {
            serialPort.close();
            serialPort = null;
            throw new IOException("Error getting input or output or input stream from serial port", e);
        }

    }

    /**
     * Closes the serial port.
     */
    public void close() {
        if (serialPort == null) {
            return;
        }
        serialPort.close();
        serialPort = null;
    }

    /**
     * Requests a data message from the remote device using IEC 62056-21 Mode C.
     * The data message received is parsed and a list of data sets is returned.
     *
     * @return A list of data sets contained in the data message response from
     *         the remote device. The first data set will contain the
     *         "identification" of the meter as the id and empty strings for
     *         value and unit.
     * @throws IOException
     *             if any kind of error other than timeout occurs while trying
     *             to read the remote device. Note that the connection is not
     *             closed when an IOException is thrown.
     * @throws TimeoutException
     *             if no response at all (not even a single byte) was received
     *             from the meter within the timeout span.
     */
    public List<DataSet> read() throws IOException, TimeoutException {

        if (serialPort == null) {
            throw new IllegalStateException("Connection is not open.");
        }

        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException("Unable to set the given serial comm parameters", e);
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        List<DataSet> dataSets = new ArrayList<DataSet>();
        String line = rd.readLine();
        while (!line.startsWith("!")) {
            DataSet dataSet;
            dataSet = parseIdentifier(line);
            if (dataSet != null) {
                dataSets.add(dataSet);
            }

            dataSet = parseDataSet(line);
            if (dataSet != null) {
                dataSets.add(dataSet);
            }

            line = rd.readLine();
        }
        return dataSets;

    }

    private DataSet parseIdentifier(String line) {
        if (!line.startsWith("/")) {
            return null;
        }

        return new DataSet(line.substring(1), "", "");
    }

    private DataSet parseDataSet(String line) {
        int openBrace = line.indexOf('(');
        if (openBrace < 0) {
            return null;
        }

        String id = line.substring(0, openBrace);
        String value, unit;

        int asterisk = line.indexOf('*', openBrace + 1);
        if (asterisk < 0) {
            int closeBrace = line.indexOf(')', openBrace + 1);
            value = line.substring(openBrace + 1, closeBrace);
            unit = "";
        } else {
            int closeBrace = line.indexOf(')', asterisk + 1);
            value = line.substring(openBrace + 1, asterisk);
            unit = line.substring(asterisk + 1, closeBrace);
        }

        DataSet dataSet = new DataSet(id, value, unit);
        return dataSet;
    }
}
