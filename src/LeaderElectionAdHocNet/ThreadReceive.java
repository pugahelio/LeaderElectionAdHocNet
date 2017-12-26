/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;
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

    //Comunicações
    private DatagramPacket packetIn;
    private byte[] bufIn = new byte[2048];

    ThreadReceive(MainNode n) {

        myNode = n;
        queue = new LinkedBlockingQueue<>();
        secondsPassed = 0;
        lastNumMsgHeartbeat = 0;

    }

    public void run() {

        TimerTask task = new TimerTask() {
            public void run() {
                secondsPassed++;
                if (secondsPassed > 10) {
                    myNode.setLid(-1);
                    myNode.setDeltaiElection(false);

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

            if (msg.getTypeMsg().equals("PROBE")) {
                myNode.getN().get(msg.getSenderId()).sendReply();
            } else if (msg.getTypeMsg().equals("REPLY")) {
                myNode.getN().get(msg.getSenderId()).setTestingProbes(false);
                myNode.getN().get(msg.getSenderId()).setAlive(true);
            } else if (msg.getTypeMsg().equals("HEARTBEAT")) {
                if (Integer.parseInt(msg.getData()) == myNode.getLid()) {
                    secondsPassed = 0;
                    if ((lastNumMsgHeartbeat != msg.getIdMsg() && (myNode.getId() != Integer.parseInt(msg.getData())))) {
                        for (Integer id : myNode.getN().keySet()) {
                            myNode.getN().get(id).sendHeartbeat(Integer.parseInt(msg.getData()));

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
