/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.network.recon.tool;

/**
 *
 * @author USER-PC
 */
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkReconTool {
    public static void main(String[] args) throws Exception
    {   
     String targetIp = "localhost";
     
     List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();
    PcapNetworkInterface selectedInterface = null;

    for (PcapNetworkInterface iface : interfaces)
    {
    String description = iface.getDescription();
    if (description == null) 
    { 
        continue; 
    }  
    if (description.contains("Miniport") || description.contains("Loopback") || description.contains("Virtual"))
    {
        continue; // skip known non-real adapters
    }
    
    selectedInterface = iface;
    break; // take the first "real-looking" one
    }
    
    if (selectedInterface == null)
    {
    System.out.println("No suitable network interface found. Defaulting to first available.");
    selectedInterface = interfaces.get(0);
    }
     
     PacketSniffer sniffer = new PacketSniffer(selectedInterface, targetIp);
     
     ExecutorService capturePool = Executors.newSingleThreadExecutor();
     
     capturePool.submit(() -> 
     {
         try
         {
             sniffer.startCapture();
             
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
     });
      
     
     
     PortScanner scanner = new PortScanner(1, 50, targetIp, "all");
     scanner.portScan();    
     
    sniffer.stopCapturing();
     
    capturePool.shutdown();
    
    
     System.out.println("===== SCAN RESULTS =====");
    for (String line : scanner.getResultLines())
    {
     System.out.println(line);
    }
    
    System.out.println();
    
    System.out.println("===== CAPTURED TRAFFIC =====");
    for (String line : sniffer.getRelevantPackets())
    {
     System.out.println(line);
    }
    
    System.out.println();
    
    System.out.println("===== SUMMARY =====");
    System.out.println("Ports scanned: " + scanner.getTotalScanned());
    System.out.println("Open: " + scanner.getOpenCount() + " | Closed: " + scanner.getClosedCount() + " | Filtered: " + scanner.getFilteredCount());
    System.out.println("Packets captured (total): " + sniffer.getPacketCount());
    System.out.println("Packets relevant to target: " + sniffer.getRelevantPackets().size());
    System.out.println();
    System.out.println("Scan complete.");
    
    }
}
