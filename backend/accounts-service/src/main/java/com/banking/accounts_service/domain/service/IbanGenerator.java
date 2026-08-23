package com.banking.accounts_service.domain.service;

import java.security.SecureRandom;

public class IbanGenerator {

    private final SecureRandom random;
    private final String countryCode;
    private final String bankCode;

    public IbanGenerator(String countryCode, String bankCode) {
        this.random = new SecureRandom();
        this.countryCode = normalizeLetters(countryCode, 2);
        this.bankCode = normalizeAlnum(bankCode, 4);
    }

    public String generate() {
        StringBuilder bban = new StringBuilder();
        bban.append(bankCode);
        bban.append(randomDigits(14));

        String checkDigits = "00";
        String provisional = countryCode + checkDigits + bban;

        int mod = mod97(toNumeric(provisional.substring(4) + provisional.substring(0, 4)));
        int cd = 98 - mod;
        return countryCode + (cd < 10 ? "0" + cd : String.valueOf(cd)) + bban;
    }

    private String randomDigits(int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String normalizeLetters(String s, int len) {
        String v = (s == null ? "" : s.trim().toUpperCase());
        if (v.length() < len) {
            v = ("XX" + v).substring(v.length());
        }
        return v.substring(0, len).replaceAll("[^A-Z]", "X");
    }

    private static String normalizeAlnum(String s, int len) {
        String v = (s == null ? "" : s.trim().toUpperCase()).replaceAll("[^A-Z0-9]", "0");
        if (v.length() < len) {
            v = ("0000" + v).substring(v.length());
        }
        return v.substring(0, len);
    }

    private static String toNumeric(String iban) {
        StringBuilder out = new StringBuilder(iban.length() * 2);
        for (int i = 0; i < iban.length(); i++) {
            char c = iban.charAt(i);
            if (c >= '0' && c <= '9') {
                out.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                out.append((c - 'A') + 10);
            }
        }
        return out.toString();
    }

    private static int mod97(String numeric) {
        int remainder = 0;
        for (int i = 0; i < numeric.length(); i++) {
            int digit = numeric.charAt(i) - '0';
            remainder = (remainder * 10 + digit) % 97;
        }
        return remainder;
    }
}
