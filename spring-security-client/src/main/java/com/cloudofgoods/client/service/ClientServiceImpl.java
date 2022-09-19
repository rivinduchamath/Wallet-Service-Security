package com.cloudofgoods.client.service;

import com.cloudofgoods.client.entity.Client;
import com.cloudofgoods.client.entity.PasswordResetToken;
import com.cloudofgoods.client.entity.VerificationToken;
import com.cloudofgoods.client.model.ClientModel;
import com.cloudofgoods.client.repository.PasswordResetTokenRepository;
import com.cloudofgoods.client.repository.ClientRepository;
import com.cloudofgoods.client.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Client registerUser(ClientModel clientModel) {
        Client client = new Client();
        client.setEmail(clientModel.getEmail());
        client.setFirstName(clientModel.getFirstName());
        client.setLastName(clientModel.getLastName());
        client.setRole("USER");
        client.setPassword(passwordEncoder.encode(clientModel.getPassword()));

        clientRepository.save(client);
        return client;
    }

    @Override
    public void saveVerificationTokenForUser(String token, Client client) {
        VerificationToken verificationToken
                = new VerificationToken(client, token);

        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken
                = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return "invalid";
        }

        Client client = verificationToken.getClient();
        Calendar cal = Calendar.getInstance();

        if ((verificationToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }

        client.setEnabled(true);
        clientRepository.save(client);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken
                = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public Client findUserByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(Client client, String token) {
        PasswordResetToken passwordResetToken
                = new PasswordResetToken(client,token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken
                = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken == null) {
            return "invalid";
        }

        Client client = passwordResetToken.getClient();
        Calendar cal = Calendar.getInstance();

        if ((passwordResetToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";
    }

    @Override
    public Optional<Client> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getClient());
    }

    @Override
    public void changePassword(Client client, String newPassword) {
        client.setPassword(passwordEncoder.encode(newPassword));
        clientRepository.save(client);
    }

    @Override
    public boolean checkIfValidOldPassword(Client client, String oldPassword) {
        return passwordEncoder.matches(oldPassword, client.getPassword());
    }
}
