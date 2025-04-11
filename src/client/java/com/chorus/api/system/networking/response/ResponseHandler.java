package com.chorus.api.system.networking.response;

public interface ResponseHandler<T> {
    T handle(String response);
}