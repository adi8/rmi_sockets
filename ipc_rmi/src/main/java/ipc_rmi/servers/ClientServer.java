package ipc_rmi.servers;

import ipc_rmi.interfaces.ClientServerInterface;
import ipc_rmi.interfaces.GameServerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ClientServer implements ClientServerInterface {

    /**
     * Start time of each call.
     */
    private static long callStart;

    /**
     * End time of each call.
     */
    private static long callEnd;

    /**
     * Tracks response time over all calls.
     */
    private static List<Long> responseTimes;

    /**
     * Terminates the client program after displaying the given
     * message.
     *
     * @param msg - Message to display.
     */
    public void terminate(String msg) {
        System.out.println(msg);

        long sum = 0;
        for (Long val : responseTimes) {
            sum += val;
        }
        double avgTime = (double) sum / responseTimes.size();
        System.out.println("\nAverage response time: " + avgTime);

        System.exit(0);
    }

    /**
     * Main function.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ./client <server-ip>");
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = 4001;

        Registry registry = null;
        GameServerInterface gameServerStub = null;
        try {
            registry = LocateRegistry.getRegistry(serverIP, serverPort);
            gameServerStub = (GameServerInterface) registry.lookup("ipc-rmi://game-server");
        }
        catch (RemoteException | NotBoundException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        // Create a stub for client
        ClientServer clientServer = new ClientServer();
        ClientServerInterface clientServerStub = null;
        try {
            clientServerStub = (ClientServerInterface) UnicastRemoteObject.exportObject(clientServer, 0);
        }
        catch (RemoteException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        // Setup input streams
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        // Setup response time list
        responseTimes = new ArrayList<>();

        int playerID = -1;
        long responseTime = 0;
        String command = "";
        String[] commandParts = command.split(" ");
        System.out.print("command > ");
        while (!commandParts[0].equalsIgnoreCase("leave")) {
            try {
                command = br.readLine();
            }
            catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage() + " Try again");
                System.out.print("\ncommand > ");
                continue;
            }

            commandParts = command.split(" ");

            try {
                switch (commandParts[0]) {
                    case "move":
                        // Check if player has joined the game
                        if (playerID == -1) {
                            System.out.println("Join a game before using this command");
                            break;
                        }

                        // Usage message for move
                        String moveUsgMsg = "Command Usage: move <direction> <steps>\n" +
                                "               direction: left|right|up|down\n" +
                                "               steps: integer";

                        // Check if appropriate number of arguments are passed
                        if (commandParts.length != 3) {
                            System.out.println(moveUsgMsg);
                            break;
                        }

                        boolean directionValid = false;
                        boolean stepsValid = false;

                        // Check if direction arugment is among left, right, top, down
                        String direction = commandParts[1];
                        switch (direction) {
                            case "down":
                            case "up":
                            case "right":
                            case "left":
                                directionValid = true;
                                break;
                        }

                        // Check if steps argument is an integer
                        int steps = 0;
                        try {
                            steps = Integer.parseInt(commandParts[2]);
                            stepsValid = true;
                        } catch (NumberFormatException e) { }

                        if (directionValid && stepsValid) {
                            callStart = System.nanoTime() ;

                            int ret = gameServerStub.movePlayer(playerID, direction, steps);

                            callEnd = System.nanoTime();

                            String msg = "";
                            if (ret != 0) {
                                switch (ret) {
                                    case 1:
                                        msg = "Player does not exist.";
                                        break;
                                    case 2:
                                        msg = "Can't move there. Position beyond board limits.";
                                        break;
                                    case 3:
                                        msg = "Can't move there. Position occupied by another player.";
                                        break;
                                    default:
                                        msg = "Can't move to this position.";
                                        break;
                                }
                                System.out.println(msg);
                            }
                            else {
                                String playerDetails = gameServerStub.getPlayerDetails(playerID);
                                System.out.println(playerDetails);

                                responseTime = (callEnd - callStart) / 1000000;
                                responseTimes.add(responseTime);
                                System.out.println("Call Time: " + responseTime + " ms");
                            }
                        } else {
                            System.out.println(moveUsgMsg);
                        }
                        break;

                    case "capture":
                        // Check if player has joined a game
                        if (playerID == -1) {
                            System.out.println("Join a game before using this command");
                            break;
                        }

                        String captureUsgMsg = "Command usage: capture";
                        if (commandParts.length != 1) {
                            System.out.println(captureUsgMsg);
                            break;
                        }

                        callStart = System.nanoTime();

                        int ret = gameServerStub.capturePokemon(playerID);

                        callEnd = System.nanoTime();

                        if (ret == 1) {
                            System.out.println("Couldn't capture pokemon. Try again");
                        }
                        else if (ret == 2) {
                            System.out.println("You have won the game! Congratulations!");

                            // Forcing client to quit
                            commandParts[0] = "leave";
                        }
                        System.out.println(gameServerStub.getPlayerDetails(playerID));

                        responseTime = (callEnd - callStart) / 1000000;
                        responseTimes.add(responseTime);
                        System.out.println("Call Time: " + responseTime + " ms");
                        break;

                    case "show":
                        // Check if player has joined a game
                        if (playerID == -1) {
                            System.out.println("Join a game before using this command");
                            break;
                        }

                        String showUsgMsg = "Command Usage: show";
                        if (commandParts.length != 1) {
                            System.out.println(showUsgMsg);
                            break;
                        }

                        callStart = System.nanoTime();

                        String playerDetails = gameServerStub.getPlayerDetails(playerID);

                        callEnd = System.nanoTime();

                        System.out.println(playerDetails);

                        responseTime = (callEnd - callStart) / 1000000;
                        responseTimes.add(responseTime);
                        System.out.println("Call Time: " + responseTime + " ms");
                        break;

                    case "join":
                        // Check if player has already joined the game
                        if (playerID != -1) {
                            System.out.println("Already joined a game");
                            break;
                        }

                        String joinUsgMsg = "Command Usage: join";
                        if (commandParts.length != 1) {
                            System.out.println(joinUsgMsg);
                            break;
                        }

                        callStart = System.nanoTime();

                        playerID = gameServerStub.addPlayer(clientServerStub);

                        callEnd = System.nanoTime();

                        System.out.println(gameServerStub.getBoardDetails() + "\n" +
                                           gameServerStub.getPlayerDetails(playerID));

                        responseTime = (callEnd - callStart) / 1000000;
                        responseTimes.add(responseTime);
                        System.out.println("Call Time: " + responseTime + " ms");
                        break;

                    case "leave":
                        // Check if player has joined a game
                        if (playerID == -1) {
                            System.out.println("Join a game before using this command");
                            break;
                        }

                        String leaveUsgMsg= "Command Usage: leave";
                        if (commandParts.length != 1) {
                            System.out.println(leaveUsgMsg);
                            break;
                        }

                        callStart = System.nanoTime();

                        gameServerStub.removePlayer(playerID);

                        callEnd = System.nanoTime();
                        System.out.println("Player left.");

                        responseTime = (callEnd - callStart) / 1000000;
                        responseTimes.add(responseTime);
                        System.out.println("Call Time: " + responseTime + " ms");
                        break;

                    default:
                        System.out.println("Command \'" + commandParts[0] + "\' not supported");
                        break;
                }

            }
            catch (RemoteException e) {
                System.out.println("ERROR: " + e.getMessage() + " Try again");
            }

            if (!commandParts[0].equalsIgnoreCase("leave"))
                System.out.print("\ncommand > ");
        }

        long sum = 0;
        for (Long val : responseTimes) {
            sum += val;
        }
        double avgTime = (double) sum / responseTimes.size();
        System.out.println("\nAverage response time: " + avgTime);

        try {
            UnicastRemoteObject.unexportObject(clientServer, true);
        }
        catch (NoSuchObjectException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}
