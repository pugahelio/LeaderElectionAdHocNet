/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PedroRodrigues
 */
public class ThreadProbes extends Thread {

    private MainNode myNode;
    private Timer myTimer;
    private int secondsPassed;
    private volatile boolean flagWait;
    private String state;

    ThreadProbes(MainNode n) {

        myNode = n;
        myTimer = new Timer();
        secondsPassed = 0;
        flagWait = false;
        state = "SendProbes";
    }

    public void run() {

        TimerTask task = new TimerTask() {
            public void run() {
                secondsPassed++;
                if ((secondsPassed >= 2) && (flagWait == true)) {
                    flagWait = false;
                }
            }
        };
        myTimer.schedule(task, 0, 1000);
        
        while (true) {

            if ((flagWait == false) && (state.equals("SendProbes"))) {

                //  envia probes a todos os vizinhos
                for (Integer id : myNode.getN().keySet()) {
                    myNode.getN().get(id).setTestingProbes(true);
                    myNode.getN().get(id).sendProbe();
                    //System.out.println("Probe enviado para " + myNode.getN().get(id).getId());
                }
                
                if(myNode.isDeltaElection() && myNode.getP()>0) {
                    myNode.getN().get(myNode.getP()).setTestingProbes(true);
                    myNode.getN().get(myNode.getP()).sendProbe();
                    //System.out.println("Send prob Pai");
                }
                
                secondsPassed = 0;
                flagWait = true;
                state = "WaitReplies";

            } else if ((flagWait == false) && (state.equals("WaitReplies"))) {
                for (Integer id : myNode.getN().keySet()) {
                    if ((myNode.getN().get(id).isTestingProbes())) {
                        myNode.getN().get(id).setAlive(false);
                        System.err.println("Nó não operacional: " + myNode.getN().get(id).getId());
                        if (myNode.getS().contains(id)) {
                            myNode.getS().remove(id);
                        }
                    }
                }
                state = "SendProbes";
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadProbes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
