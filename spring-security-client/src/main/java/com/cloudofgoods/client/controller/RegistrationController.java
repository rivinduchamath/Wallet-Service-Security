package com.cloudofgoods.client.controller;

import com.cloudofgoods.client.entity.Client;
import com.cloudofgoods.client.entity.VerificationToken;
import com.cloudofgoods.client.event.RegistrationCompleteEvent;
import com.cloudofgoods.client.model.PasswordModel;
import com.cloudofgoods.client.model.ClientModel;
import com.cloudofgoods.client.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody ClientModel clientModel, final HttpServletRequest request) {
        Client client = clientService.registerUser(clientModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                client,
                applicationUrl(request)
        ));
        return "Success";
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = clientService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")) {
            return "Client Verified Successfully";
        }
        return "Bad Client";
    }
    @GetMapping("/verifyRegistration1")
    public String verifyRegistration1(@RequestParam("token") String token) {
        String result = clientService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")) {
            return "Client Verified Successfully";
        }
        return "Bad Client";
    }


    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken
                = clientService.generateNewVerificationToken(oldToken);
        Client client = verificationToken.getClient();
        resendVerificationTokenMail(client, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        Client client = clientService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if(client !=null) {
            String token = UUID.randomUUID().toString();
            clientService.createPasswordResetTokenForUser(client,token);
            url = passwordResetTokenMail(client,applicationUrl(request), token);
        }
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel) {
        String result = clientService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }
        Optional<Client> user = clientService.getUserByPasswordResetToken(token);
        if(user.isPresent()) {
            clientService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        Client client = clientService.findUserByEmail(passwordModel.getEmail());
        if(!clientService.checkIfValidOldPassword(client,passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save New Password
        clientService.changePassword(client,passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }

    private String passwordResetTokenMail(Client client, String applicationUrl, String token) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to Reset your Password: {}",
                url);
        return url;
    }


    private void resendVerificationTokenMail(Client client, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }


    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
