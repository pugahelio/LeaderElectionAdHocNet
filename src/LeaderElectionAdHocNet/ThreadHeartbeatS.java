/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.util.Date;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author PedroRodrigues
 */
public class ThreadHeartbeatS extends Thread {

    private MainNode myNode;
    public Queue<Message> queue;
    private Timer myTimer;
    private int secondsPassed;
    private  volatile boolean flagWait;
    private String state;

    ThreadHeartbeatS(MainNode n) {

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
                if ((secondsPassed == 2) && (flagWait == true)) {
                    flagWait = false;
                    
                }
               // System.out.println(" contador " + secondsPassed);
            }
        };
        myTimer.schedule(task, 0, 1000);

        while (true) {

            if ((flagWait == false) && (state.equals("SendProbes"))) {

                System.err.println("\nVolta\n");
                //  envia probes a todos os vizinhos
                for (Integer id : myNode.getN().keySet()) {
                    myNode.getN().get(id).setHB(true);
                    myNode.getN().get(id).sendProbe();
                    
                }
                System.out.println("\n Comecar a contar \n");
                secondsPassed = 0;
                flagWait = true;
                state = "WaitReplies";
                
            }
            else if ((flagWait == false) && (state.equals("WaitReplies"))) {

                System.out.println("\nFim da volta\n");
                for (Integer id : myNode.getN().keySet()) {
                    if (myNode.getN().get(id).getHB() == true) {
                        System.err.println("\nNodo " + myNode.getN().get(id).getId() + " removido - HB " + myNode.getN().get(id).getHB());
                        myNode.removeN(myNode.getN().get(id));
                       
                    }
                }
                
                flagWait = true;
                secondsPassed = 0;
                state = "SendProbes";
            }
//System.out.println(" flagwait - "+ flagWait + " state - " + state);
          
        }
    }
}
