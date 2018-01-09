package LeaderElectionAdHocNet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class LeaderElectionAdHocNet {

    private static String state = "STANDBY";
    private static Message msg = new Message("null|null|null|null|null");
    private static int mostValuedNode;
    private static int srcNumElect = 0;
    private static int srcIdElect = 0;
    
    
  //  private int idHbAux;

    public static void main(String[] args) throws InterruptedException {
        MainNode myNode = new MainNode();
        configuration(myNode, args[0]);
        System.out.println("This Node ID: " + myNode.getId());
        myNode.setFirstExec(true);

        mostValuedNode = myNode.getId();

        // Criar a thread para receber as msg
        myNode.threadR = new ThreadReceive(myNode);
        myNode.threadProbes = new ThreadProbes(myNode);
        myNode.threadHeartbeat = new ThreadHeartbeatS(myNode);
        myNode.threadReconfig = new ThreadReconfig(myNode);
        // Iniciar a thread de receber msg
        
        myNode.threadR.start();
        
        TimeUnit.SECONDS.sleep(1);

        myNode.threadProbes.start();

        myNode.threadHeartbeat.start();
        
        myNode.threadReconfig.start();

        
        while (true) {
            TimeUnit.MILLISECONDS.sleep(1000);
            
            leaderElection(myNode);

        }
    }
    
    private static void leaderElection(MainNode n) {
        msg = n.getMessage();
        boolean action = false;
        
        /*Start a new computation*/
        if (!n.isDeltaElection() && n.getLid() == -1) {
            n.setSrc(n.getSrcNum() + 1, n.getId());
            srcNumElect = n.getSrcNum();
            srcIdElect = n.getId();
            //envia election a todos os vizinhos
            for (Integer id : n.getN().keySet()) {
                n.getN().get(id).sendElection(srcNumElect, srcIdElect, n.getLid());
                n.addS(id);
            }
            n.setDeltaiElection(true);
            n.setDeltaACK(true);
            n.setP(n.getId());
            mostValuedNode = n.getId();
            action = true;
        }
        
        /*Join the computation*/
        if(msg.getTypeMsg().equals("ELECTION") 
           && (!n.isDeltaElection() || (n.isDeltaElection() && compareSrc(msg.getmSrcNum(), msg.getmSrcId(), srcNumElect, srcIdElect)))
           && (msg.getLider() == n.getLid())) {

            //actualiza src
            srcNumElect = msg.getmSrcNum();
            srcIdElect = msg.getmSrcId();

            //actuliza o pai
            n.setP(msg.getSenderId());
            
            //envia election a todos os vizinos menos o pai e adiciona a lista de espera de ack
            for (Integer id : n.getN().keySet()) {
                if (id != n.getP()) {
                    n.getN().get(id).sendElection(srcNumElect, srcIdElect, n.getLid());
                    n.addS(id);
                }
            }
            
            n.setDeltaiElection(true);
            n.setDeltaACK(true);
            mostValuedNode = n.getId();
            action = true;
        }
        
        /* Already in computation; or I still have my leader */
        if((msg.getTypeMsg().equals("ELECTION") 
                && n.isDeltaACK()
                && (msg.getmSrcNum() == srcNumElect && msg.getmSrcId() == srcIdElect))
                || (msg.getTypeMsg().equals("ELECTION") 
                && msg.getLider() != n.getLid())) {
            
            n.getN().get(n.getP()).sendAck(srcIdElect, srcNumElect, 0, 0);
            action = true;
        }
        
        /* Update list of nodes to be heard from*/
        if ((msg.getTypeMsg().equals("ACK")
                && n.isDeltaElection()
                && (msg.getmSrcNum() == srcNumElect && msg.getmSrcId() == srcIdElect))
                //|| ( n.getS().contains(msg.getSenderId()) && !n.getN().get(msg.getSenderId()).isAlive()) //não pde ser implementado aqui 
                || (n.getS().contains(msg.getSenderId())
                && msg.getTypeMsg().equals("REPLY")
                && ((msg.getmSrcNum() != srcNumElect || msg.getmSrcId() != srcIdElect)
                || msg.getDeltaElection() == 0
                || msg.getFlag() == 0))) {
            
             n.getS().remove(msg.getSenderId());
             
             if(msg.getTypeMsg().equals("ACK") && (msg.getFlag() == 1) && (msg.getMax() > mostValuedNode)) {
                 mostValuedNode = msg.getMax();
             }
             action = true;
        }
                
        /*Report pending Ack to parent*/
        if((n.getS().isEmpty() && (n.getId() != srcIdElect) && n.isDeltaACK())){
            n.setDeltaACK(false);
            n.getN().get(n.getP()).sendAck(srcNumElect, srcIdElect, 1, mostValuedNode);
            action = true;
        }
        
        /*Terminate computation, announce leader*/
        if((n.getS().isEmpty() && (n.getId() == srcIdElect) && n.isDeltaACK())
                || (!n.isDeltaACK() && n.isDeltaElection() && !n.getN().get(n.getP()).isAlive())
                || (msg.getTypeMsg().equals("LEADER") && msg.getLider() < n.getLid() && !n.isDeltaElection())
                || (msg.getTypeMsg().equals("REPLY") && !n.isDeltaACK() 
                && ((msg.getmSrcNum() != srcNumElect || msg.getmSrcId() != srcIdElect) || msg.getDeltaElection() == 0))) {
            
            n.setDeltaACK(false);
            n.setDeltaACK(false);
            n.setLid(mostValuedNode);
   
            for (Integer id : n.getN().keySet()) {
                n.getN().get(id).sendLeader(srcNumElect, srcIdElect, n.getLid());
            }
            action = true;
        }

        /*Adopt a new leader*/
        if((!n.isDeltaACK() && n.isDeltaElection() && msg.getTypeMsg().equals("LEADER") && (mostValuedNode < msg.getLider()))
                || (!n.isDeltaElection() && msg.getTypeMsg().equals("LEADER") && n.getLid() < msg.getLider())) {
            n.setLid(msg.getLider());
            n.setDeltaiElection(false);
            srcIdElect = msg.getmSrcId();
            srcNumElect = msg.getmSrcNum();

            for (Integer id : n.getN().keySet()) {
                n.getN().get(id).sendLeader(srcNumElect, srcIdElect, n.getLid());
            }
            System.out.println("Novo Lider: " + n.getLid() + " srcNumElect: " + srcNumElect + " srcIdElect: " + srcIdElect);
            action = true;
        }
        
        /*Announce my leader to a new neighbor*/
        //feito no treadrecive
        
        /*Send reply in response to received Probe message*/
        if(msg.getTypeMsg().equals("PROBE")) {
            n.getN().get(msg.getSenderId()).sendReply(srcNumElect, srcIdElect, n.isDeltaElection()?1:0, n.getLid());
            action = true;
        }
        
        /* Deque message if no other action is enabled */ 
        
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
