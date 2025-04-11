package com.chorus.api.system.prot;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.NetworkManager;
import com.chorus.api.system.networking.packet.factory.PacketFactory;
import com.chorus.api.system.networking.response.factory.ResponseHandlerFactory;

import java.util.Map;
import java.util.concurrent.*;

@IncludeReference
public class MathProt {
    private static final Map<String, Double> consts = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;
    private static final Object INIT_LOCK = new Object();

    public static void initializeConstants() {
        if (!initialized) {
            synchronized (INIT_LOCK) {
                if (!initialized) {
                    fetchAllConstants();
                    initialized = true;
                }
            }
        }
    }

    public static void fetchAllConstants() {
        String[] constantNames = {
            "pi", "e", "phi", "sqrt2", "sqrt3", "ln2", "ln10", "size"
        };
        
        CompletableFuture<?>[] futures = new CompletableFuture[constantNames.length];
        
        for (int i = 0; i < constantNames.length; i++) {
            final String constantName = constantNames[i];
            futures[i] = CompletableFuture.runAsync(() -> {
                double value = fetchConst(constantName);
                consts.put(constantName, value);
            });
        }
        
        try {
            CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static double fetchConst(String name) {
        NetworkManager net = NetworkManager.getInstance();
        if (!net.isConnected()) return 0.0;
        
        try {
            net.sendPacket(PacketFactory.createConstantPacket(name));
            String response = net.readResponse();
            
            return ResponseHandlerFactory.getConstantResponseHandler().handle(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static double getConst(String name) {
        if (!initialized) {
            initializeConstants();
        }
        return consts.getOrDefault(name, 0.0);
    }

    public static double PI() { return getConst("pi"); }
    public static double E() { return getConst("e"); }
    public static double PHI() { return getConst("phi"); }
    public static double SQRT2() { return getConst("sqrt2"); }
    public static double SQRT3() { return getConst("sqrt3"); }
    public static double LN2() { return getConst("ln2"); }
    public static double LN10() { return getConst("ln10"); }
    public static double SIZE() { return getConst("size"); }
}
