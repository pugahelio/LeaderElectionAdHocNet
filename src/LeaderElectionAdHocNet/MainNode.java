/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author helio
 */
public class MainNode {
    private int ID;
    private boolean deltaiElection;
    private Node pi;
    private boolean deltaiACK;
    private Node lidi;
    private Set<Node> ni;
    private Set<Node> si;
    private int srciNum;
    private int nodeValue;
    
    public MainNode(int id) {
        deltaiElection = false;
        pi = null;
        deltaiACK = false;
        lidi = null;
        ni = new HashSet<>();
        si = new HashSet<>();
        ID = id;
        srciNum = 0;
    }
    
    public void setNodeValue(int nodeValue) {
        this.nodeValue = nodeValue;
    }
    
    //1- se esta numa eleiçao, 0 senão
    public void setDeltaiElection(boolean deltaiElection) {
        this.deltaiElection = deltaiElection;
    }
    
    //parent node
    public void setPi(Node pi) {
        this.pi = pi;
    }

    //se já deu ACK ao parente ou não
    public void setDeltaiACK(boolean deltaiACK) {
        this.deltaiACK = deltaiACK;
    }

    //lider
    public void setLidi(Node lidi) {
        this.lidi = lidi;
    }

    //vizinhos currentes
    public void setNi(Set<Node> ni) {
        this.ni = ni;
    }
    
    //set de nós que falta ouvir ACK
    public void setSi(Set<Node> si) {
        this.si = si;
    }
       
    public void setNodeID(int nodeID) {   
        this.ID = nodeID;
    }

    public void setSrciNum(int srciNum) {
        this.srciNum = srciNum;
    }

    //computation index
    public void setSrci(int srci) {
        this.srciNum = srci;
    }

    public int getNodeID() {
        return ID;
    }

    public boolean isDeltaiElection() {
        return deltaiElection;
    }

    public Node getPi() {
        return pi;
    }

    public boolean isDeltaiACK() {
        return deltaiACK;
    }

    public Node getLidi() {
        return lidi;
    }

    public Set<Node> getNi() {
        return ni;
    }

    public Set<Node> getSi() {
        return si;
    }

    public int getSrciNum() {
        return srciNum;
    }

    public int getNodeValue() {
        return nodeValue;
    }
}