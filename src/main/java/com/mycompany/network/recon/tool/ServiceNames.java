/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.network.recon.tool;

import java.util.Map;

/**
 *
 * @author USER-PC
 */
public class ServiceNames 
{
    
    private static final Map<Integer, String> SERVICES = Map.ofEntries(
    Map.entry(20, "FTP-data"),
    Map.entry(21, "FTP"),
    Map.entry(22, "SSH"),
    Map.entry(23, "Telnet"),
    Map.entry(25, "SMTP"),
    Map.entry(53, "DNS"),
    Map.entry(80, "HTTP"),
    Map.entry(110, "POP3"),
    Map.entry(135, "RPC"),
    Map.entry(143, "IMAP"),
    Map.entry(443, "HTTPS"),
    Map.entry(445, "SMB"),
    Map.entry(3306, "MySQL"),
    Map.entry(3389, "RDP"),
    Map.entry(5432, "PostgreSQL"),
    Map.entry(8080, "HTTP-Alt")
    );
   
    public static String lookup(int port)
    {
        String name = SERVICES.get(port);
        if(name == null)
        {
            return "unknown service";
        }
        return name;
    }
}
