# Model Context Protocol Server for github

This Model Context Protocol(MCP) server enables Large Language Models (LLMs) to work with github.

![](images/github-demo.png)


## General Usage 

1. Install [jbang](https://www.jbang.dev/download/)
2. Configure your MCP Client to run the server (see [Claude Desktop Config](#claude-desktop-config) below)

Below are examples of command lines to use for configuring the server.

Start server:

```shell
jbang github@quarkiverse/quarkus-mcp-servers
```

## Example interaction

"list issues for maxandersen/metatest"

"add comment to last issue with body: 'This is a test comment'"


## Components

Below are the MCP components provided by this server.

### Tools 


* **add_issue_comment**(sessionid:str, body:str, issue_number:int, owner:str, repo:str)
     Add a comment to an existing issue

* **list_issues**(sessionid:str, owner:str, repo:str, [perPage:int])
     List issues in a GitHub repository with filtering options

* **getSessionId**
     Obtain a session id, used with all other provided tools in this project.
     Invoking this while unauthenticated, will direct the user to visit a url, provided by
     the MCP Server, that will have the user authenticate with github. The resulting 
     github access token is then maintained by the MCP Server against the sessionid. 
     Upon successful authentication, the user is also given a logout url they can use
     to revoke the MCP Servers authentication for the sessionid.


## Creating an OAuth2 App with GitHub

Navigate to https://github.com/settings/developers

Select OAuth Apps from the left nav

Click 'New OAuth App'

Give it a name, and set the callback url to be `http://127.0.0.1:8080/auth`

Note down the client ID, and click 'generate a new client secret' and note down the secret.

Choose a method to pass the credentials to the mcp server, via application properties, or via commandline arguments

### Setting the credentials via application.properties
Copy the client ID and secret to the appropriate properties within application.properties
Ensure the properties are uncommented (they are commented by default)
Rebuild and install this server.

### Setting the credentials via arguments using claude json / and [mcp-cli](https://github.com/chrishayuk/mcp-cli)
Add the arguments as below to the end of the argument list in your `claude_desktop.json` or `server_config.json` file:

```json
{
  "mcpServers": {
    "jdbc": {
      "command": "jbang",
      "args": [
        "github@quarkiverse/quarkus-mcp-server",
        "-Dquarkus.oidc.client-id=XXX",
        "-Dquarkus.oidc.credentials.secret=YYY",
      ]
    }
  }
}
```

