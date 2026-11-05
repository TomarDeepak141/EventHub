package eventHub.deepak.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class NotificationService {
    private final NotificationProvider provider;
    public NotificationService(NotificationProvider provider) { this.provider=provider; }
    public void afterCommit(NotificationMessage message) {
        Runnable notify=()->provider.send(message);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) notify.run();
        else TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { notify.run(); }
        });
    }
}
