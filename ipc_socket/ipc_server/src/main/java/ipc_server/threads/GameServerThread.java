package ipc_server.threads;


import ipc_server.GameServer;

import java.io.*;
import java.net.Socket;

public class GameServerThread extends Thread {
    /**
     * Associated client socket.
     */
    private final Socket clientSocket;

    /**
     * Associated game server.
     */
    private final GameServer gameServer;

    /**
     * Class wide buffered writer.
     */
    private BufferedWriter bw;

    /**
     * Constructor initializing client socket and game server.
     *
     * @param clientSocket - Associated client socket
     * @param gameServer - Associated game server
     */
    public GameServerThread(Socket clientSocket, GameServer gameServer) {
        this.clientSocket = clientSocket;
        this.gameServer = gameServer;
    }

    @Override
    public void run() {
        // Setup input streams
        InputStream is = null;
        try {
            is = clientSocket.getInputStream();
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage() + " Try again!");
            // TODO: Kill thread instead of killing the program
            System.exit(1);
        }
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        // Set output streams
        OutputStream os = null;
        try {
            os = clientSocket.getOutputStream();
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage() + " Try again!");
            // TODO: Kill thread instead of killing the program
            System.exit(1);
        }
        OutputStreamWriter osw = new OutputStreamWriter(os);
        bw = new BufferedWriter(osw);

        // Get ID for player
        int playerID = -1;

        String command;
        String[] commandParts = {""};

        do {
            try {
                command = br.readLine();
            }
            catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage() + " Try again");
                continue;
            }

            // We are doing this because as the winner is found the server thread
            // should not service anymore requests.
            if (gameServer.isWinnerFound()) {
                sendMsg("Games over on this server. Please wait for server to restart.");
                command = "terminate";
            }

            int ret;
            commandParts = command.split(" ");
            switch(commandParts[0]) {
                case "move":
                    if (playerID != -1) {
                        String direction = commandParts[1];
                        int spaces = Integer.parseInt(commandParts[2]);

                        ret = gameServer.movePlayer(playerID, direction, spaces);

                        String msg = "";
                        switch(ret) {
                            case 1: msg = "Player does not exist."; break;
                            case 2: msg = "Can't move there. Position beyond board limits."; break;
                            case 3: msg = "Can't move there. Position occupied by another player."; break;
                            default: msg = "Can't move to this position."; break;
                        }

                        if (ret != 0) {
                            sendMsg(msg);
                        } else {
                            String playerDetails = gameServer.getPlayerDetails(playerID);
                            playerDetails = playerDetails.replaceAll("\n", "~");
                            sendMsg(playerDetails);
                        }
                    }
                    else {
                        sendMsg("Join a game before using this command");
                    }
                    break;

                case "capture":
                    if (playerID != -1) {
                        ret = gameServer.capturePokemon(playerID);

                        if (ret == 1) {
                            sendMsg("No pokemon at position");
                        }
                        else if (ret == 2) {
                            String playerDetails = gameServer.getPlayerDetails(playerID);
                            playerDetails += "\n" + "You have won the game! Congratulations!";
                            playerDetails = playerDetails.replaceAll("\n", "~");
                            sendMsg(playerDetails);

                            // Force thread to quit
                            commandParts[0] = "leave";
                        }
                        else {
                            String playerDetails = gameServer.getPlayerDetails(playerID);
                            playerDetails = playerDetails.replaceAll("\n", "~");

                            sendMsg(playerDetails);
                        }
                    }
                    else {
                        sendMsg("Join a game before using this command");
                    }
                    break;

                case "show":
                    if (playerID != -1) {
                        String playerDetails = gameServer.getPlayerDetails(playerID);
                        playerDetails = playerDetails.replaceAll("\n", "~");
                        sendMsg(playerDetails);
                    }
                    else {
                        sendMsg("Join a game before using this command");
                    }
                    break;

                case "join":
                    if (playerID == -1) {
                        // Add player
                        playerID = gameServer.addPlayer(this);

                        // Get details of board and players
                        String gameBoardDetails = gameServer.getBoardDetails();
                        String playerDetails = gameServer.getPlayerDetails(playerID);
                        String joinInfo = gameBoardDetails + "\n" + playerDetails;
                        joinInfo = joinInfo.replaceAll("\n", "~");

                        sendMsg(joinInfo);
                    }
                    else {
                        sendMsg("Player already joined");
                    }
                    break;

                case "leave":
                    if (playerID != -1) {
                        gameServer.removePlayer(playerID);
                    }

                    sendMsg("Player left");
                    break;

                case "terminate":
                    // Forces an exit when winner has been found.
                    commandParts[0] = "leave";
                    break;

                default:
                    sendMsg("Server does not support this command yet");
                    break;
            }

        }
        while(!commandParts[0].equalsIgnoreCase("leave"));
    }

    /**
     * Sends given message to associated client.
     *
     * @param msg - Message to send
     */
    public void sendMsg(String msg) {
        try {
            bw.write(msg + "\n");
            bw.flush();
        }
        catch (IOException e) { }
    }

}
