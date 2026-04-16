package com.demo.uaa.dto;

/**
 * @author TangZhikai
 */
public class TokenRequest {
    private String grantType;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;

    // Getters and Setters
    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
}
