 #!/usr/bin/python
from mininet.cli import CLI
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import CPULimitedHost
from mininet.link import TCLink
from mininet.util import dumpNodeConnections, quietRun
from mininet.log import setLogLevel, info
from mininet.node import Controller, RemoteController, OVSKernelSwitch
from time import sleep
 
import cPickle as pickle
import os, socket, thread, ipaddr
import subprocess



def simpleTest():
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

    net = Mininet(controller=RemoteController, switch=OVSKernelSwitch, autoSetMacs=True, autoStaticArp=True, listenPort=60001)
    net.addController( name='c0', controller=RemoteController, ip='127.0.0.1' )
    net.start()
    #net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    simpleTest()
