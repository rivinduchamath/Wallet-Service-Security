package com.cloudofgoods.client.service;

import com.cloudofgoods.client.entity.Client;
import com.cloudofgoods.client.entity.VerificationToken;
import com.cloudofgoods.client.model.ClientModel;

import java.util.Optional;

public interface ClientService {
    Client registerUser(ClientModel clientModel);

    void saveVerificationTokenForUser(String token, Client client);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    Client findUserByEmail(String email);

    void createPasswordResetTokenForUser(Client client, String token);

    String validatePasswordResetToken(String token);

    Optional<Client> getUserByPasswordResetToken(String token);

    void changePassword(Client client, String newPassword);

    boolean checkIfValidOldPassword(Client client, String oldPassword);
}
