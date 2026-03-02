package auth;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static volatile AccountManager instance;
    private final List<Account> accounts;

    private AccountManager() {
        this.accounts = new ArrayList<>();
    }

    public static AccountManager getInstance() {
        if (instance == null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager();
                }
            }
        }
        return instance;
    }

    // Проверка на существующий email перед добавлением
    public synchronized boolean register(String email, String password, String gender) {
        if (email == null || email.isEmpty() || isUserExists(email)) {
            return false;
        }
        accounts.add(new Account(email, password, gender));
        return true;
    }

    public synchronized boolean login(String email, String password) {
        return accounts.stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(email) && a.getPassword().equals(password));
    }

    private boolean isUserExists(String email) {
        return accounts.stream().anyMatch(a -> a.getEmail().equalsIgnoreCase(email));
    }
}
