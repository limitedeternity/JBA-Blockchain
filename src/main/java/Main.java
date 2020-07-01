import java.io.File;
import java.io.IOException;

public class Main {
    private static final File BLOCKDB = new File("blocks.dat");

    private static Blockchain retrieveBlockchainInstance() {
        if (BLOCKDB.exists()) {
            try {
                Blockchain tempBc = (Blockchain) SerializationUtils.deserialize(BLOCKDB);
                if (tempBc.verifyChainIntegrity()) {
                    return tempBc;
                }

                System.out.println("Blockchain is invalid. Creating a new blockchain.");
            } catch (IOException | ClassNotFoundException ignored) {
                System.out.println("Data file is malformed. Creating a new blockchain.");
            }
        }

        return new Blockchain(BLOCKDB);
    }

    public static void main(String[] args) {
        Blockchain bc = retrieveBlockchainInstance();
        MinerSimulator miner = new MinerSimulator(bc);

        Runtime.getRuntime().addShutdownHook(new Thread(miner::shutdown));

        // while (true) {
        for (int i = 0; i < 15; i++) {
            miner.generateBlock();
        }

        System.exit(0);
    }
}
