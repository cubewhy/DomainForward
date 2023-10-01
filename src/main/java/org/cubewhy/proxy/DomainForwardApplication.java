package org.cubewhy.proxy;

import org.cubewhy.proxy.utils.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class DomainForwardApplication {

    public static final File configPath = new File(System.getProperty("configPath", System.getProperty("user.home") + "/.cubewhy/domain-forward/"));
    public static final File applicationConfigFile = new File(configPath, "application.yml");
    public static final File configFile = new File(configPath, "config.json");

    public static void main(String[] args) {
        configPath.mkdirs();
        new FileUtils().extractFile("application.yml", applicationConfigFile);
        System.setProperty("spring.config.location", applicationConfigFile.getAbsolutePath()); // set config
        System.setProperty("file.encoding", "UTF-8"); // set encoding
        SpringApplication.run(DomainForwardApplication.class, args);
    }

}
