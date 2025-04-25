package io.quarkiverse.mcp.servers.github;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

// Simple session state mgr that just tracks github access token for the session
// This could instead track an object with access token/refresh token etc for the session
//
// Then isAuthorized could validate the accessToken is still valid,

@ApplicationScoped
public class MCPSessionStateManager {
    Map<String, String> authMap = new HashMap<>();

    public String getToken(String sessionId) {
        return authMap.getOrDefault(sessionId, null);
    }

    public void setToken(String sessionId, String value) {
        if (value == null) {
            authMap.remove(sessionId);
        } else {
            authMap.put(sessionId, value);
        }
    }
}
