package com.chorus.impl.screen.auth;

import chorus0.Chorus;
import com.chorus.api.system.networking.NetworkManager;
import com.chorus.api.system.networking.auth.UserData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LoginScreen extends Screen {
    private String statusMessage = "Please enter your credentials to login";
    private int statusColor = 0xFFAA0000;
    private boolean isAuthenticating = false;
    private TextFieldWidget usernameField;
    private TextFieldWidget passwordField;
    
    public LoginScreen() {
        super(Text.literal("Login"));
    }

    public static String getHWID() {
        try {
            StringBuilder systemInfo = new StringBuilder();
            String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

            if (osName.contains("win")) {
                File psDir = new File(System.getenv("SystemRoot"),
                        "System32" + File.separatorChar + "WindowsPowerShell" + File.separatorChar + "v1.0");
                if (!psDir.exists() || !psDir.isDirectory()) {
                    throw new IOException("PowerShell directory missing: " + psDir.getAbsolutePath());
                }
                String psPath = psDir.getAbsolutePath() + "\\powershell.exe";

                String[] winCommands = {
                        psPath + " (get-wmiobject -class win32_physicalmemory -namespace root\\CIMV2).Capacity",
                        psPath + " (get-wmiobject -class win32_processor -namespace root\\CIMV2)",
                        psPath + " (get-wmiobject -class win32_physicalmemory -namespace root\\CIMV2).SMBiosMemoryType",
                        psPath + " (get-wmiobject -class win32_videocontroller -namespace root\\CIMV2).Description"
                };

                for (String cmd : winCommands) {
                    Process proc = Runtime.getRuntime().exec(cmd);
                    proc.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    reader.lines().forEach(systemInfo::append);
                }
            } else if (osName.contains("mac")) {
                String[] macCommands = {
                        "sysctl -n machdep.cpu.brand_string",
                        "system_profiler SPHardwareDataType | awk '/Serial/ {print $4}'",
                        "sysctl hw.ncpu",
                        "sysctl hw.memsize"
                };

                for (String cmd : macCommands) {
                    Process proc = Runtime.getRuntime().exec(cmd);
                    proc.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    reader.lines().forEach(systemInfo::append);
                }
            } else {
                String[] linuxCmd = {"/bin/sh", "-c", "lscpu | grep -e \"Architecture:\" -e \"Byte Order:\" -e \"Model name:\""};
                Process proc = Runtime.getRuntime().exec(linuxCmd);
                proc.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    systemInfo.append(line);
                }
            }

            MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
            hasher.update(systemInfo.toString().getBytes(StandardCharsets.UTF_8));
            byte[] digest = hasher.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(this.textRenderer, "Authentication Required", this.width / 2, this.height / 2 - 170, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, statusMessage, this.width / 2, this.height / 2 - 150, statusColor);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = 200;
        int fieldHeight = 20;
        int centerX = this.width / 2 - fieldWidth / 2;
        int startY = this.height / 2 - fieldHeight - 50;

        usernameField = new TextFieldWidget(this.textRenderer, centerX, startY, fieldWidth, fieldHeight, Text.literal(""));
        usernameField.setMaxLength(32);
        usernameField.setText("");
        usernameField.setPlaceholder(Text.of("Username"));
        this.addDrawableChild(usernameField);

        passwordField = new TextFieldWidget(this.textRenderer, centerX, startY + 30, fieldWidth, fieldHeight, Text.literal(""));
        passwordField.setMaxLength(32);
        passwordField.setText("");
        passwordField.setPlaceholder(Text.of("Password"));
        passwordField.setRenderTextProvider((text, index) -> Text.literal(text.replaceAll(".", "*")).asOrderedText());
        this.addDrawableChild(passwordField);

        ButtonWidget loginButton = ButtonWidget.builder(
                Text.literal("Login"),
                button -> {
                    if (isAuthenticating) return;

                    String username = usernameField.getText().trim();
                    String password = passwordField.getText().trim();

                    if (username.isEmpty() || password.isEmpty()) {
                        statusMessage = "Please enter both username and password";
                        statusColor = 0xFFAA0000;
                        return;
                    }

                    isAuthenticating = true;
                    statusMessage = "Authenticating...";
                    statusColor = 0xFFFFAA00;
                    button.active = false;

                    synchronized(Chorus.getInstance().authLock) {
                        try {
                            String hwid = getHWID();
                            NetworkManager networkManager = NetworkManager.getInstance();

                            if (!networkManager.isConnected()) {
                                statusMessage = "Connection lost. Reconnecting...";
                                CompletableFuture<Void> connectFuture = networkManager.connect();
                                try {
                                    connectFuture.get(5, TimeUnit.SECONDS);
                                    statusMessage = "Connected. Sending login request...";
                                } catch (Exception e) {
                                    statusMessage = "Failed to connect: " + e.getMessage();
                                    statusColor = 0xFFAA0000;
                                    isAuthenticating = false;
                                    button.active = true;
                                    return;
                                }
                            }

                            CompletableFuture<UserData> loginFuture = networkManager.login(username, password, hwid);
                            UserData userData;
                            try {
                                System.out.println("Waiting for login future to complete...");
                                userData = loginFuture.get(10, TimeUnit.SECONDS);
                                System.out.println("Login future completed, userData: " + (userData != null ? "not null" : "null"));
                            } catch (Exception e) {
                                statusMessage = "Login failed: " + e.getMessage();
                                statusColor = 0xFFAA0000;
                                isAuthenticating = false;
                                button.active = true;
                                System.out.println("Exception while waiting for login: " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }

                            if (userData != null) {
                                System.out.println("Authentication successful for user: " + userData.getUsername());
                                Chorus.getInstance().isAuthenticated = true;
                                statusMessage = "Authentication successful!";
                                statusColor = 0xFF00AA00;
                                this.client.setScreen(null);
                            } else {
                                System.out.println("Authentication failed - userData is null");

                                String response = networkManager.readResponse();
                                if (response != null && response.contains("expiry=N/A") && !response.contains("type=Lifetime")) {
                                    statusMessage = "Authentication failed - Your license has expired";
                                    System.out.println("License expired detected in response: " + response);
                                } else if (response != null && !response.contains("type=Lifetime")) {
                                    statusMessage = "Authentication failed - Requires lifetime license";
                                    System.out.println("Non-lifetime license detected in response: " + response);
                                } else {
                                    statusMessage = "Authentication failed - Invalid credentials";
                                }

                                statusColor = 0xFFAA0000;
                            }
                        } catch (Exception e) {
                            statusMessage = "Error: " + e.getMessage();
                            statusColor = 0xFFAA0000;
                            e.printStackTrace();
                        }
                    }

                    isAuthenticating = false;
                    button.active = true;
                })
                .dimensions(centerX, startY + 60, fieldWidth, fieldHeight)
                .build();

        this.addDrawableChild(loginButton);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Quit Game"),
                button -> {
                    this.client.scheduleStop();
                })
                .dimensions(centerX, startY + 90, fieldWidth, fieldHeight)
                .build());
    }
}
