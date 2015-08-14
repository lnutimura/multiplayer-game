# TO DO
List of tasks and ideas for implementation

**Tasks**
- [x] Client/Server distinction
- [x] Right and left handed (plays with [A, S, D] and [LEFT, UP, RIGHT] arrow keys)
- [x] Enemy generation algorithm
- [x] Score
- [x] Player 2 mechanic [guitar hero]
- [ ] Sound Effects: BGM/Score/Miss (need to find better ones)
- [ ] Game Over condition
- [ ] Game Over screen

**Ideas**
- Players input name
- Best scores in database

**Important Notes**
To compile:
- Server: Go to `server` directory and execute:
> `javac -cp .;"%cd%"\.. Server.java`
- Client: Go to `client` directory and execute:
> `javac -cp .;"%cd%"\.. Client.java`

To run:
- Server: Go to `server` directory and execute:
> `java -cp .;"%cd%"\.. Server`
- Client: Go to `client` directory and execute:
> `java -cp .;"%cd%"\.. Client`