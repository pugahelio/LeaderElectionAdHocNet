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
    volatile private int id;
    volatile private boolean deltaElection; // 1-> esta em election 0 -> nao esta em election ou seja em principio terá lider a menos que seja a primeira vez aka tem -1 no lider
    private int pId;
    private boolean deltaACK; // 0-> ainda nao enviou ACK para o pai ; 1 -> ja enviou ACK para o pai
    volatile private int lidId;
    private Map<Integer, Node> n; // mapa que contem toda a vizinhança do main node
    private Set<Integer> s; // Set of nodes that the main node is waiting for ACKs
    private int srcNum;
    private int srcId;

    private volatile boolean firstExec;
    
    private int nodePort;
    public DatagramSocket socket;

    public ThreadReceive threadR;
    public ThreadProbes threadProbes;
    public ThreadHeartbeatS threadHeartbeat;
    public ThreadReconfig threadReconfig;

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

    public void resetElection() {
        deltaElection = false;
        pId = -1;
        deltaACK = false;
        lidId = -1;
        s.removeAll(s);
    }

    public boolean isFirstExec() {
        return firstExec;
    }

    public void setFirstExec(boolean firstExec) {
        this.firstExec = firstExec;
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
     * 1- se esta numa eleiçao, 0 senão
     */
    public void setDeltaiElection(boolean deltaiElection) {
        this.deltaElection = deltaiElection;
    }

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

    public Message getMessage(String state) {
        Message msg;
        
        if (this.threadR.queue.peek() != null) {
            msg = this.threadR.queue.poll();
            //System.out.println("Recebido: " + msg.getTrama());
            return msg;
        } else {
            return new Message("null|null|null|null|null");
        }
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
