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
    private String senderId;
    private String idMsg;
    private String typeMsg;
    private String data;
    private String trama;
    private String destId;

    public Message(int msgID, int senderId, int destId, String msgType, String text) {
        this.senderId = Integer.toString(senderId);
        this.destId = Integer.toString(destId);
        idMsg = Integer.toString(msgID);
        typeMsg = msgType;
        data = text;
        
        trama = this.idMsg + '|' + this.senderId + '|' + this.destId + "|" + this.typeMsg + '|' + this.data;
    }

    public Message(String tramaCompleta) {
        trama = tramaCompleta;
//        String[] dividido;
//        
//        dividido = trama.split("|");
//        if (dividido.length > 0) {
//            idMsg = dividido[0];
//        }
//        if (dividido.length > 1) {
//            senderId = dividido[1];
//        }
//        if (dividido.length > 2) {
//            destId = dividido[2];
//        }
//        if (dividido.length > 3) {
//            typeMsg = dividido[3];
//        }
//        if (dividido.length > 4) {
//            data = dividido[4];
//        }

        
        
        int init = 0;
        int end = tramaCompleta.indexOf('|');
        idMsg = tramaCompleta.substring(init, end);

        init = end + 1;
        end = tramaCompleta.indexOf('|', init);
        senderId = tramaCompleta.substring(init, end);

        init = end + 1;
        end = tramaCompleta.indexOf('|', init);
        destId = tramaCompleta.substring(init, end);
        
        init = end + 1;
        end = tramaCompleta.indexOf('|', init);
        typeMsg = tramaCompleta.substring(init, end);

        init = end + 1;
        end = tramaCompleta.length();
        if(end > init) 
            data = tramaCompleta.substring(init, end);
        
        //System.out.println(idMsg + "!" + senderId + "!" + destId + "!" + typeMsg + "!" + data);
        
    }

    public int getDestId() {
        return Integer.parseInt(destId);
    }
    
    public String getTrama() {
        return trama;
    }

    public int getSenderId() {
        return Integer.parseInt(senderId);
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