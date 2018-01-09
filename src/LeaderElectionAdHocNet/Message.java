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
    
    private int init, end, lider, flag, max = 0;
    private int deltaElection;
    private int mSrcNum = 0;
    private int mSrcId = 0;

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
        
        init = 0;
        end = tramaCompleta.indexOf('|');
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
        if(end > init) { 
            data = tramaCompleta.substring(init, end);
            if (typeMsg.equals("ELECTION")) {
            init = 0;
            end = data.indexOf(",");
            if (end != -1) {
                mSrcNum = Integer.parseInt(data.substring(init, end));

                init = end + 1;
                end = data.indexOf(",", init);
                if (end != -1) {
                    mSrcId = Integer.parseInt(data.substring(init, end));

                    init = end + 1;
                    end = data.length();
                    if (end > init) {
                        lider = Integer.parseInt(data.substring(init, end));
                    }
                }
            }
        } else if (typeMsg.equals("ACK")) {
            init = 0;
            end = data.indexOf(",");
            if (end != -1) {
                mSrcNum = Integer.parseInt(data.substring(init, end));

                init = end + 1;
                end = data.indexOf(",", init);
                if (end != -1) {
                    mSrcId = Integer.parseInt(data.substring(init, end));

                    init = end + 1;
                    end = data.indexOf(",", init);
                    if (end != -1) {
                        flag = Integer.parseInt(data.substring(init, end));

                        init = end + 1;
                        end = data.length();
                        if (end > init) {
                            max = Integer.parseInt(data.substring(init, end));
                        }
                    }
                }
            }
        } else if (typeMsg.equals("REPLY")) {
            init = 0;
            end = data.indexOf(",");
            if (end != -1) {
                mSrcNum = Integer.parseInt(data.substring(init, end));

                init = end + 1;
                end = data.indexOf(",", init);
                if (end != -1) {
                    mSrcId = Integer.parseInt(data.substring(init, end));

                    init = end + 1;
                    end = data.indexOf(",", init);
                    if (end != -1) {
                        deltaElection = Integer.parseInt(data.substring(init, end));

                        init = end + 1;
                        end = data.length();
                        if (end > init) {
                            lider = Integer.parseInt(data.substring(init, end));
                        }
                    }
                }
            }
        }
        }
        //System.out.println(idMsg + "!" + senderId + "!" + destId + "!" + typeMsg + "!" + data);
        
    }

    public int getDestId() {
        return Integer.parseInt(destId);
    }
    
    public String getTrama() {
        return trama;
    }

    public int getSenderId() {
        if(senderId.equals("null"))
            return -1;
        else
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

    public int getLider() {
        return lider;
    }

    public int getFlag() {
        return flag;
    }

    public int getMax() {
        return max;
    }

    public int getmSrcNum() {
        return mSrcNum;
    }

    public int getmSrcId() {
        return mSrcId;
    }

    public int getDeltaElection() {
        return deltaElection;
    }
    
    
}