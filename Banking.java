import java.io.*;
import java.util.*;

abstract class Account {
    protected String accountNumber;
    protected double balance;
    protected double overdraftLimit;
    protected boolean twoSignatories;
    protected String secondSignatory;

    public Account(String accountNumber, double overdraftLimit) {
        this.accountNumber = accountNumber;
        this.overdraftLimit = overdraftLimit;
        this.balance = 0.0;
        this.twoSignatories = false;
        this.secondSignatory = null;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getType() { return this.getClass().getSimpleName(); }
    public boolean requiresTwoSignatories() { return twoSignatories; }
    public String getSecondSignatory() { return secondSignatory; }

    // CORRECT: assign parameter -> field
    public void setSecondSignatory(String signatory) {
        this.secondSignatory = signatory;
        this.twoSignatories = signatory != null && !signatory.isEmpty();
    }

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance - amount >= -overdraftLimit) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public boolean transfer(Account target, double amount) {
        if (this == target) return false;
        if (amount > 0 && balance - amount >= -overdraftLimit) {
            this.balance -= amount;
            target.balance += amount;
            return true;
        }
        return false;
    }
}

// Account Types
class SmallBusinessAccount extends Account { public SmallBusinessAccount(String accNo) { super(accNo, 1000.0); } }
class CommunityAccount extends Account     { public CommunityAccount(String accNo) { super(accNo, 2500.0); } }
class ClientAccount extends Account        { public ClientAccount(String accNo) { super(accNo, 1500.0); } }

public class Banking {
    private static final String USERS_FILE = "users.csv";
    private static final String ACCOUNTS_FILE = "accounts.csv";

    private Map<String, Account> accounts = new HashMap<>();
    private int accountCounter = 1001;
    private String loggedInUser = null;

