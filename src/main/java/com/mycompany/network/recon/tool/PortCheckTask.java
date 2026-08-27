/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.network.recon.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.io.PrintStream;

/**
 *
 * @author USER-PC
 */
public class PortCheckTask implements Callable<ScanResult>
{
   private String host;
   private int port;
   
   
    public PortCheckTask(String inHost, int inPort)
    {
        host = inHost;
        port = inPort;
    }
    @Override
    public ScanResult call() throws IOException
    {
        
     Socket socket = new Socket();  //open port
     
     try
     {
         
     socket.connect(new InetSocketAddress(host,port), 100);
     socket.setSoTimeout(1500);
     
     if(port == 80 || port == 443 || port == 8000)
     {
         
     PrintStream out = new PrintStream(socket.getOutputStream());
     out.print("GET / HTTP/1.0\r\n\r\n");
     out.flush();
     
     }
     try
    {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String banner = reader.readLine();
        
        if(banner == null)
        {
            banner = "connection closed with no data";
        }
        
        return new ScanResult(port, "open", banner);
    }
    catch(SocketTimeoutException e)
    {
        return new ScanResult(port, "open", "no banner recieved");
    }
     
    
   
            
     }
     catch(SocketTimeoutException e)
     {
          return new ScanResult(port, "filtered", null);
     }
     catch(ConnectException e)
     {
          return new ScanResult(port, "closed", null);
     }
     catch(IOException e)
     {
         return new ScanResult(port, "error" , null);
     }
     finally
     {
         try { socket.close(); } catch(IOException e) {}
    }
}
}  

