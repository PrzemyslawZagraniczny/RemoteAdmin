package com.praca.remoteadmin.Utils;

import com.praca.remoteadmin.Connection.ConnectionHelper;

import java.io.IOException;
import java.net.InetAddress;

class Ping
{
     public static boolean ping(String ipAddress)
            throws IOException
    {
        InetAddress check = InetAddress.getByName(ipAddress);
        System.out.println("Sending Ping Request to " + ipAddress);
        return check.isReachable(5000);
    }
}