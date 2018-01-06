/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedrorodrigues
 */
public class ThreadReconfig extends Thread {

    private static BufferedReader bufferReader;
    private static NonblockingBufferedReader inputKeyBoard;
    private MainNode n;

    public ThreadReconfig(MainNode myNode) {

        n = myNode;
        bufferReader = new BufferedReader(new InputStreamReader(System.in));
        inputKeyBoard = new NonblockingBufferedReader(bufferReader);

    }

    public void run() {

        while (true) {

            String line = "";
            String splitBy = " ";

            try {
                while ((line = inputKeyBoard.readLine()) != null) {
                    String[] info = line.split(splitBy);

                    if (info[0].equals("add")) {
                        if (n.getN().containsKey(Integer.parseInt(info[1])) == true) {
                            n.getN().get(Integer.parseInt(info[1])).setBlackListed(false);
                            System.out.println("Adicionado nó: " + info[1]);
                        }
                    } else if (info[0].equals("rm")) {
                        if (n.getN().containsKey(Integer.parseInt(info[1])) == true) {
                            n.getN().get(Integer.parseInt(info[1])).setBlackListed(true);
                            System.out.println("Removido nó: " + info[1]);
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(LeaderElectionAdHocNet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
