import java.net.*;

public class CCS {
    int port;

    DatagramSocket udpChannel = null;

    public CCS(int port)
    {
        this.port = port;
        new Thread(this::startUDPListener).start();
        TCPHandler.createOrGet(port).start();
    }

    private void startUDPListener() {
        try
        {
            udpChannel = new DatagramSocket(port);
            while (!udpChannel.isClosed())
            {
                byte[] buffer = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

                udpChannel.receive(receivedPacket);
                String receivedMessage = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                if (receivedMessage.startsWith("CCS DISCOVER"))
                {
                    System.out.println("CCS DISCOVERED");
                    byte[] messageToSend = "CCS FOUND".getBytes();

                    DatagramPacket initeRequestPacket = new DatagramPacket(messageToSend, messageToSend.length, receivedPacket.getAddress(), receivedPacket.getPort());
                    udpChannel.send(initeRequestPacket);

                    System.out.println("CCS SEND to "+receivedPacket.getAddress().getHostAddress());
                }
            }
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void main(String[] args) {
        try {
            new CCS(Integer.parseInt(args[0]));
        } catch (NumberFormatException e)
        {
            System.out.println("Error podany argument nie jest poprawną liczbą");
            System.exit(0);
        }
    }
}


