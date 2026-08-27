/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.network.recon.tool;

/**
 *
 * @author USER-PC
 */
public class ScanResult {
    private int port;
    private String status;
    private String banner;

    public ScanResult(int inPort, String inStat, String  inBan) {
       port = inPort;
       status = inStat;
       banner = inBan;
    }

    public int getPort() 
    {
        return port;
    }

    public String getStatus() 
    {
        return status;
    }

    public String getBanner() 
    {
        return banner;
    }
    
    
    
}
