package org.cubewhy.proxy.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.cubewhy.proxy.DomainForwardApplication.configFile;

@Component
public class SimpleUtils {
    @Resource
    FileUtils utils;

    public String getTargetHost(String host) throws IOException {
        String jsonString = readAll(utils.getExternalFile(configFile));
        JSONObject json = JSON.parseObject(jsonString);
        JSONObject matches = json.getJSONObject("matches");
        if (matches.containsKey(host)) {
            return matches.getString(host);
        }
        return null;
    }


    public String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String str;
        while (null != (str = reader.readLine())) {
            sb.append(str);
        }
        return sb.toString();
    }

    public String readAll(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
