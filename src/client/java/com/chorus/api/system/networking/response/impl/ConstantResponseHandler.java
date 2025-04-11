package com.chorus.api.system.networking.response.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.response.ResponseHandler;

@IncludeReference
public class ConstantResponseHandler implements ResponseHandler<Double> {
    @Override
    public Double handle(String response) {
        if (response != null && response.startsWith("CONST")) {
            return Double.parseDouble(response.substring(5));
        }
        return 0.0;
    }
} 