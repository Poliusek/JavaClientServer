import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable
{
    private final Socket client;
    private PrintWriter out;
    private BufferedReader in;

    public Connection(Socket client)
    {
        this.client = client;
        TCPHandler.incrementNewUsersCount();
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                String[] args = message.split(" ");
                if (!args[0].matches("(ADD|SUB|MUL|DIV).*"))
                    continue;

                TCPHandler.incrementOperationCount();

                String result;
                int val1;
                int val2;
                String op = args[0];

                try {
                    val1 = Integer.parseInt(args[1]);
                    val2 = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    result = "ERROR";
                    continue;
                }

                switch (op) {
                    case "ADD":
                        result = String.valueOf(val1 + val2);
                        TCPHandler.incrementAddOpCount();
                        break;
                    case "SUB":
                        result = String.valueOf(val1 - val2);
                        TCPHandler.incrementSubOpCount();
                        break;
                    case "MUL":
                        result = String.valueOf(val1 * val2);
                        TCPHandler.incrementMulOpCount();
                        break;
                    case "DIV":
                        if (val2 == 0)
                        {
                            result = "ERROR";
                            TCPHandler.incrementErrorOpCount();
                            out.println(result);
                            continue;
                        }
                        result = String.valueOf(val1 / val2);
                        TCPHandler.incrementDivOpCount();
                        break;
                    default:
                        TCPHandler.incrementErrorOpCount();
                        result = "ERROR";
                        out.println(result);
                        continue;
                }

                TCPHandler.incrementSumValue(Integer.parseInt(result));

                out.println(result);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error client has disconnected forcefully");
        }
    }
}