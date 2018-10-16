#!/usr/bin/python

"""
Simple example of setting network and CPU parameters

NOTE: link params limit BW, add latency, and loss.
There is a high chance that pings WILL fail and that
iperf will hang indefinitely if the TCP handshake fails
to complete.
"""

from mininet.cli import CLI
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import CPULimitedHost
from mininet.link import TCLink
from mininet.util import dumpNodeConnections, quietRun
from mininet.log import setLogLevel, info
from mininet.node import Controller, RemoteController, OVSKernelSwitch
from time import sleep

#from INIHandler import IniHandler

import cPickle as pickle
import os, socket, thread, ipaddr
import subprocess

IP = '0.0.0.0'
PORT = 50000
ADS = (IP, PORT)

class SingleSwitchTopo(Topo):
    "Single switch connected to n self.myhosts."
    def __init__(self, n=2, **opts):
        Topo.__init__(self, **opts)
        switch = self.addSwitch('s1')
        for h in range(n):
            # Each host gets 50%/n of system CPU
            host = self.addHost('h%s' % (h + 1),
                                cpu=.5 / n)
            # 10 Mbps, 5ms delay, 10% loss
            self.addLink(host, switch,
                         bw=10, delay='5ms', loss=10, use_htb=True)

class ERCSTopo( Topo ):
    '''
    Tree and fat tree generator for 3 levels. For mininet 2.0
    Outside host -> "internet"(exit point)
    '''
    
    def __init__(self, **opts):
        "Create custom topo."
        # Initialize topology
        Topo.__init__(self, **opts)

        self.core_switches = list()
        self.agg_switches = list()
        self.edge_switches = list()
        self.myhosts = list()
        self.outside_hosts = list()
        self.alllinks = {}
        
        #Queue number per switch port and bandwidth (mbps)
        self.queue_bw = {}
        #self.queue_bw[1] = 5
        #self.queue_bw[2] = 5

        self.socket_path = ""
        self.udp_ratio = 0.5
        #self.out_no =
        self.host_detectable_timeout = 5
        #inithandler = IniHandler(str(os.path.dirname(os.path.realpath(__file__)))+"/conf.ini")
        #self.getArgsFromIni(inithandler)
        #self.generateTopology()


        
    def getArgsFromIni(self, inithandler):
        try :
            section = "TopologySwitches"
            key = "core_no"
            self.core_no = int(inithandler.read_ini_value(section, key))
            key = "agg_no"
            self.agg_no = int(inithandler.read_ini_value(section, key))
            key = "edge_no"
            self.edge_no = int(inithandler.read_ini_value(section, key))
            
            section = "TopologyHosts"
            key = "out_no"
            self.out_no = int(inithandler.read_ini_value(section, key))
            key = "host_no"
            self.host_no = int(inithandler.read_ini_value(section, key))
            key = "host_detectable_time"
            self.host_detectable_timeout = int(inithandler.read_ini_value(section, key))
            
            section = "TopologyLinks"
            key = "edgetoagglinkno"
            self.edge_agg_link_no = int(inithandler.read_ini_value(section, key))
            key = "aggtocorelinkno"
            self.agg_core_link_no = int(inithandler.read_ini_value(section, key))
            key = "coretooutlinkno"
            self.core_out_link_no = int(inithandler.read_ini_value(section, key))
            
            section = "SwitchBandwidth"
            key = "core_bw"
            self.core_bw = float(inithandler.read_ini_value(section, key))
            key = "agg_bw"
            self.agg_bw = float(inithandler.read_ini_value(section, key))
            key = "edge_bw"
            self.edge_bw = float(inithandler.read_ini_value(section, key))
            key = "out_bw"
            self.out_bw = float(inithandler.read_ini_value(section, key))
            
            section = "SwitchQueues"
            key = "queue_no"
            queue_no = int(inithandler.read_ini_value(section, key))
            for queue in range(queue_no):
                key = "queue_bw"+str(queue + 1)
                self.queue_bw[queue + 1] = float(inithandler.read_ini_value(section, key))
            
            section = "Traffic"
            key = "udp_ratio"
            self.udp_ratio = float(inithandler.read_ini_value(section, key))
            key = "iperf_port"
            self.iperf_port = int(inithandler.read_ini_value(section, key))

            section = "Socket"
            key = "socket_path"
            self.socket_path = inithandler.read_ini_value(section, key)

        except Exception, e :
            print("INI File doesn't contain valid values format")
            print e
            os._exit(0)


    #def generateTopology(self):

