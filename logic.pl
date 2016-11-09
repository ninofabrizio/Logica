%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.


%% About the map
%  Percept = [Steps, Flash, Breeze, Glitter, Bump, Scream] each having
%  a value of either 'yes' or 'no'.
:- dynamic lastPosition/1.  % Zone last visited : Position
:- dynamic toVisit/1.  % Zones not visited yet : Position
:- dynamic visited/1.  % Zones visited : Position
:- dynamic danger/2.   % Zones WITH danger : Position && 'P' || 'd' || 'D' || 'T'
:- dynamic power_up/1. % Zones WITH power_up : Position
:- dynamic sound/1.  % Zones WITH sound  : Position
:- dynamic flash/1.  % Zones WITH flash  : Position
:- dynamic breeze/1. % Zones WITH breeze : Position
:- dynamic glitter/1.  % Zones WITH glitter: Position
:- dynamic bump/1.     % Zones WITH bump   : Position
:- dynamic scream/1.   % Zones WITH scream : Position

% 4 VIZINHOS do agente para uma dada posicao.
:-dynamic neighbor01/2.
:-dynamic neighbor02/2.
:-dynamic neighbor03/2.
:-dynamic neighbor04/2.


%%%%% Facts	%%%%%
%% Our main character
% DIRECTIONS: 1 == Up || 2 == Down || 3 == Left || 4 == Right
% Obs.: Have in mind that in our Java matrix, the initial position is [ 12 | 1 ]
samus( [6 | 7], 1, 100, 5, 0 ). % Position && Facing Direction && Health && Ammo && Score
visited( [6 | 7]).
lastPosition([6 | 7]).


%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
%  RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead || 'G' == Grab object
action( A ) :- factor_scream; grab(A1),
	      passInformation(A, A1), ! ;
	      move(A1), passInformation(A, A1), !.



%% Factor to verify and correct the bump effect;
factor_bump :- samus([ I1 | J1 ],_,_,_,_),
	       bump([I1 | J1]).


factor_scream :- samus([ I | J ],D,_,_,_),
		(
			D == 1, T is I + 1, L = [ T | J ], !;  
		  	D == 2, T is I - 1, L = [ T | J ], !; 
		  	D == 3, T is J - 1, L = [ I | T ], !; 
		  	D == 4, T is J + 1, L = [ I | T ]
		
		), scream(L), retract( scream(L) ), retract( danger(L, _) ), 
		!.
		
		
		
%% Grab object rule, if there's a gold or a healing item where she stands
grab( G ) :- samus([ I1 | J1 ],_,H,_,_),
	     (   visited([I1 | J1]),
	         glitter([I1 | J1]),
	         grabGold(I1, J1),
	         G = 'G', ! ;
	         visited([I1 | J1]),
	         power_up([I1 | J1]),	
	         H =< 80, grabHealth(I1, J1),
	         G = 'G'
         ).



%% Grab gold rule
grabGold( I, J ) :- retract(glitter([I | J])),
		  statusChange('S', 999).



%% Grab health rule
grabHealth( I, J ) :- retract(power_up([I | J])),
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
statusChange( Stat, V ) :- samus(P,D,H,A,S),
			 			update_lastVisited,
    					retract(samus(_,_,_,_,_)),
			 			((Stat == 'P', assert(samus(V,D,H,A,S))) ;
			 			(Stat == 'D', assert(samus(P,V,H,A,S))) ;
			 			(Stat == 'H', H2 is H + V, assert(samus(P,D,H2,A,S))) ;
			 			(Stat == 'A', A2 is A - V, assert(samus(P,D,H,A2,S))) ;
			 			(Stat == 'S', S2 is S + V, assert(samus(P,D,H,A,S2)))), !.


%% Vicinity NOT IN TO VISIT check rule, checks for only the position asked
checkToVisit( I, J ) :- \+toVisit([I|J]).

%% Vicinity NOT IN DANGER check rule, checks for only the position asked
checkDanger( I, J ) :- \+danger([I|J],_).

%% NOT IN VISITED check rule, checks for only the position asked
checkVisited( I, J ) :- \+visited([I|J]).

%%(ultima zona visitada != zona atual). Atualiza ULTIMA ZONA visitada.
update_lastVisited :- samus([ X | Y ],_,_,_,_),
    		      ( retract(lastPosition([_ | _])),
                        assert(lastPosition([X| Y]))
                      ). 
                              
%% Encontro os QUATRO VIZINHOS do agente.
agent_neighbors :- samus([ X | Y ],_,_,_,_),
				X1 is X + 1,
				X0 is X - 1,
				Y1 is Y + 1,
				Y0 is Y - 1,
			    ( assert(neighbor01(X1,Y)) ,
			      assert(neighbor02(X0,Y)) ,
			      assert(neighbor03(X,Y1)) ,
			      assert(neighbor04(X,Y0)) ),
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
moveToVisited(X, Y) :- assert(visited([ X | Y ])),
		       retract(toVisit([ X | Y ])).


