import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {
    int port;
    String ip;

    DatagramSocket udpSocket = null;
    Socket tcpSocket = null;

    public Client(int port)
    {
        this.port = port;
        UdpMessage();
    }

    private void StartTCP()
    {
        try {
            tcpSocket = new Socket(ip, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);

            // Instruction for testing specific case
            String instruction = "DIV 100 0";
            System.out.println("Send: "+instruction);
            out.println(instruction);

            int limit = 2000;
            int messenges = 0;

            String message;
            while ((message = in.readLine()) != null)
            {
                System.out.println("Recived: " + message);
                if (messenges++ < limit-1) {
                    instruction = GetInstruction();
                    System.out.println("Send: "+instruction);
                    Thread.sleep(1);
                    out.println(instruction);
                }
                else
                    break;

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String GetInstruction()
    {
        Random random = new Random();
        int rand = random.nextInt(4);
        switch (rand) {
            case 1:
                return "SUB " + random.nextInt(100) + " " + random.nextInt(100);
            case 2:
                return "MUL " + random.nextInt(100) + " " + random.nextInt(100);
            case 3:
                return "DIV " + random.nextInt(100) + " " + random.nextInt(100);
            default:
                return "ADD " + random.nextInt(100) + " " + random.nextInt(100);
        }
    }

    private void UdpMessage()
    {
        try {
            udpSocket = new DatagramSocket();
            byte[] messageBytes = "CCS DISCOVER".getBytes();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, breadcastAddress(), port);
            System.out.println(breadcastAddress());
            udpSocket.send(packet);

            while(!udpSocket.isClosed())
            {
                byte[] buf = new byte[1024];
                DatagramPacket recivedPacket = new DatagramPacket(buf, buf.length);

                udpSocket.receive(recivedPacket);
                String recived = new String(recivedPacket.getData(), 0, recivedPacket.getLength());
                if (recived.equals("CCS FOUND"))
                {
                    ip = recivedPacket.getAddress().getHostAddress();
                    StartTCP();
                    udpSocket.close();
                    break;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InetAddress breadcastAddress() throws UnknownHostException, SocketException {
        InetAddress localAddress = InetAddress.getLocalHost();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localAddress);

        byte[] ip = localAddress.getAddress();

        short mask = 0;
        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
            if (interfaceAddress.getAddress().equals(localAddress))
                mask = interfaceAddress.getNetworkPrefixLength();

        int newmask = 0xffffffff << (32-mask);
        byte a = (byte)(ip[0] | ~(newmask >>> 24));
        byte b = (byte)(ip[1] | ~(newmask >>> 16));
        byte c = (byte)(ip[2] | ~(newmask >>> 8));
        byte d = (byte)(ip[3] | ~(newmask));

        byte[] list = new byte[]{a,b,c,d};
        return InetAddress.getByAddress(list);
    }

    public static void main(String[] args) {
        if (args.length < 1)
        {
            System.out.println("Usage: java Client <port>");
            System.exit(0);
        }

        try {
            new Client(Integer.parseInt(args[0]));
        } catch (NumberFormatException e)
        {
            System.out.println("Error: arguemnt nie jest liczbą");
            System.exit(0);
        }
    }
}
