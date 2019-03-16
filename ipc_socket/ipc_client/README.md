# Compiling IPC Socket Client

```
bash compile
```

# Running Socket Client
```
bash client <server-ip>
```
Here <server-ip> is the IP address of server.

# Commands Supported
1. join 
    * Syntax: `join`
    * Description: Creates a connection with server and joins a game
2. move
    * Syntax: `move <direction> <spaces>` 
        * _direction_ is the direction to move in. It can take values _left|right|down|up._
        * _spaces_ is the number of spaces to move.
    * Description: Moves player in a specified direction.
3. capture
    * Syntax: `capture`
    * Description: Captures a pokemon present at a players current position.
4. show
    * Syntax: `show`
    * Description: Shows players current details. It includes, player ID, position, 
                   captured pokemons and any pokemons at current position.
5. leave
    * Syntax: `leave`
    * Description: Exits a player from a game.
        
