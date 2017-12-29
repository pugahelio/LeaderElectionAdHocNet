/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author pedrorodrigues
 */
public class ThreadHeartbeatS extends Thread {

    private MainNode myNode;
    private Timer myTimer;
    private int secondsPassed;
    private volatile boolean flagWait;
    private String state;
    private int idHb;

    ThreadHeartbeatS(MainNode n) {

        myNode = n;
        myTimer = new Timer();
        secondsPassed = 0;
        flagWait = false;
        state = "SendHeartbeat";
        idHb = 0;

    }

    public void run() {

        TimerTask task = new TimerTask() {
            public void run() {
                secondsPassed++;
                if ((secondsPassed == 1) && (flagWait == true)) {
                    flagWait = false;
                }
            }
        };
        myTimer.schedule(task, 0, 1000);

        while (true) {
            if (myNode.getId() == myNode.getLid()) {
                if ((flagWait == false) && (state.equals("SendHeartbeat"))) {

                    idHb++;

                    //  envia heartbeat a todos os vizinhos
                    for (Integer id : myNode.getN().keySet()) {
                        myNode.getN().get(id).sendHeartbeat(myNode.getId(), idHb);

                    }
                    secondsPassed = 0;
                    flagWait = true;
                    state = "Wait";

                } else if ((flagWait == false) && (state.equals("Wait"))) {
                    state = "SendHeartbeat";
                }
            }
        }
    }
}
