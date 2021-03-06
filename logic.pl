%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.


%% About the map
% Dictionary: 'P' == Hole || 'd' == 20 damage enemy || 'D' == 50 damage enemy || 'T' == Teleport enemy
:- dynamic lastPosition/1.		% Zone last visited				: Position
:- dynamic toVisit/1.			% Zones not visited yet			: Position
:- dynamic visited/1.			% Zones visited					: Position
:- dynamic danger/2.			% Zones WITH danger				: Position && 'P' || 'd' || 'D' || 'T'

:- dynamic doubt/2.				% Zones WITH POTENTIAL danger	: Position && 'P' || 'D' || 'T'

:- dynamic power_up/1.			% Zones WITH power_up(health)	: Position
:- dynamic sound/1.				% Zones WITH sound(damage)		: Position
:- dynamic flash/1.				% Zones WITH flash(teleport)	: Position
:- dynamic breeze/1.			% Zones WITH breeze	(hole)		: Position
:- dynamic glitter/1.			% Zones WITH glitter(gold)		: Position
:- dynamic bump/1.				% Zones WITH bump(wall)			: Position
:- dynamic scream/1.			% Zones WITH scream(dead enemy)	: Position

:- dynamic nextDestination/1.	% Next zone to go(AStar feature): Position

:- dynamic goldLeftToTake/1.	% Number of gold left to take	: Number

:- dynamic lastShotLocation/2.	% Zone she shot last time		: Position && Number of shots

:- dynamic runToTheHills/1.		% Special fact					: Activation term (yes)

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
goldLeftToTake(3).


%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
%  RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead || 'G' == Grabed object
%					|| 'S' == Shot in front || 'C' == Climbed the ladder || 'A' == Ask for AStar orientation
action( A ) :-	( goldLeftToTake(0) ; runToTheHills(yes) ), samus([1 | 1],_,_,_,_),
				statusChange('S', -1), passInformation(A, 'C'), !.

action( A ) :-	feelings, checkUndo,
	     		grab(A1), passInformation(A, A1), undo_doubt, !.

action( A ) :-	feelings, checkUndo,
	     		move(A1), passInformation(A, A1), undo_doubt, !.



%% Rule for all the undos
checkUndo :- undo_doubt, factor_bump, factor_scream, factor_shot.



%% Check all the feelings rule
feelings :- samus(P,_,_,_,_), breeze(P), checkNeighborsDanger('P'), feel_breeze.
feelings :- samus(P,_,_,_,_), flash(P), checkNeighborsDanger('T'), feel_flash.
feelings :- samus(P,_,_,_,_), sound(P), checkNeighborsDanger('D'), checkNeighborsDanger('d'), feel_sound.
feelings :- samus(P,_,_,_,_), \+breeze(P), erraseDoubtsDangers('P').
feelings :- samus(P,_,_,_,_), \+flash(P), erraseDoubtsDangers('T').
feelings :- samus(P,_,_,_,_), \+sound(P), erraseDoubtsDangers('D').
feelings :- feel_free.
feelings :- markAllDoubts; !.



%% Check if all neighbors are danger free, depending on the danger being checked
checkNeighborsDanger( D ) :-	agent_neighbors, neighbor01(X1, Y1), neighbor02(X2, Y2), neighbor03(X3, Y3), neighbor04(X4, Y4),
								\+danger([X1 | Y1], D), \+danger([X2 | Y2], D), \+danger([X3 | Y3], D), \+danger([X4 | Y4], D),
								\+doubt([X1 | Y1], D), \+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D).



%% Errase dangers and doubts that are next to a zone without noted feeling
erraseDoubtsDangers( D ) :-	agent_neighbors, neighbor01(X1, Y1), neighbor02(X2, Y2), neighbor03(X3, Y3), neighbor04(X4, Y4),
							(( doubt([X1 | Y1], D), retract(doubt([X1 | Y1], D)) ); ( danger([X1 | Y1], D), retract(danger([X1 | Y1], D)) );
							( doubt([X2 | Y2], D), retract(doubt([X2 | Y2], D)) ); ( danger([X2 | Y2], D), retract(danger([X2 | Y2], D)) );
							( doubt([X3 | Y3], D), retract(doubt([X3 | Y3], D)) ); ( danger([X3 | Y3], D), retract(danger([X3 | Y3], D)) );
							( doubt([X4 | Y4], D), retract(doubt([X4 | Y4], D)) ); ( danger([X4 | Y4], D), retract(danger([X4 | Y4], D)) )); !.



