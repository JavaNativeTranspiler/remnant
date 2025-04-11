package com.chorus.core.client;

import java.nio.file.Path;

public record ClientInfo(String name, String version, String branch, Path clientDir, Path filesDir, Path configsDir) implements ClientInfoProvider {

    @Override
    public String getFullInfo() {
        return String.format("Welcome! Client: %s Version: %s Branch: %s", name, version, branch);
    }
}