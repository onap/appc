/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.ccadaptor;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;

public class SshJcraftWrapper {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(SshJcraftWrapper.class);

    private static final int BUFFER_SIZE = 512000;
    static final int DEFAULT_PORT = 22;
    static final String CHANNEL_SHELL_TYPE = "shell";
    static final String CHANNEL_SUBSYSTEM_TYPE = "subsystem";
    static final String TERMINAL_BASIC_MODE = "vt102";
    private InputStream inputStream = null;
    private TelnetListener listener = null;
    private String routerLogFileName = null;
    private String routerName = null;
    private char[] charBuffer = new char[BUFFER_SIZE];
    private DataInputStream dis = null;
    private BufferedReader reader = null;
    private BufferedWriter out = null;
    private File tmpFile = null;
    private JSch jsch = null;
    private Session session = null;
    private Channel channel = null;
    private String aggregatedReceivedString = "";
    private String routerCmdType = "XML";
    private String routerFileName = null;
    private File jcraftReadSwConfigFileFromDisk = new File("/tmp/jcraftReadSwConfigFileFromDisk");
    private String equipNameCode = null;
    private String hostName = null;
    private String userName = null;
    private String passWord = null;
    private Runtime runtime = Runtime.getRuntime();


    public SshJcraftWrapper() {
        this.jsch = new JSch();
    }

    SshJcraftWrapper(JSch jsch) {
        this.jsch = jsch;
    }

