package com.crud.Sheets.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleSheetsConfig {

    @Value("${google.credentials.client-id}")
    private String clientId;

    @Value("${google.credentials.client-email}")
    private String clientEmail;

    @Value("${google.credentials.private-key-id}")
    private String privateKeyId;

    @Value("${google.credentials.private-key}")
    private String privateKey;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Sheets sheetsService() throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Monta o JSON em memória com TODOS os campos que a biblioteca espera
        String credentialsJsonString = String.format(
                "{\"type\": \"service_account\", \"client_id\": \"%s\", \"private_key_id\": \"%s\", \"private_key\": \"%s\", \"client_email\": \"%s\"}",
                this.clientId,
                this.privateKeyId,
                this.privateKey,
                this.clientEmail
        );

        InputStream credentialsStream = new ByteArrayInputStream(credentialsJsonString.getBytes());

        GoogleCredentials credential = ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName("Spring Sheets CRUD")
                .build();
    }
}