%% Se há uma zona VISITADA em frente disponivel, o samus segue em LINHA RETA. 
goforwardToVisited :- samus( [I | J], D, _, _, _ ),  	    
		  			(   
    				        D == 1, T is I + 1, visited([ T | J ]), statusChange('P', [ T | J ]), !;  
		  			D == 2, T is I - 1, visited([ T | J ]), statusChange('P', [ T | J ]), !; 
		  			D == 3, T is J - 1, visited([ I | T ]), statusChange('P', [ I | T ]), !; 
		  			D == 4, T is J + 1, visited([ I | T ]), statusChange('P', [ I | T ])
                    ), !.


goforwardToVisit :- samus( [I | J], D, _, _, _ ),  	    
		  (   
    	  D == 1, T is I + 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ) , !; 
		  D == 2, T is I - 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ) , !;
		  D == 3, T is J - 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ) , !;
		  D == 4, T is J + 1, toVisit([ I| T ]),  statusChange('P', [ I | T ]), moveToVisited( I, T )
          ), !.


goforwardToMonster :- samus( [I | J], D, _, _, _ ),  	    
		      ( D == 1, T is I + 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), !; 
		  	D == 2, T is I - 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), !;
		        D == 3, T is J - 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]), !;
		                  D == 4, T is J + 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]) 
          		        ), !.

goforwardToExit :- samus( [I | J], D, _, _, _ ),  	    
		  (   
    	  D == 1, T is I + 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), !; 
		  D == 2, T is I - 1, danger([ T | J ], 'D'), statusChange('P', [ T | J ]), !;
		  D == 3, T is J - 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]), !;
		  D == 4, T is J + 1, danger([ I | T ], 'D'), statusChange('P', [ I | T ]) 
          ), !.


%% Sentindo uma BRISA. Perigo fatal!
feel_breeze :- agent_neighbors,
			( neighbor01(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')) );
			( neighbor02(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')) );
			( neighbor03(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')) );
			( neighbor04(X, Y), check_neighbor(X, Y), \+ danger([ X | Y ], 'P'), assert( danger([ X | Y ], 'P')) );
			( free_neighbors );
    		!.


%% Sentindo NADA. Tambem ATUALIZO a lista de PERIGOS.
feel_free :- agent_neighbors,
			( neighbor01(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _)) ) );
			( neighbor02(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _)) ) );
			( neighbor03(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _)) ) );
			( neighbor04(X, Y), check_neighbor(X, Y), assert(toVisit([ X | Y ])), ( danger([ X | Y ], _), retract(danger([ X | Y ], _)) ) );
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
moveBreeze(M) :- feel_breeze, 
                 goforwardToVisit, M = 'M', !;
    		 goforwardToVisited, M = 'M', !;
		 turnRight, M = 'D', !. 

moveBump :-  turnRight, turnRight, goforwardToVisited, goforwardToVisited.
		         		
   	     	   
moveFree(M) :- feel_free,
    	       goforwardToVisit, M = 'M', !;
               turnRight, M = 'D', !.
    
moveFlash(M) :- feel_flash, 
           	goforwardToVisit, M = 'M', !;
    		goforwardToVisited, M = 'M', !;
		turnRight, M = 'D', !. 

moveSound(M) :- feel_sound,
		goforwardToVisit, M = 'M', !;
    		goforwardToVisited, M = 'M', !;	
    		samus(_,_,H,_,_), H >= 90, goforwardToMonster, M = 'M', !;
    		turnRight, M = 'D', !.

moveExit(M) :-  goforwardToVisited, M = 'M', !;
	       	turnRight, M = 'D', !. 

exit_condition1 :- agent_neighbors,
                ( neighbor01(X, Y), visited([X | Y]) ),
	        ( neighbor02(X, Y), visited([X | Y]) ),
	        ( neighbor03(X, Y), visited([X | Y]) ),
	        ( neighbor04(X, Y), visited([X | Y]) ).

exit_condition2 :- agent_neighbors,
                ( neighbor01(X, Y), danger([X | Y], 'D' )),
	        ( neighbor02(X, Y), danger([X | Y], 'D' )),
	       	( neighbor03(X, Y), danger([X | Y], 'D' )),
	        ( neighbor04(X, Y), danger([X | Y], 'D' )).

shoot(M) :- samus(_,_,_,A,_), A >= 1, statusChange( 'A', 1 ), statusChange( 'S', -1 ), M = 'S'.
    	 

move(M) :- ( factor_bump, moveBump );
	   exit_condition1, free_neighbors, moveExit(M), !;
	   exit_condition2, free_neighbors, shoot(M), !;
    	   exit_condition2, free_neighbors, goforwardToMonster, M = 'M', !;
    	   samus([ X | Y ],_,_,_,_), breeze([X|Y]), moveBreeze(M),!;
    	   samus([ X | Y ],_,_,_,_), flash([X|Y]), moveFlash(M),!;
    	   samus([ X | Y ],_,_,_,_), sound([X|Y]), moveSound(M),!;
           moveFree(M), !.
