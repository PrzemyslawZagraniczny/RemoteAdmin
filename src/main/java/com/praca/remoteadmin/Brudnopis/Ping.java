package com.praca.remoteadmin.Brudnopis;

import com.praca.remoteadmin.Connection.ConnectionHelper;
import com.praca.remoteadmin.Utils.ExitStatusMapper;

import java.io.*;
import java.net.*;

class Ping
{
    // Sends ping request to a provided IP address
    public static void sendPingRequest(String ipAddress)
            throws IOException
    {
        InetAddress check = InetAddress.getByName(ipAddress);
        if (check.isReachable(5000))
            ConnectionHelper.log.info("Host <<"+ipAddress+">> is reachable");
        else
            ConnectionHelper.log.error("Can't reach to this host<<"+ipAddress+">>");
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
            throws IOException
    {

        String ipAddress = "192.168.42.141";
        sendPingRequest(ipAddress);

        ipAddress = "spk-ssh.if.uj.edu.pl";
        //sendPingRequest(ipAddress);
    }
}