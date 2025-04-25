package io.quarkiverse.mcp.servers.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.HttpClientGitHubConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.WrapBusinessError;
import io.quarkiverse.mcp.server.runtime.JsonTextContentEncoder;

@WrapBusinessError(IOException.class)
public class MCPServerGitHub {

    private static final Logger log = LoggerFactory.getLogger(MCPRestBasedAuthManager.class);

    @Inject
    JsonTextContentEncoder jsonEncoder;

    @Inject
    MCPSessionStateManager sessionMgr;

    @Tool(description = "Get session id to use with MCPGitHub")
    String getSessionId() {
        //if we were http based, not stdio, this should come from the http session
        return UUID.randomUUID().toString();
    }

    record Issue(String title, String body, String state, String createdAt, String updatedAt, String url) {
    }

    @Tool(description = "Use MCPGitHub to list issues in a GitHub repository with filtering options")
    ToolResponse list_issues(
            @ToolArg(description = "The session id for MCPGitHub") String sessionId,
            @ToolArg(description = "Owner of the repository") String owner,
            @ToolArg(description = "Name of the repository") String repo,
            @ToolArg(description = "Number of items per page", required = false) Integer perPage) throws Exception {

        String token = sessionMgr.getToken(sessionId);
        if (token == null) {
            return ToolResponse.success(
                    "You are not authorized to use MCPGitHub. To authorize, visit http://127.0.0.1:8080/auth?sessionId="
                            + sessionId);
        }

        log.info("Invoking list issues for session " + sessionId + " repo " + owner + "/" + repo);

        GitHub ghc = getGitHubClient(token);

        var issues = ghc.searchIssues().q("repo:%s/%s".formatted(owner, repo)).list();

        if (perPage == null) {
            perPage = 10;
        }
        issues.withPageSize(perPage);

        List<Issue> rawissues = new ArrayList<>();
        Iterator<GHIssue> iterator = issues.iterator();
        int count = 0;
        while (iterator.hasNext() && count < perPage) {
            GHIssue issue = iterator.next();
            rawissues.add(new Issue(issue.getTitle(), issue.getBody(), issue.getState().toString(),
                    issue.getCreatedAt().toString(), issue.getUpdatedAt().toString(), issue.getHtmlUrl().toString()));
            count++;
        }

        return ToolResponse.success(jsonEncoder.encode(rawissues));
    }

    @Tool(description = "Add a comment to an existing issue")
    ToolResponse add_issue_comment(
            @ToolArg(description = "The session id for MCPGitHub") String sessionId,
            @ToolArg(description = "Comment body") String body,
            @ToolArg(description = "Issue number") int issue_number,
            @ToolArg(description = "Owner of the repository") String owner,
            @ToolArg(description = "Name of the repository") String repo) throws IOException {
        String token = sessionMgr.getToken(sessionId);
        if (token == null) {
            return ToolResponse.success(
                    "You are not authorized to use MCPGitHub. To authorize, visit http://127.0.0.1:8080/auth?sessionId="
                            + sessionId);
        }

        log.info("Invoking add issue comment for session " + sessionId + " repo " + owner + "/" + repo);

        GitHub ghc = getGitHubClient(token);

        GHIssue issue = ghc.getRepository(owner + "/" + repo).getIssue(issue_number);
        var result = issue.comment(body);
        return ToolResponse.success("Comment added: " + result.getHtmlUrl());
    }

    private static GitHub getGitHubClient(String token) {
        log.info("Creating GitHub Client with token: " + token);
        try {
            return new GitHubBuilder()
                    .withConnector(new HttpClientGitHubConnector())
                    .withOAuthToken(token)
                    .build();
        } catch (IOException e) {
            log.error("Failed to login to github with token", e);
            throw new ToolCallException("Failed to perform github operation, you may need to log in again.");
        }
    }

}
