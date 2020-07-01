import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Blockchain implements Serializable {
    private static final long serialVersionUID = 1L;

    private int hashZeros = 0;
    private final Deque<Block> chain = new LinkedList<>();
    private final File dbFile;

    private final List<Map<String, String>> serviceData = new ArrayList<>();
    private final HashMap<String, HashMap<String, String>> knownServices = new HashMap<>() {{
        put("ChatService", new HashMap<>() {{
            put("signature", "06c4599883efc5f0a5353d87398efce003e07b85b1a0e006919925d836f67d5b");
            put("permissions", "");
        }});

        put("PrismaShop", new HashMap<>() {{
            put("signature", "ec5257885446be3b7bd413270b1a2ddae9ee8d1f39f9cdc28f98b5f14e081c0d");
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
            switch (hashZerosDelta) {
                case 0:
                    System.out.print("N stays the same\n\n");
                    break;

                case -1:
                    System.out.printf("N was decreased to %d\n\n", hashZeros);
                    break;

                case 1:
                    System.out.printf("N was increased to %d\n\n", hashZeros);
                    break;
            }
        }

        chain.addLast(block);
        saveState();
    }

    public void acceptServiceData(String serviceId, String signature, String data) {
        String[] parts = signature.split("#");
        String serviceName = new String(Base64.getDecoder().decode(parts[0].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String serviceSignature = new String(Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        if (!knownServices.containsKey(serviceName) || !knownServices.get(serviceName).get("signature").equals(serviceSignature)) {
            return;
        }

        // Potential Chain of Responsibility pattern
        if (data.matches("^(\\d+) bought (.+) for (\\d+) VC$")
                && !knownServices.get(serviceName).get("permissions").contains("transact")) {
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
