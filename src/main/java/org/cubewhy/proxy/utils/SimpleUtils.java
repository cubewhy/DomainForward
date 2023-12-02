package org.cubewhy.proxy.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.cubewhy.proxy.DomainForwardApplication.configFile;

@Component
public class SimpleUtils {
    private JSONObject config;
    private JSONObject matches;
    private JSONObject redirects;

    public String findRedirect(String url) {
        check();
        if (redirects.containsKey(url)) {
            return redirects.getString(url);
        }
        return null;
    }

    public String getTargetHost(String host) throws IOException {
        check();
        if (matches.containsKey(host)) {
            return matches.getString(host);
        }
        return null;
    }

    private void check() {
        try {
            String jsonString = readAll(new FileUtils().getExternalFile(configFile));
            config = JSON.parseObject(jsonString);
            matches = patchMatches(config.getJSONObject("matches"));
            redirects = config.getJSONObject("redirects");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject patchMatches(JSONObject raw) {
        JSONObject json = new JSONObject();
        for (String s : raw.keySet()) {
            for (String domain : s.replace(" ", "").split(",")) {
                json.put(domain, raw.getString(s));
            }
        }
        return json;
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
