package ipc_client.threads;

import ipc_client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {
    /**
     * Associated server socket.
     */
    private Socket serverSocket;

    /**
     * Constructor.
     *
     * @param serverSocket - Server socket associated with client
     */
    public ClientThread(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        InputStream is = null;
        try {
            is = serverSocket.getInputStream();
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);


        String recvdMsg = "";
        do {
            try {
                recvdMsg = br.readLine();
            }
            catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }

            Client.endTime = System.nanoTime();

            recvdMsg = recvdMsg.replaceAll("~", "\n");
            System.out.println(recvdMsg);

            long responseTime = (Client.endTime - Client.startTime) / 1000000;
            if (Client.startTime > 0) {
                Client.responseTimes.add(responseTime);
                System.out.println("Response Time: " + responseTime + " ms");

                // Set start time as negative to know whether a call was made
                // or are we being terminated because a winner was found
                Client.startTime = -1;
            }

            if (!recvdMsg.equalsIgnoreCase("Player left") &&
                !recvdMsg.contains("Thank you for playing") &&
                !recvdMsg.contains("Congratulations")) {
                System.out.print("\ncommand > ");
            }

        }
        while (!recvdMsg.equalsIgnoreCase("Player left") &&
               !recvdMsg.contains("Thank you for playing") &&
               !recvdMsg.contains("Congratulations"));

        double sum = 0;
        for (long val : Client.responseTimes) {
            sum += val;
        }

        double avgTime = sum / Client.responseTimes.size();
        System.out.println("\nAverage Response Time: " + avgTime);

        System.exit(0);
    }
}