topos = { 'ercstopo': ( lambda: ERCSTopo() ) }

def waitControllertoConnectSwitches():
    '''
    Add remote controller
    '''
    info( '*** Waiting for all switches to connect to the controller' )
    while 'is_connected' not in quietRun( 'ovs-vsctl show' ):
        sleep( 1 )
        info( '.' )
    info( ' Done\n' )

def makeHostDetectable(ercs_topo, hosts, net):
    '''
    Make Host detectable by hosttracker
    '''
    print "making hosts detectable"
    #simpleHost = net.getNodeByName("h255")
    hosts2 = hosts;
    #for host_name in hosts:
    for host_name in hosts:
        host = net.getNodeByName(host_name)
        #print "host name = ", host_name
        #host.sendCmd("timeout " + str(ercs_topo.host_detectable_timeout) + " ping 10.0.0.255 -W 1")
        #host.cmd( "ping 10.0.0.255 -w 2 &")
        for host_target_name in hosts2:
            if host_name != host_target_name: # and host_name != "h255" and host_target_name !="h255": 
                 print "pinging from host name = " + host_name + " to " + host_target_name
                 host_target = net.getNodeByName(host_target_name)
                 host.cmd( "ping " + str(host_target.IP()) + " -w 1 &")
                 #print "ping sent from host name = " + host_name + " to " + host_target_name
    print "Done!"
    sleep(ercs_topo.host_detectable_timeout)
    #for host_name in hosts:
    #for host_name in hosts:
    #    host = net.getNodeByName(host_name)
    #    host.monitor()
   
    print "Done making host detectable"

def waitControllertoConnectTpGenerator(ercs_topo, net, *args):
    info( '*** Waiting controller to connect to topology generator...' )
    #s = socket.socket(socket.AF_UNIX, socket.STREAM)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #try:
       #os.remove(ADS)
    #except OSError:
    #    pass
    s.bind(ADS) #131.114.172.185
    print(' bind successful\n')
    s.listen(1)
    print("Socket now listening")
    conn, addr = s.accept()
    print(' Done\n')
    if len(args)==0 :
        thread.start_new_thread(receiveDataFromController, (conn, ercs_topo, net)) 
    else :
        receiveDataFromController(conn, ercs_topo, net)
    #conn.close()

def ping_thread (threadName, src, dst):
    while (1):
        sleep(5)
        src.cmd("ping -c5 " + dst.IP() + " >> pingResults.txt &")

