package ipc_server.utils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ServerUtils {
    /**
     * Reads a list of pokemons from give file path.
     *
     * @param filePath - Path to file containing pokemon list.
     * @return List<String>
     */
    public static List<String> readPokemons(String filePath) {
        FileReader fr = null;
        try {
            fr = new FileReader(filePath);
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        List<String> pokemons = null;

        if (fr != null) {
            pokemons = new ArrayList<>();
            BufferedReader br = new BufferedReader(fr);

            String line = "";
            do {
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }

                if (line != null) {
                    line = line.trim();
                    String pokemonName = line.split(",")[1];
                    pokemons.add(pokemonName);
                }
            }
            while (line != null);
        }

        return pokemons;
    }

    /**
     * Returns the IP address of current node.
     *
     * @return String
     */
    public static String getAddress() {
        String ipAddress = "";
        try {
            for (final Enumeration<NetworkInterface> interfaces
                 = NetworkInterface.getNetworkInterfaces();
                 interfaces.hasMoreElements();)
            {
                final NetworkInterface cur = interfaces.nextElement();

                if ( cur.isLoopback() )
                    continue;

                if (!(cur.getDisplayName().startsWith("w") || cur.getDisplayName().startsWith("e")))
                    continue;

                for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
                    final InetAddress inetAddr = addr.getAddress();

                    if (!(inetAddr instanceof Inet4Address))
                        continue;

                    ipAddress += inetAddr.getHostAddress() + " ";
                }

            }
        }
        catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
            e.printStackTrace();
        }

        return ipAddress;
    }
}
