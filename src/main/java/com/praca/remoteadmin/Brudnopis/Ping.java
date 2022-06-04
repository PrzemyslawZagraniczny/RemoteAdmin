package com.praca.remoteadmin.Brudnopis;

import java.io.*;
import java.net.*;

class Ping
{
    // Sends ping request to a provided IP address
    public static void sendPingRequest(String ipAddress)
            throws UnknownHostException, IOException
    {
        InetAddress geek = InetAddress.getByName(ipAddress);
        System.out.println("Sending Ping Request to " + ipAddress);
        if (geek.isReachable(5000))
            System.out.println("Host is reachable");
        else
            System.out.println("Sorry ! We can't reach to this host");
    }

    // Driver code
    public static void main(String[] args)
            throws UnknownHostException, IOException
    {
        String ipAddress = "127.0.0.1";
        sendPingRequest(ipAddress);

        ipAddress = "spk-ssh.if.uj.edu.pl";
        sendPingRequest(ipAddress);
    }
}