def receiveDataFromController(conn, ercs_topo, net):
    info( '\n*** Waiting for data from the controller...' )
    hostIperfport = list()
    accepted_list = []
    pingString = []
    pingSummary = []
    avg_latency = []
    while 1:
        try:
            #Get the data from the stream
            #(host_ip, bw, holding_time, outside_host_ip) = pickle.loads(conn.recv(2048))
            #try:
            print("receiving data from client...")
            data = conn.recv(4096)
            print "received data:", data
            #if not data: break
            #conn.send("Thank you client")
            allData = data.split("***")
             
            
            for currentData in allData:
                print "check currentData"
                if not currentData:
	            continue                
                [request_id, ip_src, ip_dst, band, timeout, serviceTime] = currentData.split("///")
                #print("response has been sent to client ", data)    
     
                #get a unique port number by the combination of the two ip strings plus the starting port nunmber
                #hostIperfport.append(host_ip+outside_host_ip)
                #add the other string because we need to ports, UDP and TCP
                #hostIperfport.append(outside_host_ip+host_ip)    
                #Get host by ip (get both outside hosts and other host)
                src = getHostNamebyIP(ercs_topo, net, ip_src)
                dst = getHostNamebyIP(ercs_topo, net, ip_dst)
                
                if request_id=="0" :
                    src0=src
                    dst0=dst
                    ip_dst0=ip_dst
                    ip_src0=ip_src
                    #sleep(5)
                    #thread.start_new_thread(ping_thread, ("pinging thread", src0, dst0))
                    src.cmd( "ping " + str(ip_dst) + " >> pingresults.txt &") 
                    #subprocess.call("ping %s > pingresults.txt" % )
                else :
                    '''
                    #get the ports for iperf
                    iperf_udp_port = ercs_topo.iperf_port + hostIperfport.index(host_ip+ip_src) 
                    iperf_tcp_port = ercs_topo.iperf_port + hostIperfport.index(ip_src+ip_dst)
 
                    #host.monitor()
                    #outside_host.monitor()
                    #UDP            
                    '''
                    #dst.cmd("timeout " + str(timeout) + " iperf -s -u -p " + str(iperf_udp_port) + "&")
                   
                    #dst.cmd("timeout " + serviceTime*2 + " iperf -s -u &")
                    dst.cmd("iperf -s -u -i 1 &")
                    #src.cmd("iperf -c "+dst+" -u -r -p "+str(iperf_udp_port)+" -t "+str(timeout)+
                    #        " -b "+band+"M" + "&")
                    #src.cmd("iperf -c " + str(ip_dst) + " -u -t " + serviceTime + " -b 10M &")
                    src.cmd("iperf -c " + str(ip_dst) + " -u -t " + serviceTime + " -b 10M &") 
                    #results = net.iperf( ( src, dst ), l4Type='UDP', udpBw='5M') #, seconds=30 ) # args='-t 30' )
                    #src.cmd("ping "+ip_dst+"&")    
    
                    #r="Thank you"
                    #conn.send("Thank you \r\n")

                    print "UDP traffic sent from ", src, " to ", dst
                    #src.cmd( "ping -w " + str(serviceTime) + " "+ str(ip_dst) + " >> pingresults\\result" + str(request_id) + ".txt &")
                   
                    
                    #if request_id not in accepted_list:
                        #pingString.append(" ")
                        #accepted_list.append(request_id)
                        #src.cmd( "ping -w " + str(serviceTime) + " "+ str(ip_dst) )) # + " >> pingresults\\result" + str(request_id) + ".txt &")
                        #pingSummary[int(request_id)-1] = []               
                        #pingSummary[int(request_id)-1] = pingString[int(request_id)-1].split("/")
                        #avg_latency[int(request_id)-1] = pingSummary[int(request_id)-1][4]
                        #f = open('pingresults' + request_id,'w')
                        #f.write(avg_latency[request_id-1]) # python will convert \n to os.linesep
                        #f.close() # you can omit in most cases as the destructor will call it
                    #print "Thank you" 
      
                    #Should close socket after program quitting
                    #conn.close()
            
            #if request_id not in accepted_list:
                #sleep(15)
                #src0.cmd( "ping -c 5 " + str(ip_dst0) + " >> pingresults.txt &") # | tail -2
                #accepted_list.append(request_id)
        except Exception, e:
            info('*** Controller Disconnected\n')
            print e
            conn.close()
            while True :
                waitControllertoConnectTpGenerator(ercs_topo, net, 0)
                sleep(1)

def installStaticARPEntry(ercs_topo, net):
    '''
    Install static arp entries for all the outside hosts and hosts
    '''
    #populate the arp table of the hosts
    for host_name in ercs_topo.myhosts:
        host = net.getNodeByName(host_name)
        for outhost_name in ercs_topo.outside_hosts:
            outhost = net.getNodeByName(outhost_name)
            mac = outhost.cmd("ifconfig -a | grep HW | cut -c39-55").rstrip()
            host.cmd("arp -s "+str(outhost.IP())+" "+str(mac))
            #print "arp -s "+str(outhost.IP())+" "+str(mac)

    #populate the arp table of the outside hosts
    for outhost_name in ercs_topo.outside_hosts:
        outhost = net.getNodeByName(outhost_name)
        for host_name in ercs_topo.myhosts:
            host = net.getNodeByName(host_name)
            mac = host.cmd("ifconfig -a | grep HW | cut -c39-55").rstrip()
            outhost.cmd("arp -s "+str(host.IP())+" "+str(mac))
            #print "arp -s "+str(host.IP())+" "+str(mac)
    

