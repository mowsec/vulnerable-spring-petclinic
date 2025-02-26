# JNDI-Injection-Exploit

[Materials about JNDI Injection](https://www.blackhat.com/docs/us-16/materials/us-16-Munoz-A-Journey-From-JNDI-LDAP-Manipulation-To-RCE.pdf)

## Description

JNDI-Injection-Exploit is a tool for generating workable JNDI links and provide background services by starting RMI server,LDAP server and HTTP server. RMI server and LDAP server are based on  [marshals](https://github.com/mbechler/marshalsec) and modified further to link with HTTP server.
This is a modified version of  https://github.com/welk1n/JNDI-Injection-Exploit .

## In App Trojan
The In App Trojan is an example of a Log4Shell payload that rather than executing a command or downloading a file onto the underlying system, 
it sets up a Thread within the JVM of the application which continues to listen for inbound requests to the webapp. When one is found with a specific parameter.
It base64 decodes the value of that paremeter and passes it to the Nashorn Scripting Engine for execution.



