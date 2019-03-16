package ipc_rmi.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {
    /**
     * Default width of a game board.
     */
    public static final int DEFAULT_WIDTH = 30;

    /**
     * Default height of a game board.
     */
    public static final int DEFAULT_HEIGHT = 30;

    /**
     * Number of pokemons to capture to win.
     */
    public static final int WINNER_POKEMONS = 5;

    /**
     * Path of csv holding list of pokemons.
     */
    public static final String POKEMON_CSV_FILEPATH = "src/main/resources/ipc_server/pokemon.csv";

    /**
     * Width of board.
     */
    private int width;

    /**
     * Height of board.
     */
    private int height;

    /**
     * Positions of players active on this board.
     */
    private Map<Integer, Position> playerPositions;

    /**
     * List of pokemons captured by each active player.
     */
    private Map<Integer, List<String>> playerPokemons;

    /**
     * Positions of pokemons on game board.
     */
    private Map<Position, String> pokemonPositions;

    /**
     * Flag to indicate winner found.
     */
    private boolean winnerFound;

    /**
     * Default constructor.
     */
    public GameBoard() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructor that initializes board of width * height size.
     * @param width - width
     * @param height - height
     */
    public GameBoard(int width, int height) {
        this.width       = width;
        this.height      = height;
        winnerFound      = false;
        playerPositions  = new HashMap<>();
        pokemonPositions = new HashMap<>();
        playerPokemons   = new HashMap<>();
        initPokemonPositions();
    }

    /**
     * Places pokemons in random positions of the board.
     */
    public void initPokemonPositions() {
        List<String> pokemons = Utils.readPokemons(POKEMON_CSV_FILEPATH);
        if (pokemons != null) {
            for (String pokemon : pokemons) {
                boolean positionFound = false;
                while (!positionFound) {
                    // Get random locations on game board
                    int x = (int) (Math.random() * width + 1);
                    int y = (int) (Math.random() * height + 1);
                    Position tmp = new Position(x, y);

                    // Check if position already occupied
                    if (!pokemonPositions.containsKey(tmp)) {
                        positionFound = true;
                        pokemonPositions.put(tmp, pokemon);
//                        if (Math.random() >= 0.5)
//                            System.out.println("Position: " + tmp.toString() + ", Pokemon: " + pokemon);
                    }
                }
            }
        }
        else {
            System.exit(1);
        }
    }

    /**
     * Moves a player up by spaces.
     *
     * @param id - Player ID
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Player does not exist
     *               2 - New position beyond board
     *               3 - New position occupied by another player
     */
    public int moveUp(int id, int spaces) {
        int ret = 1;
        Position playerPos = this.playerPositions.getOrDefault(id, null);

        if (playerPos != null) {
            ret = 2;
            int newY = playerPos.getY() + spaces;

            // Check if new position within board
            if (newY <= height) {
                ret = 3;
                Position newPos = new Position(playerPos.getX(), newY);

                // Check if new position already occupied
                if (!playerPositions.containsValue(newPos)) {
                    playerPos.incrementY(spaces);
                    ret = 0;
                }
            }
        }

        return ret;
    }

    /**
     * Moves a player down by spaces.
     *
     * @param id - Player ID
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Player does not exist
     *               2 - New position beyond board
     *               3 - New position occupied by another player
     */
    public int moveDown(int id, int spaces) {
        int ret = 1;
        Position playerPos = this.playerPositions.getOrDefault(id, null);

        if (playerPos != null) {
            ret = 2;
            int newY = playerPos.getY() - spaces;

            // Check if new position within board
            if (newY >= 0) {
                ret = 3;
                Position newPos = new Position(playerPos.getX(), newY);

                // Check if new position already occupied
                if (!playerPositions.containsValue(newPos)) {
                    playerPos.decrementY(spaces);
                    ret = 0;
                }
            }
        }

        return ret;
    }

    /**
     * Moves a player left by spaces.
     *
     * @param id - Player ID
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Player does not exist
     *               2 - New position beyond board
     *               3 - New position occupied by another player
     */
    public int moveLeft(int id, int spaces) {
        int ret = 1;
        Position playerPos = this.playerPositions.getOrDefault(id, null);

        if (playerPos != null) {
            ret = 2;
            int newX = playerPos.getX() - spaces;

            // Check if new position is within the board
            if (newX >= 0) {
                ret = 3;
                Position newPos = new Position(newX, playerPos.getY());

                // Check if new position already occupied
                if (!playerPositions.containsValue(newPos)) {
                    playerPos.decrementX(spaces);
                    ret = 0;
                }
            }
        }

        return ret;
    }

    /**
     * Moves a player right by spaces.
     *
     * @param id - Player ID
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Player does not exist
     *               2 - New position beyond board
     *               3 - New position occupied by another player
     */
    public int moveRight(int id, int spaces) {
        int ret = 1;
        Position playerPos = this.playerPositions.getOrDefault(id, null);

        if (playerPos != null) {
            int newX = playerPos.getX() + spaces;
            ret = 2;

            // Check if new position within board
            if (newX <= width) {
                ret = 3;
                Position newPos = new Position(newX, playerPos.getY());

                // Check if new position already occupied
                if (!playerPositions.containsValue(newPos)) {
                    playerPos.incrementX(spaces);
                    ret = 0;
                }
            }
        }

        return ret;
    }

    /**
     * Moves a player in given direction by spaces.
     *
     * @param id - Player ID
     * @param direction - Direction to move in
     * @param spaces - Number of spaces to move
     * @return int - 0 - Success
     *               1 - Failure
     */
    public int move(int id, String direction, int spaces) {
        int ret = 1;
        switch(direction) {
            case "left":
                ret = moveLeft(id, spaces);
                break;

            case "right":
                ret = moveRight(id, spaces);
                break;

            case "up":
                ret =  moveUp(id, spaces);
                break;

            case "down":
                ret = moveDown(id, spaces);
                break;
        }

        return ret;
    }

    /**
     * Captures a pokemon and adds to players sack.
     *
     * @param id - Player ID
     * @return int - 0 - Success
     *               1 - Failure
     */
    public synchronized int capturePokemon(int id) {
        int ret = 1;
        Position playerPos = this.playerPositions.getOrDefault(id, null);

        if (playerPos != null) {
            String pokemonAtPos = this.pokemonPositions.getOrDefault(playerPos, null);

            if (pokemonAtPos != null) {
                List<String> playerPokemons = this.playerPokemons.getOrDefault(id, new ArrayList<>());
                playerPokemons.add(pokemonAtPos);
                this.playerPokemons.put(id, playerPokemons);

                // Remove capture pokemon from board
                pokemonPositions.remove(playerPos);

                if (playerPokemons.size() == WINNER_POKEMONS) {
                    winnerFound = true;
                    ret = 2;
                }
                else {
                    ret = 0;
                }

            }
        }

        return ret;
    }

    /**
     * Returns a players position.
     * @param id - Player ID
     * @return Position - null - Failure
     */
    public Position getPlayerPos(int id) {
        return playerPositions.getOrDefault(id, null);
    }

    /**
     * Returns pokemons captured by a player.
     *
     * @param id - Player ID
     * @return List<String>
     */
    public List<String> getPlayerPokemons(int id) {
        return playerPokemons.getOrDefault(id, new ArrayList<>());
    }

    /**
     * Rerturns pokemon at given position if any.
     *
     * @param pos - Players current position
     * @return String
     */
    public String getPokemon(Position pos) {
        String ret = "-";
        if (pokemonPositions.containsKey(pos)) {
            ret = pokemonPositions.get(pos);
        }
        return ret;
    }

    /**
     * Adds given player at mentioned position.
     *
     * @param id - Player ID
     */
    public void addPlayer(int id) {
        int x = (int) (Math.random() * width + 1);
        int y = (int) (Math.random() * height + 1);
        Position pos = new Position(x, y);
        playerPositions.put(id, pos);
    }

    /**
     * Remove given player.
     *
     * @param id - Player ID
     */
    public void removePlayer(int id) {
        playerPositions.remove(id);
        playerPokemons.remove(id);
    }

    /**
     * Return details of the game board.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "Board size: (" + width + ", " + height + ")\n" +
               "Number of pokemons: " + pokemonPositions.size() + "\n" +
               "First player to capture " + WINNER_POKEMONS + " pokemons wins! Good luck";
    }
}
