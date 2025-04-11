package com.chorus.api.system.networking.response.factory;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.auth.UserData;
import com.chorus.api.system.networking.response.ResponseHandler;
import com.chorus.api.system.networking.response.impl.ConstantResponseHandler;
import com.chorus.api.system.networking.response.impl.JarSizeResponseHandler;
import com.chorus.api.system.networking.response.impl.LoginResponseHandler;

@IncludeReference
public class ResponseHandlerFactory {
    private static final LoginResponseHandler loginResponseHandler = new LoginResponseHandler();
    private static final JarSizeResponseHandler jarSizeResponseHandler = new JarSizeResponseHandler();
    private static final ConstantResponseHandler constantResponseHandler = new ConstantResponseHandler();
    
    public static ResponseHandler<UserData> getLoginResponseHandler() {
        return loginResponseHandler;
    }
    
    public static ResponseHandler<Boolean> getJarSizeResponseHandler() {
        return jarSizeResponseHandler;
    }
    
    public static ResponseHandler<Double> getConstantResponseHandler() {
        return constantResponseHandler;
    }
} 