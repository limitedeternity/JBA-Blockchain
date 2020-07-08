import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Blockchain implements Serializable {
    private static final long serialVersionUID = 1L;

    private int hashZeros = 0;
    private final Deque<Block> chain = new LinkedList<>();
    private final File dbFile;

    private final List<Map<String, String>> serviceData = new ArrayList<>();
    private final HashMap<String, HashMap<String, String>> knownServices = new HashMap<>() {{
        put("dUaKL3CEiFx6mQB8mBTf", new HashMap<>() {{
            put("signature", "0f34eeee95f4b6b225c41d45b52669ea92c3ca828e40ba4de116662cd648e2e6");
            put("permissions", "");
        }});

        put("dSJxhS6faBIZPY0", new HashMap<>() {{
            put("signature", "e8bac969ef741e3d2d55c288254446746ad8b697f55977e14dacb0d6dcb376c0");
            put("permissions", "transact");
        }});
    }};

    public Blockchain(File blockDB) {
        dbFile = blockDB;
    }

    private void saveState() {
        try {
            SerializationUtils.serialize(this, dbFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void acceptNextBlockCandidate(Block block, boolean verbose) {
        if (block == null
                || !block.getCurrentHash().startsWith(StringUtils.repeat("0", hashZeros))
                || !block.getPreviousHash().equals(chain.peekLast() != null ? chain.peekLast().getCurrentHash() : "0")
                || !block.getBlockData().isEmpty()
        ) {
            return;
        }

        boolean dataAccepted = block.acceptData(serviceData);
        if (dataAccepted) {
            serviceData.clear();
        }

        int hashZerosDelta = 0;
        if (block.getElapsedSeconds() < 10L) {
            hashZeros++;
            hashZerosDelta++;
        } else if (block.getElapsedSeconds() > 20L) {
            hashZeros--;
            hashZerosDelta--;
        }

        if (verbose) {
            System.out.print(block.toString());
            if (hashZerosDelta > 0) {
                System.out.printf("N was increased to %d\n\n", hashZeros);
            } else if (hashZerosDelta < 0) {
                System.out.printf("N was decreased to %d\n\n", hashZeros);
            } else {
                System.out.print("N stays the same\n\n");
            }
        }

        chain.addLast(block);
        saveState();
    }

    public void acceptServiceData(String serviceId, String signature, String data) {
        if (!knownServices.containsKey(serviceId)
                || !knownServices.get(serviceId).get("signature").equals(StringUtils.applySha256(signature))) {
            return;
        }

        // Potential Chain of Responsibility pattern
        if (data.matches("^(\\d+) bought (.+) for (\\d+) VC$")
                && !knownServices.get(serviceId).get("permissions").contains("transact")) {
            return;
        }

        Map<String, String> serviceToDataCorrespondence = Collections.singletonMap(serviceId, data);
        synchronized (this) {
            serviceData.add(serviceToDataCorrespondence);
        }
    }

    public int getHashZeros() {
        return hashZeros;
    }

    public Block getLastBlock() {
        return chain.peekLast();
    }

    public Deque<Block> getChain() {
        return chain;
    }

    public boolean verifyChainIntegrity() {
        Iterator<Block> iterDown = chain.descendingIterator();
        List<String> messageIds = new ArrayList<>();

        Block currentBlock = iterDown.next();
        currentBlock.getBlockData().values()
                .forEach(v -> messageIds.addAll(v.stream().map(hm -> hm.get("id")).collect(Collectors.toList())));

        while (iterDown.hasNext()) {
            Block previousBlock = iterDown.next();
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())
                || currentBlock.getId() != previousBlock.getId() + 1) {
                return false;
            }

            previousBlock.getBlockData().values()
                    .forEach(v -> messageIds.addAll(v.stream().map(hm -> hm.get("id")).collect(Collectors.toList())));
            currentBlock = previousBlock;
        }

        return messageIds.size() == messageIds.stream().distinct().count();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Block block : chain) {
            builder.append(block.toString());
        }

        return builder.toString();
    }
}
