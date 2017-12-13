/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Communication {

    private DatagramSocket socketOut, socketIn;
    private byte[] bufOut = new byte[2048];
    private byte[] bufIn = new byte[2048];
    private DatagramPacket packetOut;
    private DatagramPacket packetIn;
    
    public Communication(int portNode, int portMainNode){
        try {
            socketOut = new DatagramSocket(portNode);
            socketIn = new DatagramSocket(portMainNode);
        } catch (SocketException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] getMessage(){
        packetIn = new DatagramPacket(bufIn, bufIn.length);
        try {
            socketIn.receive(packetIn);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packetIn.getData();
    }

    public void sendElection(InetAddress nodeAddress, int port) {
        bufOut = "ELECTION".getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, port);
        try {
            socketOut.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendAck(InetAddress nodeAddress, int port) {
        bufOut = "ACK".getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, port);
        try {
            socketOut.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendLeader(InetAddress nodeAddress, int port, int leaderID) {
        bufOut = Integer.toString(leaderID).getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, port);
        try {
            socketOut.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendProbe(InetAddress nodeAddress, int port) {
        bufOut = "PROBE".getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, port);
        try {
            socketOut.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendReply(InetAddress nodeAddress, int port) {
        bufOut = "REPLY".getBytes();
        packetOut = new DatagramPacket(bufOut, bufOut.length, nodeAddress, port);
        try {
            socketOut.send(packetOut);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