    public void connect(String hostname, String username, String password, String prompt, int timeOut)
        throws IOException {
        log.debug("Attempting to connect to {0} username={1} prompt='{2}' timeOut={3}",
            hostname, username, prompt, timeOut);
        routerName = hostname;
        hostName = hostname;
        userName = username;
        passWord = password;
        try {
            session = jsch.getSession(username, hostname, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            session.setPassword(password);
            session.setUserInfo(ui);
            session.connect(timeOut);
            channel = session.openChannel(CHANNEL_SHELL_TYPE);
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            ((ChannelShell) channel).setPtyType(TERMINAL_BASIC_MODE);
            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), BUFFER_SIZE);
            channel.connect();
            log.info("Successfully connected. Flushing input buffer.");
            try {
                receiveUntil(prompt, 3000, "No cmd was sent, just waiting");
            } catch (IOException e) {
                log.warn("Caught an Exception: Nothing to flush out.", e);
            }
        } catch (JSchException e) {
            log.error("Could not connect to host=" + hostname, e);
            throw new IOException(e.toString());
        }
    }

    // User specifies the port number.
    public void connect(String hostname, String username, String password, String prompt, int timeOut, int portNum)
        throws IOException {
        log.debug("Attempting to connect to {0} username={1} prompt='{2}' timeOut={3} portNum={4}",
            hostname, username, prompt, timeOut, portNum);
        routerName = hostname;
        hostName = hostname;
        userName = username;
        passWord = password;
        try {
            session = jsch.getSession(username, hostname, portNum);
            UserInfo ui = new MyUserInfo();
            session.setPassword(password);
            session.setUserInfo(ui);
            session.setConfig("StrictHostKeyChecking", "no");
            log.debug("StrictHostKeyChecking set to 'no'");

            session.connect(timeOut);
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            channel = session.openChannel(CHANNEL_SHELL_TYPE);
            ((ChannelShell) channel).setPtyType(TERMINAL_BASIC_MODE);
            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), BUFFER_SIZE);
            channel.connect();
            log.info("Successfully connected. Flushing input buffer.");
            try {
                if ("]]>]]>".equals(prompt)) {
                    receiveUntil("]]>]]>", 10000, "No cmd was sent, just waiting");
                } else {
                    receiveUntil(":~#", 5000, "No cmd was sent, just waiting");
                }
            } catch (IOException e) {
                log.warn("Caught an Exception: Nothing to flush out.", e);
            }
        } catch (JSchException e) {
            log.error("Could not connect to host=" + hostname, e);
            throw new IOException(e.toString());
        }
    }


    public String receiveUntil(String delimeters, int timeout, String cmdThatWasSent) throws IOException {
        boolean match = false;
        boolean cliPromptCmd = false;
        StringBuilder sb = new StringBuilder();
        StringBuilder sbReceive = new StringBuilder();
        log.debug("delimeters='{0}' timeout={1} cmdThatWasSent='{2}'", delimeters, timeout, cmdThatWasSent);
        int readCounts = 0;
        aggregatedReceivedString = "";
        FileWriter fileWriter = null;

        long deadline = new Date().getTime() + timeout;
        try {
            session.setTimeout(timeout);  // This is the socket timeout value.
            while (!match) {
                if (new Date().getTime() > deadline) {
                    String formattedCmd = removeWhiteSpaceAndNewLineCharactersAroundString(cmdThatWasSent);
                    log.error("Routine has timed out: routerName={0} CmdThatWasSent={1}", routerName, formattedCmd);
                    throw new TimedOutException("Routine has timed out");
                }
                try {
                    Thread.sleep(500);
                } catch (java.lang.InterruptedException ee) {
                    Thread.currentThread().interrupt();
                }
                int len = reader.read(charBuffer, 0, BUFFER_SIZE);
                log.trace("After reader. Read command len={0}", len);
                if (len <= 0) {
                    log.error("Reader failed to read any bytes. Suspected socket timeout, router={0}", routerName);
                    throw new TimedOutException("Received a SocketTimeoutException router=" + routerName);
                }
                if (!cliPromptCmd) {
                    if (cmdThatWasSent.indexOf("IOS_XR_uploadedSwConfigCmd") != -1) {
                        if (out == null) {
                            // This is a IOS XR sw config file. We will write it to the disk.
                            timeout = timeout * 2;
                            deadline = new Date().getTime() + timeout;
                            log.debug("IOS XR upload for software config: timeout={0}", timeout);
                            StringTokenizer st = new StringTokenizer(cmdThatWasSent);
                            st.nextToken();
                            routerFileName = st.nextToken();
                            fileWriter = new FileWriter(routerFileName);
                            out = new BufferedWriter(fileWriter);
                            routerLogFileName = "/tmp/" + routerName;
                            tmpFile = new File(routerLogFileName);
                            log.debug("Prepared for writing swConfigFile to disk, routerFileName=" + routerFileName);
                        }
                        out.write(charBuffer, 0, len);
                        out.flush();
                        log.debug("{0} bytes has been written to the disk", len);
                        if (tmpFile.exists()) {
                            appendToRouterFile(routerLogFileName, len);
                        }
                        match = checkIfReceivedStringMatchesDelimeter(len, "\nXML>");
                        if (match) {
                            out.flush();
                            out.close();
                            out = null;
                            return null;
                        }
                    } else {
                        readCounts++;
                        log.debug("Reader read {0} of data within {1} read iteration", len, readCounts);
                        int c;
                        sb.setLength(0);
                        for (int i = 0; i < len; i++) {
                            c = charBuffer[i];
                            if ((c != 7) && (c != 13) && (c != 0) && (c != 27)) {
                                sbReceive.append(charBuffer[i]);
                                sb.append(charBuffer[i]);
                            }
                        }
                        appendToRouterFile("/tmp/" + routerName, len);
                        if (listener != null) {
                            listener.receivedString(sb.toString());
                        }
                        match = checkIfReceivedStringMatchesDelimeter(delimeters, sb.toString(), cmdThatWasSent);
                        if (match) {
                            log.trace("Match was true, breaking the loop.");
                            break;
                        }
                    }
                } else {
                    log.trace("cliPromptCmd");
                    sb.setLength(0);
                    for (int i = 0; i < len; i++) {
                        sbReceive.append(charBuffer[i]);
                        sb.append(charBuffer[i]);
                    }
                    appendToRouterFile("/tmp/" + routerName, sb);
                    if (listener != null) {
                        listener.receivedString(sb.toString());
                    }
                    log.debug("sb2={0}  delimiters={1}", sb.toString(), delimeters);
                    if (sb.toString().contains("\nariPrompt>")) {
                        log.debug("Found ari prompt");
                        break;
                    }
                }
            }
        } catch (JSchException e) {
            log.error("JSchException occurred", e);
            throw new TimedOutException(e.getMessage());
        } catch (IOException e) {
            log.error("IOException occurred", e);
            throw new TimedOutException(e.getMessage());
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException ex) {
                log.warn("Failed to close fileWriter output stream", ex);
            }
        }
        return stripOffCmdFromRouterResponse(sbReceive.toString());
    }

    public boolean checkIfReceivedStringMatchesDelimeter(String delimeters, String receivedString,
        String cmdThatWasSent) {
        // The delimeters are in a '|' seperated string. Return true on the first match.
        log.debug("Entered checkIfReceivedStringMatchesDelimeter: delimeters={0} cmdThatWasSent={1} receivedString={2}",
            delimeters, cmdThatWasSent, receivedString);
        StringTokenizer st = new StringTokenizer(delimeters, "|");

        if ((delimeters.contains("#$")) || ("CLI".equals(routerCmdType)))  // This would be an IOS XR, CLI command.
        {
            int x = receivedString.lastIndexOf('#');
            int y = receivedString.length() - 1;
            log.debug("IOS XR, CLI command");
            if (log.isTraceEnabled()) {
                log.trace("cmdThatWasSent={0}, lastIndexOf hash delimiter={1}, maxIndexNum={2}", cmdThatWasSent, x, y);
            }
            return (x != -1) && (y == x);
        }
        if (cmdThatWasSent.contains("show config")) {
            log.trace("In the block for 'show config'");
            while (st.hasMoreTokens()) {
                String delimeter = st.nextToken();
                // Make sure we don't get faked out by a response of " #".
                // Proc #0
                //   # signaling-local-address ipv6 FD00:F4D5:EA06:1::110:136:254
                // LAAR2#
                int x = receivedString.lastIndexOf(delimeter);
                if ((receivedString.lastIndexOf(delimeter) != -1) && (receivedString.lastIndexOf(" #") != x - 1)) {
                    log.debug("receivedString={0}", receivedString);
                    log.trace("Found ending for 'show config' command, exiting.");
                    return true;
                }
            }
        } else {
            aggregatedReceivedString = aggregatedReceivedString + receivedString;
            appendToFile("/tmp/aggregatedReceivedString.debug", aggregatedReceivedString);

            log.debug("receivedString={0}", receivedString);
            while (st.hasMoreTokens()) {
                String delimeter = st.nextToken();
                log.debug("Looking for an delimiter of:{0}", delimeter);
                if (aggregatedReceivedString.indexOf(delimeter) != -1) {
                    log.debug("Found delimiter={0}, exiting", delimeter);
                    aggregatedReceivedString = "";
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIfReceivedStringMatchesDelimeter(int len, String delimeter) {
        int x;
        int c;
        String str = StringUtils.EMPTY;

        if (jcraftReadSwConfigFileFromDisk()) {
            log.trace("jcraftReadSwConfigFileFromDisk block");
            File fileName = new File(routerFileName);
            log.debug("jcraftReadSwConfigFileFromDisk::: Will read the tail end of the file from the disk");
            try {
                str = getLastFewLinesOfFile(fileName, 3);
            } catch (IOException e) {
                log.warn("IOException occurred, while reading file=" + fileName, e);
            }
        } else {
            // When looking at the end of the charBuffer, don't include any linefeeds or spaces. We only want to make the smallest string possible.
            for (x = len - 1; x >= 0; x--) {
                c = charBuffer[x];
                if ((c != 10) && (c != 32)) // Not a line feed nor a space.
                {
                    break;
                }
            }
            if ((x + 1 - 13) >= 0) {
                str = new String(charBuffer, x + 1 - 13, 13);
                log.debug("str:{0}", str);
            } else {
                File fileName = new File(routerFileName);
                log.debug("Will read the tail end of the file from the disk, x={0} len={1} str={2} routerFileName={3}",
                    x, len, str, routerFileName);
                try {
                    str = getLastFewLinesOfFile(fileName, 3);
                } catch (IOException e) {
                    log.warn("IOException occurred, while reading file=" + fileName, e);
                }
            }
        }

        log.debug("Parsed string was str='{0}', searched delimiter was {1}");
        return str.contains(delimeter);
    }

    public void closeConnection() {
        log.info("Closing connection");
        inputStream = null;
        dis = null;
        charBuffer = null;
        session.disconnect();
        session = null;
    }

    public void send(String cmd) throws IOException {
        try (OutputStream os = channel.getOutputStream(); DataOutputStream dos = new DataOutputStream(os)) {
            sendSshCommand(cmd, dos);
        } catch (IOException e) {
            log.error("IOException occurred while sending command=" + cmd, e);
            throw e;
        }
    }

    public void sendChar(int v) throws IOException {
        try (OutputStream os = channel.getOutputStream(); DataOutputStream dos = new DataOutputStream(os)) {
            if (log.isTraceEnabled()) {
                log.trace("Sending charCode: {0}", v);
            }
            dos.writeChar(v);
            dos.flush();
        } catch (IOException e) {
            log.error("IOException occurred while writing char to channel output stream", e);
            throw e;
        }
    }

    public void send(byte[] b, int off, int len) throws IOException {
        try (OutputStream os = channel.getOutputStream(); DataOutputStream dos = new DataOutputStream(os)) {
            dos.write(b, off, len);
            dos.flush();
        } catch (IOException e) {
            log.error("IOException occurred while writing bytes to channel output stream", e);
            throw e;
        }
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptYesNo(String str) {
            return false;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public void showMessage(String message) {
            //stub
        }

        @Override
        public String[] promptKeyboardInteractive(String destination,
            String name,
            String instruction,
            String[] prompt,
            boolean[] echo) {
            return new String[0];
        }
    }

    public void addListener(TelnetListener listener) {
        this.listener = listener;
    }

    private void appendToFile(String fileName, String dataToWrite) {
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            try (FileWriter fw = new FileWriter(fileName, true); BufferedWriter ow = new BufferedWriter(fw)) {
                ow.write(dataToWrite);
                ow.close();
            } catch (IOException e) {
                log.error("IOException occurred while writing to file=" + fileName, e);
            }
        }
    }

    public String getTheDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy H:mm:ss  ");
        return dateFormat.format(Calendar.getInstance().getTime());
    }


    public void appendToRouterFile(String fileName, StringBuilder dataToWrite) {
        appendToFile(fileName, dataToWrite.toString());
    }

    public void appendToRouterFile(String fileName, int len) {
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            try (FileWriter fw = new FileWriter(fileName, true); BufferedWriter ow = new BufferedWriter(fw)) {
                ow.write(charBuffer, 0, len);
                ow.close();
            } catch (IOException e) {
                log.error("IOException occurred while writing to router file=" + fileName, e);
            }
        }
    }

    public String removeWhiteSpaceAndNewLineCharactersAroundString(String str) {
        if (str != null && !StringUtils.EMPTY.equals(str)) {
            StringTokenizer strTok = new StringTokenizer(str, "\n");
            StringBuilder sb = new StringBuilder();

            while (strTok.hasMoreTokens()) {
                String line = strTok.nextToken();
                sb.append(line);
            }
            return sb.toString().trim();
        }
        return StringUtils.EMPTY;
    }

    public String stripOffCmdFromRouterResponse(String routerResponse) {
        // The session of SSH will echo the command sent to the router, in the router's response.
        // Since all our commands are terminated by a '\n', strip off the first line
        // of the response from the router. This first line contains the orginal command.
        StringTokenizer rr = new StringTokenizer(routerResponse, "\n");
        StringBuilder sb = new StringBuilder();

        int numTokens = rr.countTokens();
        if (numTokens > 1) {
            rr.nextToken(); //Skip the first line.
            while (rr.hasMoreTokens()) {
                sb.append(rr.nextToken()).append("\n");
            }
        }
        return sb.toString();
    }

    public void setRouterCommandType(String type) {
        this.routerCmdType = type;
        log.debug("Router command type is set to: {0}", type);
    }

    public String getLastFewLinesOfFile(File file, int linesToRead) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        int lines = 0;
        StringBuilder builder = new StringBuilder();
        String tail = "";
        long length = file.length();
        length--;
        randomAccessFile.seek(length);
        for (long seek = length; seek >= 0; --seek) {
            randomAccessFile.seek(seek);
            char c = (char) randomAccessFile.read();
            builder.append(c);
            if (c == '\n') {
                builder = builder.reverse();
                tail = builder.toString() + tail;
                lines++;
                builder.setLength(0);
                if (lines == linesToRead) {
                    break;
                }
            }
        }
        randomAccessFile.close();
        if (log.isDebugEnabled()) {
            log.debug("Content read from file={0} was tail={1}", file.getName(), tail);
        }
        return tail;
    }

    public boolean jcraftReadSwConfigFileFromDisk() {
        return jcraftReadSwConfigFileFromDisk.exists();
    }

    public String getEquipNameCode() {
        return equipNameCode;
    }

    public void setEquipNameCode(String equipNameCode) {
        this.equipNameCode = equipNameCode;
    }

    public String getRouterName() {
        return routerName;
    }

    // Routine does reads until it has read 'nchars' or times out.
    public void receiveUntilBufferFlush(int ncharsSent, int timeout, String message) throws IOException {
        log.debug("ncharsSent={0}, timeout={1}, message={2}", ncharsSent, timeout, message);
        int ncharsTotalReceived = 0;
        int ncharsRead = 0;

        long deadline = new Date().getTime() + timeout;
        logMemoryUsage();
        try {
            session.setTimeout(timeout);  // This is the socket timeout value.
            while (true) {
                if (new Date().getTime() > deadline) {
                    log.error("Routine has timed out: ncharsSent={0}, ncharsTotalReceived={1}", ncharsSent,
                        ncharsTotalReceived);
                    throw new TimedOutException("Routine has timed out");
                }
                ncharsRead = reader.read(charBuffer, 0, BUFFER_SIZE);
                if (listener != null) {
                    listener.receivedString(String.copyValueOf(charBuffer, 0, ncharsRead));
                }
                appendToRouterFile("/tmp/" + routerName, ncharsRead);
                ncharsTotalReceived = ncharsTotalReceived + ncharsRead;
                if (ncharsTotalReceived >= ncharsSent) {
                    log.debug("Received the correct number of characters, ncharsSent={0}, ncharsTotalReceived={1}",
                        ncharsSent, ncharsTotalReceived);
                    logMemoryUsage();
                    return;
                }
            }
        } catch (JSchException e) {
            log.error("JSchException occurred while command sending", e);
            log.debug("ncharsSent={0}, ncharsTotalReceived={1}, ncharsRead={2} until error occurred",
                ncharsSent, ncharsTotalReceived, ncharsRead);
            throw new TimedOutException(e.getMessage());
        }
    }

    public String getHostName() {
        return hostName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void sftpPutFile(String sourcePath, String destDirectory) throws IOException {
        try {
            Session sftpSession = jsch.getSession(userName, hostName, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            sftp.connect();
            log.debug("Sending via sftp from source: {0} to destination: {1}", sourcePath, destDirectory);
            sftp.put(sourcePath, destDirectory, ChannelSftp.OVERWRITE);
            sftpSession.disconnect();
        } catch (JSchException ex) {
            log.error("JSchException occurred while handling sftp session", ex);
            throw new IOException(ex.getMessage());
        } catch (SftpException ex) {
            log.error("SftpException occurred during file transfer", ex);
            throw new IOException(ex.getMessage());
        }
    }

    public void sftpPutStringData(String stringOfData, String fullPathDest) throws IOException {
        try {
            Session sftpSession = jsch.getSession(userName, hostName, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            sftp.connect();
            InputStream is = new ByteArrayInputStream(stringOfData.getBytes());
            log.debug("Sending via sftp stringOfData to destination: {0}", fullPathDest);
            sftp.put(is, fullPathDest, ChannelSftp.OVERWRITE);
            sftpSession.disconnect();
        } catch (JSchException ex) {
            log.error("JSchException occurred while handling sftp session", ex);
            throw new IOException(ex.getMessage());
        } catch (SftpException ex) {
            log.error("SftpException occurred during data transfer", ex);
            throw new IOException(ex.getMessage());
        }
    }

    public String sftpGet(String fullFilePathName) throws IOException {
        try {
            Session sftpSession = jsch.getSession(userName, hostName, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            sftp.connect();
            InputStream in = sftp.get(fullFilePathName);
            String sftpFileString = readInputStreamAsString(in);
            log.debug("Received data via sftp connection sftpFileString={0} from fullFilePathName={1}",
                sftpFileString, fullFilePathName);
            sftpSession.disconnect();
            return sftpFileString;
        } catch (JSchException ex) {
            log.error("JSchException occurred while handling sftp session", ex);
            throw new IOException(ex.getMessage());
        } catch (SftpException ex) {
            log.error("SftpException occurred during data transfer", ex);
            throw new IOException(ex.getMessage());
        }
    }

    public static String readInputStreamAsString(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }


    public void logMemoryUsage() {
        int mb = 1024 * 1024;
        long usedMemory;
        long maxMemoryAvailable;
        long memoryLeftOnHeap;
        maxMemoryAvailable =runtime.maxMemory() / mb;
        usedMemory = (runtime.totalMemory() / mb) - (runtime.freeMemory() / mb);
        memoryLeftOnHeap = maxMemoryAvailable - usedMemory;
        log.info("Memory usage: maxMemoryAvailable={0}, usedMemory={1}, memoryLeftOnHeap={2}",
            maxMemoryAvailable, usedMemory, memoryLeftOnHeap);
    }

    // User specifies the port number, and the subsystem
    public void connect(String hostname, String username, String password, String prompt, int timeOut, int portNum,
        String subsystem) throws IOException {

        if (log.isDebugEnabled()) {
            log.debug(
                "Attempting to connect to {0} username={1} prompt='{2}' timeOut={3} portNum={4} subsystem={5}",
                hostname, username, prompt, timeOut, portNum, subsystem);
        }
        routerName = hostname;
        try {
            session = jsch.getSession(username, hostname, portNum);
            UserInfo ui = new MyUserInfo();
            session.setPassword(password);
            session.setUserInfo(ui);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(timeOut);
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            channel = session.openChannel(CHANNEL_SUBSYSTEM_TYPE);
            ((ChannelSubsystem) channel).setSubsystem(subsystem);
            ((ChannelSubsystem) channel).setPty(true); //expected ptyType vt102

            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), BUFFER_SIZE);
            channel.connect(5000);
        } catch (JSchException e) {
            log.error("JschException occurred ", e);
            throw new IOException(e.getMessage());
        }
    }

    public void connect(String hostName, String username, String password) throws IOException {
        log.debug("Attempting to connect to {0} username={1} portNumber={2}", hostName, username, DEFAULT_PORT);
        routerName = hostName;
        this.hostName = hostName;
        userName = username;
        passWord = password;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(username, hostName, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            session.setConfig(config);
            session.setPassword(password);
            session.setUserInfo(ui);
            session.connect(30000);
            channel = session.openChannel(CHANNEL_SHELL_TYPE);
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            ((ChannelShell) channel).setPtyType(TERMINAL_BASIC_MODE);
            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), BUFFER_SIZE);
            channel.connect();
            try {
                receiveUntil(":~#", 9000, "No cmd was sent, just waiting, but we can stop on a '~#'");
            } catch (Exception e) {
                log.warn("Caught an Exception: Nothing to flush out.", e);
            }

        } catch (JSchException e) {
            log.error("JschException occurred ", e);
            throw new IOException(e.getMessage());
        }
    }


    public void put(String sourcePath, String destDirectory) throws IOException {
        try {
            Session sftpSession = jsch.getSession(userName, hostName, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            sftp.connect();
            log.debug("Sending via sftp from source: {0} to destination: {1}", sourcePath, destDirectory);
            sftp.put(sourcePath, destDirectory, ChannelSftp.OVERWRITE);
            sftpSession.disconnect();
        } catch (JSchException ex) {
            log.error("JSchException occurred while handling sftp session", ex);
            throw new IOException(ex.getMessage());
        } catch (SftpException ex) {
            log.error("SftpException occurred during file transfer", ex);
            throw new IOException(ex.getMessage());
        }
    }

    public void put(InputStream is, String fullPathDest, String hostName, String userName, String passWord)
        throws IOException {
        Session sftpSession = null;
        try {
            log.debug("Sftp put invoked, connection details: username={1} hostname={2}",
                userName, hostName);
            jsch = new JSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            sftpSession = jsch.getSession(userName, hostName, DEFAULT_PORT);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.setConfig(config);
            sftpSession.connect(30 * 1000);
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            sftp.connect();
            String oldFiles = fullPathDest + "*";
            log.debug("Deleting old files: {0}", oldFiles);
            try {
                sftp.rm(oldFiles);
            } catch (SftpException ex) {
                String exp = "No such file";
                if (ex.getMessage() != null && ex.getMessage().contains(exp)) {
                    log.warn("No files found, continue");
                } else {
                    log.error("SftpException while invoking rm command over sftp", ex);
                    throw ex;
                }
            }
            log.debug("Sending stringOfData to destination {0}", fullPathDest);
            sftp.put(is, fullPathDest, ChannelSftp.OVERWRITE);
        } catch (JSchException ex) {
            log.error("JSchException occurred while handling sftp session", ex);
            throw new IOException(ex.getMessage());
        } catch (SftpException ex) {
            log.error("SftpException occurred during file transfer", ex);
            throw new IOException(ex.getMessage());
        } finally {
            if (sftpSession != null) {
                sftpSession.disconnect();
            }
        }
    }


    public String get(String fullFilePathName, String hostName, String userName, String passWord) throws IOException {
        Session sftpSession = null;
        try {
            log.debug("Sftp get invoked, connection details: username={1} hostname={2}",
                userName, hostName);
            jsch = new JSch();
            sftpSession = jsch.getSession(userName, hostName, DEFAULT_PORT);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.setConfig(config);
            sftpSession.connect(30 * 1000);
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            sftp.connect();
            InputStream in = sftp.get(fullFilePathName);
            return readInputStreamAsString(in);
        } catch (JSchException ex) {
            log.error("JSchException occurred while handling sftp session", ex);
            throw new IOException(ex.getMessage());
        } catch (SftpException ex) {
            log.error("SftpException occurred during file transfer", ex);
            throw new IOException(ex.getMessage());
        } finally {
            if (sftpSession != null) {
                sftpSession.disconnect();
            }
        }
    }

    public String send(String cmd, String delimiter) throws IOException {
        try (OutputStream os = channel.getOutputStream(); DataOutputStream dos = new DataOutputStream(os)) {
            sendSshCommand(cmd, dos);
            return receiveUntil(delimiter, 300000, cmd);
        } catch (IOException ex) {
            log.error("IOException occurred", ex);
            throw new IOException(ex.getMessage());
        }
    }

    private void sendSshCommand(@Nonnull String originalCommand, @Nonnull DataOutputStream channelOutputStream)
        throws IOException {
        String command = enhanceCommandWithEOL(originalCommand);
        int length = command.length(); // 2,937,706
        int charsChunkSize = 300000;
        int charsTotalSent = 0;

        log.debug("Sending ssh command: length={0}, payload: {1}", length, command);
        if (isCmdLengthEnoughToSendInChunks(length, charsChunkSize)) {
            int timeout = 9000;
            for (int i = 0; i < length; i += charsChunkSize) {
                String commandChunk = command.substring(i, Math.min(length, i + charsChunkSize));
                int numCharsSentInChunk = commandChunk.length();
                charsTotalSent = charsTotalSent + commandChunk.length();
                log.debug("Iteration nr:{0}, sending command chunk: {1}", i, numCharsSentInChunk);
                channelOutputStream.writeBytes(commandChunk);
                channelOutputStream.flush();
                try {
                    if (numCharsSentInChunk < length) {
                        receiveUntilBufferFlush(numCharsSentInChunk, timeout, "buffer flush  i=" + i);
                    } else {
                        log.trace("i={0}, flush immediately", i);
                        channelOutputStream.flush();
                    }
                } catch (IOException ex) {
                    log.warn("IOException occurred: nothing to flush out", ex);
                }
            }
        } else {
            channelOutputStream.writeBytes(command);
        }
        channelOutputStream.flush();
    }

    private boolean isCmdLengthEnoughToSendInChunks(int length, int chunkSize) {
        return length > 2 * chunkSize;
    }

    private String enhanceCommandWithEOL(@Nonnull String originalCommand) {
        char commandEnding = originalCommand.charAt(originalCommand.length() - 1);
        if (commandEnding != '\n' && commandEnding != '\r') {
            return originalCommand + "\n";
        }
        return originalCommand;
    }
}
