# OVSDBREST ONOS APPLICATION

This application provides a ***minimal*** interface to an ovsdb device by exposing REST APIs.
The API allows to create/delete a bridge, attach/remove ports from an existing bridge, create peer patch and setup GRE tunnels.

## Install
To install the application on a running onos instance run the following steps.

- first of all, if it is not ready installed, you need to install the ovsdb driver provided by onos. On your onos root directory run:

        $ cd buck-out/gen/drivers/ovsdb/onos-drivers-ovsdb-oar
        $ onos-app {onos-address} install app.oar

- then build the source code of the ovsdbrest application through maven:

        $ git clone https://github.com/netgroup-polito/onos-applications
        $ cd onos-applications/ovsdb-rest
        $ mvn clean install

- Finally you can install the application through the command:

        $ onos-app {onos-address} install target/ovsdb-rest-1.9.0-SNAPSHOT.oar

(onos-address is the ip-address of ONOS server, e.g., 192.168.123.1)


## Activate
After installing the application, you can activate it through the onos cli by typing:

    # Open the ONOS cli (in this example, we suppose that ONOS is listening at the address 192.168.123.1)
    $ client -h 192.168.123.1
    onos> app activate org.onosproject.sampleapp

To check that the app has been activated type the following command in the onos cli:

    onos> log:tail

## Configure
After activating the application you need to configure the ovsdb node IP. This is done by using the onos Network Configuration system.

- Send a REST request as follows:

    **POST http://{onos-address}:8181/onos/v1/network/configuration/**

    ```json
    {
    	"apps": {
    		"org.onosproject.sampleapp": {
    			"ovsdbrest": {
    				"nodes": [
    					{
    						"ovsdbIp": "192.168.123.2",
    						"ovsdbPort": "6640"
    					}
    				]
    			}
    		}
    	}
    }
  ```

Check your ovsdb configuration to get the correct ip and port for the ovsdb node. If you have not configured the ovsdb-manager, run first:

        $ sudo ovs-vsctl set-manager ptcp:6640

The request uses basic HTTP authentication, so you need to provide onos username and password.
To verify that the configuration has been correctly pushed you can type log:tail from the onos cli.
The app will start contacting the ovsdb nodes and you should see some related logs from the onos cli.
* Remember to update the configuration based on your topology, i.e. typing the "devices" command on your ONOS cli, you have to add all the ovsdb nodes that appear on the cli


## API

- Create/Delete bridge:

    **POST http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}**

    **DELETE http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}**
    
 - Get the bridge ID given its name
    
    **GET http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}**

- Add/Remove a port in a bridge:

    **POST http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}**

    **DELETE http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}**

- Create patch port:

    **POST http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}/patch_peer/{peer-port}**

- Create/Delete a GRE tunnel:

    **POST http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}/gre/{local-ip}/{remote-ip}/{key}**

    **DELETE http://{onos-address}:8181/onos/ovsdb/{ovsdb-ip}/bridge/{bridge-name}/port/{port-name}/gre**