def getHostNamebyIP(ercs_topo, net, ip):
    for host_name in ercs_topo.myhosts: #+ercs_topo.outside_hosts:
        host = net.getNodeByName(host_name)
        for intf in host.intfNames():
            if str(host.IP(intf)) == ip:
                return host
    return None

def runCli(net, *args):
    net.run( CLI, net )

def startTopology():
    #Create network and add queues to switches
    ercs_topo = ERCSTopo()
    #net = Mininet(controller = RemoteController, topo=ercs_topo,
    #             link=TCLink)
   
    #taken from my code
    net = Mininet(controller=RemoteController, switch=OVSKernelSwitch, autoSetMacs=True, autoStaticArp=True, listenPort=60001)
    #net = Mininet(controller=RemoteController, topo=ercs_topo, switch=OVSKernelSwitch, autoSetMacs=True, autoStaticArp=False, listenPort=60001)
    #Add remote controller
    net.addController( name='c0', controller=RemoteController,
            ip='131.114.171.153' )

    '''
    for num in range(ercs_topo.out_no):
        hostname='o%i' % (num+1)
        hostip = '10.0.0.%i' % (num+1)
        net.getNodeByName(hostname).setIP(hostip, 8)
    '''
    
    info( '*** Adding hosts\n' )
    h1 = net.addHost( 'h1', ip='10.0.1.1' )
    h2 = net.addHost( 'h2', ip='10.0.2.1' )
    h3 = net.addHost( 'h3', ip='10.0.3.1' )
    h4 = net.addHost( 'h4', ip='10.0.4.1' )
    h5 = net.addHost( 'h5', ip='10.0.5.1' )
    h6 = net.addHost( 'h6', ip='10.0.6.1' )
    h7 = net.addHost( 'h7', ip='10.0.7.1' )
    h8 = net.addHost( 'h8', ip='10.0.8.1' )
    h9 = net.addHost( 'h9', ip='10.0.9.1' )
    h10 = net.addHost( 'h10', ip='10.0.10.1' )
    h11 = net.addHost( 'h11', ip='10.0.11.1' )

    htest1 = net.addHost( 'htest1', ip='10.0.14.1' )
    htest2 = net.addHost( 'htest2', ip='10.0.15.1' )

    #h255 = net.addHost( 'h255', ip='10.0.0.255' )

    ercs_topo.myhosts.append( 'h1' )
    ercs_topo.myhosts.append( 'h2' )
    ercs_topo.myhosts.append( 'h3' )
    ercs_topo.myhosts.append( 'h4' )
    ercs_topo.myhosts.append( 'h5' )
    ercs_topo.myhosts.append( 'h6' )
    ercs_topo.myhosts.append( 'h7' )
    ercs_topo.myhosts.append( 'h8' )
    ercs_topo.myhosts.append( 'h9' )
    ercs_topo.myhosts.append( 'h10' )
    ercs_topo.myhosts.append( 'h11' )
    ercs_topo.myhosts.append( 'htest1' )
    ercs_topo.myhosts.append( 'htest2' )
    #ercs_topo.myhosts.append( 'h255' )

    info( '*** Adding switch\n' )
    seattle = net.addSwitch( 's1' )
    sunnyvale = net.addSwitch( 's2' )
    losangeles = net.addSwitch( 's3' )
    denver = net.addSwitch( 's4' )
    kansascity = net.addSwitch( 's5' )
    housten = net.addSwitch( 's6' )
    chicago = net.addSwitch( 's7' )
    indianapolis = net.addSwitch( 's8' )
    atlanta = net.addSwitch( 's9' )
    washingtondc = net.addSwitch('s10' )
    newyork = net.addSwitch('s11' )

    
    #linkopts = dict(bw=20, delay='1ms', losspctl=0, max_queue_size=10000, use_htb=True)

    info( '*** Creating links\n' )
    net.addLink( h1, seattle, 0, 1 )#, cls=None, **linkopts )
    net.addLink( htest1, seattle, 0, 9 )# , cls=None, **linkopts )
    net.addLink( h2, seattle, 0, 7 )#, cls=None, **linkopts )
    net.addLink( h3, losangeles, 0, 1 )# , cls=None, **linkopts )
    net.addLink( h4, losangeles, 0, 7 )#, cls=None, **linkopts )
    net.addLink( h5, kansascity, 0, 1 )#, cls=None, **linkopts  )
    net.addLink( h6, kansascity, 0, 7 )#, cls=None, **linkopts  )
    net.addLink( h7, chicago, 0, 1 )#, cls=None, **linkopts  )
    net.addLink( h8, chicago, 0, 7 )#, cls=None, **linkopts  )
    net.addLink( h9, atlanta, 0, 1 )#, cls=None, **linkopts  )
    net.addLink( h10, atlanta, 0, 7 )#, cls=None, **linkopts  )
    net.addLink( htest2, atlanta, 0, 9 )
    net.addLink( h11, newyork, 0, 1 )#, cls=None, **linkopts  )
    #net.addLink( htest2, newyork, 0, 9 )#, cls=None, **linkopts  )

    net.addLink( seattle, sunnyvale, 2, 2 )#, cls=None, **linkopts  )
    net.addLink( seattle, denver, 5, 2 )#, cls=None, **linkopts  )
    net.addLink( sunnyvale, denver, 5, 5 )#, cls=None, **linkopts  )
    net.addLink( sunnyvale, sunnyvale, 3, 4 )#, cls=None, **linkopts  )  #link to middlebox
    net.addLink( sunnyvale, losangeles, 6, 2 )#, cls=None, **linkopts  )
    net.addLink( losangeles, housten, 5, 2 )#, cls=None, **linkopts  )
    net.addLink( denver, denver, 3, 4 )#, cls=None, **linkopts  )  #link to middlebox
    net.addLink( denver, kansascity, 6, 2 )#, cls=None, **linkopts  )
    net.addLink( kansascity, housten, 5, 5 )#, cls=None, **linkopts  ) #port for housten changed from 2 to 5
    net.addLink( kansascity, indianapolis, 6, 2 )#, cls=None, **linkopts  )
    net.addLink( housten, housten, 3, 4 )#, cls=None, **linkopts  ) #link to midddlebox
    net.addLink( housten, atlanta, 6, 2 )#, cls=None, **linkopts  )
    net.addLink( atlanta, indianapolis, 5, 5 )#, cls=None, **linkopts  )
    net.addLink( atlanta, washingtondc, 6, 2 )#, cls=None, **linkopts  )
    net.addLink( washingtondc, washingtondc, 3, 4 )#, cls=None, **linkopts  ) #link to middlebox
    net.addLink( indianapolis, indianapolis, 3, 4 )#, cls=None, **linkopts  ) #link to middlebox
    net.addLink( indianapolis, chicago, 6, 2 )#, cls=None, **linkopts  )
    net.addLink( chicago, newyork, 5, 2 )#, cls=None, **linkopts  )
    net.addLink( newyork, washingtondc, 5, 5 )#, cls=None, **linkopts  )

    net.start()

    waitControllertoConnectSwitches()
    

    #Andrea: remember to insert the correct attribute, that collects the host of your topology
    makeHostDetectable(ercs_topo,ercs_topo.myhosts, net)  
   
    #installStaticARPEntry(ercs_topo, net) 
    print "Done!"
    '''
    Connecting the controller to the Topology generator
    '''
    waitControllertoConnectTpGenerator(ercs_topo, net)
        
    '''
    Run Cli
    '''
    info( '\n*** Running CLI\n' )
    CLI( net )

    '''
    Stop Topology
    '''
    net.stop()

    #open thread to wait for controllers to connect

    #rc = RemoteController('rc')
    
    #rc_state = rc.start()
    #rc.stop()
    #scratchnet - try to connect with it


    #net.iperf( )
    '''
    dumpNodeConnections(net.self.myhosts)
    print "Testing network connectivity"
    net.pingAll()
    print "Testing bandwidth between h1 and h4"
    h1, h4 = net.getNodeByName('h1', 'h4')
    net.iperf((h1, h4))
    '''

if __name__ == '__main__':
    setLogLevel('info')
    startTopology()
    #ss = SingleSwitchTopo()
    
    #net = Mininet(controller = RemoteController, topo=ss,
    #             host=CPULimitedHost, link=TCLink)
    #net.start()
