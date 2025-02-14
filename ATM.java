import java.io.*;
import java.util.*;

abstract class AbstractUser {
    protected String accountNumber;
    protected String pin;
    protected double balance;
    protected List<String> transactions;

    public AbstractUser(String accountNumber, String pin, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.transactions = new LinkedList<>();
    }

    public abstract void deposit(double amount);
    public abstract boolean withdraw(double amount);
}

class User extends AbstractUser implements Serializable {
    private StringBuffer accountHolder;
    private StringBuilder transactionHistory;

    public User(String accountNumber, String pin, double balance) {
        super(accountNumber, pin, balance);
        this.accountHolder = new StringBuffer("User");
        this.transactionHistory = new StringBuilder();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getPin() { return pin; }
    public double getBalance() { return balance; }
    public List<String> getTransactions() { return transactions; }
    public StringBuffer getAccountHolder() { return accountHolder; }

    @Override
    public void deposit(double amount) {
        balance += amount;
        transactions.add("Deposited: $" + amount);
        transactionHistory.append("Deposited: $").append(amount).append("\n");
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            transactions.add("Withdrew: $" + amount);
            transactionHistory.append("Withdrew: $").append(amount).append("\n");
            return true;
        }
        return false;
    }
    
    public void changePin(String newPin) {
        this.pin = newPin;
    }
    
    public void displayTransactionHistory() {
        System.out.println(transactionHistory.toString());
    }
}

class FileHandler {
    private static final String FILE_NAME = "users.dat";

    public static Map<String, User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    public static void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}

class ATM {
    private static Map<String, User> users;

    public static void main(String[] args) {
        users = FileHandler.loadUsers();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nWelcome to the ATM");
            System.out.println("1. Create Account\n2. Login\n3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    createAccount(scanner);
                    break;
                case 2:
                    authenticateUser(scanner);
                    break;
                case 3:
                    System.out.println("Thank you for using our ATM. Goodbye!");
                    scanner.close();
                    FileHandler.saveUsers(users);
                    System.gc(); 
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void createAccount(Scanner scanner) {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();
        System.out.print("Enter Initial Balance: ");
        double balance = scanner.nextDouble();
        scanner.nextLine();

        users.put(accountNumber, new User(accountNumber, pin, balance));
        System.out.println("Account successfully created!");
    }

    private static void authenticateUser(Scanner scanner) {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();

        User user = users.get(accountNumber);
        if (user != null && user.getPin().equals(pin)) {
            System.out.println("Login successful!");
            showMenu(scanner, user);
        } else {
            System.out.println("Authentication failed.");
        }
    }

    private static void showMenu(Scanner scanner, User user) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n1. Balance Inquiry\n2. Deposit\n3. Withdraw\n4. Transaction History\n5. Change PIN\n6. Delete Account\n7. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Current Balance: $" + user.getBalance());
                    break;
                case 2:
                    System.out.print("Enter deposit amount: ");
                    double depositAmount = scanner.nextDouble();
                    user.deposit(depositAmount);
                    System.out.println("Deposit successful!");
                    break;
                case 3:
                    System.out.print("Enter withdrawal amount: ");
                    double withdrawalAmount = scanner.nextDouble();
                    if (user.withdraw(withdrawalAmount)) {
                        System.out.println("Withdrawal successful!");
                    } else {
                        System.out.println("Insufficient funds!");
                    }
                    break;
                case 4:
                    System.out.println("Transaction History:");
                    user.displayTransactionHistory();
                    break;
                case 5:
                    System.out.print("Enter new PIN: ");
                    String newPin = scanner.nextLine();
                    user.changePin(newPin);
                    System.out.println("PIN changed successfully!");
                    break;
                case 6:
                    System.out.print("Are you sure you want to delete this account? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) {
                        users.remove(user.getAccountNumber());
                        System.out.println("Account deleted successfully.");
                        exit = true;
                        System.gc(); 
                    }
                    break;
                case 7:
                    exit = true;
                    System.out.println("Logging out...");
                    System.gc(); 
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
