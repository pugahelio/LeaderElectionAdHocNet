package LeaderElectionAdHocNet;

import java.net.InetAddress;

public class LEforAdHocNet {

    public static void main(String[] args) {
        MainNode me = new MainNode(1);
        Node nodeA, nodeB;

        nodeA = new Node(1, 5555, InetAddress.getLoopbackAddress());

        while (true) {
            nodeA.sendElection(2);
            nodeA.sendAck();
            nodeA.sendProbe();
            nodeA.sendReply();
            System.out.println(nodeA.getMessage().trama);
        }
    }

}
