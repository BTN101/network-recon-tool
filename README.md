# network-recon-tool

A Java network reconnaissance tool combining active TCP port scanning, service identification, and real-time packet capture — correlating live scan traffic with raw packet-level evidence, all running simultaneously.

Built as the third project in a deliberate security portfolio sequence: [java-port-scanner](https://github.com/BTN101/java-port-scanner) → [java-arp-spoof-detector](https://github.com/BTN101/java-arp-spoof-detector) → **network-recon-tool**.

---

## What it does

Point the tool at a target IP and it runs two systems simultaneously:

**Active scanning** — a multi-threaded TCP port scanner checks a range of ports concurrently, identifies open/closed/filtered status, grabs service banners where possible, and maps ports to conventional service names.

**Passive capture** — a packet sniffer runs on a separate thread for the entire duration of the scan, capturing raw traffic on the network interface in promiscuous mode, decoding TCP/IP headers, and filtering the results down to only traffic involving the scan target.

The result: you see the same event — "is this port open" — from two levels of abstraction at once. Java's socket API (exceptions, timeouts) on one side, and the raw wire protocol (SYN/ACK/RST packets) on the other, running concurrently and reported together.

---

## Sample output

```
===== SCAN RESULTS =====
53 (DNS): open	Banner: no banner recieved
80 (HTTP): open	Banner: HTTP/1.0 200 OK
20 (FTP-data): filtered	Banner: null
21 (FTP): filtered	Banner: null
...

===== CAPTURED TRAFFIC =====
192.168.1.20:54918 -> 192.168.1.1:80  [SYN (connection request)]
192.168.1.1:80 -> 192.168.1.20:54918  [SYN-ACK (connection accepted)]
192.168.1.20:54918 -> 192.168.1.1:80  [ACK]
192.168.1.20:54918 -> 192.168.1.1:80  [RST (connection refused/reset)]

===== SUMMARY =====
Ports scanned: 1000
Open: 2 | Closed: 0 | Filtered: 998
Packets captured (total): 552
Packets relevant to target: 62

Scan complete.
```

---

## Architecture

```
NetworkReconTool (main)
  ├── selects a real network interface (filters out virtual/Miniport adapters)
  ├── starts PacketSniffer on a dedicated thread (capturePool)
  │     └── captures continuously in promiscuous mode while the scan runs
  ├── runs PortScanner (blocking — main thread waits for full scan)
  │     ├── PortCheckTask × N — one per port, on a 30-thread pool
  │     │     ├── connect (100ms timeout) → open / closed / filtered / error
  │     │     ├── active HTTP probe on ports 80/443/8080 (services that
  │     │     │     wait for the client to speak first)
  │     │     └── passive banner read (1500ms timeout) for everything else
  │     └── ServiceNames.lookup() — port → conventional service name
  └── stops the sniffer, prints three sections: results / traffic / summary
```

### Six classes

| Class | Responsibility |
|---|---|
| `ScanResult` | Data container — one port's outcome (status + banner) |
| `PortCheckTask` | `Callable<ScanResult>` — checks one port, returns a result |
| `PortScanner` | Orchestrates the thread pool, collects results, tracks counts |
| `ServiceNames` | Port → service name lookup table (a labeled guess, not proof) |
| `PacketSniffer` | Captures and decodes packets, filters to the scan target |
| `NetworkReconTool` | Entry point — wires everything together, prints the final report |

---

## Running it

Requires [Npcap](https://npcap.com) installed (Windows packet capture driver — check "WinPcap API-compatible mode" during install) and administrator privileges to run.

```bash
mvn compile
mvn exec:java
```

Edit `targetIp` in `NetworkReconTool.main()` to set the scan target. A future version could take this as a command-line argument (see [Known Limitations](#known-limitations)).

---

## What I discovered building this

**Interface ordering isn't stable.** The tool's first working version selected the network interface by list index (`interfaces.get(0)`). This worked in early testing — but weeks later, on the same machine, three new virtual "WAN Miniport" adapters had appeared ahead of the real Ethernet adapter in the list, silently pushing it from index 0 to index 3. The tool kept running with zero errors, capturing exactly nothing, because it was listening on a dead virtual interface. Fixed by searching for a real adapter by filtering out known-virtual keywords ("Miniport", "Loopback", "Virtual") rather than trusting position in a list that isn't guaranteed to stay stable across sessions or machines.

**A hang isn't always a bug — sometimes it's a missing signal.** Early in development, a large port scan appeared to freeze partway through. Extensive debugging (adding a `.get()` timeout, testing narrower port ranges, adding progress counters) eventually revealed the scan was working correctly the whole time — it just had no way to distinguish "still processing" from "stuck," because there was no completion message. The actual fix was two print statements. The debugging process was still worth it: it surfaced a real, separate bug (`.get()` with no timeout can hang forever on an unexpected failure) that's now a permanent safety net in the code.

**Banner grabbing is genuinely port-agnostic — verified, not assumed.** To confirm the scanner could identify a service running on a nonstandard port (not just the "well-known" ports), a tiny standalone test server was built to send a fake SSH-style banner on port 5000 — an arbitrary port with no conventional association. The scanner correctly captured the banner text, proving the detection is based on protocol behavior, not port-number lookup tables.

**Running two systems concurrently surfaced a subtle race condition.** When the scan and the sniffer's startup happened simultaneously, the scan intermittently returned incorrect results (every port "filtered" or "error") — while the sniffer, tested alone, worked fine, and the scanner, tested alone, worked fine. Adding a short delay after starting the capture thread and before beginning the scan resolved it — evidence that opening a promiscuous-mode capture handle briefly affects other concurrent network operations on the same interface.

---

## Known limitations

| Limitation | Impact | 
|---|---|
| Target IP hardcoded in source | No CLI argument — requires editing and recompiling to change target |
| HTTPS (port 443) can't be meaningfully probed | Would require a full TLS handshake — out of scope |
| Binary protocols (DNS, MySQL, RDP, PostgreSQL) | Send structured byte data `readLine()` can't parse — detected as open, banner not decoded |
| Windows-specific | Npcap and interface-selection logic are Windows-only in current form |
| No continuous/standalone capture mode | Sniffer's lifetime is tied to the scan duration — a "capture until user stops" mode was designed but not built, to keep scope contained |

---

## Testing methodology

Since scanning a live network with a real attacker isn't reproducible or safe to demonstrate, verification relied on:
- A custom `ServerSocket`-based test server sending a known banner on an arbitrary port, confirming banner grabbing works regardless of port number
- Comparing scanner output directly against sniffer-captured packets for the same scan, confirming the same events (open port, closed port, HTTP response) are visible and consistent at both the socket-API level and the raw packet level


---
Legal use

Port scanning and packet capture — even on your own network — should only be run against systems you own or have explicit permission to test. Scanning or capturing traffic on a network you don't control or lack authorization for is illegal in most jurisdictions, including South Africa under the Cybercrimes Act (2020). This tool was built and tested exclusively against the author's own home network and devices.

Promiscuous-mode capture in particular can see traffic belonging to other devices on the same network segment, not just your own — this is exactly why authorization matters, not just for the target being scanned but for the network itself.

---

## Stack

- **Language:** Java (Maven project)
- **Packet capture:** [pcap4j](https://github.com/kaitoy/pcap4j) + [Npcap](https://npcap.com)
- **Concurrency:** `ExecutorService`, `Callable`/`Future`, `volatile` cross-thread signaling
- **Tested on:** Windows 10/11

---

## Portfolio context

Third project in a security tooling sequence, each building on the last:

1. [java-port-scanner](https://github.com/BTN101/java-port-scanner) — TCP sockets, threading fundamentals (54× speedup: 108s → 2s)
2. [java-arp-spoof-detector](https://github.com/BTN101/java-arp-spoof-detector) — ProcessBuilder, ARP protocol, tiered detection
3. **network-recon-tool** — Maven, Callable/Future, raw packet parsing, concurrent system correlation ← *you are here*

MIT License
