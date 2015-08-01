# TO DO
List of tasks and ideas for implementation

**Tasks**
- [ ] Enemy generation algorithm
- [x] Client 1/Client 2/Server distinction
- [ ] Player 2 mechanic [guitar hero] : WIP
- [ ] Score
- [x] Right and left handed (plays with [A, S, D] and [LEFT, UP, RIGHT] arrow keys)

**Ideas**
- Client loads configs (screen size, game speed, etc.) from Server
- Players input name
- Best scores in database

**WORK IN PROGRESS**

Player 2 Mechanic [Guitar Hero]:

	The basic mechanics are working.
	
	Needs some improvements on the gameplay itself:
	
		- Score points (?)
		- Punishment when failed to score a point (To prevent the player from pressing
		the buttons constantly)
		- Improve the random algorithm ? Maybe put some presetted enemies' positions (Idea):
		
		1.         2.
		
		x          x x
		
		 x          x
		 
		  x        x x
		  
		 x          x
		 
		x          x x
