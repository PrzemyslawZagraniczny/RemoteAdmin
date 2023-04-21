package com.praca.remoteadmin.Brudnopis;

import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Utils.ExitStatusMapper;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

class Ping
{
    // Sends ping request to a provided IP address
    public static boolean sendPingRequest(String ipAddress)
            throws IOException
    {
        InetAddress check = InetAddress.getByName(ipAddress);
        if (check.isReachable(5000)) {
            ConnectionHelper.log.info("Host <<" + ipAddress + ">> is reachable");
            return true;
        }
        else {
            ConnectionHelper.log.error("Can't reach to this host<<" + ipAddress + ">>");
            return false;
        }
    }

    public boolean ping(String ipAddress)
            throws IOException
    {
        InetAddress check = InetAddress.getByName(ipAddress);
        System.out.println("Sending Ping Request to " + ipAddress);
        return check.isReachable(5000);
    }
    // Driver code
    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException {

//        String []ipAddressess = new String [] {"192.168.42.16", "192.168.42.116", "192.168.42.86"};
//
//        pingCheck(ipAddressess);
//        String ipAddress = "192.168.42.116";
//        sendPingRequest(ipAddress);
//
//        ipAddress = "spk-ssh.if.uj.edu.pl";
        //sendPingRequest(ipAddress);
    }

    //sprawdza pingiem dostępność wpisanych adresów
    private static void pingCheck(String[] ipAddressess) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(ipAddressess.length);

        Set<Callable<Boolean>> callables = new HashSet<>();
        Arrays.stream(ipAddressess).forEach( (ipAddr) -> {
            callables.add( () -> {
                return sendPingRequest(ipAddr);
            });
        });

        List<Future<Boolean>> futures = executorService.invokeAll(callables);

        int iSuccess = 0;
        int iFailed = 0;
        for(Future<Boolean> future : futures){
            int tmp = future.get() == true ? iSuccess++ : iFailed++;
            System.out.println(future.get());
        }
        executorService.shutdown();
        System.out.println(iSuccess+"/"+(iFailed+iSuccess) + " maszyn jest dostępnych w tej chwili");
    }
}