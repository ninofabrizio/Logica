%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.


%% About the map
% Dictionary: 'P' == Hole || 'd' == 20 damage enemy || 'D' == 50 damage enemy || 'T' == Teleport enemy
:- dynamic lastPosition/1.	% Zone last visited				: Position
:- dynamic toVisit/1.		% Zones not visited yet			: Position
:- dynamic visited/1.		% Zones visited					: Position
:- dynamic danger/2.		% Zones WITH danger				: Position && 'P' || 'd' || 'D' || 'T'

:- dynamic doubt/2.			% Zones WITH POTENTIAL danger	: Position && 'P' || 'd' || 'D' || 'T'

:- dynamic power_up/1.		% Zones WITH power_up(health)	: Position
:- dynamic sound/1.			% Zones WITH sound(damage)		: Position
:- dynamic flash/1.			% Zones WITH flash(teleport)	: Position
:- dynamic breeze/1.		% Zones WITH breeze	(hole)		: Position
:- dynamic glitter/1.		% Zones WITH glitter(gold)		: Position
:- dynamic bump/1.			% Zones WITH bump(wall)			: Position
:- dynamic scream/1.		% Zones WITH scream(dead enemy)	: Position

% 4 VIZINHOS do agente para uma dada posicao.
:-dynamic neighbor01/2.
:-dynamic neighbor02/2.
:-dynamic neighbor03/2.
:-dynamic neighbor04/2.


%%%%% Facts	%%%%%
%% Our main character
% DIRECTIONS: 1 == Up || 2 == Down || 3 == Left || 4 == Right
% Obs.: Have in mind that in our Java matrix, the initial position is [ 12 | 1 ]
samus( [1 | 1], 1, 100, 5, 0 ). % Order: Position && Facing Direction && Health && Ammo && Score
visited( [1 | 1] ).
lastPosition( [1 | 1] ).


%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
%  RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead || 'G' == Grabed object || 'S' == Shot in front || 'C' == Climbed the ladder
action( A ) :-	factor_bump, % Here checks for bump first.
	     		grab(A1), passInformation(A, A1), !.
action( A ) :-	factor_scream, % Here checks for scream.
	     		grab(A1), passInformation(A, A1), !.
action( A ) :-	grab(A1), passInformation(A, A1), !.
action( A ) :-	factor_bump, % Here checks for bump first.
	     		move(A1), passInformation(A, A1), !.
action( A ) :-	factor_scream, % Here checks for scream.
	     		move(A1), passInformation(A, A1), !.
action( A ) :-	move(A1), passInformation(A, A1), !.



%% Factor to verify and correct the bump effect
factor_bump :-	samus(P,_,_,_,_),
				bump(P),
				retract(visited(P)),
				lastPosition(L),
				statusChange('P', L).



factor_scream :- samus([ I | J ],D,_,_,_),
		(
			D == 1, T is I + 1, L = [ T | J ];  
		  	D == 2, T is I - 1, L = [ T | J ]; 
		  	D == 3, T is J - 1, L = [ I | T ]; 
		  	D == 4, T is J + 1, L = [ I | T ]
		
		), scream(L), retract(scream(L)), (retract(danger(L, _)) ; retract(doubt(L,_))), !.
		
		
		
%% Grab object rule, if there's a gold or a healing item where she stands
grab( G ) :-	samus([ I1 | J1 ],_,H,_,_),
	     	(	glitter([I1 | J1]),
	     		grabGold(I1, J1),
	         	G = 'G', ! ;
	         	power_up([I1 | J1]),	
	         	H =< 80, grabHealth(I1, J1),
	         	G = 'G'
         	), !.



%% Grab gold rule
grabGold( I, J ) :- retract(glitter([I | J])),
		  			statusChange('S', 999).



%% Grab health rule
grabHealth( I, J ) :- 	retract(power_up([I | J])),
		   				statusChange('H', 20),
		  				statusChange('S', -1).



%% Concatenation rule, first the action taken, then all of Samus' information
passInformation( L, A1 ) :- samus(P,D,H,A,S),
			 append([A1], [P], L1),
			 append(L1, [D], L2),
			 append(L2, [H], L3),
			 append(L3, [A], L4),
			 append(L4, [S], L).


