package com.cloudofgoods.client.event.listener;

import com.cloudofgoods.client.entity.Client;
import com.cloudofgoods.client.service.ClientService;
import com.cloudofgoods.client.event.RegistrationCompleteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements
        ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private ClientService clientService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //Create the Verification Token for the Client with Link
        Client client = event.getClient();
        String token = UUID.randomUUID().toString();
        clientService.saveVerificationTokenForUser(token, client);
        //Send Mail to client
        String url =
                event.getApplicationUrl()
                        + "/verifyRegistration?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }
}
