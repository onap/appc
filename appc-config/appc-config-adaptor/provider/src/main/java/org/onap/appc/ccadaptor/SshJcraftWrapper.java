/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
 * ================================================================================
 * Modifications Copyright (c) 2019 IBM
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.ccadaptor;

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
import org.apache.commons.lang.StringUtils;

public class SshJcraftWrapper {

    InputStream inputStream = null;
    OutputStream outputStream = null;
    DebugLog debugLog = new DebugLog();
    private String debugLogFileName = "/tmp/sshJcraftWrapperDebug";
    private TelnetListener listener = null;
    private String routerLogFileName = null;
    private String host = null;
    private String routerName = null;
    private int bufferSize = 512000;
    char[] charBuffer = new char[bufferSize];
    private DataInputStream dis = null;
    private BufferedReader reader = null;
    private BufferedWriter out = null;
    private File tmpFile = null;
    private JSch jsch = null;
    private Session session = null;
    private Channel channel = null;
    private String tId = "";
    private String aggregatedReceivedString = "";
    private File extraDebugFile = new File("/tmp/sshJcraftWrapperDEBUG");
    private String routerCmdType = "XML";
    private String routerFileName = null;
    private File jcraftReadSwConfigFileFromDisk = new File("/tmp/jcraftReadSwConfigFileFromDisk");
    private String equipNameCode = null;
    private String hostName = null;
    private String userName = null;
    private String passWord = null;
    private StringBuffer charactersFromBufferFlush = new StringBuffer();
    private Runtime runtime = Runtime.getRuntime();
    private DebugLog dbLog = new DebugLog();

    public SshJcraftWrapper() {
        String fn = "SshJcraftWrapper.SshJcraftWrapper";
        DebugLog.printRTAriDebug(fn, "SshJcraftWrapper has been instantated");
        routerLogFileName = "/tmp/" + host;
    }

