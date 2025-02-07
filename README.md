
# Modified Petclinic app for Demo

## Changes made to upstream code:

- move readme.md to upstream-readme.md
- modified docker-compose.yml to start petclinic in addition to mysql for a fully self contained environment.
- modified petclinic to have two datasources, petclinic and pii, to get petclinic some sensitive pii information
    - The Owner object was copied to a Customer object.  The Customer object is considered PII.
    These objects are accessible via the `/customers/..` REST path.
- Added a sample SQLI vulnerability on the `DELETE /customers/{customerId}` REST path.
- Added a sample SQLI vulnerability on the `GET /customers` REST path. Use payload `' or 1=1; # `.
- modified docker-compose.yml to run an Agent and read config from the local `/agent` dir where people
can change Agent binaries and configs without needing to change any docker-compose config or other code/config
files.
- Add multi-stage Dockerfile to build petclinic and produce a docker image. It
doesn't require a person to have any dev tools installed as all building happens in the docker image
- Change database initialization from default springboot directives to manually configured beans because
springboot doesn't support automatic initialization of multiple datasources.
- Add a fake "diagnostics" page just for some interesting demo content.
- Add Basic user/password auth to `/customers/*` endpoints.
  - The username is `user` and the password is `password`.
- Add outbound service call to an astrology service on the welcome page
- Add a file read action to get the welcome message for the welcome page.

## Build and Run

Add agent credentials to the yaml file in the “agent” folder.

Start the app in a set of docker containers (1 app + 1 database server with 2 db instances) with the following commands:

docker compose build \
docker compose up 

Browse to localhost:8080

## Authenticating and Authorizing:
There are 3 different users with varying levels of permissions. 
Unfortunately, there isn’t a logout button yet, so in order to change your user, you must restart the server and log in again.

- Admin: U “admin” P “password” - this is the only role that has access to /owners endpoints
- User: U “user” P “password” - this is the role that has access to /customers endpoints
- No-role User: U “bob” P “password”



## Run on bare metal 
`sudo MYSQL_USER=$MYSQL_USER MYSQL_PASS=$MYSQL_PASS./buildandrun.sh`