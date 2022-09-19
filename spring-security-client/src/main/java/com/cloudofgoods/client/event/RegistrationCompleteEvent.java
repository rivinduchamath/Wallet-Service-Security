package com.cloudofgoods.client.event;

import com.cloudofgoods.client.entity.Client;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {

    private final Client client;
    private final String applicationUrl;

    public RegistrationCompleteEvent(Client client, String applicationUrl) {
        super(client);
        this.client = client;
        this.applicationUrl = applicationUrl;
    }
}
