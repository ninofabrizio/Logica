%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.

%% About the map
:- dynamic lastPosition/1. % Zone last visited : Position
:- dynamic toVisit/1. % Zones not visited yet : Position
:- dynamic visited/2. % Zones visited : Position && '.' || 'U' || 'W' || 'O' (temporarily)
%:- dynamic doubt/2. % Zones where danger MAY lay : Position && 'p' || 'e' || 'r'
:- dynamic danger/2. % Zones WITH danger : Position && 'P' || 'd' || 'D' || 'T'



%%%%%			Facts			%%%%%
%% Our main character
% DIRECTIONS: 1 == Up || 2 == Down || 3 == Left || 4 == Right
% Obs.: Have in mind that in our Java matrix, the initial position is [ 12 | 1 ]
samus( [1 | 1], 1, 100, 5, 0 ). % Position && Facing Direction && Health && Ammo && Score

visited( [1 | 1], '.' ).

lastPosition( [1 | 1] ).

toVisit( [2 | 1] ).
toVisit( [0 | 1] ).
toVisit( [1 | 2] ).
toVisit( [1 | 0] ).



%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
% RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead || 'G' == Grab object
action( A ) :- (grab(A1),
				passInformation( A, A1 ), ! ;
				move(A1), passInformation( A, A1 ) /*, ! ;
				  NO ENEMIES, NO HOLES, NO ENERGY NEEDED, */ ), !.



%% Grab object rule, if there's a gold or a healing item where she stands
grab( G ) :- samus([ I1 | J1 ],_,H,_,_),
			(visited([I1 | J1], 'O'),
				grabGold(I1, J1),
				G = 'G', ! ;
				visited([I1 | J1], 'U'),
				H =< 80, grabHealth(I1, J1),
				G = 'G', !).



%% Grab gold rule
grabGold( I, J ) :- retract(visited([I | J], 'O')),
					assert(visited([I | J], '.')),
					statusChange('S', 999).



%% Grab health rule
grabHealth( I, J ) :- retract(visited([I | J], 'U')),
						assert(visited([I | J], '.')),
						statusChange('H', 20),
						statusChange('S', -1).



%% Concatenation rule, first the action taken, then all of Samus' information
passInformation( L, A1 ) :- samus(P,D,H,A,S),
							append([A1], [P], L1),
							append(L1, [D], L2),
							append(L2, [H], L3),
							append(L3, [A], L4),
							append(L4, [S], L).



%% Movement rules, first checks if the direction is correct
% Move up
move( M ) :- samus([ I1 | J1 ],D,_,_,_),
				I1 < 12,
				I2 is I1 + 1,
				I2 =< 12,
				/*toVisit([I2 | J1]),*/
				checkDanger(I2,J1),
					(D == 1,
					statusChange('P', [ I2 | J1 ]),
					updateMapKnowledge([I2 | J1], [I1 | J1]),
					statusChange('S', -1),
					M = 'M', ! ;
					D \= 1,
					turnRight,
					M = 'D', !).
% Move right
move( M ) :- samus([ I1 | J1 ],D,_,_,_),
				J1 < 12,
				J2 is J1 + 1,
				J2 =< 12,
				/*toVisit([I1 | J2]),*/
				checkDanger(I1,J2),
				(D == 4,
					statusChange('P', [ I1 | J2 ]),
					updateMapKnowledge([I1 | J2], [I1 | J1],
					statusChange('S', -1),
					M = 'M', ! ;
					D \= 4,
					turnRight,
					M = 'D', !).
% Move down
move( M ) :- samus([ I1 | J1 ],D,_,_,_),
				I1 > 1,
				I2 is I1 - 1,
				I2 >= 1,
				/*toVisit([I2 | J1]),*/
				checkDanger(I2,J1),
				(D == 2,
					statusChange('P', [ I2 | J1 ]),
					updateMapKnowledge([I2 | J1], [I1 | J1]),
					statusChange('S', -1),
					M = 'M', ! ;
					D \= 2,
					turnRight,
					M = 'D', !).
% Move left
move( M ) :- samus([ I1 | J1 ],D,_,_,_),
				J1 > 1,
				J2 is J1 - 1,
				J2 >= 1,
				/*toVisit([I1 | J2]),*/
				checkDanger(I1,J2),
				(D == 3,
					statusChange('P', [ I1 | J2 ]),
					updateMapKnowledge([I1 | J2], [I1 | J1]),
					statusChange('S', -1),
					M = 'M', ! ;
					D \= 3,
					turnRight,
					M = 'D', !).



%% Vicinity NOT IN TO VISIT check rule, checks for only the position asked
checkToVisit( I, J ) :- \+toVisit([I|J]).



%% Vicinity NOT IN DANGER check rule, checks for only the position asked
checkDanger( I, J ) :- \+danger([I|J],_).



%% NOT IN VISITED check rule, checks for only the position asked
checkVisited( I, J ) :- \+visited([I|J],_).



%% Map facts update rule, update the facts known for a done movement
updateMapKnowledge( [I1 | J1], [I2 | J2]) :- retract(lastPosition(_)),
												assert(lastPosition([I2 | J2])),
												((checkVisited(I, J),
													assert(visited([I | J], '.'))); % '.' not true for every zone!
													(toVisit([I | J]),
													retract(toVisit([I | J])),
													addNeighborsToVisit(I,J))).



%% Add neighbors to visit rule, checks each that's still not visited
addNeighborsToVisit(I,J) :- I1 is I + 1,
							J1 is J + 1,
							I2 is I - 1,
							J2 is J - 1,
							((checkToVisit(I1, J),
								assert(toVisit([I1 | J])));
								(checkToVisit(I2, J),
								assert(toVisit([I2 | J])));
								(checkToVisit(I, J2),
								assert(toVisit([I | J2])));
								(checkToVisit(I, J1),
								assert(toVisit([I | J1])))).



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
							retract(samus(_,_,_,_,_)),
							((Stat == 'P', assert(samus(V,D,H,A,S))) ;
								(Stat == 'D', assert(samus(P,V,H,A,S))) ;
								(Stat == 'H', H2 is H + V, assert(samus(P,D,H2,A,S))) ;
								(Stat == 'A', A2 is A - V, assert(samus(P,D,H,A2,S))) ;
								(Stat == 'S', S2 is S + V, assert(samus(P,D,H,A,S2)))), !.