package LeaderElectionAdHocNet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderElectionAdHocNet {

    public static void main(String[] args) throws InterruptedException {
        MainNode myNode = new MainNode();
        configuration(myNode, args[0]);
        System.out.println("This Node ID: " + myNode.getId());

        // Iniciar a thread de receber msg
        myNode.threadR.start();

        TimeUnit.SECONDS.sleep(1);

        myNode.threadHbS.start();

//        Message msg;
//        while (true) {
//
//            msg = myNode.getMessage();
//        }
        //Leader election algorithm
        stateMachineElection(myNode);
    }

    @SuppressWarnings("empty-statement")
    private static void stateMachineElection(MainNode n) throws InterruptedException {
        String state = "STANDBY";
        Message msg = new Message("null|null|null|null|null");
        int mostValuedNode = n.getId();
        int srcNum = 0;
        int srcId = 0;
        while (true) {
            TimeUnit.SECONDS.sleep(1);

            switch (state) {
                case "STANDBY":
                    //n.resetElection(); // para estar permanentemente a fazer elections

                    if(n.getLid() == -1 && !n.isDeltaElection()) {
                        state = "START_ELECTION";
                    } else {
                        msg = n.getMessage(state);
                        if (msg.getTypeMsg().equals("ELECTION")) {
                            state = "CHECK_ELECTION";
                        } else if ((msg.getTypeMsg().equals("LEADER")) && (!(Integer.parseInt(msg.getData()) == n.getLid()))) {
                            state = "BROADCAST_LEADER";
                        }
                    }
                    break;

                case "START_ELECTION":
                    //esta em eleiçao
                    n.setDeltaiElection(true);
                    //imcrementa o src
                    n.setSrc(n.getSrcNum() + 1, n.getId());
                    //envia election a todos os vizinhos

                    for (Integer id : n.getN().keySet()) {
                        n.getN().get(id).sendElection(n.getSrcId(), n.getSrcNum());
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
                    srcNum = Integer.parseInt(msg.getData().substring(init, end));

                    init = end + 1;
                    end = msg.getData().length();
                    srcId = Integer.parseInt(msg.getData().substring(init, end));

                    //Se tiver src igual estou a falar do mesmo logo responde instataneamente.
                    if (srcNum == n.getSrcNum() && srcId == n.getSrcId()) {
                        n.getN().get(msg.getSenderId()).sendAck(mostValuedNode);
                        state = "WAIT_ACK";

                        // Uma eleição com src superior
                    } else if (compareSrc(srcNum, srcId, n.getSrcNum(), n.getSrcId())) {
                        state = "RETRANSMIT_ELECTION";
                    } else if (!(compareSrc(srcNum, srcId, n.getSrcNum(), n.getSrcId()))) {
                        state = "WAIT_ACK";
                       // System.out.println("src_num " + n.getSrcNum() + " src_id " + n.getSrcId() + " scrNum da mensagem " + srcNum + " scrId da mensagem " + srcId);
                    }

                    break;

                case "WAIT_ACK":

                     if ((n.getS().isEmpty() && (n.getId() != n.getSrcId()))) {
                        state = "ACK_TO_PARENT";
                        break;
                    } else if ((n.getS().isEmpty()) && (n.getId() == n.getSrcId())) {
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
                    n.setSrc(srcNum, srcId);
                    //envia election a todos os vizinos menos o pai e adiciona a lista de espera de ack
                    for (Integer id : n.getN().keySet()) {
                        if (id != n.getP()) {
                            n.getN().get(id).sendElection(n.getSrcId(), n.getSrcNum());
                            if (n.getN().get(id).isAlive()) {
                                System.err.println("Adicionei " + id);
                                n.addS(id);
                            }
                        }
                    }

                    state = "WAIT_ACK";
                    break;

                case "ACK_TO_PARENT":
                    //envia nó mais valioso
                    n.getN().get(n.getP()).sendAck(mostValuedNode);
                    //coloca a variavel que diz se ele já enviou a true
                    n.setDeltaACK(true);

                    state = "WAIT_LEADER";
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
                    System.out.println(" Lider: " + n.getLid() + " Pai " + n.getP() + " src_id " + n.getSrcId() + " src_num " + n.getSrcNum());

                    break;

            }

            System.out.println("State = " + state + " || Lider: " + n.getLid());
        }
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
