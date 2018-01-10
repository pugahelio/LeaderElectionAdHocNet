/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rodrigoborges
 */
public class ThreadPerformance extends Thread {

    private MainNode myNode;
    public int numberOfElections;
    public int numberOfMessages;
    public int numberOfMessagesAnt;
    public long startTime;
    public long totalTimeInElection;

    public ThreadPerformance(MainNode n) {

        myNode = n;
        startTime = 0;
        numberOfElections = 0;
        numberOfMessages = 0;
        numberOfMessagesAnt = 0;
        totalTimeInElection = 0;

    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {

        startTime = System.nanoTime();

        while (true) {


                if (myNode.isDeltaElection()) {

                    numberOfElections++;

                    long startElectionTime = System.nanoTime();

                    while (myNode.isDeltaElection()) {
                        try {
                            //wait for election to be over

                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ThreadPerformance.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    long endElectionTime = System.nanoTime();

                    long electionTime = endElectionTime - startElectionTime;

                    //System.out.println("Election-Time (T): " + (electionTime / 1000000) + " milliseconds");
                    totalTimeInElection = totalTimeInElection + (electionTime / 1000000);

                    for (Integer id : myNode.getN().keySet()) {

                        numberOfMessages = numberOfMessages + myNode.getN().get(id).getMsgCounter();
                    }

                    DecimalFormat df = new DecimalFormat("0.000");

                    System.out.println("Election-Time (T): " + (electionTime / 1000000) + " milliseconds"
                            + "\n" + "Message Overhead (M): "
                            + df.format(((double) numberOfMessages / numberOfElections))
                            + "\n" + "Number of Elections: " + numberOfElections
                            + "\n" + "Number of Messages: " + (numberOfMessages - numberOfMessagesAnt));

                    numberOfMessagesAnt = numberOfMessages;
                    numberOfMessages = 0;

                }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadPerformance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public int getNumberOfElections() {
        return numberOfElections;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTotalTimeInElection() {
        return totalTimeInElection;
    }

    public int getNumberOfMessagesAnt() {
        return numberOfMessagesAnt;
    }

}
