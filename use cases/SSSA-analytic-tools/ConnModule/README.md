# ONOS CONFIGURATION AGENT

This module is a Web Service that is in charge of mediate between one or more service layers and the (one or more) Java/ONOS applications that will be orchestrated.

## Getting Started

### Prerequisites

This is a maven application, so you have to install maven.

### Installing

If you want, you can use the DoubleDecker service, in this case you need to have a DoubleDecker broker (you can find it here: https://github.com/Acreo/DoubleDecker), and put the tenant keys in the keys.json file (that you can find in the package resources/files). You should also change eventually the address where to find the ddbroker (default: localhost:5555) in the file ConnectionModule.java (line 59).

### Compile
Run the command
```
mvn clean install
```

It will generate the .war in the target subfolder.

#### RUN

You just have to deploy the web service: I used the Glassfish server 4.1 server (you can find the download here: http://download.oracle.com/glassfish/4.1.1/nightly/index.html). Unzip the downoaded zip. To start a server domain and deploy the ConnectionModule, from the CLI:

```
cd glassfish/javadb
bin/startNetworkServer
```
and from another CLI
```
cd glassfish/glassfish4/bin
./asadmin start-domain
./asadmin deploy --name ConnectionModule /path/to/the/target/frogsssa-1.0-SNAPSHOT.war
```

Only after that, you have to run the SDN Application with the StateListener (example with NAT here: https://github.com/netgroup-polito/onos-applications/tree/ovsdbrest/model-based-configurable-nat)

You are now able to reach the web service through the APIs, and orchestrate each Application is attached to it.

APIs are:
- GET /{AppId}/DM
- GET /{AppId}/{varId}
- POST /{AppId}/{varId}
- DELETE /{AppId}/{varId}

where DM is the Data Model, and the {varId} is the path from the root of the DataModel to the variable you want to indicate (e.g. /rootVar/containerVar/listVar[index]/leafVar)
