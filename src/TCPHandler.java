import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPHandler extends Thread {
    private ServerSocket serverSocket;
    private final int port;
    private ExecutorService pool;
    protected HashMap<StatisticType, Integer> statistics;
    protected HashMap<StatisticType, Integer> tempStatistics;

    static TCPHandler singleton;

    private TCPHandler(int port) {
        this.port = port;
        pool = Executors.newCachedThreadPool();
        InitializeStatistics();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    System.out.println("Statystyki z ostatnich 10 sekund:");
                    for (StatisticType type : StatisticType.values()) {
                        System.out.println(type + ": " + tempStatistics.get(type));
                        tempStatistics.put(type, 0);
                    }
                    System.out.println("Statystyki od startu serwera:");
                    for (StatisticType type : StatisticType.values())
                        System.out.println(type + ": " + statistics.get(type));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static TCPHandler createOrGet(int port)
    {
        if (singleton == null)
            singleton = new TCPHandler(port);

        return singleton;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed())
            {
                Socket s = serverSocket.accept();
                pool.execute(new Connection(s));
            }
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    private HashMap<StatisticType, Integer> initializeStatistics() {
        HashMap<StatisticType, Integer> stats = new HashMap<>();
        for (StatisticType type : StatisticType.values()) {
            stats.put(type, 0);
        }
        return stats;
    }

    private void InitializeStatistics() {
        statistics = initializeStatistics();
        tempStatistics = initializeStatistics();
    }

    static void incrementNewUsersCount() {
        incrementStatistic(StatisticType.newUsersCount);
    }

    static void incrementOperationCount() {
        incrementStatistic(StatisticType.operationCount);
    }

    static void incrementAddOpCount() {
        incrementStatistic(StatisticType.addOpCount);
    }

    static void incrementSubOpCount() {
        incrementStatistic(StatisticType.subOpCount);
    }

    static void incrementMulOpCount() {
        incrementStatistic(StatisticType.mulOpCount);
    }

    static void incrementDivOpCount() {
        incrementStatistic(StatisticType.divOpCount);
    }

    static void incrementErrorOpCount() {
        incrementStatistic(StatisticType.errorOpCount);
    }

    static void incrementSumValue(int value) {
        TCPHandler.singleton.statistics.put(StatisticType.sumValue, TCPHandler.singleton.statistics.get(StatisticType.sumValue) + value);
        TCPHandler.singleton.tempStatistics.put(StatisticType.sumValue, TCPHandler.singleton.tempStatistics.get(StatisticType.sumValue) + value);
    }

    private static void incrementStatistic(StatisticType type) {
        TCPHandler.singleton.statistics.put(type, TCPHandler.singleton.statistics.get(type) + 1);
        TCPHandler.singleton.tempStatistics.put(type, TCPHandler.singleton.tempStatistics.get(type) + 1);
    }
}

enum StatisticType {
    newUsersCount,
    operationCount,
    addOpCount,
    subOpCount,
    mulOpCount,
    divOpCount,
    errorOpCount,
    sumValue
}