package ilia.nemankov.steamscan.configuration.proxy;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Proxy {

    private String address;
    private int port;

    public Proxy(String string) {
        String[] parts = string.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("String " + string + " has unknown format.");
        }
        address = parts[0];
        port = Integer.parseInt(parts[1]);
    }

}