    public Banking() {
        ensureFilesExist();
        // don't load accounts here — loggedInUser is null now; accounts will be loaded after login
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void ensureFilesExist() {
        try {
            File users = new File(USERS_FILE);
            if (!users.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(users))) {
                    pw.println("username,password");
                    pw.println("admin,1234");
                }
            }

            File accountsFile = new File(ACCOUNTS_FILE);
            if (!accountsFile.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(accountsFile))) {
                    pw.println("username,accountNumber,type,balance,twoSignatories,secondSignatory");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public boolean login(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 2 &&
                    parts[0].trim().equals(username.trim()) &&
                    parts[1].trim().equals(password.trim())) {
                    loggedInUser = username.trim();
                    loadAccounts();   // load accounts for this user now that loggedInUser set
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void logout() {
        loggedInUser = null;
        accounts.clear();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String registerUser(String username, String password) {
        if (usernameExists(username)) return "Username already exists.";
        try (FileWriter fw = new FileWriter(USERS_FILE, true)) {
            fw.write(username + "," + password + "\n");
            return "User registered: " + username;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error registering user.";
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private boolean usernameExists(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 1 && parts[0].equals(username)) return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Load accounts for the currently logged-in user
    private void loadAccounts() {
        accounts.clear();
        accountCounter = Math.max(1001, accountCounter); // keep baseline

        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                // preserve trailing empty fields
                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                String username = parts[0];
                if (loggedInUser == null || !username.equals(loggedInUser)) continue;

                String accNo = parts[1];
                String type = parts[2];
                double balance = 0.0;
                try { balance = Double.parseDouble(parts[3]); } catch (NumberFormatException ignored) {}
                boolean twoSignatories = Boolean.parseBoolean(parts[4]);
                String secondSignatory = parts[5].isEmpty() ? null : parts[5];

                Account acc = switch (type) {
                    case "SmallBusinessAccount" -> new SmallBusinessAccount(accNo);
                    case "CommunityAccount"     -> new CommunityAccount(accNo);
                    case "ClientAccount"        -> new ClientAccount(accNo);
                    default -> null;
                };

                if (acc != null) {
                    acc.balance = balance;
                    if (twoSignatories) acc.setSecondSignatory(secondSignatory);
                    accounts.put(accNo, acc);

                    // ensure accountCounter continues from highest ACC number
                    try {
                        int n = Integer.parseInt(accNo.replaceFirst("^ACC", ""));
                        accountCounter = Math.max(accountCounter, n + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // debug: print loaded accounts to console
        System.out.println("=== Loading accounts for " + loggedInUser + " (" + accounts.size() + ") ===");
        for (Account a : accounts.values()) {
            System.out.println("  " + a.getAccountNumber() + " " + a.getType() + " bal=" + a.getBalance() +
                               (a.requiresTwoSignatories() ? " | joint:" + a.getSecondSignatory() : ""));
        }
    }

    // Save all users' accounts (merge current user into file)
    private void saveAccounts() {
        Map<String, List<Account>> allAccounts = new HashMap<>();

        // read existing file into the map (use split with -1)
        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                String username = parts[0];
                String accNo = parts[1];
                String type = parts[2];
                double balance = 0.0;
                try { balance = Double.parseDouble(parts[3]); } catch (NumberFormatException ignored) {}
                boolean twoSignatories = Boolean.parseBoolean(parts[4]);
                String secondSignatory = parts[5].isEmpty() ? null : parts[5];

                Account acc = switch (type) {
                    case "SmallBusinessAccount" -> new SmallBusinessAccount(accNo);
                    case "CommunityAccount"     -> new CommunityAccount(accNo);
                    case "ClientAccount"        -> new ClientAccount(accNo);
                    default -> null;
                };

                if (acc != null) {
                    acc.balance = balance;
                    if (twoSignatories) acc.setSecondSignatory(secondSignatory);
                    allAccounts.computeIfAbsent(username, k -> new ArrayList<>()).add(acc);
                }
            }
        } catch (IOException e) {
            // file may not exist yet; we'll create it when writing
        }

        // replace this user's group with the current in-memory accounts
        if (loggedInUser != null) {
            allAccounts.put(loggedInUser, new ArrayList<>(accounts.values()));
        }

        // now write everything back to the CSV
        try (PrintWriter pw = new PrintWriter(new FileWriter(ACCOUNTS_FILE))) {
            pw.println("username,accountNumber,type,balance,twoSignatories,secondSignatory");
            for (Map.Entry<String, List<Account>> entry : allAccounts.entrySet()) {
                String user = entry.getKey();
                for (Account acc : entry.getValue()) {
                    pw.println(user + "," +
                               acc.getAccountNumber() + "," +
                               acc.getType() + "," +
                               acc.getBalance() + "," +
                               acc.requiresTwoSignatories() + "," +
                               (acc.getSecondSignatory() != null ? acc.getSecondSignatory() : ""));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create account
    public String createAccount(int type, boolean twoSignatories, String secondSignatory) {
        if (loggedInUser == null) return "Not logged in.";

        boolean exists = accounts.values().stream()
            .anyMatch(acc -> (type == 1 && acc instanceof SmallBusinessAccount) ||
                             (type == 2 && acc instanceof CommunityAccount) ||
                             (type == 3 && acc instanceof ClientAccount));
        if (exists) return "Account of this type already exists.";

        String accNo = "ACC" + accountCounter++;
        Account acc = switch (type) {
            case 1 -> new SmallBusinessAccount(accNo);
            case 2 -> new CommunityAccount(accNo);
            case 3 -> new ClientAccount(accNo);
            default -> null;
        };
        if (acc != null) {
            if (twoSignatories) acc.setSecondSignatory(secondSignatory);
            accounts.put(accNo, acc);

            // persist and keep in-memory consistent
            saveAccounts();
            // no need to call loadAccounts() — accounts already up-to-date in-memory

            return "Created " + acc.getType() + " account: " + accNo +
                   (twoSignatories ? " | Joint account with second signatory: " + secondSignatory : "");
        }
        return "Invalid account type.";
    }

    public String getAccountsList() {
        if (accounts.isEmpty()) return "No accounts created.";
        StringBuilder sb = new StringBuilder();
        accounts.forEach((k, v) -> sb.append(k)
            .append(" | Type: ").append(v.getType())
            .append(" | Balance: £").append(v.getBalance())
            .append(v.requiresTwoSignatories() ? " | Joint (second: " + v.getSecondSignatory() + ")" : "")
            .append("\n"));
        return sb.toString();
    }

    public boolean deposit(String accNo, double amount) {
        Account acc = accounts.get(accNo);
        if (acc != null) {
            acc.deposit(amount);
            saveAccounts();
            return true;
        }
        return false;
    }

    public boolean withdraw(String accNo, double amount) {
        Account acc = accounts.get(accNo);
        if (acc != null) {
            // restriction: block withdraw if joint (requires second signatory approval)
            if (acc.requiresTwoSignatories()) return false;
            if (acc.withdraw(amount)) {
                saveAccounts();
                return true;
            }
        }
        return false;
    }

    public boolean transfer(String fromAcc, String toAcc, double amount) {
        Account from = accounts.get(fromAcc);
        Account to = accounts.get(toAcc);
        if (from == null || to == null) return false;

        // restriction: block if either account is joint
        if (from.requiresTwoSignatories() || to.requiresTwoSignatories()) return false;

        if (from.transfer(to, amount)) {
            saveAccounts();
            return true;
        }
        return false;
    }
}
