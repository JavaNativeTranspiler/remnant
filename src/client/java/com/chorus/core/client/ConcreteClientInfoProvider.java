package com.chorus.core.client;

import java.nio.file.Path;

public class ConcreteClientInfoProvider implements ClientInfoProvider {
    @Override
    public String branch() {
        return "main";
    }

    @Override
    public Path clientDir() {
        return Path.of(System.getProperty("user.home"), ".chorus");
    }

    @Override
    public Path configsDir() {
        return Path.of(System.getProperty("user.home"), ".chorus", "configs");
    }

    @Override
    public Path filesDir() {
        return Path.of(System.getProperty("user.home"), ".chorus", "files");
    }

    @Override
    public String name() {
        return "Chorus";
    }

    public ClientInfo provideClientInfo() {
        return new ClientInfo(name(), version(), branch(), clientDir(), filesDir(), configsDir());
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String getFullInfo() {
        return String.format("Welcome! Client: %s Version: %s Branch: %s", name(), version(), branch());
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
