package org.wiremock.webhooks;

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.wiremock.webhooks.interceptors.WebhookTransformer;
import org.wiremock.webhooks.WebhookDefinition;
import wiremock.com.jayway.jsonpath.JsonPath;

public class AddRequestIdWebhookTransformer implements WebhookTransformer {

    private Notifier notifier;
  
    public AddRequestIdWebhookTransformer(Notifier notifier) {
        this.notifier = notifier;
    }
  
    @Override
    public WebhookDefinition transform(ServeEvent serveEvent, WebhookDefinition webhookDefinition) {
        this.notifier.info("Running the AddRequestIdWebhookTransformer");

        String body = serveEvent.getResponse().getBodyAsString();
        this.notifier.info("Response body = " + body);

        String requestId = JsonPath.read(body, "$.request_id");
        this.notifier.info("RequestId = " + requestId);

        String finalBody = webhookDefinition.getBody().replace("{{request_id}}", requestId);

        this.notifier.info("Callback body = " + finalBody);
  
        return webhookDefinition.withBody(finalBody);
    }
  }