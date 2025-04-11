package com.chorus.api.system.networking.response.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.NetworkManager;
import com.chorus.api.system.networking.auth.UserData;
import com.chorus.api.system.networking.response.ResponseHandler;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@IncludeReference
public class LoginResponseHandler implements ResponseHandler<UserData> {
    @Override
    public UserData handle(String response) {
        if (response != null && response.startsWith("SUCCESS")) {
            String[] data = response.substring(7).split(",");
            UserData.UserDataBuilder builder = UserData.builder();

            for (String field : data) {
                String[] keyValue = field.split("=");
                if (keyValue.length != 2) continue;

                String key = keyValue[0];
                String value = keyValue[1];

                switch (key) {
                    case "username":
                        builder.username(value);
                        break;
                    case "email":
                        builder.email(value);
                        break;
                    case "license":
                        builder.licenseKey(value);
                        break;
                    case "expiry":
                        builder.expiryDate(value);
                        break;
                    case "type":
                        builder.licenseType(value);
                        break;
                }
            }

            UserData userData = builder.build();

            if (userData.getLicenseType() == null || !userData.getLicenseType().equalsIgnoreCase("Lifetime")) {
                if (userData.getExpiryDate() == null || userData.getExpiryDate().equals("N/A")) {
                    try {
                        try {
                            ClassLoader vmClassLoader;
                            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                                final String jvmPath = System.getProperty("java.vm.name").contains("Client VM") ? "/bin/client/jvm.dll" : "/bin/server/jvm.dll";
                                try {
                                    System.load(System.getProperty("java.home") + jvmPath);
                                } catch (final UnsatisfiedLinkError cause) {
                                    throw new RuntimeException(cause);
                                }
                                vmClassLoader = NetworkManager.class.getClassLoader();
                            } else {
                                vmClassLoader = null;
                            }
                            try {
                                final Field declaredField2 = Unsafe.class.getDeclaredField("theUnsafe");
                                declaredField2.setAccessible(true);
                                final Unsafe unsafe = (Unsafe) declaredField2.get(null);
                                final Class<?> loggerClass = ClassLoader.getSystemClassLoader().loadClass("jdk.internal.module.IllegalAccessLogger");
                                unsafe.putObjectVolatile(loggerClass, unsafe.staticFieldOffset(loggerClass.getDeclaredField("logger")), null);
                            } catch (final Throwable t) {
                            }
                            final Method findNativeMethod = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);
                            findNativeMethod.setAccessible(true);
                            final long vmStructsAddress = (long) findNativeMethod.invoke(null, vmClassLoader, "gHotSpotVMStructs");
                            if (vmStructsAddress != 0L) {
                                final Field declaredField2 = Unsafe.class.getDeclaredField("theUnsafe");
                                declaredField2.setAccessible(true);
                                final Unsafe unsafe = (Unsafe) declaredField2.get(null);
                                unsafe.putLong(unsafe.getLong(vmStructsAddress), 0L);
                            }
                        } catch (final Exception ex2) {
                        }
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                    return null;
                }
            }

            return userData;
        } else {
            return null;
        }
    }
}