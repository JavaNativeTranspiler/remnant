package com.chorus.api.system.networking.connection;

import cc.polymorphism.annot.IncludeReference;
import lombok.Getter;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@IncludeReference
public class ConnectionManager {
    private static final String SERVER_HOST = "146.71.78.242";
    private static final int SERVER_PORT = 1234;

    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    @Getter
    private final ReentrantLock socketLock = new ReentrantLock();
    private static SSLSocketFactory sslSocketFactory;

    static {
        staticInit();
    }

    public static void staticInit() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            System.err.println("Failed to initialize SSL context");
        }
    }

    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                socketLock.lock();
                try {
                    closeExistingConnection();

                    System.out.println("Attempting to connect to the auth server...");
                    socket = (SSLSocket) sslSocketFactory.createSocket(SERVER_HOST, SERVER_PORT);
                    
                    configureSocket();

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    isConnected.set(true);
                    System.out.println("Connected to the auth server");
                } finally {
                    socketLock.unlock();
                }
            } catch (SSLHandshakeException e) {
                System.err.println("SSL Handshake failed");
                disconnect();
            } catch (Exception e) {
                System.err.println("Connection failed");
                disconnect();
            }
        });
    }

    private void closeExistingConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }

    private void configureSocket() throws IOException {
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(null);
        sslParams.setProtocols(new String[] {"TLSv1.2"});
        sslParams.setCipherSuites(socket.getSupportedCipherSuites());
        sslParams.setNeedClientAuth(false);
        sslParams.setWantClientAuth(false);
        socket.setSSLParameters(sslParams);
        socket.setSoTimeout(5000);
        
        try {
            socket.startHandshake();
        } catch (SSLHandshakeException e) {
            System.err.println("Failed to configure socket");
        }
    }

    public void disconnect() {
        if (isConnected.compareAndSet(true, false)) {
            socketLock.lock();
            try {
                if (socket != null) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socketLock.unlock();
            }
        }
    }

    public String readResponse() throws IOException {
        socketLock.lock();
        try {
            if (in != null && isConnected.get()) {
                return in.readLine();
            }
            return null;
        } finally {
            socketLock.unlock();
        }
    }

    public void sendPacket(String packet) {
        socketLock.lock();
        try {
            if (isConnected.get() && out != null) {
                out.println(packet);
            }
        } finally {
            socketLock.unlock();
        }
    }

    public void setTimeout(int timeout) {
        socketLock.lock();
        try {
            if (socket != null && isConnected.get()) {
                socket.setSoTimeout(timeout);
            }
        } catch (Exception e) {
            System.err.println("Failed to set timeout");
            disconnect();
        } finally {
            socketLock.unlock();
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host = SERVER_HOST;
        private int port = SERVER_PORT;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Builds the ConnectionManager
         * @return A new ConnectionManager
         */
        public ConnectionManager build() {
            return new ConnectionManager();
        }
    }
}