%% Rule to verify and correct the bump feeling
factor_bump :-	(samus(P,_,_,_,_),
				bump(P),
				lastPosition(L),
				statusChange('P', L)); !.



%% Rule to verify and treat the scream feeling
factor_scream :-	(samus([ I | J ],D,_,_,_),
					(
					D == 1, T is I + 1, L = [ T | J ];  
		  			D == 2, T is I - 1, L = [ T | J ]; 
		  			D == 3, T is J - 1, L = [ I | T ]; 
		  			D == 4, T is J + 1, L = [ I | T ]
					), scream([I | J]), retract(scream([I | J])), (retract(danger(L,_)) ; retract(doubt(L,_))), assert(toVisit(L)),
					( (sound([I | J]), eliminateFeeling(L, 'D')) ; (flash([I | J]), eliminateFeeling(L, 'T')) )); !.



%% Rule to verify and treat place shot, and if doubt, zone is free
factor_shot :-	(lastShotLocation(P,A), A == 5, ( doubt(P, 'D'), retract(doubt(P, 'D')) ; doubt(P, 'T'), retract(doubt(P, 'T')) ),
				assert(toVisit(P)), undo_doubt); !.



%% Rule to eliminate enemy feelings (sound or flash) when it's dead
eliminateFeeling( [X | Y], T ) :-	X1 is X + 1,
									X2 is X - 1,
									Y1 is Y + 1,
									Y2 is Y - 1,
									((T == 'D', retract(sound([X1 | Y])), retract(sound([X2 | Y])), retract(sound([X | Y1])), retract(sound([X | Y2])) );
									(T == 'T', retract(flash([X1 | Y])), retract(flash([X2 | Y])), retract(flash([X | Y1])), retract(flash([X | Y2])) )); !.



