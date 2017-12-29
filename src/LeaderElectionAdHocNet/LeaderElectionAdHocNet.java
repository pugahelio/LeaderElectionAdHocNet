package LeaderElectionAdHocNet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderElectionAdHocNet {

    static String state = "STANDBY";
    static Message msg = new Message("null|null|null|null|null");
    static int mostValuedNode;
    static int srcNumElect = 0;
    static int srcIdElect = 0;
    static int srcNumAux = 0;
    static int srcIdAux = 0;
    static BufferedReader bufferReader;
    static NonblockingBufferedReader inputKeyBoard;

    public static void main(String[] args) throws InterruptedException {
        MainNode myNode = new MainNode();
        configuration(myNode, args[0]);
        System.out.println("This Node ID: " + myNode.getId());

        bufferReader = new BufferedReader(new InputStreamReader(System.in));
        
        inputKeyBoard = new NonblockingBufferedReader(bufferReader);
        
        mostValuedNode = myNode.getId();

        // Iniciar a thread de receber msg
        myNode.threadR.start();

        TimeUnit.SECONDS.sleep(1);

        myNode.threadProbes.start();

        myNode.threadHeartbeat.start();

        
        while (true) {

            reconfigureNode(myNode);

            stateMachineElection(myNode);

        }
    }

    private static void reconfigureNode(MainNode n) {
        String line = "";
        String splitBy = " ";

        try {
            while ((line = inputKeyBoard.readLine()) != null) {
                String[] info = line.split(splitBy);
             
                if (info[0].equals("add")) {
                    n.getN().get(Integer.parseInt(info[1])).setBlackListed(false);
                     System.out.println("Adicionado nó: " + info[1]);
                }
                else if (info[0].equals("rm")) {
                    n.getN().get(Integer.parseInt(info[1])).setBlackListed(true);
                    System.out.println("Removido nó: " + info[1]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LeaderElectionAdHocNet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void stateMachineElection(MainNode n) throws InterruptedException {

        TimeUnit.MILLISECONDS.sleep(300);
        
        switch (state) {
            case "STANDBY":
                //n.resetElection(); // para estar permanentemente a fazer elections
                mostValuedNode = n.getId();
                srcNumElect = 0; // reset do src num da eleiçao
                srcIdElect = 0; // reset do src id da eleiçao
                n.setDeltaiElection(false);

                if (n.getLid() == -1 && !n.isDeltaElection()) {
                    state = "START_ELECTION";
                } else {
                    msg = n.getMessage(state);
                    if (msg.getTypeMsg().equals("ELECTION")) {
                        state = "CHECK_ELECTION";
                    } else if ((msg.getTypeMsg().equals("LEADER")) && (!(Integer.parseInt(msg.getData()) == n.getLid()))) {
                        n.setLid(Integer.parseInt(msg.getData()));
                        state = "BROADCAST_LEADER";
                    }
                }
                break;

            case "START_ELECTION":
                n.resetElection();
                //esta em eleiçao
                n.setDeltaiElection(true);
                //imcrementa o src
                n.setSrc(n.getSrcNum() + 1, n.getId());
                srcNumElect = n.getSrcNum();
                srcIdElect = n.getId();
                //envia election a todos os vizinhos
                for (Integer id : n.getN().keySet()) {
                    n.getN().get(id).sendElection(srcNumElect, srcIdElect);
                    if (n.getN().get(id).isAlive()) {
                        System.err.println("Adicionei " + id);
                        n.addS(id);
                    }
                }

                state = "WAIT_ACK";
                break;
            //espera por receber os ack todos para responder

            case "CHECK_ELECTION":

                int init = 0;
                int end = msg.getData().indexOf(",");
                srcNumAux = Integer.parseInt(msg.getData().substring(init, end));

                init = end + 1;
                end = msg.getData().length();
                srcIdAux = Integer.parseInt(msg.getData().substring(init, end));

                //Se tiver src igual estou a falar do mesmo logo responde instataneamente.
                if (srcNumAux == srcNumElect && srcIdAux == srcIdElect) {
                    n.getN().get(msg.getSenderId()).sendAck(mostValuedNode);
                    state = "WAIT_ACK";

                    // Uma eleição com src superior
                } else if (compareSrc(srcNumAux, srcIdAux, srcNumElect, srcIdElect)) {
                    state = "RETRANSMIT_ELECTION";
                } else if (!(compareSrc(srcNumAux, srcIdAux, srcNumElect, srcIdElect))) {
                    state = "WAIT_ACK";

                }

                break;

            case "WAIT_ACK":

                if ((n.getS().isEmpty() && (n.getId() != srcIdElect))) {
                    state = "ACK_TO_PARENT";
                    break;
                } else if ((n.getS().isEmpty()) && (n.getId() == srcIdElect)) {
                    n.setLid(mostValuedNode);
                    state = "BROADCAST_LEADER";
                    break;
                }
                msg = n.getMessage(state);

                if (msg.getTypeMsg().equals("ACK")) {

                    //se receber um ack retira o nó da lista
                    n.getS().remove(msg.getSenderId());
                    int val = Integer.parseInt(msg.getData());
                    //actualiza o no mais valioso
                    if (val > mostValuedNode) {
                        mostValuedNode = val;
                    }
                } else if (msg.getTypeMsg().equals("ELECTION")) {

                    state = "CHECK_ELECTION";
                } else if ((msg.getTypeMsg().equals("LEADER")) && (!(Integer.parseInt(msg.getData()) == n.getLid()))) {
                    n.setLid(Integer.parseInt(msg.getData()));
                    state = "BROADCAST_LEADER";
                }

                break;

            case "RETRANSMIT_ELECTION":

                n.resetElection();
                //esta no eleiçao
                n.setDeltaiElection(true);
                //actualiza o pai
                n.setP(msg.getSenderId());
                //actualiza src
                srcNumElect = srcNumAux;
                srcIdElect = srcIdAux;

                //envia election a todos os vizinos menos o pai e adiciona a lista de espera de ack
                for (Integer id : n.getN().keySet()) {
                    if (id != n.getP()) {
                        n.getN().get(id).sendElection(srcNumElect, srcIdElect);
                        if (n.getN().get(id).isAlive()) {
                            System.err.println("Adicionei " + id);
                            n.addS(id);
                        }
                    }
                }

                state = "WAIT_ACK";
                break;

            case "ACK_TO_PARENT":
         
                if (n.getN().containsKey(n.getP())) {
                    //envia nó mais valioso
                    n.getN().get(n.getP()).sendAck(mostValuedNode);
                    //coloca a variavel que diz se ele já enviou a true
                    n.setDeltaACK(true);
                    state = "WAIT_LEADER";
                }
                else{
                    n.setLid(mostValuedNode);
                    state = "BROADCAST_LEADER";
                }
                
                break;

            case "WAIT_LEADER":

                msg = n.getMessage(state);
                if (msg.getTypeMsg().equals("LEADER")) {

                    n.setLid(Integer.parseInt(msg.getData()));
                    state = "BROADCAST_LEADER";
                } else if (msg.getTypeMsg().equals("ELECTION")) {
                    state = "CHECK_ELECTION";
                }

                break;

            case "BROADCAST_LEADER":

                for (Integer id : n.getN().keySet()) {
                    if (id != n.getP()) {
                        n.getN().get(id).sendLeader((n.getLid()));
                    }
                }

                state = "STANDBY";
                System.out.println(" Lider: " + n.getLid() + " Pai " + n.getP() + " src_num " + srcNumElect + " src_id " + srcIdElect);

                break;

        }

        System.out.println("State = " + state + " || Lider: " + n.getLid());

    }

    /**
     *
     * @param srcId1
     * @param srcNum1
     * @param srcId2
     * @param srcNum2
     * @return True se 1 > 2; False senão
     */
    private static boolean compareSrc(int srcId1, int srcNum1, int srcId2, int srcNum2) {
        return (srcNum1 > srcNum2) || ((srcNum1 == srcNum2) && (srcId1 > srcId2));
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
