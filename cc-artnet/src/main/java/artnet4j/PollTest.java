/*
 * This file is part of artnet4j.
 * 
 * Copyright 2009 Karsten Schmidt (PostSpectacular Ltd.)
 * 
 * artnet4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * artnet4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with artnet4j. If not, see <http://www.gnu.org/licenses/>.
 */

package artnet4j;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import artnet4j.ArtNet;
import artnet4j.ArtNetException;
import artnet4j.ArtNetNode;
import artnet4j.events.ArtNetDiscoveryListener;
import artnet4j.packets.ArtDmxPacket;

public class PollTest implements ArtNetDiscoveryListener {

    public static void main(String[] args) {
        new PollTest().test();
    }

    private Map<String,ArtNetNode> netLynx = new HashMap<>();

    private int sequenceID;

    @Override
    public void discoveredNewNode(ArtNetNode node) {
    	System.out.println(node.getIPAddress().getHostAddress());
        if (!netLynx.containsKey(node.getIPAddress().getHostAddress())) {
            netLynx.put(node.getIPAddress().getHostAddress(), node);
            System.out.println("found net lynx");
        }
    }

    @Override
    public void discoveredNodeDisconnected(ArtNetNode node) {
        System.out.println("node disconnected: " + node);
        if (node == netLynx) {
            netLynx = null;
        }
    }

    @Override
    public void discoveryCompleted(List<ArtNetNode> nodes) {
        System.out.println(nodes.size() + " nodes found:");
        for (ArtNetNode n : nodes) {
            System.out.println(n);
        }
    }

    @Override
    public void discoveryFailed(Throwable t) {
        System.out.println("discovery failed");
    }

    private void test() {
        ArtNet artnet = new ArtNet();
        try {
            artnet.start();
            artnet.getNodeDiscovery().addListener(this);
            artnet.startNodeDiscovery();
            while (true) {
            	for(ArtNetNode myNode:netLynx.values()){
            		System.out.println("send:" + myNode.getIPAddress() +  " : " +myNode.getSubNet() + " : " + myNode.getDmxOuts()[0]);
            		ArtDmxPacket dmx = new ArtDmxPacket();
                    dmx.setUniverse(myNode.getSubNet(), myNode.getDmxOuts()[0]);
                    dmx.setSequenceID(sequenceID % 255);
                    byte[] buffer = new byte[510];
                    for (int i = 0; i < buffer.length; i++) {
                        buffer[i] =
                                (byte) (Math.sin(sequenceID * 0.05 + i * 0.8) * 127 + 128);
                    }
                    dmx.setDMX(buffer, buffer.length);
                    artnet.unicastPacket(dmx, myNode.getIPAddress());
//                    dmx.setUniverse(myNode.getSubNet(), myNode.getDmxOuts()[1]);
//                    artnet.unicastPacket(dmx, netLynx.getIPAddress());
//                    dmx.setUniverse(myNode.getSubNet(), netLynx.getDmxOuts()[2]);
//                    artnet.unicastPacket(dmx, netLynx.getIPAddress());
//                    dmx.setUniverse(myNode.getSubNet(), netLynx.getDmxOuts()[3]);
//                    artnet.unicastPacket(dmx, netLynx.getIPAddress());
                    sequenceID++;
            	}
                if (netLynx != null) {
                    
                }
                Thread.sleep(30);
            }
        } catch (SocketException e) {
        	throw new AssertionError(e);
        } catch (ArtNetException e) {
        	throw new AssertionError(e);
        } catch (InterruptedException e) {
        	throw new AssertionError(e);
        }
    }
}