%% Rule to check for doubts and undo them (turning it to danger or not) when they are not meant to exist
undo_doubt :-	agent_neighbors, samus(P,_,_,_,_),
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				doubt([X1 | Y1], D),
				((D == 'T', (\+flash(P), X1 > 0, X1 < 13, Y1 > 0, Y1 < 13, retract(doubt([X1 | Y1], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D), retract(doubt([X1 | Y1], D)), \+danger([X1 | Y1], D), assert(danger([X1 | Y1], D))));
				(D == 'P', (\+breeze(P), X1 > 0, X1 < 13, Y1 > 0, Y1 < 13, retract(doubt([X1 | Y1], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D), retract(doubt([X1 | Y1], D)), \+danger([X1 | Y1], D), assert(danger([X1 | Y1], D))));
				(D == 'D', (\+sound(P), X1 > 0, X1 < 13, Y1 > 0, Y1 < 13, retract(doubt([X1 | Y1], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D), retract(doubt([X1 | Y1], D)), \+danger([X1 | Y1], D), assert(danger([X1 | Y1], D))))).
undo_doubt :-	agent_neighbors, samus(P,_,_,_,_),
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				doubt([X2 | Y2], D),
				((D == 'T', (\+flash(P), X2 > 0, X2 < 13, Y2 > 0, Y2 < 13, retract(doubt([X2 | Y2], D)) ;
							\+doubt([X1 | Y1], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D), retract(doubt([X2 | Y2], D)), \+danger([X2 | Y2], D), assert(danger([X2 | Y2], D))));
				(D == 'P', (\+breeze(P), X2 > 0, X2 < 13, Y2 > 0, Y2 < 13, retract(doubt([X2 | Y2], D)) ;
							\+doubt([X1 | Y1], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D), retract(doubt([X2 | Y2], D)), \+danger([X2 | Y2], D), assert(danger([X2 | Y2], D))));
				(D == 'D', (\+sound(P), X2 > 0, X2 < 13, Y2 > 0, Y2 < 13, retract(doubt([X2 | Y2], D)) ;
							\+doubt([X1 | Y1], D), \+doubt([X3 | Y3], D), \+doubt([X4 | Y4], D), retract(doubt([X2 | Y2], D)), \+danger([X2 | Y2], D),assert(danger([X2 | Y2], D))))).
undo_doubt :-	agent_neighbors, samus(P,_,_,_,_),
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				doubt([X3 | Y3], D),
				((D == 'T', (\+flash(P), X3 > 0, X3 < 13, Y3 > 0, Y3 < 13, retract(doubt([X3 | Y3], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X1 | Y1], D), \+doubt([X4 | Y4], D), retract(doubt([X3 | Y3], D)), \+danger([X3 | Y3], D), assert(danger([X3 | Y3], D))));
				(D == 'P', (\+breeze(P), X3 > 0, X3 < 13, Y3 > 0, Y3 < 13, retract(doubt([X3 | Y3], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X1 | Y1], D), \+doubt([X4 | Y4], D), retract(doubt([X3 | Y3], D)), \+danger([X3 | Y3], D), assert(danger([X3 | Y3], D))));
				(D == 'D', (\+sound(P), X3 > 0, X3 < 13, Y3 > 0, Y3 < 13, retract(doubt([X3 | Y3], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X1 | Y1], D), \+doubt([X4 | Y4], D), retract(doubt([X3 | Y3], D)), \+danger([X3 | Y3], D), assert(danger([X3 | Y3], D))))).
undo_doubt :-	agent_neighbors, samus(P,_,_,_,_),
				neighbor01(X1, Y1), neighbor02(X2, Y2),
				neighbor03(X3, Y3), neighbor04(X4, Y4),
				doubt([X4 | Y4], D),
				((D == 'T', (\+flash(P), X4 > 0, X4 < 13, Y4 > 0, Y4 < 13, retract(doubt([X4 | Y4], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X1 | Y1], D), retract(doubt([X4 | Y4], D)), \+danger([X4 | Y4], D), assert(danger([X4 | Y4], D))));
				(D == 'P', (\+breeze(P), X4 > 0, X4 < 13, Y4 > 0, Y4 < 13, retract(doubt([X4 | Y4], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X1 | Y1], D), retract(doubt([X4 | Y4], D)), \+danger([X4 | Y4], D), assert(danger([X4 | Y4], D))));
				(D == 'D', (\+sound(P), X4 > 0, X4 < 13, Y4 > 0, Y4 < 13, retract(doubt([X4 | Y4], D)) ;
							\+doubt([X2 | Y2], D), \+doubt([X3 | Y3], D), \+doubt([X1 | Y1], D), retract(doubt([X4 | Y4], D)), \+danger([X4 | Y4], D), assert(danger([X4 | Y4], D))))); !.



%% AStar actions rule, this is the one Java calls to perform an action if AStar was activated
%  RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead
aStarAction( A ) :- feelings, moveToDestination(A1), passInformation(A, A1), !.



%% Move to destination rule, if it doesn't moves turns right
moveToDestination( M ) :-	samus([X1 | Y1], D,_,_,_), nextDestination([X2 | Y1]),
							D == 1, X3 is X1 + 1, X3 == X2, statusChange('P', [X3 | Y1]), statusChange('S', -1), retract(nextDestination(_)), M = 'M', undo_doubt, !.
moveToDestination( M ) :-	samus([X1 | Y1], D,_,_,_), nextDestination([X2 | Y1]),
							D == 2, X3 is X1 - 1, X3 == X2, statusChange('P', [X3 | Y1]), statusChange('S', -1), retract(nextDestination(_)), M = 'M', undo_doubt, !.
moveToDestination( M ) :-	samus([X1 | Y1], D,_,_,_), nextDestination([X1 | Y2]),
							D == 3, Y3 is Y1 - 1, Y3 == Y2, statusChange('P', [X1 | Y3]), statusChange('S', -1), retract(nextDestination(_)), M = 'M', undo_doubt, !.
moveToDestination( M ) :-	samus([X1 | Y1], D,_,_,_), nextDestination([X1 | Y2]),
							D == 4, Y3 is Y1 + 1, Y3 == Y2, statusChange('P', [X1 | Y3]), statusChange('S', -1), retract(nextDestination(_)), M = 'M', undo_doubt, !.
moveToDestination( M ) :-	turnRight, M = 'D', undo_doubt, !.
	

		
%% Grab object rule, if there's a gold or a healing item where she stands
grab( G ) :-	samus([ I1 | J1 ],_,H,_,_),
	     	(	glitter([I1 | J1]),
	     		grabGold(I1, J1),
	         	G = 'G', ! ;
	         	power_up([I1 | J1]),	
	         	H =< 50, grabHealth(I1, J1),
	         	G = 'G'
         	), !.



%% Grab gold rule
grabGold( I, J ) :- retract(glitter([I | J])),
		  			statusChange('S', 999),
	     			goldLeftToTake(N1),
	     			N2 is N1 - 1,
	     			retract(goldLeftToTake(_)),
	     			assert(goldLeftToTake(N2)).



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



%%(ultima zona visitada != zona atual). Atualiza ULTIMA ZONA visitada.
update_lastVisited :-	samus(P,_,_,_,_),
    		      		retract(lastPosition(_)),
                        assert(lastPosition(P)).



%% Vicinity NOT IN TO VISIT check rule, checks for only the position asked
checkToVisit( I, J ) :- \+toVisit([I|J]).

%% Vicinity NOT IN DANGER check rule, checks for only the position asked
checkDanger( I, J ) :- \+danger([I|J],_).

%% NOT IN VISITED check rule, checks for only the position asked
checkVisited( I, J ) :- \+visited([I|J]).



%% Encontro os QUATRO VIZINHOS do agente, apagando os anteriores antes.
agent_neighbors :-	samus([ X | Y ],_,_,_,_),
					X1 is X + 1,
					X0 is X - 1,
					Y1 is Y + 1,
					Y0 is Y - 1,
			    	(neighbor01(_,_), retract(neighbor01(_,_)), assert(neighbor01(X1,Y)) ; assert(neighbor01(X1,Y))),
			    	(neighbor02(_,_), retract(neighbor02(_,_)), assert(neighbor02(X0,Y)) ; assert(neighbor02(X0,Y))),
			    	(neighbor03(_,_), retract(neighbor03(_,_)), assert(neighbor03(X,Y0)) ; assert(neighbor03(X,Y0))),
			      	(neighbor04(_,_), retract(neighbor04(_,_)), assert(neighbor04(X,Y1)) ; assert(neighbor04(X,Y1))), !.



%% Rule to check if she has no choice left endeed to visit a monster or doubt
noChoiceLeft( P, H ) :- \+toVisit(_), (danger(P, 'D'), H > 50) ; (danger(P, 'd'), H > 20) ; (doubt(P, 'D'), H > 50) ; danger(P, 'T') ; doubt(P, 'T').



%% MOVER da lista "A_VISITAR" PARA "VISITADOS".
moveToVisited(X, Y) :- 	assert(visited([X | Y])),
		       			retract(toVisit([X | Y])).



%% Se há uma zona VISITADA em frente disponivel, a Samus segue em LINHA RETA. 
goForwardToVisited :- samus( [I | J], D, _, _, _),  	    
		  			(   
    				D == 1, T is I + 1, visited([ T | J ]), \+bump([T | J]), \+danger([T | J],_), statusChange('P', [ T | J ]), statusChange('S', -1), !;  
		  			D == 2, T is I - 1, visited([ T | J ]), \+bump([T | J]), \+danger([T | J],_), statusChange('P', [ T | J ]), statusChange('S', -1), !; 
		  			D == 3, T is J - 1, visited([ I | T ]), \+bump([I | T]), \+danger([I | T],_), statusChange('P', [ I | T ]), statusChange('S', -1), !; 
		  			D == 4, T is J + 1, visited([ I | T ]), \+bump([I | T]), \+danger([I | T],_), statusChange('P', [ I | T ]), statusChange('S', -1)
                    ), !.



%% Se há uma zona A VISITAR em frente disponivel, a Samus segue em LINHA RETA.
goForwardToVisit :- samus([I | J], D,_,_,_),  	    
		  			(   
    	  			D == 1, T is I + 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ), statusChange('S', -1), !; 
		  			D == 2, T is I - 1, toVisit([ T | J ]), statusChange('P', [ T | J ]), moveToVisited( T, J ), statusChange('S', -1), !;
		  			D == 3, T is J - 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ), statusChange('S', -1), !;
		  			D == 4, T is J + 1, toVisit([ I | T ]), statusChange('P', [ I | T ]), moveToVisited( I, T ), statusChange('S', -1)
          			), !.



% Se há uma zona de PERIGO com MONSTRO DE DANO em frente e visitar não resultará em MORRER, a Samus segue em LINHA RETA.
goForwardToMonster :-	samus([I | J], D, H,_,_),
		      			(D == 1, T is I + 1, noChoiceLeft([ T | J ], H), statusChange('P', [ T | J ]), statusChange('S', -1), (\+visited([T | J]), moveToVisited( T, J ); !); 
		  				D == 2, T is I - 1, noChoiceLeft([ T | J ], H), statusChange('P', [ T | J ]), statusChange('S', -1), (\+visited([T | J]), moveToVisited( T, J ); !);
		        		D == 3, T is J - 1, noChoiceLeft([ I | T ], H), statusChange('P', [ I | T ]), statusChange('S', -1), (\+visited([I | T]), moveToVisited( I, T ); !);
		        		D == 4, T is J + 1, noChoiceLeft([ I | T ], H), statusChange('P', [ I | T ]), statusChange('S', -1), (\+visited([I | T]), moveToVisited( I, T ); !)
          				).



% Se há uma zona de DUVIDA com BURACO em frente, a Samus segue em LINHA RETA.
goForwardToHoleDoubt :-	samus([I | J], D,_,_,_),
		      			(D == 1, T is I + 1, doubt([ T | J ], 'P'), statusChange('P', [ T | J ]), statusChange('S', -1), moveToVisited( T, J ), !; 
		  				D == 2, T is I - 1, doubt([ T | J ], 'P'), statusChange('P', [ T | J ]), statusChange('S', -1), moveToVisited( T, J ), !;
		        		D == 3, T is J - 1, doubt([ I | T ], 'P'), statusChange('P', [ I | T ]), statusChange('S', -1), moveToVisited( I, T ), !;
		        		D == 4, T is J + 1, doubt([ I | T ], 'P'), statusChange('P', [ I | T ]), statusChange('S', -1), moveToVisited( I, T ), !
          				).



%% Rule to turn 'd' into 'D' for doubt, if necessary
checkDangerType( D, D1 ) :- (D == 'd', D1 = 'D') ; D1 = D.



markAllDoubts :- 	agent_neighbors, neighbor01(X1, Y1), neighbor02(X2, Y2), neighbor03(X3, Y3), neighbor04(X4, Y4),
					mark_doubt([X1 | Y1], [X2 | Y2], [X3 | Y3], [X4 | Y4]), mark_doubt([X2 | Y2], [X1 | Y1], [X3 | Y3], [X4 | Y4]),
					mark_doubt([X3 | Y3], [X2 | Y2], [X1 | Y1], [X4 | Y4]), mark_doubt([X4 | Y4], [X2 | Y2], [X3 | Y3], [X1 | Y1]).



%% Marks neighbors as doubts, if more than one neighbor is a danger of the same kind
mark_doubt(P1, P2, P3, P4) :- 	danger(P1, D), (danger(P2, D) ; danger(P3, D) ; danger(P4, D)),
								checkDangerType(D, D1), \+doubt(P1, D1), assert(doubt(P1, D1)), retract(danger(P1, D)),
								((danger(P2, D), \+doubt(P2, D1), assert(doubt(P2, D1)), retract(danger(P2, D)) );
								(danger(P3, D), \+doubt(P3, D1), assert(doubt(P3, D1)), retract(danger(P3, D)) );
								(danger(P4, D), \+doubt(P4, D1), assert(doubt(P4, D1)), retract(danger(P4, D)) )); !.



%% Rule to avoid indexes out of bounds and the corners (she will never reach them)
verifyMapLimit( X, Y ) :-	X > -1, X < 14, Y > -1, Y < 14,
							Corner1 = [0 | 0], Corner2 = [13 | 0], Corner3 = [13 | 13], Corner4 = [0 | 13],
							\+(Corner1 == [X | Y]), \+(Corner2 == [X | Y]), \+(Corner3 == [X | Y]), \+(Corner4 == [X | Y]).



%% Verifico se um dado VIZINHO NAO SE ENCONTRA nas listas "VISITADOS" ,"A_VISITAR", "PERIGO" e "BUMP"(parede)
check_neighbor(X, Y) :- checkVisited(X, Y),
						checkToVisit(X, Y),
						checkDanger(X, Y),
    					\+bump([X | Y]).



%% Sentindo NADA.
feel_free :- agent_neighbors,
			( neighbor01(X1, Y1), check_neighbor(X1, Y1), \+doubt([ X1 | Y1 ],_), verifyMapLimit(X1, Y1), assert(toVisit([ X1 | Y1 ])) );
			( neighbor02(X2, Y2), check_neighbor(X2, Y2), \+doubt([ X2 | Y2 ],_), verifyMapLimit(X2, Y2), assert(toVisit([ X2 | Y2 ])) );
			( neighbor03(X3, Y3), check_neighbor(X3, Y3), \+doubt([ X3 | Y3 ],_), verifyMapLimit(X3, Y3), assert(toVisit([ X3 | Y3 ])) );
			( neighbor04(X4, Y4), check_neighbor(X4, Y4), \+doubt([ X4 | Y4 ],_), verifyMapLimit(X4, Y4), assert(toVisit([ X4 | Y4 ])) ); !.



%% Sentindo uma BRISA. Perigo fatal!
feel_breeze :- agent_neighbors,
			( neighbor01(X1, Y1), check_neighbor(X1, Y1), \+doubt([ X1 | Y1 ], 'P'), assert( danger([ X1 | Y1 ], 'P')));
			( neighbor02(X2, Y2), check_neighbor(X2, Y2), \+doubt([ X2 | Y2 ], 'P'), assert( danger([ X2 | Y2 ], 'P')));
			( neighbor03(X3, Y3), check_neighbor(X3, Y3), \+doubt([ X3 | Y3 ], 'P'), assert( danger([ X3 | Y3 ], 'P')));
			( neighbor04(X4, Y4), check_neighbor(X4, Y4), \+doubt([ X4 | Y4 ], 'P'), assert( danger([ X4 | Y4 ], 'P'))); !.



%% Sentindo FLASH.
feel_flash :- agent_neighbors,
			( neighbor01(X1, Y1), check_neighbor(X1, Y1), \+doubt([ X1 | Y1 ], 'T'), assert( danger([ X1 | Y1 ], 'T')) );
			( neighbor02(X2, Y2), check_neighbor(X2, Y2), \+doubt([ X2 | Y2 ], 'T'), assert( danger([ X2 | Y2 ], 'T')) );
			( neighbor03(X3, Y3), check_neighbor(X3, Y3), \+doubt([ X3 | Y3 ], 'T'), assert( danger([ X3 | Y3 ], 'T')) );
			( neighbor04(X4, Y4), check_neighbor(X4, Y4), \+doubt([ X4 | Y4 ], 'T'), assert( danger([ X4 | Y4 ], 'T')) ); !.



%% Sentindo SOM DE PASSOS.
feel_sound :- agent_neighbors,
			( neighbor01(X1, Y1), check_neighbor(X1, Y1), \+doubt([ X1 | Y1 ], 'D'), assert( danger([ X1 | Y1 ], 'D')) );
			( neighbor02(X2, Y2), check_neighbor(X2, Y2), \+doubt([ X2 | Y2 ], 'D'), assert( danger([ X2 | Y2 ], 'D')) );
			( neighbor03(X3, Y3), check_neighbor(X3, Y3), \+doubt([ X3 | Y3 ], 'D'), assert( danger([ X3 | Y3 ], 'D')) );
			( neighbor04(X4, Y4), check_neighbor(X4, Y4), \+doubt([ X4 | Y4 ], 'D'), assert( danger([ X4 | Y4 ], 'D')) ); !.



%% Rule to update info about last place she shot
updateLastShotLocation :-	samus([I | J], D,_,_,_),
							(   
    	 					D == 1, T is I + 1, P = [T | J];
		  					D == 2, T is I - 1, P = [T | J]; 
		  					D == 3, T is J - 1, P = [I | T];
		  					D == 4, T is J + 1, P = [I | T] 
          					), ( ( \+lastShotLocation(_,_) ; \+lastShotLocation(P,_), retract(lastShotLocation(_,_)) ), A = 1 ; lastShotLocation(P,A1), A is A1 + 1 ),
          					assert(lastShotLocation(P, A)), !.



%% Rule to shoot
shoot(S) :- samus(_,_,_,A,_), A >= 1, statusChange('A', 1), statusChange('S', -1), updateLastShotLocation, S = 'S'.



%% MOVIMENTO sem PERIGO
moveFree(M) :- goForwardToVisit, M = 'M', !;
               turnRight, M = 'D', !.



%% MOVIMENTO para BRISA
moveBreeze(M) :- 	goForwardToVisit, M = 'M', !.
    		 		%goForwardToVisited, M = 'M', !.
		 			%turnRight, M = 'D', !.
		         		
   	

%% MOVIMENTO com FLASH
moveFlash(M) :- goForwardToVisit, M = 'M', !.
    			%goForwardToVisited, M = 'M', !.
    			%goForwardToMonster, M = 'M', !.
				%turnRight, M = 'D', !. 



%% MOVIMENTO com SOM DE PASSOS
moveSound(M) :- goForwardToVisit, M = 'M', !.
    			%goForwardToVisited, M = 'M', !;
    			%goForwardToMonster, M = 'M', !.
    			%turnRight, M = 'D', !.



%% MOVIMENTO para VISITADO
moveExit(M) :-  goForwardToVisited, M = 'M', !.
	       		%turnRight, M = 'D', !. 



exit_condition1 :- 	agent_neighbors,
                	neighbor01(X1, Y1), toVisit([X1 | Y1]);
	        		neighbor02(X2, Y2), toVisit([X2 | Y2]);
	       			neighbor03(X3, Y3), toVisit([X3 | Y3]);
	        		neighbor04(X4, Y4), toVisit([X4 | Y4]).


exit_condition2 :- 	agent_neighbors,
                	neighbor01(X1, Y1), visited([X1 | Y1]),
	        		neighbor02(X2, Y2), visited([X2 | Y2]),
	        		neighbor03(X3, Y3), visited([X3 | Y3]),
	        		neighbor04(X4, Y4), visited([X4 | Y4]).



exit_condition3 :- 	agent_neighbors, samus(_,D,_,_,_),
                	((D == 1, neighbor01(X1, Y1), (danger([X1 | Y1], 'D') ; danger([X1 | Y1], 'd') ; danger([X1 | Y1], 'T') ; doubt([X1 | Y1], 'D') ; doubt([X1 | Y1], 'T')), (lastShotLocation([X1 | Y1],_), lastShotLocation([X1 | Y1], A), A =< 4 ; !) );
	        		(D == 2, neighbor02(X2, Y2), (danger([X2 | Y2], 'D') ; danger([X2 | Y2], 'd') ; danger([X2 | Y2], 'T') ; doubt([X2 | Y2], 'D') ; doubt([X2 | Y2], 'T')), (lastShotLocation([X2 | Y2],_), lastShotLocation([X2 | Y2], A), A =< 4 ; !) );
	       			(D == 3, neighbor03(X3, Y3), (danger([X3 | Y3], 'D') ; danger([X3 | Y3], 'd') ; danger([X3 | Y3], 'T') ; doubt([X3 | Y3], 'D') ; doubt([X3 | Y3], 'T')), (lastShotLocation([X3 | Y3],_), lastShotLocation([X3 | Y3], A), A =< 4 ; !) );
	        		(D == 4, neighbor04(X4, Y4), (danger([X4 | Y4], 'D') ; danger([X4 | Y4], 'd') ; danger([X4 | Y4], 'T') ; doubt([X4 | Y4], 'D') ; doubt([X4 | Y4], 'T')), (lastShotLocation([X4 | Y4],_), lastShotLocation([X4 | Y4], A), A =< 4 ; !) )).



exit_condition4 :- 	agent_neighbors, samus(_,D,_,_,_),
                	((D == 1, neighbor01(X1, Y1), (danger([X1 | Y1], 'D') ; danger([X1 | Y1], 'd') ; danger([X1 | Y1], 'T') ; doubt([X1 | Y1], 'D') ; doubt([X1 | Y1], 'T')) );
	        		(D == 2, neighbor02(X2, Y2), (danger([X2 | Y2], 'D') ; danger([X2 | Y2], 'd') ; danger([X2 | Y2], 'T'); doubt([X2 | Y2], 'D') ; doubt([X2 | Y2], 'T')) );
	       			(D == 3, neighbor03(X3, Y3), (danger([X3 | Y3], 'D') ; danger([X3 | Y3], 'd') ; danger([X3 | Y3], 'T'); doubt([X3 | Y3], 'D') ; doubt([X3 | Y3], 'T')) );
	        		(D == 4, neighbor04(X4, Y4), (danger([X4 | Y4], 'D') ; danger([X4 | Y4], 'd') ; danger([X4 | Y4], 'T'); doubt([X4 | Y4], 'D') ; doubt([X4 | Y4], 'T')) )).



exit_condition5 :- 	agent_neighbors,
                	neighbor01(X1, Y1), ( danger([X1 | Y1], _) ; doubt([X1 | Y1], _) ),
	        		neighbor02(X2, Y2), ( danger([X2 | Y2], _) ; doubt([X2 | Y2], _) ),
	        		neighbor03(X3, Y3), ( danger([X3 | Y3], _) ; doubt([X3 | Y3], _) ),
	        		neighbor04(X4, Y4), ( danger([X4 | Y4], _) ; doubt([X4 | Y4], _) ).



exit_condition6 :- 	agent_neighbors, samus(_,D,_,_,_),
                	((D == 1, neighbor01(X1, Y1), doubt([X1 | Y1], 'P') );
	        		(D == 2, neighbor02(X2, Y2), doubt([X2 | Y2], 'P') );
	       			(D == 3, neighbor03(X3, Y3), doubt([X3 | Y3], 'P') );
	        		(D == 4, neighbor04(X4, Y4), doubt([X4 | Y4], 'P') )).



%% Main rule for movement
move(M) :-	exit_condition1, moveFree(M), !;
			exit_condition5, exit_condition3, shoot(M), !; % no choice here
			exit_condition5, (goForwardToMonster ; goForwardToHoleDoubt),  M = 'M', !; % no choice here
			toVisit(_), M = 'A', !;
			samus([ X | Y ],_,_,_,_), breeze([X|Y]), moveBreeze(M), !;
			samus([ X | Y ],_,_,_,_), flash([X|Y]), moveFlash(M), !;
			samus([ X | Y ],_,_,_,_), sound([X|Y]), moveSound(M), !;
			%exit_condition2, moveExit(M), !; % useless rule?
			exit_condition3, shoot(M), !;
			exit_condition4, goForwardToMonster, M = 'M', !;
			exit_condition6, goForwardToHoleDoubt, M = 'M', !;
			turnRight, M = 'D', !.