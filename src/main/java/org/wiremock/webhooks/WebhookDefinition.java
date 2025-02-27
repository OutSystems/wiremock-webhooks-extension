package org.wiremock.webhooks;

import wiremock.com.fasterxml.jackson.annotation.JsonCreator;
import wiremock.com.fasterxml.jackson.annotation.JsonIgnore;
import wiremock.com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.net.URI;
import java.util.List;

import static wiremock.com.google.common.collect.Lists.newArrayList;

public class WebhookDefinition {
    
    private RequestMethod method;
    private URI url;
    private List<HttpHeader> headers;
    private Body body = Body.none();
    private String delayInSeconds;

    @JsonCreator
    public WebhookDefinition(@JsonProperty("method") RequestMethod method,
                             @JsonProperty("url") URI url,
                             @JsonProperty("headers") HttpHeaders headers,
                             @JsonProperty("body") String body,
                             @JsonProperty("base64Body") String base64Body,
                             @JsonProperty("delayInSeconds") String delayInSeconds) {
        this.method = method;
        this.url = url;
        this.headers = newArrayList(headers.all());
        this.body = Body.fromOneOf(null, body, null, base64Body);

        this.delayInSeconds = delayInSeconds;
    }

    public WebhookDefinition() {
    }

    public RequestMethod getMethod() {
        return method;
    }

    public URI getUrl() {
        return url;
    }

    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    public String getBase64Body() {
        return body.isBinary() ? body.asBase64() : null;
    }

    public String getBody() {
        return body.isBinary() ? null : body.asString();
    }

    @JsonIgnore
    public byte[] getBinaryBody() {
        return body.asBytes();
    }

    public WebhookDefinition withMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public String getDelayInSeconds() {
        return this.delayInSeconds;
    }

    public WebhookDefinition withUrl(URI url) {
        this.url = url;
        return this;
    }

    public WebhookDefinition withUrl(String url) {
        withUrl(URI.create(url));
        return this;
    }

    public WebhookDefinition withHeaders(List<HttpHeader> headers) {
        this.headers = headers;
        return this;
    }

    public WebhookDefinition withHeader(String key, String... values) {
        if (headers == null) {
            headers = newArrayList();
        }

        headers.add(new HttpHeader(key, values));
        return this;
    }

    public WebhookDefinition withBody(String body) {
        this.body = new Body(body);
        return this;
    }

    public WebhookDefinition withBinaryBody(byte[] body) {
        this.body = new Body(body);
        return this;
    }

    public WebhookDefinition withDelayInSeconds(String delayInSeconds) {
        this.delayInSeconds = delayInSeconds;
        return this;
    }

}
