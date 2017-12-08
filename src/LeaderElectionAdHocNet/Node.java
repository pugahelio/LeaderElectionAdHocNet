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

/**
 *
 * @author helio
 */
public class Node {
    private InetAddress nodeAddress;
    private int nodePort;
    private int ID;
    private DatagramSocket socket, socketIn;
    private byte[] bufOut = new byte[2048];
    private byte[] bufIn = new byte[2048];
    private DatagramPacket packetOut;
    private DatagramPacket packetIn;

    public Node(int id, int port, InetAddress nodeAddr) {
        ID = id;
        nodePort = port;
        nodeAddress = nodeAddr;
        try {
            socket = new DatagramSocket(nodePort);
            //socketIn = new DatagramSocket(portMainNode);
        } catch (SocketException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Message getMessage() {
        packetIn = new DatagramPacket(bufIn, bufIn.length);
        Message msg;
        String trama;
        do {
            try {
                socket.receive(packetIn);
            } catch (IOException ex) {
                Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
            }
            trama = new String(packetIn.getData(), 0, packetIn.getLength());
            msg = new Message(trama);
        } while (msg.getIdNode() != ID);

        return msg;
    }

    public void sendElection(int src) {
        Message msg = new Message(ID, 1, "ELECTION", Integer.toString(src));
        
        bufOut = msg.getTrama().getBytes();
        
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
        try {
            socket.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendAck() {
        Message msg = new Message(ID, 1, "ACK", null);

        bufOut = msg.getTrama().getBytes();
        
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
        try {
            socket.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendLeader(int leaderID) {
        Message msg = new Message(ID, 1, "LEADER", Integer.toString(leaderID));

        bufOut = msg.getTrama().getBytes();
        
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
        try {
            socket.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendProbe() {
        Message msg = new Message(ID, 1, "PROBE", null);

        bufOut = msg.getTrama().getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
        try {
            socket.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendReply() {
        Message msg = new Message(ID, 1, "REPLY", null);
        
        bufOut = msg.getTrama().getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, nodePort);
        try {
            socket.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
