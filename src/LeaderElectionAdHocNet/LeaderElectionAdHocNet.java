package LeaderElectionAdHocNet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderElectionAdHocNet {

    public static void main(String[] args) {
        MainNode myNode = new MainNode();
        configuration(myNode, args[0]);
        System.out.println("This Node ID: " + myNode.getId());

        //Leader election algorithm
        stateMachineElection(myNode);
        
        System.out.println("Terminou");
//        while (true) {
//            
//            
//            
//            if (myNode.getId() == 1) {
//                myNode.getN().get(2).sendAck(myNode.getId());
//                myNode.getN().get(3).sendAck(myNode.getId());
//            } else {
//                System.out.println("Mesage: " + myNode.getMessage().getTrama());
//            }
//        }
    }

    private static void stateMachineElection(MainNode n) {
        String state = "STANDBY";
        Message msg = new Message("null|null|null|null|null");
        int mostValuedNode = n.getId();

        while (true) {
            if(n.getId() != 1 && state.equals("STANDBY"))
                msg = n.getMessage();
            switch (state) {
                case "STANDBY":
                    if (n.getId() == 1 /*n.getLid() == -1 && !n.isDeltaElection()*/) {
                        state = "START_ELECTION";
                    } else {
                        if (msg.getTypeMsg().equals("ELECTION")) {
                            state = "RETRANSMIT_ELECTION";
                        }
                    }
                    break;
                
                    //é este nó que inicia a eleição
                case "START_ELECTION":
                    state = "WAIT_ACK_TO_SEND_LEADER";
                    break;
                    //espera por receber os ack todos para responder
                case "WAIT_ACK_TO_SEND_LEADER":
                    if(msg.getTypeMsg().equals("ELECTION")){
                        state = "RETRANSMIT_ELECTION";
                    } else if(n.getS().isEmpty()){
                        state = "BROADCAST_LEADER";
                    }
                    break;
                
                    //Retransmite a mensagem de ELECTION para os vizinhos se >src
                    //Se tiver igual src responde com um ACK com o nó mais valioso que tem
                case "RETRANSMIT_ELECTION":
                    state = "WAIT_ACK";
                    break;
                    
                // Espera pelos ACK dos vizinhos
                case "WAIT_ACK":
                    if(n.getS().isEmpty()){
                        state = "SEND_ACK_TO_PARENT";
                    } else {
                        if(msg.getTypeMsg().equals("ELECTION")){
                            state = "RETRANSMIT_ELECTION";
                        }
                    }
                    break;
                
                //Envia o ack para o pai
                case "SEND_ACK_TO_PARENT":
                    state = "WAIT_FOR_LEADER";
                    break;
                
                //Espera que seja dada a informção sobre quem é o lider.    
                case "WAIT_FOR_LEADER":
                    if(msg.getTypeMsg().equals("LEADER")){
                        state = "BROADCAST_LEADER";
                    }
                    break;
                 
                case "BROADCAST_LEADER":
                    state = "STANDBY";
                    break;
                    
                //case "REPLY":
                //    break;
            }

            //ações
            if (state.equals("STANDBY")) {
                n.setDeltaiElection(false);

            } else if (state.equals("START_ELECTION")) {
                //esta no eleiçao
                n.setDeltaiElection(true);
                //imcrmenta o src
                n.setSrc(n.getSrcNum()+ 1, n.getId());
                //envia election a todos os vizinos
                for (Integer id : n.getN().keySet()) {
                    n.getN().get(id).sendElection(n.getSrcId(), n.getSrcNum());
                    n.addS(id);
                }
            } else if(state.equals("WAIT_ACK_TO_SEND_LEADER")){
                //se receber um ack retira o nó da lista
                n.getS().remove(msg.getSenderId());
                int val = Integer.parseInt(msg.getData());
                //actualiza o no mais valioso
                if (val > mostValuedNode) {
                    mostValuedNode = val;
                }
                
            } else if (state.equals("RETRANSMIT_ELECTION")) {
                //Verificar se src é superior
                
                int init = 0;
                int end =  msg.getData().indexOf(",");
                int srcNum =  Integer.parseInt(msg.getData().substring(init, end));

                init = end + 1;
                end =  msg.getData().length();
                int srcId =  Integer.parseInt(msg.getData().substring(init, end));
                
                //Se tiver src igual estou a falar do mesmo logo responde instataneamente.
                if(srcNum == n.getSrcNum() && srcId == n.getSrcId()){
                    n.getN().get(msg.getSenderId()).sendAck(mostValuedNode);
                    
                // Uma eleição com src superior
                } else if (compareSrc(srcNum, srcId, n.getSrcId(), n.getSrcNum())) {
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
                            n.addS(id);
                        }
                    }
                }
          
            } else if (state.equals("WAIT_ACK")) {
                //se receber um ack retira o nó da lista
                n.getS().remove(msg.getSenderId());
                int val = Integer.parseInt(msg.getData());
                //actualiza o no mais valioso
                if (val > mostValuedNode) {
                    mostValuedNode = val;
                }
                
            } else if (state.equals("SEND_ACK_TO_PARENT")){
                //envia nó mais valioso
                n.getN().get(n.getP()).sendAck(mostValuedNode);
                //coloca a variavel que diz se ele já enviou a true
                n.setDeltaACK(true);
            
            } else if (state.equals("BROADCAST_LEADER")) {
                for (Integer id : n.getN().keySet()) {
                    if (id != n.getP()) {
                        n.getN().get(id).sendLeader(Integer.parseInt(msg.getData()));
                    }
                }
            }
             System.out.println("State = " + state + " Lider: " + n.getLid());
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
    private static boolean compareSrc(int srcId1, int srcNum1, int srcId2, int srcNum2){
        return ((srcNum1 > srcNum2) || ((srcNum1 == srcNum2) && (srcId1 > srcId2)));
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
