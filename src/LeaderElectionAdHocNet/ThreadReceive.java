/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PedroRodrigues
 */
public class ThreadReceive extends Thread {

    private MainNode myNode;
    public BlockingQueue<Message> queue;

    private int secondsPassed;
    private Timer myTimer;
    private int lastNumMsgHeartbeat;
    private int id_hb_aux;

    //Comunicações
    private DatagramPacket packetIn;
    private byte[] bufIn = new byte[2048];

    ThreadReceive(MainNode n) {

        myNode = n;
        myTimer = new Timer();
        queue = new LinkedBlockingQueue<>();
        secondsPassed = 0;
        lastNumMsgHeartbeat = 0;
        id_hb_aux = 0;

    }

    public void run() {

        TimerTask task = new TimerTask() {
            public void run() {
                
                if(myNode.getLid() != (-1) && (!myNode.isDeltaElection()) && (myNode.getId() != myNode.getLid())){
                       secondsPassed++;
                       System.out.println("Contador " + secondsPassed);
                       
                }
             
                if ((secondsPassed > 9) && (myNode.getLid() != (myNode.getId())) && (myNode.getLid() != (-1)) && (!myNode.isDeltaElection())) {
                    System.err.println("\nLider inativo \n");
                    myNode.setLid(-1);
                    myNode.setDeltaiElection(false);
                    secondsPassed = 0;

                }
                // System.out.println(" contador " + secondsPassed);
            }
        };
        myTimer.schedule(task, 0, 1000);

        packetIn = new DatagramPacket(bufIn, bufIn.length);
        Message msg;
        String trama;

        while (true) {

            do {
                try {
                    myNode.socket.receive(packetIn);
                } catch (IOException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                trama = new String(packetIn.getData(), 0, packetIn.getLength());
                msg = new Message(trama);
            } while (msg.getDestId() != myNode.getId());

            //System.out.println("Recebido: " + msg.getTrama());
            if (msg.getTypeMsg().equals("PROBE")) {
                myNode.getN().get(msg.getSenderId()).sendReply();
            } else if (msg.getTypeMsg().equals("REPLY")) {
                myNode.getN().get(msg.getSenderId()).setTestingProbes(false);
                myNode.getN().get(msg.getSenderId()).setAlive(true);
            } else if (msg.getTypeMsg().equals("HEARTBEAT")) {
                    int init = 0;
                    int end = msg.getData().indexOf(",");
                    int lider_hb = Integer.parseInt(msg.getData().substring(init, end));

                    init = end + 1;
                    end = msg.getData().length();
                    int id_hb = Integer.parseInt(msg.getData().substring(init, end));

                
                if (lider_hb == myNode.getLid()) {
                    secondsPassed = 0;
                    if (id_hb_aux != id_hb) {
                        id_hb_aux = id_hb;
                        for (Integer id : myNode.getN().keySet()) {
                            myNode.getN().get(id).sendHeartbeat(lider_hb,id_hb);

                        }
                    }
                }
            } else {
                try {
                    queue.put(msg);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadReceive.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }
}
