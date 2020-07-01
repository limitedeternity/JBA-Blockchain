import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MinerSimulator {
    private final Blockchain instance;
    private final ChatService chat;
    private final PrismaShop shop;

    private final ExecutorService execPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    private final List<Callable<Void>> workers = IntStream.range(0, Runtime.getRuntime().availableProcessors())
            .mapToObj(ignored -> new Callable<Void>() {
                @Override
                public Void call() {
                    instance.acceptNextBlockCandidate(
                            new Block(instance.getLastBlock(), instance.getHashZeros()), true);

                    chat.receiveMessage("*enters chat*");
                    chat.receiveMessage("*chats*");
                    chat.receiveMessage("*leaves chat*");
                    shop.sellFood(PrismaShop.Products.SANDWICH);
                    return null;
                }
            })
            .collect(Collectors.toList());

    MinerSimulator(Blockchain bc) {
        instance = bc;
        chat = new ChatService(bc);
        shop = new PrismaShop(bc);
    }

    void generateBlock() {
        try {
            // it's invokeAll, but I don't want my processor to suffocate
            execPool.invokeAny(workers);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    void shutdown() {
        execPool.shutdownNow();
    }
}
