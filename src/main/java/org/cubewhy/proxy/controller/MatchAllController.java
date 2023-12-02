package org.cubewhy.proxy.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.proxy.entity.RestBean;
import org.cubewhy.proxy.utils.SimpleUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@Slf4j
public class MatchAllController {
    @Resource
    SimpleUtils utils;
    @Resource
    SimpleClientHttpRequestFactory httpRequestFactory;

    @RequestMapping(value = "/**")
    public void forward(HttpServletRequest request, HttpServletResponse response) throws Exception {
        URI uri = new URI(URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8));
        String host = ServletUriComponentsBuilder.fromCurrentRequest().build().getHost();
        String targetHost = null;
        String path = uri.getPath();
        String query = request.getQueryString();
        if (host != null) {
            targetHost = utils.getTargetHost(host);
        }
        // check www first
        if (utils.getWwwRedirect(host)) {
            log.info("WWW redirect: " + host);
            response.sendRedirect(request.getProtocol().split("/")[0]  + "://" + "www." + host + path + "?" + query);
            return;
        } else if (targetHost == null) {
            log.error("Domain not found: " + host);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(RestBean.failure(404, "域名不在映射列表中,如果你是站长,请在配置文件中添加域名" + host).toJson());
            return;
        }
        String target = targetHost + path;
        String redirect = utils.findRedirect(host + path);
        // match redirect
        if (redirect != null) {
            // send!
            log.info("Redirect " + host + path + " to " + redirect);
            response.sendRedirect(redirect);
            return;
        }
        if (query != null && !query.isEmpty() && !query.equals("null")) {
            target = target + "?" + query;
        }
        log.info("Forward " + host + ":" + request.getLocalPort() + path + " to " + target);
        URL newUri = new URL(target);
        String methodName = request.getMethod();
        HttpMethod httpMethod = HttpMethod.valueOf(methodName);
        ClientHttpRequest delegate = httpRequestFactory.createRequest(newUri.toURI(), httpMethod);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> v = request.getHeaders(headerName);
            List<String> arr = new ArrayList<>();
            while (v.hasMoreElements()) {
                arr.add(v.nextElement());
            }
            delegate.getHeaders().addAll(headerName, arr);
        }
        StreamUtils.copy(request.getInputStream(), delegate.getBody());

        try (ClientHttpResponse clientHttpResponse = delegate.execute()) {
            response.setStatus(clientHttpResponse.getStatusCode().value());
            clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> response.setHeader(key, it)));
            StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
        }
    }
}