%% Right turn rule, turns one time only
turnRight :- samus(_,D,_,_,_),
			 changeDirection(D, D1),
			 statusChange('D', D1),
			 statusChange('S', -1).



%% Direction changing rule, the turning logic
changeDirection( D, D1 ) :- D == 1,
			 		        D1 = 4, ! ;
			 			    D == 4,
			 			    D1 = 2, ! ;
			 			    D == 2,
			 			    D1 = 3, ! ;
			 			    D == 3,
			 			    D1 = 1, !.



%% Samus' status change rule, check wich one to change and replace it
statusChange( Stat, V ) :-	samus(P,D,H,A,S),
			 				((Stat == 'P', update_lastVisited, assert(samus(V,D,H,A,S))) ;
			 				(Stat == 'D', assert(samus(P,V,H,A,S))) ;
			 				(Stat == 'H', H2 is H + V, assert(samus(P,D,H2,A,S))) ;
			 				(Stat == 'A', A2 is A - V, assert(samus(P,D,H,A2,S))) ;
			 				(Stat == 'S', S2 is S + V, assert(samus(P,D,H,A,S2)))), 
							retract(samus(P,D,H,A,S)), !.




%% Vicinity NOT IN TO VISIT check rule, checks for only the position asked
checkToVisit( I, J ) :- \+toVisit([I|J]).

%% Vicinity NOT IN DANGER check rule, checks for only the position asked
checkDanger( I, J ) :- \+danger([I|J],_).

%% NOT IN VISITED check rule, checks for only the position asked
checkVisited( I, J ) :- \+visited([I|J]).



%%(ultima zona visitada != zona atual). Atualiza ULTIMA ZONA visitada.
update_lastVisited :-	samus(P,_,_,_,_),
    		      		retract(lastPosition(_)),
                        assert(lastPosition(P)).                




%% Encontro os QUATRO VIZINHOS do agente.
agent_neighbors :- samus([ X | Y ],_,_,_,_),
				X1 is X + 1,
				X0 is X - 1,
				Y1 is Y + 1,
				Y0 is Y - 1,
			    ( assert(neighbor01(X1,Y)) ,
			      assert(neighbor02(X0,Y)) ,
			      assert(neighbor03(X,Y0)) ,
			      assert(neighbor04(X,Y1))),
			    !.

%% Verifico se um dado VIZINHO NAO SE ENCONTRA nas listas "VISITADOS" e "A_VISITAR"
check_neighbor(X, Y) :- checkVisited( X, Y ),
						checkToVisit( X, Y ),
    					\+ bump([X|Y]).

%% APAGAR todos os VIZINHOS.						
free_neighbors :- retract(neighbor01(_, _)),
				  retract(neighbor02(_, _)),
				  retract(neighbor03(_, _)),
				  retract(neighbor04(_, _)).


%% MOVER da lista "A_VISITAR" PARA "VISITADOS".
moveToVisited(X, Y) :- 	assert(visited([ X | Y ])),
		       			retract(toVisit([ X | Y ])).


%% Se há uma zona VISITADA em frente disponivel, a Samus segue em LINHA RETA. 
goforwardToVisited :- samus( [I | J], D, _, _, _),  	    
		  			(   
    				D == 1, T is I + 1, visited([ T | J ]), statusChange('P', [ T | J ]), statusChange('S', -1), !;  
		  			D == 2, T is I - 1, visited([ T | J ]), statusChange('P', [ T | J ]), statusChange('S', -1), !; 
		  			D == 3, T is J - 1, visited([ I | T ]), statusChange('P', [ I | T ]), statusChange('S', -1), !; 
		  			D == 4, T is J + 1, visited([ I | T ]), statusChange('P', [ I | T ]), statusChange('S', -1)
                    ), !.


goforwardToVisit :- samus( [I | J], D, _, _, _ ),  	    
		  (   
    	  D == 1, T is I + 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ), statusChange('S', -1), !; 
		  D == 2, T is I - 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ), statusChange('S', -1), !;
		  D == 3, T is J - 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ), statusChange('S', -1), !;
		  D == 4, T is J + 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ), statusChange('S', -1)
          ), !.


