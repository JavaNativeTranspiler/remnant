package com.chorus.api.system.networking.auth;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.connection.ConnectionManager;
import com.chorus.api.system.networking.packet.factory.PacketFactory;
import com.chorus.api.system.networking.response.factory.ResponseHandlerFactory;
import lombok.Getter;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@IncludeReference
public class AuthenticationService {
    private final ConnectionManager connectionManager;
    private final ExecutorService networkExecutor;
    private volatile String lastLoginResponse;
    @Getter
    private volatile UserData currentUser;

    private AuthenticationService(Builder builder) {
        this.connectionManager = builder.connectionManager;
        this.networkExecutor = builder.networkExecutor;

        try {
            final String[] debugFlags = {"-javaagent", "-Xdebug", "-agentlib", "-Xrunjdwp", "-Xnoagent", "-DproxySet", "-DproxyHost", "-DproxyPort", "-Djavax.net.ssl.trustStorePassword"};
            for (String vmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                for (int length = debugFlags.length, i = 0; i < length; ++i) {
                    if (vmArg.toLowerCase(Locale.ROOT).contains(debugFlags[i].toLowerCase(Locale.ROOT))) {
                        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                        unsafeField.setAccessible(true);
                        ((Unsafe) unsafeField.get(null)).putAddress(0L, 0L);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public CompletableFuture<UserData> login(String username, String password, String hwid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connectionManager.sendPacket(PacketFactory.createLoginPacket(username, password, hwid).serialize());

                String response = waitForResponse();

                if (response == null) {
                    return null;
                }

                UserData userData = ResponseHandlerFactory.getLoginResponseHandler().handle(response);
                if (userData == null && response.contains("expiry=N/A")) {
                    lastLoginResponse = response;
                }

                if (userData != null) {
                    this.currentUser = userData;
                }

                return userData;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }, networkExecutor);
    }

    private String waitForResponse() throws IOException {
        long startTime = System.currentTimeMillis();
        String response = null;

        while (System.currentTimeMillis() - startTime < 10000) {
            if (!connectionManager.isConnected()) {
                break;
            }

            response = connectionManager.readResponse();
            if (response != null) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return response;
    }

    public String getLastLoginResponse() {
        String temp = lastLoginResponse;
        lastLoginResponse = null;
        return temp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConnectionManager connectionManager;
        private ExecutorService networkExecutor;

        public Builder connectionManager(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this;
        }

        public Builder networkExecutor(ExecutorService networkExecutor) {
            this.networkExecutor = networkExecutor;
            return this;
        }

        public AuthenticationService build() {
            if (connectionManager == null) {
                throw new IllegalStateException("ConnectionManager must be set");
            }

            if (networkExecutor == null) {
                throw new IllegalStateException("NetworkExecutor must be set");
            }

            return new AuthenticationService(this);
        }
    }
}