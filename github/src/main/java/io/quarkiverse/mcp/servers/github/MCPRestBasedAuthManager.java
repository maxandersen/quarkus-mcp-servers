package io.quarkiverse.mcp.servers.github;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;

@Path("")
public class MCPRestBasedAuthManager {

    private static final Logger log = LoggerFactory.getLogger(MCPRestBasedAuthManager.class);

    @Inject
    MCPSessionStateManager session;

    @Inject
    UserInfo userInfo;

    @Inject
    AccessTokenCredential accessToken;

    @Path("auth")
    @Authenticated
    @GET
    @Produces("text/html")
    public String authorize(@QueryParam("sessionid") String sessionid) {
        log.info("Authorizing " + sessionid + " with " + userInfo);
        String rawToken = accessToken.getToken();
        session.setToken(sessionid, rawToken);
        return "<html><body>Thank you " + userInfo.getName()
                + ",<br>You are now authorized, to logout <a href=\"http://127.0.0.1:8080/logout/" + sessionid
                + "\">click here.</a><br>DEBUG TOKEN:" + rawToken + "</body></html>";
    }

    @Path("logout/{sessionid}")
    @Authenticated
    @GET
    public String logout(@PathParam("sessionid") String sessionid) {
        log.info("Deauthing " + sessionid);
        session.setToken(sessionid, null);
        return "Authorization revoked";
    }
}