% PROBLEMA: ESTA REGRA E A DE BAIXO SÃO IGUAIS!
goforwardToMonster :- samus( [I | J], D, _, _, _ ),  	    
		      ( D == 1, T is I + 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), statusChange('S', -1), !; 
		  		D == 2, T is I - 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), statusChange('S', -1), !;
		        D == 3, T is J - 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]), statusChange('S', -1), !;
		        D == 4, T is J + 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]), statusChange('S', -1) 
          		), !.

goforwardToExit :- samus( [I | J], D, _, _, _ ),  	    
		  (   
    	  D == 1, T is I + 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), statusChange('S', -1), !; 
		  D == 2, T is I - 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), statusChange('S', -1), !;
		  D == 3, T is J - 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]), statusChange('S', -1), !;
		  D == 4, T is J + 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]), statusChange('S', -1) 
          ), !.


%% Marks neighbors as doubts (DEVERIA TER QUE TIRAR DOS FATOS DE PERIGO, SE FOR DUVIDA)
mark_doubts :- 	agent_neighbors,
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				danger([X1 | Y1], D1), (danger([X2 | Y2], D1) ; danger([X3 | Y3], D1) ; danger([X4 | Y4], D1)),
				((danger([X2 | Y2], D1), assert(doubt([X2 | Y2], D1)) /*,retract(danger([X2 | Y2], D))*/ );
				(danger([X3 | Y3], D1), assert(doubt([X3 | Y3], D1)) /*,retract(danger([X3 | Y3], D))*/ );
				(danger([X4 | Y4], D1), assert(doubt([X4 | Y4], D1)) /*,retract(danger([X4 | Y4], D)))*/ )),
				assert(doubt([X1 | Y1], D1)) /*,retract(danger[X1 | Y1], D))*/ ,
				free_neighbors.
mark_doubts :- 	agent_neighbors,
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				danger([X2 | Y2], D2), (danger([X1 | Y1], D2) ; danger([X3 | Y3], D2) ; danger([X4 | Y4], D2)),
				((danger([X1 | Y1], D2), assert(doubt([X1 | Y1], D2)) /*,retract(danger([X2 | Y2], D))*/ );
				(danger([X3 | Y3], D2), assert(doubt([X3 | Y3], D2)) /*,retract(danger([X3 | Y3], D))*/ );
				(danger([X4 | Y4], D2), assert(doubt([X4 | Y4], D2)) /*,retract(danger([X4 | Y4], D)))*/ )),
				assert(doubt([X2 | Y2], D2)) /*,retract(danger[X1 | Y1], D))*/,
				free_neighbors.
mark_doubts :- 	agent_neighbors,
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				danger([X3 | Y3], D3), (danger([X2 | Y2], D3) ; danger([X1 | Y1], D3) ; danger([X4 | Y4], D3)),
				((danger([X2 | Y2], D3), assert(doubt([X2 | Y2], D3)) /*,retract(danger([X2 | Y2], D))*/ );
				(danger([X1 | Y1], D3), assert(doubt([X1 | Y1], D3)) /*,retract(danger([X3 | Y3], D))*/ );
				(danger([X4 | Y4], D3), assert(doubt([X4 | Y4], D3)) /*,retract(danger([X4 | Y4], D)))*/ )),
				assert(doubt([X3 | Y3], D3)) /*,retract(danger[X1 | Y1], D))*/,
				free_neighbors.
mark_doubts :- 	agent_neighbors,
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				danger([X4 | Y4], D4), (danger([X2 | Y2], D4) ; danger([X3 | Y3], D4) ; danger([X1 | Y1], D4)),
				((danger([X2 | Y2], D4), assert(doubt([X2 | Y2], D4)) /*,retract(danger([X2 | Y2], D))*/ );
				(danger([X3 | Y3], D4), assert(doubt([X3 | Y3], D4)) /*,retract(danger([X3 | Y3], D))*/ );
				(danger([X1 | Y1], D4), assert(doubt([X1 | Y1], D4)) /*,retract(danger([X4 | Y4], D)))*/ )),
				assert(doubt([X4 | Y4], D4)) /*,retract(danger[X1 | Y1], D))*/,
				free_neighbors.


