/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.network.recon.tool;

import java.util.ArrayList;
import java.util.List;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

/**
 *
 * @author USER-PC
 */
public class PacketSniffer 
{
    private PcapNetworkInterface networkInterface;
    private volatile boolean keepCapturing = true;
    private List<Packet> capturedPackets = new ArrayList<>();
    private int count = 0;
    private String targetIp;
    private List<String> relevantPackets = new ArrayList<>();
    
    
    public PacketSniffer(PcapNetworkInterface inInterface, String targetIp)
    {
        this.targetIp = targetIp;
        networkInterface = inInterface;
    }
    public void stopCapturing()
    {
        keepCapturing = false;
    }
    public void startCapture() throws Exception
    {
        PcapHandle handle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        
        while(keepCapturing)
        {
            Packet packet = handle.getNextPacket();
            if(packet != null)
            {
                capturedPackets.add(packet);
                count++;
                processPacket(packet);
            }
        }
        handle.close();
    }
    
    private void processPacket(Packet packet)
    {
{
    TcpPacket tcpPacket = packet.get(TcpPacket.class);
    if (tcpPacket == null) { return; }  // silently skip — see note below
    
    IpV4Packet ipPacket = packet.get(IpV4Packet.class);
    if (ipPacket == null) { return; }
    
    IpV4Packet.IpV4Header ipHeader = ipPacket.getHeader();
    TcpPacket.TcpHeader tcpHeader = tcpPacket.getHeader();
    String srcIp = ipHeader.getSrcAddr().getHostAddress();
    String dstIp = ipHeader.getDstAddr().getHostAddress();
    int srcPort = tcpHeader.getSrcPort().valueAsInt();
    int dstPort = tcpHeader.getDstPort().valueAsInt();
    boolean isSyn = tcpHeader.getSyn();
    boolean isAck = tcpHeader.getAck();
    boolean isRst = tcpHeader.getRst();
    
    if(!srcIp.equals(targetIp) && !dstIp.equals(targetIp))
    {
        return;
    }
    
    String description;
    if (isSyn && !isAck)
    {
        description = "SYN (connection request)";
    }
    else if (isSyn && isAck)
    {
        description = "SYN-ACK (connection accepted)";
    }
    else if (isRst)
    {
        description = "RST (connection refused/reset)";
    }
    else
    {
        description = "ACK";
    }
    
    relevantPackets.add(srcIp + ":" + srcPort + " -> " + dstIp + ":" + dstPort + "  [" + description + "]");
    
}
    }   
    
    public int getPacketCount()
    {
        return count;
    }
    public List<String>getRelevantPackets()
    {
        return relevantPackets;
    }
}
