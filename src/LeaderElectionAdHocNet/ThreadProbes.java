/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author PedroRodrigues
 */
public class ThreadProbes extends Thread {

    private MainNode myNode;
    public Queue<Message> queue;
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
                if ((secondsPassed == 10) && (flagWait == true)) {
                    flagWait = false;

                }
                // System.out.println(" contador " + secondsPassed);
            }
        };
        myTimer.schedule(task, 0, 1000);

        while (true) {

            if ((flagWait == false) && (state.equals("SendProbes"))) {

                //  envia probes a todos os vizinhos
                for (Integer id : myNode.getN().keySet()) {
                    myNode.getN().get(id).setTestingProbes(true);
                    myNode.getN().get(id).sendProbe();

                }
                secondsPassed = 0;
                flagWait = true;
                state = "WaitReplies";

            } else if ((flagWait == false) && (state.equals("WaitReplies"))) {

                for (Integer id : myNode.getN().keySet()) {
                    
                    if((myNode.getN().get(id).isTestingProbes())){
                        myNode.getN().get(id).setAlive(false);
                        System.err.println("\n Nodo não operacional "+myNode.getN().get(id).getId() );
                    }
                            
                    if ((myNode.getN().get(id).isTestingProbes()) && (myNode.getS().contains(id))){
                       // System.err.println("\nNodo " + myNode.getN().get(id).getId() + " removido do HB " + myNode.getN().get(id).isTestingProbes() + "\n");
                        myNode.getS().remove(id);
                        
                    }
                    
//                    if((myNode.getN().get(id).isTestingProbes()) && (myNode.getN().get(id).getId() == myNode.getLid())){
//                        System.err.println("\n Lider dead ");
//                        myNode.resetElection();
//                    }
//                                        
                }


                flagWait = true;
                secondsPassed = 0;
                state = "SendProbes";
            }
        }
//System.out.println(" flagwait - "+ flagWait + " state - " + state);

    }
}
