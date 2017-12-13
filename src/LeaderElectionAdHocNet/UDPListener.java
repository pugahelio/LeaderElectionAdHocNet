/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LeaderElectionAdHocNet;

import java.net.*;
/**
 *
 * @author helio
 */
public class UDPListener extends Thread{
    private DatagramSocket socket;
    private boolean running;
    private InetAddress address;
    private byte[] buf = new byte[1024];
    private int port;
    
    public void UDPListener(int p) throws SocketException{
        port = p;
        socket = new DatagramSocket(port);
    }
    
    
}
