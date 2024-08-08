# Eclipse Ditto Java Client examples

An integration for Eclipse Ditto that allows interaction with Ditto Thing as Digital Twins in the WoDT Platform.

## Configure

The module is preconfigured to work with a local Eclipse Ditto running in Docker. Find more information on
 [GitHub](https://github.com/eclipse/ditto/tree/master/deployment/docker).

You can change the configuration to your liking by editing `src/main/resources/config.properties`.
The configured usernames and passwords must be added to the nginx.htpasswd of Eclipse Ditto.
```bash
htpasswd nginx.htpasswd user1
```