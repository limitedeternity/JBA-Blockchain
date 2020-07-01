import javafx.util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient final long minerId = Thread.currentThread().getId();
    private final long id;
    private final long timeStamp = new Date().getTime();
    private transient final String magicNumber;
    private final String previousHash;
    private final String currentHash;

    private final Map<String, List<HashMap<String, String>>> blockData = new HashMap<>();
    private final long elapsedSeconds;

    public Block(Block previousBlock, int hashZeros) {
        Pair<String, String> magicHashPair = StringUtils.generateRandomHash(hashZeros);

        if (previousBlock == null) {
            id = 1;
            previousHash = "0";
        } else {
            id = previousBlock.getId() + 1;
            previousHash = previousBlock.getCurrentHash();
        }

        magicNumber = magicHashPair.getKey();
        currentHash = magicHashPair.getValue();
        elapsedSeconds = (new Date().getTime() - timeStamp) / 1000;
    }

    boolean acceptData(List<Map<String, String>> serviceData) {
        if (previousHash.equals("0") || serviceData.isEmpty()) {
            return false;
        }

        blockData.putAll(serviceData.stream().collect(
            Collectors.groupingBy(m -> (String) m.keySet().toArray()[0],
                Collectors.flatMapping(
                        (Function<Map<String, String>, Stream<HashMap<String, String>>>) m -> m.values().stream().map(
                                (Function<String, HashMap<String, String>>) v ->
                                        new HashMap<>() {{
                                            put("id", StringUtils.generateRandomString(32));
                                            put("message", v);
                                        }}),
                        Collectors.toList()))));

        return true;
    }

    public long getId() {
        return id;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public Map<String, List<HashMap<String, String>>> getBlockData() {
        return blockData;
    }

    public long getMinerId() {
        return minerId;
    }

    @Override
    public String toString() {
        return "Block:\n" +
                "Created by miner # " + minerId + "\n" +
                "Miner # " + minerId + " gets 100 VC\n" +
                "Id: " + id + "\n" +
                "Timestamp: " + timeStamp + "\n" +
                "Magic number: " + magicNumber + "\n" +
                "Hash of the previous block:\n" + previousHash + "\n" +
                "Hash of the block:\n" + currentHash + "\n" +
                "Block data: " + (blockData.isEmpty() ? "no messages" : "\n" + blockData) + "\n" +
                "Block was generating for " + elapsedSeconds + " seconds\n";
    }
}