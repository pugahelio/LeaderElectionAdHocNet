/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

/**
 *
 * @author rodrigoborges
 */
public class ThreadPerformance extends Thread {

    private MainNode myNode;
    private Node node;
    private boolean aux;
    private int numberOfElections;
    private int numberOfMessages;
    private int numberOfMessagesAnt;

    public ThreadPerformance(MainNode n) {

        myNode = n;
        numberOfElections = 0;
        numberOfMessages = 0;
        numberOfMessagesAnt = 0;

    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
            
        while(true){
            
            
            if (myNode.isDeltaElection()) {
                
                numberOfElections++;
                
                long startTime = System.nanoTime();
                
                while(myNode.isDeltaElection()){
                    
                }
                
                long endTime = System.nanoTime();
                
                long output = endTime - startTime;
                
                System.out.println("Elapsed time in milliseconds: " + output / 1000000 + " Number of elections: " + numberOfElections);

                for (Integer id : myNode.getN().keySet()) {
                    
                    numberOfMessages = numberOfMessages + myNode.getN().get(id).getMsgCounter();
                }
                
                System.out.println("Number of messages: " + (numberOfMessages - numberOfMessagesAnt));
                
                numberOfMessagesAnt = numberOfMessages;
                numberOfMessages = 0;

            }
        }
            

    }

}
