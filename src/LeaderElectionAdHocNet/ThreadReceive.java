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

    public volatile int secondsPassed;
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
                    //System.out.println("Contador " + secondsPassed);
                }
                if ((secondsPassed > 4) && (myNode.getLid() != (myNode.getId())) && (myNode.getLid() != (-1)) && (!myNode.isDeltaElection())) {
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
        idHbAux = 0;
        while (true) {

            do {
                try {
                    myNode.socket.receive(packetIn);
                } catch (IOException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                trama = new String(packetIn.getData(), 0, packetIn.getLength());
                msg = new Message(trama);
            } while ((myNode.isFirstExec() && msg.getTypeMsg().equals("HEARTBEAT")) //na primeira execuçao ignorar heartbeat para obrigar a fazer uma eleiçao
                    || ((msg.getDestId() != myNode.getId())  // se nao for para mim
                    || (myNode.getN().get(msg.getSenderId()).isBlackListed())  // se estiver na blak list
                    || (myNode.isDeltaElection() && msg.getTypeMsg().equals("HEARTBEAT")) // ignorar heartbeats durante eleiçao
                    || (myNode.isDeltaElection() && !myNode.getN().get(msg.getSenderId()).isAlive()))); /* Se estiver numa eleição e se o no do qual recebi não esta vivo.
            Isto implica que tu não recebas mensagens quando o nó é dado como morto até saires da eleição
            */

            
            switch (msg.getTypeMsg()) {
                case "PROBE":
                    myNode.getN().get(msg.getSenderId()).sendReply();
                    //System.out.println("Probe recebido e reply enviado para " + myNode.getN().get(msg.getSenderId()).getId());
                    break;
                case "REPLY":
                    //System.out.println("Reply recebido de " + myNode.getN().get(msg.getSenderId()).getId());
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
                        
                            
                    //System.out.println("Heartbeat receive from " + lider + " idHbAux " + idHbAux + " idHb " + idHb);
                        if (idHbAux != idHb){ 
                            idHbAux = idHb;
                            for (Integer id : myNode.getN().keySet()) {
                                myNode.getN().get(id).sendHeartbeat(lider, idHb);
                                //System.out.println("HB reenviado para " + id);

                            }
                        }
                    } else if (lider > myNode.getLid()) {
                        myNode.threadR.resetSecondsPassed();
                        idHbAux = 0;
                        myNode.setLid(lider);
                        System.out.println("Mudei de líder para " + myNode.getLid());
                        for (Integer id : myNode.getN().keySet()) {
                            myNode.getN().get(id).sendHeartbeat(lider, idHb);
                        }
                    }
                    break;
                default:
                    try {
                        queue.put(msg);
                       //System.out.println("Recebido: " + msg.getTrama());
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
