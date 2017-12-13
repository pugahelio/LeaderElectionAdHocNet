/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helio
 */
public class MainNode {

    //Leader election varibles
    private int id;
    private boolean deltaElection;
    private int pId;
    private boolean deltaACK;
    private int lidId;
    private Map<Integer, Node> n;
    private Set<Integer> s;
    private int srcNum;
    private int srcId;

    //Comunicações
    private int nodePort;
    private DatagramSocket socket;
    private DatagramPacket packetIn;
    private byte[] bufIn = new byte[2048];

    public MainNode() {
        deltaElection = false;
        pId = -1;
        deltaACK = false;
        lidId = -1;
        n = new HashMap<>();
        s = new HashSet<>();
        this.id = -1;
        srcNum = 0;
        srcId = this.id;
        this.nodePort = -1;
    }

    public void resetElection(){
        deltaElection = false;
        pId = -1;
        deltaACK = false;
        lidId = -1;
        s.removeAll(s);
    }
    
    public Message getMessage() {
        packetIn = new DatagramPacket(bufIn, bufIn.length);
        Message msg;
        String trama;
        do {
            try {
                socket.receive(packetIn);
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            trama = new String(packetIn.getData(), 0, packetIn.getLength());
            msg = new Message(trama);
        } while (msg.getDestId() != id);
        System.err.println("Recebido: " + msg.getTrama());
        return msg;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
        try {
            socket = new DatagramSocket(this.nodePort);
        } catch (SocketException ex) {
            Logger.getLogger(MainNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
    *1- se esta numa eleiçao, 0 senão
    */
    public void setDeltaiElection(boolean deltaiElection) {
        this.deltaElection = deltaiElection;
    }

    /**
     * parent node
     */
    public void setP(int pId) {
        this.pId = pId;
    }

    /**
     * se já deu ACK ao parente ou não
     */
    public void setDeltaACK(boolean deltaACK) {
        this.deltaACK = deltaACK;
    }

    /**
     * lider
     */
    public void setLid(int lidId) {
        this.lidId = lidId;
    }

    /**
     * vizinhos correntes
     */
    public void addN(Node n) {
        this.n.put(n.getId(), n);
    }

    /**
     * set de nós que falta ouvir ACK
     */
    public void addS(int s) {
        this.s.add(s);
    }

    public void setSrc(int srcNum, int srcId) {
        this.srcNum = srcNum;
        this.srcId = srcId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isDeltaElection() {
        return deltaElection;
    }

    public int getP() {
        return pId;
    }

    public boolean isDeltaACK() {
        return deltaACK;
    }

    public int getLid() {
        return lidId;
    }

    public Map<Integer, Node> getN() {
        return n;
    }

    public Set<Integer> getS() {
        return s;
    }

    public int getSrcNum() {
        return srcNum;
    }
    
    public int getSrcId() {
        return srcId;
    }
}
