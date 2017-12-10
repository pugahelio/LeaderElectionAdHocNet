package LeaderElectionAdHocNet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class LeaderElectionAdHocNet {

    public static void main(String[] args) {
        MainNode myNode = new MainNode();
        configuration(myNode, args[0]);
        System.out.println("This Node ID: " + myNode.getId());

        //Leader election algorithm
        while (true) {
            if (myNode.getId() == 1) {
                myNode.getN().get(2).sendAck();
                myNode.getN().get(3).sendAck();
            } else {
                System.out.println("Mesage: " + myNode.getMessage().getTrama());
            }
        }
    }

    private static void configuration(MainNode n, String path) {
        String csvFile = path;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        int i = 0;

        System.out.println("Path to conf file: " + path);

        try {
            br = new BufferedReader(new FileReader(csvFile));

            while ((line = br.readLine()) != null) {
                String[] info = line.split(cvsSplitBy);
                if (i == 0) {
                    n.setId(Integer.parseInt(info[0]));
                    n.setNodePort(Integer.parseInt(info[1]));
                    i++;
                } else {
                    //public Node(int neighborId, int portDest, InetAddress nodeAddr, int mainNodeId)
                    n.addN(new Node(Integer.parseInt(info[0]),
                            Integer.parseInt(info[1]),
                            InetAddress.getByName(info[2]), 
                            n.getId()));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
