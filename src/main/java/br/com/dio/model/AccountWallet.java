package br.com.dio.model;

import lombok.Getter;

import java.util.List;

@Getter
public class AccountWallet extends Wallet {

    private final List<String> pix;

    public AccountWallet(List<String> pix) {
        super(BankService.ACCOUNT);
        this.pix = pix;
    }

    public AccountWallet(final long amount, List<String> pix) {
        super(BankService.ACCOUNT);
        this.pix = pix;
        addMoney(amount, "Valor de criação da conta");
    }

    public void addMoney(final long amount, final String description) {
        var money = generateMoney(amount, description);
        this.money.addAll(money);
    }
}
