package com.chorus.api.system.networking.response.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.response.ResponseHandler;

@IncludeReference
public class JarSizeResponseHandler implements ResponseHandler<Boolean> {
    @Override
    public Boolean handle(String response) {
        if (response != null) {
            String[] parts = response.split(":");
            return Boolean.parseBoolean(parts[0]);
        }
        return false;
    }
} 