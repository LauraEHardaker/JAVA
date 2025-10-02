import java.awt.*;
import javax.swing.*;

public class BankingGUI {
    private final Banking bankingApp;   // backend logic
    private JFrame frame;
    private JTextArea outputArea;

    public BankingGUI(Banking app) {
        this.bankingApp = app;
        showLoginScreen();
    }

    // ----------------- LOGIN SCREEN -----------------
    private void showLoginScreen() {
        frame = new JFrame("Banking App - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 280);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("Welcome to Banking App");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(userLabel, gbc);

        JTextField userField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(passField, gbc);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        JLabel status = new JLabel(" ");
        status.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(status, gbc);

        // ---- Login action
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (bankingApp.login(username, password)) {
                frame.dispose();
                showMainMenu();
            } else {
                status.setText("Invalid username or password.");
            }
        });

        // ---- Register action
        registerBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(frame, "Enter new username:");
            if (username == null || username.isBlank()) return;
            String password = JOptionPane.showInputDialog(frame, "Enter new password:");
            if (password == null || password.isBlank()) return;

            String result = bankingApp.registerUser(username.trim(), password.trim());
            JOptionPane.showMessageDialog(frame, result);
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    // ----------------- MAIN MENU -----------------
    private void showMainMenu() {
        frame = new JFrame("Banking App - Main Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 500);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JButton createAccBtn = new JButton("Create Account");
        JButton listAccBtn = new JButton("List Accounts");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        JButton logoutBtn = new JButton("Logout");

        JButton[] buttons = {createAccBtn, listAccBtn, depositBtn, withdrawBtn, transferBtn, logoutBtn};
        for (JButton b : buttons) b.setFont(new Font("Arial", Font.PLAIN, 14));

        // ---- Actions
        createAccBtn.addActionListener(e -> {
            String[] options = {"Small Business", "Community", "Client"};
            int choice = JOptionPane.showOptionDialog(frame,
                    "Choose account type",
                    "Create Account",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);
            if (choice >= 0) {
                int twoSigChoice = JOptionPane.showConfirmDialog(frame,
                        "Does this account require two signatories?", 
                        "Two Signatories", 
                        JOptionPane.YES_NO_OPTION);
                    boolean twoSignatories = (twoSigChoice == JOptionPane.YES_OPTION);
                    String secondSignatory = null;

                    if (twoSignatories) {
                        secondSignatory = JOptionPane.showInputDialog(frame, "Enter second signatory name:");
                        if (secondSignatory == null || secondSignatory.isBlank()) {
                            outputArea.append("Second signatory name cannot be empty.\n");
                            return; 
                        }
                    }
                String result = bankingApp.createAccount(choice + 1, twoSignatories, secondSignatory); 
                outputArea.append(result + "\n");
            } });

        listAccBtn.addActionListener(e -> {
            String accountsList = bankingApp.getAccountsList();
            outputArea.append(accountsList + "\n");
        });

        depositBtn.addActionListener(e -> {
            String accNo = JOptionPane.showInputDialog(frame, "Enter account number:");

            String amtStr = JOptionPane.showInputDialog(frame, "Enter deposit amount:");
            try {
                double amount = Double.parseDouble(amtStr);
                boolean ok = bankingApp.deposit(accNo, amount);
                outputArea.append(ok ? "Deposited £" + amount + " into " + accNo + "\n"
                        : "Deposit failed. Check account.\n");
            } catch (Exception ex) {
                outputArea.append("Invalid input for deposit.\n");
            }
        });

        withdrawBtn.addActionListener(e -> {
            String accNo = JOptionPane.showInputDialog(frame, "Enter account number:");
            String amtStr = JOptionPane.showInputDialog(frame, "Enter withdrawal amount:");
            try {
                double amount = Double.parseDouble(amtStr);
                boolean ok = bankingApp.withdraw(accNo, amount);
                outputArea.append(ok ? "Withdrew £" + amount + " from " + accNo + "\n"
                        : "Withdrawal failed. Check balance/overdraft. May require permission\n");
            } catch (Exception ex) {
                outputArea.append("Invalid input for withdrawal.\n");
            }
        });

        transferBtn.addActionListener(e -> {
            String fromAcc = JOptionPane.showInputDialog(frame, "Enter FROM account number:");
            String toAcc = JOptionPane.showInputDialog(frame, "Enter TO account number:");
            String amtStr = JOptionPane.showInputDialog(frame, "Enter transfer amount:");
            try {
                double amount = Double.parseDouble(amtStr);
                boolean ok = bankingApp.transfer(fromAcc, toAcc, amount);
                outputArea.append(ok ? "Transferred £" + amount + " from " + fromAcc + " to " + toAcc + "\n"
                        : "Transfer failed. Check accounts/balance.\n");
            } catch (Exception ex) {
                outputArea.append("Invalid input for transfer.\n");
            }
        });

        logoutBtn.addActionListener(e -> {
            bankingApp.logout();
            frame.dispose();
            showLoginScreen();
        });

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.add(createAccBtn);
        buttonPanel.add(listAccBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(transferBtn);
        buttonPanel.add(logoutBtn);

        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    // ----------------- MAIN -----------------
    public static void main(String[] args) {
        Banking app = new Banking();
        SwingUtilities.invokeLater(() -> new BankingGUI(app));
    }
}
