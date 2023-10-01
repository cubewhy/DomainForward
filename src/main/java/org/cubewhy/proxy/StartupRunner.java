package org.cubewhy.proxy;

import jakarta.annotation.Resource;
import org.cubewhy.proxy.utils.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static org.cubewhy.proxy.DomainForwardApplication.configFile;
import static org.cubewhy.proxy.DomainForwardApplication.configPath;

@Component
public class StartupRunner implements CommandLineRunner {
    @Resource
    FileUtils utils;

    @Override
    public void run(String... args) {
        utils.extractFile("config.json", configFile);
    }
}
