1. Launch the broker

$ ddbroker -k broker-keys.json -r tcp://127.0.0.1:5555 -l d -s 0/0/0

2. [optional] Launch the ddclient and subscribe for the root topic

$ ddclient -d tcp://127.0.0.1:5555 -k public-keys.json -n c1
> subscribe sssaLara all

3. Deploy the "onos-configuration-agent" (former Connection Module) server

    [optional] you can modify and recompile this module with
    
    $ cd {onos-configuration-agent-dir}
    $ mvn clean install

    3.1 Start glassfish domain

    $ cd {glassfish4-dir}/glassfish/bin
    $ ./asadmin start-domain

    3.2 Open a separate terminal and start the server

    $ cd {glassfish4-dir}/javadb/bin
    $ ./startNetworkServer
    
    3.3 Go back on the previous terminal and deploy:
    
    $ ./asadmin deploy --name ConnectionModule {onos-configuration-agent-dir}/target/ConnectionModule-1.0-SNAPSHOT.war
    
    To undeploy please follow the steps in this order:
    
    undeploy (3.3) -> stop domain (3.1) -> close the server (3.2)


4. Start the DSE SDNApp:
    
    4.1 Run
    
    Open the SOON_ONOS project with an IDE (e.g., IntelliJ), then compile it and run the main class SOON_ONOS/src/SOON/DSE/dse.javadb
    You should see the information about the new application into the ddclient interface (the one of step 2)
    
    4.2 [optional] modify the data model and or PMT (former mapping file)
    
    If you need to modify the the YANG file, after editing you have to generate the new json yin. You may use the script provided in this folder:
    
    $ cd {onos-configuration-agent-dir}
    $ pyang -f yin -o yinFile.xml yangFile.yang
    $ python3 json_yin.py
    
    This will generate the yinFile.json
    
    Then you have to update all the modified files (yangFile.yang, yinFile.json, mappingFile.txt) in the SOON_ONOS/dist/SOON_ONOS.jar ("configuration/" folder).

5. Test

    You should be able to read and configure the application state at the following base url:
    
    http://127.0.0.1:9090/ConnectionModule-1.0-SNAPSHOT/webresources/sssaLara/orchestrator

6. Comparison tests

    For validation purpose, an onos application featuring the same internal structure of the DSE can be found in the sample-app folder. It can be deployed on an ONOS controller and the state can be accessed read/write at the following URL:
    
    http://127.0.0.1:8181/onos/sample/
