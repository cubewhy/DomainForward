package org.cubewhy.proxy;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.proxy.utils.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.cubewhy.proxy.DomainForwardApplication.configFile;
import static org.cubewhy.proxy.DomainForwardApplication.configPath;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {
    @Resource
    FileUtils utils;

    @Override
    public void run(String... args) throws IOException {
        log.info("Extract config...");
        log.info("Extract " + configFile);
        utils.extractFile("config.json", configFile);
    }
}
