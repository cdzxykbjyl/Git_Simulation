package test;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UdpSender {

    private String udpIP;

    private int udpPort;

    public UdpSender( String udpIP, int udpPort) {
        this.udpIP = udpIP;
        this.udpPort = udpPort;
    }

    public void sendDataToUdp(String message) {
            try {
                InetAddress address = InetAddress.getByName(udpIP);
                byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, udpPort);
                DatagramSocket dsocket = new DatagramSocket();
                dsocket.send(packet);
                dsocket.close();
            } catch (Exception e) {

            }
    }

}
