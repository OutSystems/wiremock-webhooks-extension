package org.wiremock.webhooks;


import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.wiremock.webhooks.interceptors.WebhookTransformer;
import org.wiremock.webhooks.WebhookDefinition;
import wiremock.com.jayway.jsonpath.JsonPath;
import wiremock.org.apache.commons.io.IOUtils;
import wiremock.org.apache.http.client.HttpClient;
import wiremock.org.apache.http.client.methods.*;
import wiremock.org.apache.http.entity.ContentType;
import wiremock.org.apache.http.entity.StringEntity;
import wiremock.org.apache.http.Header;
import wiremock.org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;


public class CognitoHttpHeaderWebhookTransformer implements WebhookTransformer {

    private Notifier notifier;

    public CognitoHttpHeaderWebhookTransformer(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public WebhookDefinition transform(ServeEvent serveEvent, WebhookDefinition webhookDefinition) {
        return webhookDefinition.withHeader("Authorization", this.getAccessToken());
    }

    private String getAccessToken() {
        try {
            // Reads cognito configurations from environment variables
            String cognitoUrl = System.getenv("COGNITO_URL");
            String cognitoClientId = System.getenv("COGNITO_CLIENT_ID");
            String cognitoClientSecret = System.getenv("COGNITO_CLIENT_SECRET"); 

            this.notifier.info("COGNITO_URL = " + cognitoUrl);
            this.notifier.info("COGNITO_CLIENT_ID = " + cognitoClientId);
            this.notifier.info("COGNITO_CLIENT_SECRET size = " + ("" + cognitoClientSecret).length());

            // Add headers
            ArrayList<HttpHeader> headers = new ArrayList<HttpHeader>();
            headers.add(new HttpHeader("Content-Type", "application/x-www-form-urlencoded"));
            BasicCredentials basicCredentials = new BasicCredentials(cognitoClientId, cognitoClientSecret);
            headers.add(new HttpHeader("Authorization", basicCredentials.asAuthorizationHeaderValue()));

            // Builds the body
            String body = "grant_type=client_credentials&client_id=" +  cognitoClientId + "&scope=";
            
            // Calls cognito
            HttpUriRequest request = buildRequestWithHeaders(cognitoUrl, headers, body);
            HttpClient httpClient = HttpClientFactory.createClient();
            HttpResponse response = httpClient.execute(request);
            this.notifier.info("Cognito call done");
            this.notifier.info("Status code: " + response.getStatusLine().getStatusCode());

            // Checks the response
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Error getting cognito token");
            }

            // Returns the token
            String getTokenResponse = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            String accessToken = JsonPath.read(getTokenResponse, "$.access_token");

            this.notifier.info("Authorization token is: " + accessToken.substring(0, 10) + "...");

            return accessToken;

        } catch (IOException e) {
            this.notifier.info("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }        
    }

    private static HttpUriRequest buildRequestWithHeaders(String url, ArrayList<HttpHeader> headers, String body) {
        HttpUriRequest request = new HttpPost(url);

        for (HttpHeader header: headers) {
            request.addHeader(header.key(), header.firstValue());
        }

        HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
        entityRequest.setEntity(new StringEntity(body, ContentType.create("application/json")));

        return request;
    }

}