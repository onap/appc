/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.ssh.sshd;

import org.apache.sshd.client.channel.ClientChannelEvent;
import org.onap.appc.adapter.ssh.Constants;
import org.onap.appc.adapter.ssh.SshConnection;
import org.onap.appc.adapter.ssh.SshException;
import org.onap.appc.encryption.EncryptionTool;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.io.File;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.Arrays;

/**
 * Implementation of SshConnection interface based on Apache MINA SSHD library.
 */
class SshConnectionSshd implements SshConnection {

    private static final EELFLogger logger = EELFManager.getInstance().getApplicationLogger();

    private static final long AUTH_TIMEOUT = 60000;
    private static final long EXEC_TIMEOUT = 120000;

    private String host;
    private int port;
    private String username;
    private String password;
    private long timeout = EXEC_TIMEOUT;
    private String keyFile;
    private SshClient sshClient;
    private ClientSession clientSession;
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    public SshConnectionSshd(String host, int port, String username, String password, String keyFile) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.keyFile = keyFile;
    }

    public SshConnectionSshd(String host, int port, String username, String password) {
        this(host, port, username, password, null);
    }

    public SshConnectionSshd(String host, int port, String keyFile) {
        this(host, port, null, null, keyFile);
    }

    @Override
    public void connect() {
        sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        try {
            clientSession =
                sshClient.connect(EncryptionTool.getInstance().decrypt(username), host, port).verify().getSession();
            if (password != null) {
                clientSession.addPasswordIdentity(EncryptionTool.getInstance().decrypt(password));
            } else if (keyFile != null) {
                KeyPairProvider keyPairProvider = new FileKeyPairProvider(
                        new File(keyFile).toPath()
                );
                KeyPair keyPair = keyPairProvider.loadKeys().iterator().next();
                clientSession.addPublicKeyIdentity(keyPair);
            }
            AuthFuture authFuture = clientSession.auth();
            authFuture.await(AUTH_TIMEOUT);
            if (!authFuture.isSuccess()) {
                throw new SshException("Error establishing ssh connection to [" + username + "@" + host + ":" + port
                    + "]. Authentication failed.");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SshException("Error establishing ssh connection to [" + username + "@" + host + ":" + port + "].",
                e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("SSH: connected to [" + toString() + "]");
        }
    }

    @Override
    public void connectWithRetry() {
        int retryCount ;
        int retryDelay ;
        int retriesLeft;
        retryCount = configuration.getIntegerProperty(Constants.CONNECTION_RETRY_COUNT,
            Constants.DEFAULT_CONNECTION_RETRY_COUNT);
        retryDelay = configuration.getIntegerProperty(Constants.CONNECTION_RETRY_DELAY,
            Constants.DEFAULT_CONNECTION_RETRY_DELAY);
        retriesLeft = retryCount + 1;
        do {
            try {
                this.connect();
                break;
            } catch (RuntimeException e) {
                if (retriesLeft > 1) {
                    logger.debug("SSH Connection failed. Waiting for change in server's state.");
                    waitForConnection(retryDelay);
                    retriesLeft--;
                    logger.debug("Retrying SSH connection. Attempt [" + Integer.toString(retryCount - retriesLeft + 1)
                        + "] out of [" + retryCount + "]");
                } else {
                    throw e;
                }
          }
        } while (retriesLeft > 0);
    }

    @Override
    public void disconnect() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("SSH: disconnecting from [" + toString() + "]");
            }
            clientSession.close(false);
        } finally {
            if (sshClient != null) {
                sshClient.stop();
            }
        }
    }

    @Override
    public void setExecTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public int execCommand(String cmd, OutputStream out, OutputStream err) {
        return execCommand(cmd, out, err, false);
    }

    @Override
    public int execCommandWithPty(String cmd, OutputStream out) {
        return execCommand(cmd, out, out, true);
    }

    private int execCommand(String cmd, OutputStream out, OutputStream err, boolean usePty) {
        try(ChannelExec client = clientSession.createExecChannel(cmd)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SSH: executing command");
            }
            client.setUsePty(usePty); // use pseudo-tty?
            client.setOut(out);
            client.setErr(err);
            OpenFuture openFuture = client.open();
            int exitStatus;
            client.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), timeout);
            openFuture.verify();
            Integer exitStatusI = client.getExitStatus();
            if (exitStatusI == null) {
                throw new SshException("Error executing command [" + cmd + "] over SSH [" + username + "@" + host + ":" + port + "]. Operation timed out.");
            }
            exitStatus = exitStatusI;
            return exitStatus;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e1) {
            throw new SshException(
                "Error executing command [" + cmd + "] over SSH [" + username + "@" + host + ":" + port + "]", e1);
        }
    }

    private void waitForConnection(int retryDelay) {
        long time = retryDelay * 1000L;
        long future = System.currentTimeMillis() + time;
        if (time != 0) {
            while (System.currentTimeMillis() < future && time > 0) {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    /*
                     * This is rare, but it can happen if another thread interrupts us while we are sleeping. In that
                     * case, the thread is resumed before the delay time has actually expired, so re-calculate the
                     * amount of delay time needed and reenter the sleep until we get to the future time.
                     */
                    time = future - System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public String toString() {
        String address = host;
        if (username != null) {
            address = username + '@' + address;
        }
        return address;
    }
}
