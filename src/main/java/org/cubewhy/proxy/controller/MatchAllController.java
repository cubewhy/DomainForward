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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@RestController
@Slf4j
public class MatchAllController {
    @Resource
    SimpleUtils utils;

    @RequestMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public void forward(HttpServletRequest request, HttpServletResponse response) throws Exception {
        URI uri = new URI(request.getRequestURI());
        String host = ServletUriComponentsBuilder.fromCurrentRequest().build().getHost();
        String targetHost = null;
        if (host != null) {
            targetHost = utils.getTargetHost(host);
        }
        if (targetHost == null) {
            log.error("Domain not found: " + host);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(RestBean.failure(404, "域名不在映射列表中,如果你是站长,请在配置文件中添加域名" + host).toJson());
            return;
        }
        String path = uri.getPath();
        String query = request.getQueryString();
        String target = targetHost + path;
        log.info("Forward " + host + ":" + request.getLocalPort() + path + " to " + target);
        if (query != null && !query.isEmpty() && !query.equals("null")) {
            target = target + "?" + query;
        }
        URI newUri = new URI(target);
        String methodName = request.getMethod();
        HttpMethod httpMethod = HttpMethod.valueOf(methodName);
        ClientHttpRequest delegate = new SimpleClientHttpRequestFactory().createRequest(newUri, httpMethod);
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
        response.setHeader("X-Real-IP", request.getRemoteAddr());
    }
}
