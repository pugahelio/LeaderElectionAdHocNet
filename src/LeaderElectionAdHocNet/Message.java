/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

/**
 *
 * @author helio
 */
public class Message {
    String idNode;
    String idMsg;
    String typeMsg;
    String data;
    String trama;

    public Message(int nodeID, int msgID, String msgType, String text) {
        idNode = Integer.toString(nodeID);
        idMsg = Integer.toString(msgID);
        typeMsg = msgType;
        data = text;
        
        trama = idNode + '|' + idMsg + '|' + typeMsg + '|' + data;
    }

    public Message(String tramaCompleta) {
        trama = tramaCompleta;
        
        int init = 0;
        int end = tramaCompleta.indexOf('|');
        idNode = tramaCompleta.substring(init, end);
        
        init = end + 1;
        end = tramaCompleta.indexOf('|', init);
        idMsg = tramaCompleta.substring(init, end);
        
        init = end + 1;
        end = tramaCompleta.indexOf('|', init);
        typeMsg = tramaCompleta.substring(init, end);
        
        init = end + 1;
        end = tramaCompleta.length();
        if(end <= init) 
            data = tramaCompleta.substring(init, end);
        
    }

    public String getTrama() {
        return trama;
    }

    public int getIdNode() {
        return Integer.parseInt(idNode);
    }

    public int getIdMsg() {
        return Integer.parseInt(idMsg);
    }

    public String getTypeMsg() {
        return typeMsg;
    }

    public String getData() {
        return data;
    }
}
