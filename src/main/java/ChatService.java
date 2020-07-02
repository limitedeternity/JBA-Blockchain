public class ChatService {
    private static final String serviceId = "dUaKL3CEiFx6mQB8mBTf";
    private static final String signature = "06c4599883efc5f0a5353d87398efce003e07b85b1a0e006919925d836f67d5b";

    private final Blockchain instance;

    ChatService(Blockchain bc) {
        instance = bc;
    }

    void receiveMessage(String message) {
        long author = Thread.currentThread().getId();
        instance.acceptServiceData(serviceId, signature, author + ": " + message);
    }
}
