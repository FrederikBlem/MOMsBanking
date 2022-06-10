package dk.fb.si.momsloanproducer.service;

import java.util.Random;

public class IdBuilder {
    private String builtId;

    //For building customer Id
    public void buildId(int len) {
        String characters = "!@#$%&ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(characters.charAt(random.nextInt(characters.length())));
        builtId = sb.toString();
    }

    public String getId() {
        buildId(20);
        return builtId;
    }
}