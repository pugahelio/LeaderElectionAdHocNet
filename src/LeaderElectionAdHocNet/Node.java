/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

    private int mainNodeId;
    private InetAddress nodeAddress;
    private int nodePort;
    private int id;
    private DatagramSocket socketOut;
    private byte[] bufOut = new byte[2048];
    private DatagramPacket packetOut;
    private int msgId;
    private int msgCounter;
    public volatile boolean testingProbes;
    public volatile boolean alive;
    
    private boolean blackListed;

    public Node(int neighborId, int portDest, InetAddress nodeAddr, int mainNodeId) {
        msgId = 0;
        msgCounter = 0;
        testingProbes = false;
        alive = true;
        id = neighborId;
        this.mainNodeId = mainNodeId;
        nodePort = portDest;
        nodeAddress = nodeAddr;
        blackListed = false;
        
        try {
            socketOut = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isBlackListed() {
        return blackListed;
    }

    public void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getId() {
        return id;
    }

    public void setTestingProbes(boolean value) {
        testingProbes = value;
    }

    public boolean isTestingProbes() {
        return testingProbes;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgCounter() {
        return msgCounter;
    }

    public void setMsgCounter(int msgCounter) {
        this.msgCounter = msgCounter;
    }

    public void sendElection(int srcNum, int srcId) {
        if (!blackListed) {
            
            Message msg = new Message(msgId, mainNodeId, id, "ELECTION", Integer.toString(srcNum) + "," + Integer.toString(srcId));
            
            bufOut = msg.getTrama().getBytes();
            
            packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
            
            try {
                socketOut.send(packetOut);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgId++;
            msgCounter++;
            //System.err.println("Enviado " + msg.getTrama());
        }
    }

    public void sendAck(int mostValueNode) {
        if (!blackListed) {
            Message msg = new Message(msgId, mainNodeId, id, "ACK", Integer.toString(mostValueNode));
            
            bufOut = msg.getTrama().getBytes();
            
            packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
            try {
                socketOut.send(packetOut);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgId++;
            msgCounter++;
            //System.err.println("Enviado " + msg.getTrama());
        }
    }

    public void sendLeader(int leaderID) {
        if (!blackListed) {
            Message msg = new Message(msgId, mainNodeId, id, "LEADER", Integer.toString(leaderID));
            
            bufOut = msg.getTrama().getBytes();
            
            packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
            try {
                socketOut.send(packetOut);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgId++;
            msgCounter++;
            //System.err.println("Enviado " + msg.getTrama());
        }
    }

    public void sendProbe() {
        if (!blackListed) {
            Message msg = new Message(msgId, mainNodeId, id, "PROBE", null);
            
            bufOut = msg.getTrama().getBytes();
            packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
            try {
                socketOut.send(packetOut);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgId++;
            //System.err.println("Enviado " + msg.getTrama());
        }
    }
    
    public void sendHeartbeat(int lider, int idHb) {
        if (!blackListed) {
            Message msg = new Message(msgId, mainNodeId, id, "HEARTBEAT", lider + "," + idHb);
            
            bufOut = msg.getTrama().getBytes();
            packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
            try {
                socketOut.send(packetOut);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgId++;
            //System.err.println("Enviado " + msg.getTrama());
        }
    }

    public void sendReply() {
        if (!blackListed) {
            Message msg = new Message(msgId, mainNodeId, id, "REPLY", null);
            
            bufOut = msg.getTrama().getBytes();
            packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
            try {
                socketOut.send(packetOut);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            msgId++;
            //System.err.println("Enviado " + msg.getTrama());
        }
    }
}
