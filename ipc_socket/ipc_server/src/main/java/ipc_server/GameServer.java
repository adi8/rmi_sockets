package ipc_server;

import ipc_server.threads.GameServerThread;
import ipc_server.utils.GameBoard;
import ipc_server.utils.Position;
import ipc_server.utils.ServerUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServer {
    /**
     * Maximum number of players supported.
     */
    public static final int MAX_PLAYERS = 100;

    /**
     * Port on which server listens.
     */
    public static final int SERVER_PORT = 4001;

    /**
     * Game board of a server.
     */
    private GameBoard gameBoard;

    /**
     * Server threads that are currently active for
     * each player.
     */
    private Map<Integer, GameServerThread> serverThreadsMap;

    /**
     * Count of active players.
     */
    private int playerCount;

    /**
     * ID to be assigned to next player.
     */
    private int nextPlayerID;

    /**
     * Default constructor.
     */
    public GameServer() {
        playerCount = 0;
        nextPlayerID = 1;
        serverThreadsMap = new HashMap<>();
        initGameBoard();
    }

    /**
     * Initializes game board.
     */
    private void initGameBoard() {
        gameBoard = new GameBoard();
    }

    /**
     * Adds a player to player list along with the server thread that
     * provides service.
     *
     * @param gameServerThread - Thread servicing a client
     * @return int - Player ID
     */
    public int addPlayer(GameServerThread gameServerThread) {
        int ret;
        synchronized (this) {
            ret = nextPlayerID++;
            playerCount++;
            serverThreadsMap.put(ret, gameServerThread);
        }

        // Add player to game board
        synchronized (gameBoard) {
            gameBoard.addPlayer(ret);
        }
        return ret;
    }

    /**
     * Removes a player that wants to leave.
     *
     * @param id - Player ID
     */
    public void removePlayer(int id) {
        synchronized (this) {
            playerCount--;
            serverThreadsMap.remove(id);

            // Remove player from game board
            synchronized (gameBoard) {
                gameBoard.removePlayer(id);
            }
        }
    }

    /**
     * Moves a player in direction 'direction' by 'spaces' number
     * of spaces.
     *
     * @param id - Player ID
     * @param direction - Direction to move in
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Player does not exist
     *               2 - New position beyond board
     *               3 - New position occupied by another player
     */
    public int movePlayer(int id, String direction, int spaces) {
        int ret = 1;
        synchronized (gameBoard) {
            ret = gameBoard.move(id, direction, spaces);
        }

        return ret;
    }

    /**
     * Captures a pokemon present at a players position.
     *
     * @param id - Player ID
     * @return int - 0 - Success
     *               1 - Failure
     *               2 - Winner
     */
    public int capturePokemon(int id) {
        int ret = 1;
        synchronized (gameBoard) {
            ret = gameBoard.capturePokemon(id);

            if (gameBoard.isWinnerFound()) {
                ret = 2;
                List<Integer> tmp = new ArrayList<>();
                for (Map.Entry<Integer, GameServerThread> entry : serverThreadsMap.entrySet()) {
                    if (!entry.getKey().equals(id)) {
                        entry.getValue().sendMsg("Player-" + id + " has won the game. Thank you for playing!");

                        tmp.add(entry.getKey());
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Return true if winner has been found.
     *
     * @return - boolean
     */
    public boolean isWinnerFound() {
        boolean ret;
        synchronized (gameBoard) {
            ret = gameBoard.isWinnerFound();

        }
        return ret;
    }

    /**
     * Returns current state of a player.
     * @param id - Player ID
     * @return String - Player details
     */
    public String getPlayerDetails(int id) {
        String ret = "";
        synchronized (gameBoard) {
            Position playerPos = gameBoard.getPlayerPos(id);
            List<String> playerPokemons = gameBoard.getPlayerPokemons(id);
            ret = "Player ID: " + id + "\n" +
                  "Position: " + playerPos.toString() + "\n" +
                  "Captured Pokemons: " + playerPokemons.toString() + "\n" +
                  "Pokemon at current position: " + gameBoard.getPokemon(playerPos);
        }
        return ret;
    }

    /**
     * Returns details of game board.
     *
     * @return String
     */
    public String getBoardDetails() {
        return gameBoard.toString();
    }

    /**
     * Main function.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Initialize game server
        GameServer gameServer = new GameServer();

        ServerSocket ss = null;
        // Initialize a server socket
        try {
            ss = new ServerSocket(SERVER_PORT);
        }
        catch (IOException e) {
            System.out.println("ERROR: Failed to init server socket");
            System.exit(1);
        }

        System.out.println("Game server started");

        String ipAddresses = ServerUtils.getAddress();
        System.out.println("System IP addresses: " + ipAddresses);

        while(true) {
            Socket s = null;
            try {
                s = ss.accept();
            }
            catch (IOException e) {
                System.out.println("ERROR: Failed to accept client connection");
            }

            new GameServerThread(s, gameServer).start();
        }

    }
}