    public void connect(String hostname, String username, String password, String prompt, int timeOut)
        throws IOException {
        String fn = "SshJcraftWrapper.connect";
        jsch = getJSch();
        DebugLog.printRTAriDebug(fn,
            "Attempting to connect to " + hostname + " username=" + username + " prompt='"
                + prompt + "' timeOut=" + timeOut);
        DebugLog.printRTAriDebug(fn, "Trace A");
        routerName = hostname;
        hostName = hostname;
        userName = username;
        passWord = password;
        try {
            session = jsch.getSession(username, hostname, 22);
            UserInfo ui = new MyUserInfo();
            session.setPassword(password);
            session.setUserInfo(ui);
            session.connect(timeOut);
            channel = session.openChannel("shell");
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            ((ChannelShell) channel).setPtyType("vt102");
            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), bufferSize);
            channel.connect();
            DebugLog.printRTAriDebug(fn, "Successfully connected.");
            DebugLog.printRTAriDebug(fn, "Flushing input buffer");
            try {
                receiveUntil(prompt, 3000, "No cmd was sent, just waiting");
            } catch (Exception e) {
                DebugLog.printRTAriDebug(fn, "Caught an Exception: Nothing to flush out.");
            }
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception. e=" + e);
            throw new IOException(e.toString());
        }
    }

    // User specifies the port number.
    public void connect(String hostname, String username, String password, String prompt, int timeOut, int portNum)
        throws IOException {
        String fn = "SshJcraftWrapper.connect";
        DebugLog.printRTAriDebug(fn,
            ":Attempting to connect to " + hostname + " username=" + username + " prompt='"
                + prompt + "' timeOut=" + timeOut + " portNum=" + portNum);
        routerName = hostname;
        hostName = hostname;
        userName = username;
        passWord = password;
        routerName = hostname;
        jsch = getJSch();
        try {
            session = jsch.getSession(username, hostname, portNum);
            UserInfo ui = new MyUserInfo();
            session.setPassword(password);
            session.setUserInfo(ui);
            session.setConfig("StrictHostKeyChecking", "no");
            DebugLog.printRTAriDebug(fn, ":StrictHostKeyChecking set to 'no'");

            session.connect(timeOut);
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");
            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), bufferSize);
            channel.connect();
            DebugLog.printRTAriDebug(fn, ":Successfully connected.");
            DebugLog.printRTAriDebug(fn, ":Flushing input buffer");
            try {
                if (prompt.equals("]]>]]>")) {
                    receiveUntil("]]>]]>", 10000, "No cmd was sent, just waiting");
                } else {
                    receiveUntil(":~#", 5000, "No cmd was sent, just waiting");
                }
            } catch (Exception e) {
                DebugLog.printRTAriDebug(fn, "Caught an Exception::: Nothing to flush out.");
            }
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, ":Caught an Exception. e=" + e);
            dbLog.outputStackTrace(e);

            throw new IOException(e.toString());
        }
    }


    public String receiveUntil(String delimeters, int timeout, String cmdThatWasSent)
        throws TimedOutException, IOException {
        String fn = "SshJcraftWrapper.receiveUntil";
        boolean match = false;
        boolean cliPromptCmd = false;
        StringBuffer sb2 = new StringBuffer();
        StringBuffer sbReceive = new StringBuffer();
        DebugLog.printRTAriDebug(fn,
            "delimeters='" + delimeters + "' timeout=" + timeout + " cmdThatWasSent='" + cmdThatWasSent + "'");
        appendToFile(debugLogFileName,
            fn + " delimeters='" + delimeters + "' timeout=" + timeout + " cmdThatWasSent='" + cmdThatWasSent + "'\n");
        String CmdThatWasSent = removeWhiteSpaceAndNewLineCharactersAroundString(cmdThatWasSent);
        int readCounts = 0;
        aggregatedReceivedString = "";
        FileWriter fileWriter = null;

        long deadline = new Date().getTime() + timeout;
        try {
            session.setTimeout(timeout);  // This is the socket timeout value.
            while (!match) {
                if (new Date().getTime() > deadline) {
                    DebugLog.printRTAriDebug(fn,
                        "Throwing a TimedOutException: time in routine has exceed our deadline: routerName:"
                            + routerName + " CmdThatWasSent=" + CmdThatWasSent);
                    throw new TimedOutException("Timeout: time in routine has exceed our deadline");
                }
                try {
                    delay(500);
                } catch (java.lang.InterruptedException ee) {
                    Thread.currentThread().interrupt();
                }
                int len = reader.read(charBuffer, 0, bufferSize);
                appendToFile(debugLogFileName, fn + " After reader.read cmd: len=" + len + "\n");
                if (len <= 0) {
                    DebugLog.printRTAriDebug(fn,
                        "Reader read " + len + " bytes. Looks like we timed out, router=" + routerName);
                    throw new TimedOutException("Received a SocketTimeoutException router=" + routerName);
                }
                if (!cliPromptCmd) {
                    if (cmdThatWasSent.indexOf("IOS_XR_uploadedSwConfigCmd") != -1) {
                        if (out == null) {
                            // This is a IOS XR sw config file. We will write it to the disk.
                            timeout = timeout * 2;
                            deadline = new Date().getTime() + timeout;
                            DebugLog.printRTAriDebug(fn, "IOS XR upload for software config: timeout=" + timeout);
                            StringTokenizer st = new StringTokenizer(cmdThatWasSent);
                            st.nextToken();
                            routerFileName = st.nextToken();
                            fileWriter = new FileWriter(routerFileName);
                            out = new BufferedWriter(fileWriter);
                            routerLogFileName = "/tmp/" + routerName;
                            tmpFile = new File(routerLogFileName);
                            DebugLog.printRTAriDebug(fn,
                                "Will write the swConfigFile to disk, routerFileName=" + routerFileName);
                        }
                        out.write(charBuffer, 0, len);
                        out.flush();
                        appendToFile(debugLogFileName, fn + " Wrote " + len + " bytes to the disk\n");
                        if (tmpFile.exists()) {
                            appendToRouterFile(routerLogFileName, len);
                        }
                        match = checkIfReceivedStringMatchesDelimeter(len, "\nXML>");
                        if (match == true) {
                            out.flush();
                            out.close();
                            out = null;
                            return null;
                        }
                    } else {
                        readCounts++;
                        appendToFile(debugLogFileName,
                            fn + " readCounts=" + readCounts + "  Reader read " + len + " of data\n");
                        int c;
                        sb2.setLength(0);
                        for (int i = 0; i < len; i++) {
                            c = charBuffer[i];
                            if ((c != 7) && (c != 13) && (c != 0) && (c != 27)) {
                                sbReceive.append((char) charBuffer[i]);
                                sb2.append((char) charBuffer[i]);
                            }
                        }
                        appendToRouterFile("/tmp/" + routerName, len);
                        if (listener != null) {
                            listener.receivedString(sb2.toString());
                        }

                        appendToFile(debugLogFileName, fn + " Trace 1\n");
                        match = checkIfReceivedStringMatchesDelimeter(delimeters, sb2.toString(), cmdThatWasSent);
                        appendToFile(debugLogFileName, fn + " Trace 2\n");
                        if (match == true) {
                            appendToFile(debugLogFileName, fn + " Match was true, breaking...\n");
                            break;
                        }
                    }
                }
            }
        } catch (JSchException e) {
            DebugLog.printRTAriDebug(fn, "Caught an JSchException e=" + e.toString());
            dbLog.outputStackTrace(e);
            throw new TimedOutException(e.toString());
        } catch (IOException ee) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException: ee=" + ee.toString());
            dbLog.outputStackTrace(ee);
            throw new TimedOutException(ee.toString());
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch(IOException ex) {
                DebugLog.printRTAriDebug(fn, "Failed to close fileWriter output stream: ex=" + ex);
            }
        }
        String result = stripOffCmdFromRouterResponse(sbReceive.toString());
        DebugLog.printRTAriDebug(fn, "Leaving method successfully");
        return result;
    }

    public boolean checkIfReceivedStringMatchesDelimeter(String delimeters, String receivedString,
        String cmdThatWasSent) {
        // The delimeters are in a '|' seperated string. Return true on the first match.
        String fn = "SshJcraftWrapper.checkIfReceivedStringMatchesDelimeter";
        appendToFile(debugLogFileName,
            fn + " Entered:  delimeters='" + delimeters + " cmdThatWasSent='" + cmdThatWasSent + "' receivedString='"
                + receivedString + "'\n");
        StringTokenizer st = new StringTokenizer(delimeters, "|");

        if ((delimeters.indexOf("#$") != -1) || (routerCmdType.equals("CLI")))  // This would be an IOS XR, CLI command.
        {
            int x = receivedString.lastIndexOf("#");
            int y = receivedString.length() - 1;
            appendToFile(debugLogFileName, fn + " IOS XR, CLI command\n");
            if (extraDebugFile.exists()) {
                appendToFile(debugLogFileName,
                    fn + " :::cmdThatWasSent='" + cmdThatWasSent + "'  x=" + x + " y=" + y + "\n");
            }
            return (x != -1) && (y == x);
        }
        if (cmdThatWasSent.indexOf("show config") != -1) {
            appendToFile(debugLogFileName, fn + "In the block for 'show config'\n");
            while (st.hasMoreTokens()) {
                String delimeter = st.nextToken();
                // Make sure we don't get faked out by a response of " #".
                // Proc #0
                //   # signaling-local-address ipv6 FD00:F4D5:EA06:1::110:136:254
                // LAAR2#
                int x = receivedString.lastIndexOf(delimeter);
                if ((receivedString.lastIndexOf(delimeter) != -1) && (receivedString.lastIndexOf(" #") != x - 1)) {
                    appendToFile(debugLogFileName, fn + "receivedString=\n'" + receivedString + "'\n");
                    appendToFile(debugLogFileName,
                        fn + "Returning true for the 'show config' command. We found our real delmeter. \n\n");
                    return (true);
                }
            }
        } else {
            aggregatedReceivedString = aggregatedReceivedString + receivedString;
            _appendToFile("/tmp/aggregatedReceivedString.debug", aggregatedReceivedString);

            while (st.hasMoreTokens()) {
                String delimeter = st.nextToken();
                appendToFile(debugLogFileName, fn + " Looking for an delimeter of:'" + delimeter + "'\n");
                appendToFile(debugLogFileName, fn + " receivedString='" + receivedString);
                if (aggregatedReceivedString.indexOf(delimeter) != -1) {
                    DebugLog.printRTAriDebug(fn, "Found our delimeter, which was: '" + delimeter + "'");
                    aggregatedReceivedString = "";
                    return (true);
                }
            }
        }
        return (false);
    }

    public boolean checkIfReceivedStringMatchesDelimeter(int len, String delimeter) {
        String fnName = "SshJcraftWrapper.checkIfReceivedStringMatchesDelimeter:::";
        int x;
        int c;
        String str = StringUtils.EMPTY;

        if (jcraftReadSwConfigFileFromDisk()) {
            DebugLog.printAriDebug(fnName, "jcraftReadSwConfigFileFromDisk block");
            File fileName = new File(routerFileName);
            appendToFile(debugLogFileName,
                fnName + " jcraftReadSwConfigFileFromDisk::: Will read the tail end of the file from the disk");
            try {
                str = getLastFewLinesOfFile(fileName, 3);
            } catch (IOException e) {
                DebugLog.printAriDebug(fnName, "Caught an Exception, e=" + e);
                dbLog.outputStackTrace(e);
            }
        } else {
            // When looking at the end of the charBuffer, don't include any linefeeds or spaces. We only want to make the smallest string possible.
            for (x = len - 1; x >= 0; x--) {
                c = charBuffer[x];
                if (extraDebugFile.exists()) {
                    appendToFile(debugLogFileName, fnName + " x=" + x + " c=" + c + "\n");
                }
                if ((c != 10) && (c != 32)) // Not a line feed nor a space.
                {
                    break;
                }
            }
            if ((x + 1 - 13) >= 0) {
                str = new String(charBuffer, (x + 1 - 13), 13);
                appendToFile(debugLogFileName, fnName + " str:'" + str + "'\n");
            } else {
                File fileName = new File(routerFileName);
                appendToFile(debugLogFileName,
                    fnName + " Will read the tail end of the file from the disk, x=" + x + " len=" + len + " str::'"
                        + str + "' routerFileName='" + routerFileName + "'\n");
                DebugLog.printAriDebug(fnName,
                    "Will read the tail end of the file from the disk, x=" + x + " len=" + len + " str::'" + str
                        + "' routerFileName='" + routerFileName + "'");
                try {
                    str = getLastFewLinesOfFile(fileName, 3);
                } catch (IOException e) {
                    DebugLog.printAriDebug(fnName, "Caught an Exception, e=" + e);
                    dbLog.outputStackTrace(e);
                }
            }
        }

        if (str.indexOf(delimeter) != -1) {
            DebugLog.printAriDebug(fnName, "str in break is:'" + str + "'" + " delimeter='" + delimeter + "'");
            appendToFile(debugLogFileName,
                fnName + " str in break is:'" + str + " delimeter='" + delimeter + "'" + "'\n");
            return (true);
        } else {
            appendToFile(debugLogFileName, fnName + " Returning false");
            return (false);
        }

    }

    public void closeConnection() {
        String fn = "SshJcraftWrapper.closeConnection";
        DebugLog.printRTAriDebug(fn, "Executing the closeConnection....");
        inputStream = null;
        outputStream = null;
        dis = null;
        charBuffer = null;
        session.disconnect();
        session = null;
    }

    public void send(String cmd) throws IOException {
        String fn = "SshJcraftWrapper.send";
        OutputStream out = channel.getOutputStream();
        DataOutputStream dos = getDataOutputStream(out);
        if ((cmd.charAt(cmd.length() - 1) != '\n') && (cmd.charAt(cmd.length() - 1) != '\r')) {
            cmd += "\n";
        }
        int length = cmd.length();
        int i = -1;
        int nchars = 300000;
        int ncharsTotalSent = 0;
        int ncharsSent = 0;

        appendToFile(debugLogFileName, fn + ": Sending: '" + cmd);
        DebugLog.printRTAriDebug(fn, "Length of cmd is:" + length); // 2,937,706
        try {
            if (length > 600000) {
                int timeout = 9000;
                for (i = 0; i < length; i += nchars) {
                    String Cmd = cmd.substring(i, Math.min(length, i + nchars));
                    ncharsSent = Cmd.length();
                    ncharsTotalSent = ncharsTotalSent + Cmd.length();
                    DebugLog.printRTAriDebug(fn, "i=" + i + " Sending Cmd: ncharsSent=" + ncharsSent);
                    dos.writeBytes(Cmd);
                    dos.flush();
                    try {
                        DebugLog.printRTAriDebug(fn, ":::i=" + i + " length=" + length);
                        if (ncharsSent < length) {
                            receiveUntilBufferFlush(ncharsSent, timeout, "buffer flush  i=" + i);
                        } else {
                            DebugLog.printRTAriDebug(fn, "i=" + i + " No Waiting this time....");
                            dos.flush();
                        }
                    } catch (Exception e) {
                        DebugLog.printRTAriDebug(fn, "Caught an Exception: Nothing to flush out.");
                    }
                }
            } else {
                DebugLog.printRTAriDebug(fn, "Before executing the dos.writeBytes");
                dos.writeBytes(cmd);
            }
            dos.flush();
            DebugLog.printRTAriDebug(fn, "Leaving method");
            appendToFile(debugLogFileName, fn + ": Leaving method\n");
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException. e=" + e);
            dbLog.outputStackTrace(e);
            throw new IOException(e.toString());
        }
    }


    public void sendChar(int v) throws IOException {
        String fn = "SshJcraftWrapper.sendChar";
        OutputStream out = channel.getOutputStream();
        DataOutputStream dos = getDataOutputStream(out);
        try {
            DebugLog.printRTAriDebug(fn, "Sending: '" + v + "'");
            dos.writeChar(v);
            dos.flush();
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException. e=" + e);
            throw new IOException(e.toString());
        }
    }

    public void send(byte[] b, int off, int len) throws IOException {
        String fn = "SshJcraftWrapper.send:byte[]";
        OutputStream out = channel.getOutputStream();
        DataOutputStream dos = getDataOutputStream(out);
        try {
            dos.write(b, off, len);
            dos.flush();
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException. e=" + e);
            throw new IOException(e.toString());
        }
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {

        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return false;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination,
            String name,
            String instruction,
            String[] prompt,
            boolean[] echo) {
            return null;
        }
    }

    public void addListener(TelnetListener listener) {
        this.listener = listener;
    }

    public void appendToFile(String fileName, String dataToWrite) {
        String fn = "SshJcraftWrapper.appendToFile";

        try {
            // First check to see if a file 'fileName' exist, if it does
            // write to it. If it does not exist, don't write to it.
            File tmpFile = new File(fileName);
            if (tmpFile.exists()) {
                try(BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
                    out.write(getTheDate() + ": " + tId + ": " + dataToWrite);
                }
            }
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException: e=" + e);
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception: e=" + e);
        }
    }

    public void _appendToFile(String fileName, String dataToWrite) {
        String fn = "SshJcraftWrapper.appendToFile";

        try {
            // First check to see if a file 'fileName' exist, if it does
            // write to it. If it does not exist, don't write to it.
            File tmpFile = new File(fileName);
            if (tmpFile.exists()) {
                try(BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
                    out.write(dataToWrite);
                }
            }
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException: e=" + e);
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception: e=" + e);
        }
    }


    public String getTheDate() {
        Calendar cal = Calendar.getInstance();
        java.util.Date today = cal.getTime();
        DateFormat df3 = new SimpleDateFormat("MM/dd/yyyy H:mm:ss  ");
        return (df3.format(today));
    }


    public void appendToRouterFile(String fileName, StringBuffer dataToWrite) {
        String fnName = "SshJcraftWrapper.appendToRouterFile";
        DebugLog.printRTAriDebug(fnName, "Entered.... ");
        try {
            // First check to see if a file 'fileName' exist, if it does
            // write to it. If it does not exist, don't write to it.
            File tmpFile = new File(fileName);
            {
                if (tmpFile.exists()) {
                    try(BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
                        out.write(dataToWrite.toString());
                    }
                }
            }
        } catch (IOException e) {
            DebugLog.printAriDebug(fnName, "writeToFile() exception:, e=" + e);
            dbLog.outputStackTrace(e);
        }
    }

    public void appendToRouterFile(String fileName, int len) {
        try {
            // First check to see if a file 'fileName' exist, if it does
            // write to it. If it does not exist, don't write to it.
            File tmpFile = new File(fileName);
            if (tmpFile.exists()) {
                try(BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
                    out.write(charBuffer, 0, len);
                }
            }
        } catch (IOException e) {
            dbLog.outputStackTrace(e);
        }
    }

    public String removeWhiteSpaceAndNewLineCharactersAroundString(String str) {
        if (str != null) {
            StringTokenizer strTok = new StringTokenizer(str, "\n");
            StringBuffer sb = new StringBuffer();

            while (strTok.hasMoreTokens()) {
                String line = strTok.nextToken();
                sb.append(line);
            }
            return (sb.toString().trim());
        } else {
            return (str);
        }
    }

    public String stripOffCmdFromRouterResponse(String routerResponse) {
        // The session of SSH will echo the command sent to the router, in the router's response.
        // Since all our commands are terminated by a '\n', strip off the first line
        // of the response from the router. This first line contains the original command.

        StringTokenizer rr = new StringTokenizer(routerResponse, "\n");
        StringBuffer sb = new StringBuffer();

        int numTokens = rr.countTokens();
        if (numTokens > 1) {
            while (rr.hasMoreTokens()) {
                sb.append(rr.nextToken() + '\n');
            }
        }
        return (sb.toString());
    }

    public void setRouterCommandType(String type) {
        String fn = "SshJcraftWrapper.setRouterCommandType";
        this.routerCmdType = type;
        DebugLog.printRTAriDebug(fn, "Setting routerCmdType to a value of '" + type + "'");
    }

    public String getLastFewLinesOfFile(File file, int linesToRead) throws IOException {
        String fn = "SshJcraftWrapper.getLastFewLinesOfFile";
        String tail = "";
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            int lines = 0;
            StringBuilder builder = new StringBuilder();
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
        }
        if (!jcraftReadSwConfigFileFromDisk()) {
            DebugLog.printRTAriDebug(fn, "tail='" + tail + "'");
        }
        appendToFile(debugLogFileName, "tail='" + tail + "'\n");
        return tail;
    }

    public boolean jcraftReadSwConfigFileFromDisk() {
        if (jcraftReadSwConfigFileFromDisk.exists()) {
            return (true);
        } else {
            return (false);
        }
    }

    public String getEquipNameCode() {
        return (equipNameCode);

    }

    public void setEquipNameCode(String equipNameCode) {
        this.equipNameCode = equipNameCode;
    }

    public String getRouterName() {
        return (routerName);
    }

    // Routine does reads until it has read 'nchars' or times out.
    public void receiveUntilBufferFlush(int ncharsSent, int timeout, String message)
        throws TimedOutException, IOException {
        String fn = "SshJcraftWrapper.receiveUntilBufferFlush";
        DebugLog.printRTAriDebug(fn, "ncharsSent=" + ncharsSent + " timeout=" + timeout + " " + message);
        int ncharsTotalReceived = 0;
        int ncharsRead = 0;
        charactersFromBufferFlush.setLength(0);

        long deadline = new Date().getTime() + timeout;
        logMemoryUsage();
        try {
            session.setTimeout(timeout);  // This is the socket timeout value.
            while (true) {
                if (new Date().getTime() > deadline) {
                    DebugLog.printRTAriDebug(fn,
                        "Throwing a TimedOutException: time in routine has exceed our deadline: ncharsSent="
                            + ncharsSent + " ncharsTotalReceived=" + ncharsTotalReceived);
                    throw new TimedOutException("Timeout: time in routine has exceed our deadline");
                }
                ncharsRead = reader.read(charBuffer, 0, bufferSize);
                if (listener != null) {
                    listener.receivedString(String.copyValueOf(charBuffer, 0, ncharsRead));
                }
                appendToRouterFile("/tmp/" + routerName, ncharsRead);
                ncharsTotalReceived = ncharsTotalReceived + ncharsRead;
                if (ncharsTotalReceived >= ncharsSent) {
                    DebugLog.printRTAriDebug(fn,
                        "Received the correct number of characters, ncharsSent=" + ncharsSent + " ncharsTotalReceived="
                            + ncharsTotalReceived);
                    logMemoryUsage();
                    return;
                }
            }
        } catch (JSchException e) {
            DebugLog.printRTAriDebug(fn, "Caught an JSchException e=" + e);
            DebugLog.printRTAriDebug(fn,
                "ncharsSent=" + ncharsSent + " ncharsTotalReceived=" + ncharsTotalReceived + " ncharsRead="
                    + ncharsRead);
            throw new TimedOutException(e.toString());
        }
    }

    public String getHostName() {
        return (hostName);
    }

    public String getUserName() {
        return (userName);
    }

    public String getPassWord() {
        return (passWord);
    }

    public void sftpPut(String sourcePath, String destDirectory) throws IOException {
        // delegate to duplicate method
        put(sourcePath, destDirectory);
    }


    public void SftpPut(String stringOfData, String fullPathDest) throws IOException {
        String fn = "SshJcraftWrapper.Sftp";
        try {
            Session sftpSession = jsch.getSession(userName, hostName, 22);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            DebugLog.printRTAriDebug(fn, "Opening up an sftp channel....");
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            DebugLog.printRTAriDebug(fn, "Connecting....");
            sftp.connect();
            InputStream is = new ByteArrayInputStream(stringOfData.getBytes());
            DebugLog.printRTAriDebug(fn, "Sending stringOfData --> " + fullPathDest);
            sftp.put(is, fullPathDest, ChannelSftp.OVERWRITE);
            DebugLog.printRTAriDebug(fn, "Sent successfully");
            sftpSession.disconnect();
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception, e=" + e);
            throw new IOException(e.toString());
        }
    }

    public String sftpGet(String fullFilePathName) throws IOException {
        String fn = "SshJcraftWrapper.Sftp";
        try {
            Session sftpSession = jsch.getSession(userName, hostName, 22);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            DebugLog.printRTAriDebug(fn, "Opening up an sftp channel....");
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            DebugLog.printRTAriDebug(fn, "Connecting....");
            sftp.connect();
            InputStream in = null;
            in = sftp.get(fullFilePathName);
            String sftpFileString = readInputStreamAsString(in);
            DebugLog.printRTAriDebug(fn, "Retreived successfully");
            sftpSession.disconnect();
            return (sftpFileString);
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception, e=" + e);
            throw new IOException(e.toString());
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
        String fn = "SshJcraftWrapper.logMemoryUsage";
        int mb = 1024 * 1024;
        long usedMemory;
        long maxMemoryAdvailable;
        long memoryLetfOnHeap;
        maxMemoryAdvailable = (runtime.maxMemory() / mb);
        usedMemory = ((runtime.totalMemory() / mb) - (runtime.freeMemory() / mb));
        memoryLetfOnHeap = maxMemoryAdvailable - usedMemory;
        DebugLog.printAriDebug(fn,
            "maxMemoryAdvailable=" + maxMemoryAdvailable + " usedMemory=" + usedMemory + " memoryLetfOnHeap="
                + memoryLetfOnHeap);
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------


    // User specifies the port number, and the subsystem
    public void connect(String hostname, String username, String password, String prompt, int timeOut, int portNum,
        String subsystem) throws IOException {
        String fn = "SshJcraftWrapper.connect";

        DebugLog.printRTAriDebug(fn,
            ":::Attempting to connect to " + hostname + " username=" + username + " prompt='"
                + prompt + "' timeOut=" + timeOut + " portNum=" + portNum + " subsystem=" + subsystem);
        routerName = hostname;
        jsch = getJSch();
        try {
            session = jsch.getSession(username, hostname, portNum);
            UserInfo ui = new MyUserInfo();
            session.setPassword(password);
            session.setUserInfo(ui);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(timeOut);
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            channel = session.openChannel("subsystem");
            ((ChannelSubsystem) channel).setSubsystem(subsystem);
            ((ChannelSubsystem) channel).setPty(true);

            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), bufferSize);
            channel.connect();
            DebugLog.printRTAriDebug(fn, "Successfully connected.");
            DebugLog.printRTAriDebug(fn, "Five second sleep....");
            try {
                delay(5000);
            } catch (java.lang.InterruptedException ee) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception. e=" + e);
            throw new IOException(e.toString());
        }
    }

    public void connect(String hostName, String username, String password, int portNumber) throws IOException {
        String fn = "SshJcraftWrapper.connect";
        jsch = getJSch();
        DebugLog.printRTAriDebug(fn,
            "::Attempting to connect to " + hostName + " username=" + username + " portNumber=" + portNumber);
        DebugLog.printRTAriDebug(fn, "Trace C");
        routerName = hostName;
        this.hostName = hostName;
        userName = username;
        passWord = password;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(username, hostName, 22);
            UserInfo ui = new MyUserInfo();
            session.setConfig(config);
            session.setPassword(password);
            session.setUserInfo(ui);
            session.connect(30000);
            channel = session.openChannel("shell");
            session.setServerAliveCountMax(
                0); // If this is not set to '0', then socket timeout on all reads will not work!!!!
            ((ChannelShell) channel).setPtyType("vt102");
            inputStream = channel.getInputStream();
            dis = new DataInputStream(inputStream);
            reader = new BufferedReader(new InputStreamReader(dis), bufferSize);
            channel.connect();
            DebugLog.printRTAriDebug(fn, "::Successfully connected.");
            DebugLog.printRTAriDebug(fn, "::Flushing input buffer");
            try {
                receiveUntil(":~#", 9000, "No cmd was sent, just waiting, but we can stop on a '~#'");
            } catch (Exception e) {
                DebugLog.printRTAriDebug(fn, "Caught an Exception::: Nothing to flush out.");
            }

        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception. e=" + e);
            throw new IOException(e.toString());
        }
    }


    public void put(String sourcePath, String destDirectory) throws IOException {
        String fn = "SshJcraftWrapper.sftp";
        try {
            Session sftpSession = jsch.getSession(userName, hostName, 22);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.connect(30 * 1000);
            DebugLog.printRTAriDebug(fn, "Opening up an sftp channel....");
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            DebugLog.printRTAriDebug(fn, "Connecting....");
            sftp.connect();
            DebugLog.printRTAriDebug(fn, "Sending " + sourcePath + " --> " + destDirectory);
            sftp.put(sourcePath, destDirectory, ChannelSftp.OVERWRITE);
            DebugLog.printRTAriDebug(fn, "Sent successfully");
            sftpSession.disconnect();
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception, e=" + e);
            throw new IOException(e.toString());
        }
    }

    public void put(InputStream is, String fullPathDest, String hostName, String userName, String passWord)
        throws IOException {
        String fn = "SshJcraftWrapper.put";
        Session sftpSession = null;
        try {
            DebugLog.printRTAriDebug(fn, "userName=" + userName + " hostName=" + hostName);
            jsch = getJSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            sftpSession = jsch.getSession(userName, hostName, 22);
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.setConfig(config);
            sftpSession.connect(30 * 1000);
            DebugLog.printRTAriDebug(fn, "Opening up an sftp channel....");
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            DebugLog.printRTAriDebug(fn, "Connecting....");
            sftp.connect();
            String oldFiles = fullPathDest + "*";
            DebugLog.printRTAriDebug(fn, "Deleting old files --> " + oldFiles);
            try {
                sftp.rm(oldFiles);
                DebugLog.printRTAriDebug(fn, "Sending stringOfData --> " + fullPathDest);
            } catch (SftpException sft) {
                String exp = "No such file";
                if (sft.getMessage() != null && sft.getMessage().contains(exp)) {
                    DebugLog.printRTAriDebug(fn, "No files found -- Continue");
                } else {
                    DebugLog.printRTAriDebug(fn, "Exception while sftp.rm " + sft.getMessage());
                    throw sft;
                }
            }
            sftp.put(is, fullPathDest, ChannelSftp.OVERWRITE);
            DebugLog.printRTAriDebug(fn, "Sent successfully");
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception, e=" + e);
            throw new IOException(e.toString());
        } finally {
            if(sftpSession != null) {
                sftpSession.disconnect();
            }
        }
    }


    public String get(String fullFilePathName, String hostName, String userName, String passWord) throws IOException {
        String fn = "SshJcraftWrapper.get";
        Session sftpSession = null;
        try {
            DebugLog.printRTAriDebug(fn, "userName=" + userName + " hostName=" + hostName);
            jsch = getJSch();
            sftpSession = jsch.getSession(userName, hostName, 22);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            UserInfo ui = new MyUserInfo();
            sftpSession.setPassword(passWord);
            sftpSession.setUserInfo(ui);
            sftpSession.setConfig(config);
            sftpSession.connect(30 * 1000);
            DebugLog.printRTAriDebug(fn, "Opening up an sftp channel....");
            ChannelSftp sftp = (ChannelSftp) sftpSession.openChannel("sftp");
            DebugLog.printRTAriDebug(fn, "Connecting....");
            sftp.connect();
            InputStream in = sftp.get(fullFilePathName);
            String sftpFileString = readInputStreamAsString(in);
            DebugLog.printRTAriDebug(fn, "Retreived successfully");
            return sftpFileString;
        } catch (Exception e) {
            DebugLog.printRTAriDebug(fn, "Caught an Exception, e=" + e);
            throw new IOException(e.toString());
        } finally {
            if(sftpSession != null) {
                sftpSession.disconnect();
            }
        }
    }

    public String send(String cmd, String delimiter) throws IOException {
        String fn = "SshJcraftWrapper.send";
        OutputStream out = channel.getOutputStream();
        DataOutputStream dos = getDataOutputStream(out);

        if ((cmd.charAt(cmd.length() - 1) != '\n') && (cmd.charAt(cmd.length() - 1) != '\r')) {
            cmd += "\n";
        }
        int length = cmd.length();
        int i = -1;
        int nchars = 300000;
        int ncharsTotalSent = 0;
        int ncharsSent = 0;

        DebugLog.printRTAriDebug(fn, "Length of cmd is:" + length); // 2,937,706
        try {
            if (length > 600000) {
                int timeout = 9000;
                for (i = 0; i < length; i += nchars) {
                    String Cmd = cmd.substring(i, Math.min(length, i + nchars));
                    ncharsSent = Cmd.length();
                    ncharsTotalSent = ncharsTotalSent + Cmd.length();
                    DebugLog.printRTAriDebug(fn, "i=" + i + " Sending Cmd: ncharsSent=" + ncharsSent);
                    dos.writeBytes(Cmd);
                    dos.flush();
                    try {
                        DebugLog.printRTAriDebug(fn, ":::i=" + i + " length=" + length);
                        if (ncharsSent < length) {
                            receiveUntilBufferFlush(ncharsSent, timeout, "buffer flush  i=" + i);
                        } else {
                            DebugLog.printRTAriDebug(fn, "i=" + i + " No Waiting this time....");
                            dos.flush();
                        }
                    } catch (Exception e) {
                        DebugLog.printRTAriDebug(fn, "Caught an Exception: Nothing to flush out.");
                    }
                }
            } else {
                DebugLog.printRTAriDebug(fn, "Before executing the dos.writeBytes");
                dos.writeBytes(cmd);
            }
            dos.flush();
            // Now lets get the response.
            String response = receiveUntil(delimiter, 300000, cmd);
            DebugLog.printRTAriDebug(fn, "Leaving method");
            return (response);
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "Caught an IOException. e=" + e);
            throw new IOException(e.toString());
        }
    }

    protected JSch getJSch() {
        return new JSch();
    }

    protected DataOutputStream getDataOutputStream(OutputStream out) {
        return new DataOutputStream(out);
    }

    protected void delay(int milliseconds) throws InterruptedException {
        Thread.sleep(5000);
    }
}
