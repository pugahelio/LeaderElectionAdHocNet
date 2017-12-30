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

    private volatile int secondsPassed;
    private Timer myTimer;

    //Comunicações
    private DatagramPacket packetIn;
    private byte[] bufIn = new byte[2048];

    private int idHbAux;

    ThreadReceive(MainNode n) {
        myNode = n;
        myTimer = new Timer();
        queue = new LinkedBlockingQueue<>();
        secondsPassed = 0;
    }

    public void run() {

        TimerTask task = new TimerTask() {
            public void run() {
                if (myNode.getLid() != (-1) && (!myNode.isDeltaElection()) && (myNode.getId() != myNode.getLid())) {
                    secondsPassed++;
                    System.out.println("Contador " + secondsPassed);
                }
                if ((secondsPassed > 2) && (myNode.getLid() != (myNode.getId())) && (myNode.getLid() != (-1)) && (!myNode.isDeltaElection())) {
                    System.err.println("\nLider inativo \n");
                    myNode.setLid(-1);
                    myNode.setDeltaiElection(false);
                    secondsPassed = 0;
                }
            }
        };

        myTimer.schedule(task, 0, 1000);

        packetIn = new DatagramPacket(bufIn, bufIn.length);
        Message msg;
        String trama;
        int lider;
        int idHb;
        int init;
        int end;
        while (true) {

            do {
                try {
                    myNode.socket.receive(packetIn);
                } catch (IOException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                trama = new String(packetIn.getData(), 0, packetIn.getLength());
                msg = new Message(trama);
            } while (myNode.isFirstExec() 
                    || (msg.getDestId() != myNode.getId()) 
                    || (myNode.getN().get(msg.getSenderId()).isBlackListed()) 
                    || (myNode.isDeltaElection() && !myNode.getN().get(msg.getSenderId()).isAlive()));

            //System.out.println("Recebido: " + msg.getTrama());
            
            switch (msg.getTypeMsg()) {
                case "PROBE":
                    myNode.getN().get(msg.getSenderId()).sendReply();
                    break;
                case "REPLY":
                    myNode.getN().get(msg.getSenderId()).setTestingProbes(false);
                    myNode.getN().get(msg.getSenderId()).setAlive(true);
                    break;
                case "HEARTBEAT":
                    init = 0;
                    end = msg.getData().indexOf(",");
                    lider = Integer.parseInt(msg.getData().substring(init, end));
                    init = end + 1;
                    end = msg.getData().length();
                    idHb = Integer.parseInt(msg.getData().substring(init, end));
                    //só faz broadcast se...
                    if (lider == myNode.getLid()) {
                        this.resetSecondsPassed();
                        if (idHbAux < idHb) {
                            idHbAux = idHb;
                            for (Integer id : myNode.getN().keySet()) {
                                myNode.getN().get(id).sendHeartbeat(lider, idHb);

                            }
                        }
                    } else if (lider > myNode.getLid()) {
                        myNode.threadR.resetSecondsPassed();
                        idHbAux = 0;
                        myNode.setLid(lider);
                        for (Integer id : myNode.getN().keySet()) {
                            myNode.getN().get(id).sendHeartbeat(lider, idHb);
                        }
                    }
                    break;
                default:
                    try {
                        queue.put(msg);
                        System.out.println("Recebido: " + msg.getTrama());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ThreadReceive.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        }
    }

    public void resetSecondsPassed() {
        secondsPassed = 0;
    }
}
