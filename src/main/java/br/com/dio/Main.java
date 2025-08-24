package br.com.dio;

import br.com.dio.exception.AccountNotFoundException;
import br.com.dio.exception.AccountWithInvestmentException;
import br.com.dio.exception.NoFundsEnoughException;
import br.com.dio.exception.WalletNotFoundException;
import br.com.dio.model.AccountWallet;
import br.com.dio.repository.AccountRepository;
import br.com.dio.repository.InvestmentRepository;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private final static AccountRepository accountRepository = new AccountRepository();
    private final static InvestmentRepository investmentRepository = new InvestmentRepository();

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Olá, seja bem vindo ai DIO Bank.");
        while (true) {
            System.out.println("Selecione a operação desejada");
            System.out.println("1 - Criar uma conta");
            System.out.println("2 - Criar um investimento");
            System.out.println("3 - Fazer um investimento");
            System.out.println("4 - Depositar na conta");
            System.out.println("5 - Sacar da conta");
            System.out.println("6 - Transferência entre contas");
            System.out.println("7 - Investir");
            System.out.println("8 - Sacar investimento");
            System.out.println("9 - Listar contas");
            System.out.println("10 - Listar investimentos");
            System.out.println("11 - Listar carteiras de investimento");
            System.out.println("12 - Atualizar investimentos");
            System.out.println("13 - Histórico de conta");
            System.out.println("14 - Sair");

            var option = scanner.nextInt();
            switch (option) {
                case 1 -> createAccount();
                case 2 -> createInvestment();
                case 3 -> createWallerInvestment();
                case 4 -> deposit();
                case 5 -> withdraw();
                case 6 -> transferBetweenAccounts();
                case 7 -> incInvestment();
                case 8 -> rescueInvestment();
                case 9 -> accountRepository.list().forEach(System.out::println);
                case 10 -> investmentRepository.list().forEach(System.out::println);
                case 11 -> investmentRepository.listWallets().forEach(System.out::println);
                case 12 -> {
                    investmentRepository.updateAmount();
                    System.out.println("Investimentos atualizados.");
                }
                case 13 -> checkHistory();
                case 14 -> System.exit(0);
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void createAccount() {
        System.out.println("Informe as chaves pix separadas por ';'");
        var pix = Arrays.stream(scanner.next().split(";")).toList();
        System.out.println("Informe o valor inicial de deposito");
        var amount = scanner.nextLong();
        var wallet = accountRepository.create(pix, amount);
        System.out.println("Conta criada: " + wallet);
    }

    private static void createInvestment() {
        System.out.println("Informe a taxa do investimento");
        var tax = scanner.nextInt();
        System.out.println("Informe o valor inicial de deposito");
        var initialFunds = scanner.nextLong();
        var investment = investmentRepository.create(tax, initialFunds);
        System.out.println("Investimento criado: " + investment);
    }

    private static void deposit() {
        System.out.println("Informe a chave pix da conta para deposito");
        var pix = scanner.next();
        System.out.println("Informe o valor do deposito");
        var amount = scanner.nextLong();
        try {
            accountRepository.deposit(pix, amount);
            System.out.println("Deposito realizado");
        } catch (AccountNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void withdraw() {
        System.out.println("Informe a chave pix da conta para saque");
        var pix = scanner.next();
        System.out.println("Informe o valor do saque");
        var amount = scanner.nextLong();
        try {
            accountRepository.withdraw(pix, amount);
            System.out.println("Saque realizado");
        } catch (AccountNotFoundException | NoFundsEnoughException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void transferBetweenAccounts() {
        System.out.println("Informe a chave pix da conta de origem");
        var source = scanner.next();
        System.out.println("Informe a chave pix da conta de destino");
        var target = scanner.next();
        System.out.println("Informe o valor da transferência");
        var amount = scanner.nextLong();
        try {
            accountRepository.transferMoney(source, target, amount);
            System.out.println("Transferência realizada");
        } catch (AccountNotFoundException | NoFundsEnoughException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void createWallerInvestment() {
        System.out.println("Informe a chave pix da conta");
        var pix = scanner.next();
        var account = accountRepository.findByPix(pix);
        System.out.println("Informe o identificador do investimento");
        var investmentId = scanner.nextInt();
        try {
            var investmentWallet = investmentRepository.initInvestment(account, investmentId);
            System.out.println("Conta de investimento criada: " + investmentWallet);
        } catch (AccountWithInvestmentException | NoFundsEnoughException | WalletNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void incInvestment() {
        System.out.println("Informe a chave pix da conta para investimento");
        var pix = scanner.next();
        System.out.println("Informe o valor do investimento");
        var amount = scanner.nextLong();
        try {
            var investmentWallet = investmentRepository.deposit(pix, amount);
            System.out.println("Investimento realizado");
        } catch (WalletNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void rescueInvestment() {
        System.out.println("Informe a chave pix da conta para resgatar o investimento");
        var pix = scanner.next();
        System.out.println("Informe o valor a ser resgatado");
        var amount = scanner.nextLong();
        try {
            var investmentWallet = investmentRepository.withdraw(pix, amount);
            System.out.println("Resgate realizado");
        } catch (WalletNotFoundException | NoFundsEnoughException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void checkHistory() {
        AccountWallet wallet;

        System.out.println("Informe a chave pix da conta para verificar o extrato");
        var pix = scanner.next();
        try {
            wallet = accountRepository.findByPix(pix);
            var audit = wallet.getFinancialTransactions();
            var group = audit.stream()
                                .collect(
                                        Collectors.groupingBy(
                                                t -> t.createdAt()
                                                                    .truncatedTo(ChronoUnit.SECONDS)
                                        )
                                );
            System.out.println(audit);
        } catch (AccountNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }
}