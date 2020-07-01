public class ChatService {
    private static final String serviceId = "dUaKL3CEiFx6mQB8mBTf";
    private static final String signature =
            "Q2hhdFNlcnZpY2U=#MDZjNDU5OTg4M2VmYzVmMGE1MzUzZDg3Mzk4ZWZjZTAwM2UwN2I4NWIxYTBlMDA2OTE5OTI1ZDgzNmY2N2Q1Yg==";

    private final Blockchain instance;

    ChatService(Blockchain bc) {
        instance = bc;
    }

    void receiveMessage(String message) {
        long author = Thread.currentThread().getId();
        instance.acceptServiceData(serviceId, signature, author + ": " + message);
    }
}
