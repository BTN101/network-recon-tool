/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.network.recon.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author USER-PC
 */
public class PortScanner {
    
    private int count =0;
    private int startPort;
    private int endPort;
    private String host;
    private String filter;
    private ExecutorService pool = Executors.newFixedThreadPool(30); //creating a facotry of workers to grab the ports
    private List<Future<ScanResult>> futures = new ArrayList<>(); //creating an array list where workers will have a place to hold the port/ticket and come back with it
    private List<String> resultLines = new ArrayList<>();
    private int openCount = 0;
    private int closedCount = 0;
    private int filteredCount = 0;
    
    public PortScanner(int strPort, int endPrt, String inHost, String inFilter)
    {
     startPort = strPort;
     endPort = endPrt;
     host = inHost;
     filter = inFilter;
    }
    
    public void portScan() throws Exception
    {
        
    for(int k = startPort; k<=endPort; k++)
    {
        PortCheckTask task = new PortCheckTask(host, k);
        Future<ScanResult> future = pool.submit(task); //Now grabbing the tickets
        futures.add(future); //adding the tickets to the list
    }
    
for(Future<ScanResult> future : futures)
{
    try 
    {
        ScanResult result = future.get(3, TimeUnit.SECONDS);
      
        
    if(filter.equals("all") || filter.equals(result.getStatus()))
    {
    resultLines.add(result.getPort() + " (" + ServiceNames.lookup(result.getPort()) + "): " + result.getStatus() + "\tBanner: " + result.getBanner());
    count++;
    }   

    if (result.getStatus().equals("open")) 
    {
        openCount++; 
    }
    else if (result.getStatus().equals("closed")) 
    {
        closedCount++; 
    }
    else if (result.getStatus().equals("filtered")) 
    {
        filteredCount++; 
    }
    }
    
        catch (TimeoutException e)
    {
        System.out.println("(a port check took too long and was skipped)");
    }
    
}
     if(count == 0)
        {
            System.out.println("No ports matching the selcted filter: " + filter + " were found");
        }
        System.out.println("Scan Complete. " + futures.size() + " ports checked.");
        pool.shutdown();
    }
    public List<String>getResultLines()
    {
        return resultLines;
    }
    public int getOpenCount() 
    { 
        return openCount; 
    }
    public int getClosedCount() 
    { 
        return closedCount; 
    }
    public int getFilteredCount() 
    {
        return filteredCount; 
    }
    public int getTotalScanned() 
    {
        return futures.size(); 
    }
}

