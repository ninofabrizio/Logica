%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.


%% About the map
%  Percept = [Steps, Flash, Breeze, Glitter, Bump, Scream] each having
%  a value of either 'yes' or 'no'.
:- dynamic lastPosition/1.  % Zone last visited : Position
:- dynamic toVisit/1.  % Zones not visited yet : Position
:- dynamic visited/1.  % Zones visited : Position
:- dynamic doubt/1.    % Zones doubt: Position
:- dynamic danger/2.   % Zones WITH danger : Position && 'P' || 'd' || 'D' || 'T'
:- dynamic power_up/1. % Zones WITH power_up : Position
:- dynamic sound/1.  % Zones WITH sound  : Position
:- dynamic flash/1.  % Zones WITH flash  : Position
:- dynamic breeze/1. % Zones WITH breeze : Position
:- dynamic glitter/1.  % Zones WITH glitter: Position
:- dynamic bump/1.     % Zones WITH bump   : Position
:- dynamic scream/1.   % Zones WITH scream : Position

% 4 vizinhos de uma posicao qualquer do agente.
:-dynamic neighbor01/2.
:-dynamic neighbor02/2.
:-dynamic neighbor03/2.
:-dynamic neighbor04/2.


%%%%% Facts	%%%%%
%% Our main character
% DIRECTIONS: 1 == Up || 2 == Down || 3 == Left || 4 == Right
% Obs.: Have in mind that in our Java matrix, the initial position is [ 12 | 1 ]
samus( [1 | 1], 1, 100, 5, 0 ). % Position && Facing Direction && Health && Ammo && Score
visited( [1 | 1]).
lastPosition([1 | 1]).

%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
%  RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead || 'G' == Grab object
action( A ) :- factor_bump; ( (grab(A1),
	     passInformation(A, A1), ! ;
	     move(A1), passInformation(A, A1) /*, ! ;
	     NO ENEMIES, NO HOLES, NO ENERGY NEEDED, */ ), ! ).


%% Factor to verify and correct the bump effect;
factor_bump :- samus([ I1 | J1 ],_,_,_,_),
	     bump([I1 | J1]),
	     lastPosition(L),
	     statusChange( 'P', L ).
			   


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
						checkToVisit( X, Y ).

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
		  (   (D == 1, T is J + 1, visited([ I | T ]), statusChange('P', [ I | T ])), !; 
			  (D == 2, T is J - 1, visited([ I | T ]), statusChange('P', [ I | T ])), !;
			  (D == 3, T is I - 1, visited([ T | J ]), statusChange('P', [ T | J ])), !;
			  (D == 4, T is I + 1, visited([ T | J ]), statusChange('P', [ T | J ])) 
          ).


goforwardToVisit :- samus( [I | J], D, _, _, _ ),  	    
		  (   (D == 1, T is J + 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ) ), !; 
			  (D == 2, T is J - 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ) ), !;
			  (D == 3, T is I - 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ) ), !;
			  (D == 4, T is I + 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ) ) 
          ).


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
			( free_neighbors );
    		!.



%% MOVIMENTO para BRISA
moveBreeze(M) :- samus([ X | Y ],_,_,_,_),
    	   breeze([ X | Y ]), 
           feel_breeze, 
           goforwardToVisited, 
           samus([ A | B ],_,_,_,_), 
    	   (A == X, B == Y, turnRight, M = 'D'), !;
           (M = 'M').
    	  
    	   
moveFree(M) :- samus([ X | Y ],_,_,_,_),
               feel_free,
    		   goforwardToVisit,
    		   samus([ A | B ],_,_,_,_),
               (A == X, B == Y, turnRight, M = 'D'), !;
               (M = 'M').
    

move(M) :- moveBreeze(M),
           moveFree(M).