%% Sentindo uma BRISA. Perigo fatal!
feel_breeze :- agent_neighbors,
			( neighbor01(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')));
			( neighbor02(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')));
			( neighbor03(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')));
			( neighbor04(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')));
			( free_neighbors );
    		!.


%% Sentindo NADA. Tambem ATUALIZO a lista de PERIGOS e DUVIDAS.
feel_free :- agent_neighbors,
			( neighbor01(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _))), ( doubt([ X | Y ], _), retract(doubt([ X | Y ], _))));
			( neighbor02(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _))), ( doubt([ X | Y ], _), retract(doubt([ X | Y ], _))));
			( neighbor03(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _))), ( doubt([ X | Y ], _), retract(doubt([ X | Y ], _))));
			( neighbor04(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _))), ( doubt([ X | Y ], _), retract(doubt([ X | Y ], _))));
			( free_neighbors ),
    		!.

feel_flash :- agent_neighbors,
			( neighbor01(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'T'), assert( danger([ X | Y ], 'T')) );
			( neighbor02(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'T'), assert( danger([ X | Y ], 'T')) );
			( neighbor03(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'T'), assert( danger([ X | Y ], 'T')) );
			( neighbor04(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'T'), assert( danger([ X | Y ], 'T')) );
			( free_neighbors );
    		!.

feel_sound :- agent_neighbors,
			( neighbor01(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'D'), assert( danger([ X | Y ], 'D')) );
			( neighbor02(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'D'), assert( danger([ X | Y ], 'D')) );
			( neighbor03(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'D'), assert( danger([ X | Y ], 'D')) );
			( neighbor04(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'D'), assert( danger([ X | Y ], 'D')) );
			( free_neighbors );
    		!.

%% MOVIMENTO para BRISA
moveBreeze(M) :- 	feel_breeze, mark_doubts,
                 	goforwardToVisit, M = 'M', !;
    		 		goforwardToVisited, M = 'M', !;
		 			turnRight, M = 'D', !.
		         		
   	     	   
moveFree(M) :- feel_free,
    	       goforwardToVisit, M = 'M', !;
               turnRight, M = 'D', !.
    
moveFlash(M) :- feel_flash, mark_doubts,
           		goforwardToVisit, M = 'M', !;
    			goforwardToVisited, M = 'M', !;
				turnRight, M = 'D', !. 

moveSound(M) :- feel_sound, mark_doubts,
				goforwardToVisit, M = 'M', !;
    			goforwardToVisited, M = 'M', !;	
    			samus(_,_,H,_,_), H >= 90, goforwardToMonster, M = 'M', !;
    			turnRight, M = 'D', !.

moveExit(M) :-  goforwardToVisited, M = 'M', !;
	       		turnRight, M = 'D', !. 

exit_condition1 :- 	agent_neighbors,
                	( neighbor01(X, Y), visited([X | Y]) ),
	        		( neighbor02(X, Y), visited([X | Y]) ),
	        		( neighbor03(X, Y), visited([X | Y]) ),
	        		( neighbor04(X, Y), visited([X | Y]) ).

exit_condition2 :- 	agent_neighbors,
                	( neighbor01(X, Y), danger([X | Y], 'D' )),
	        		( neighbor02(X, Y), danger([X | Y], 'D' )),
	       			( neighbor03(X, Y), danger([X | Y], 'D' )),
	        		( neighbor04(X, Y), danger([X | Y], 'D' )).

shoot(S) :- samus(_,_,_,S,_), S >= 1, statusChange('A', 1), statusChange('S', -1), S = 'S'.
    	 

move(M) :-	exit_condition1, free_neighbors, moveExit(M), !;
			exit_condition2, free_neighbors, shoot(M), !;
			exit_condition2, free_neighbors, goforwardToMonster, M = 'M', !;
    	   	samus([ X | Y ],_,_,_,_), breeze([X|Y]), moveBreeze(M),!;
    	   	samus([ X | Y ],_,_,_,_), flash([X|Y]), moveFlash(M),!;
    	   	samus([ X | Y ],_,_,_,_), sound([X|Y]), moveSound(M),!;
           	moveFree(M), !.