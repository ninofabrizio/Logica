%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.

%% About the map
:- dynamic toVisit/1. % Position (com 'u' e 'w' ou sem?)
:- dynamic visited/2. % Position | '.' || 'U' || 'W' || 'O' (temporarily)
:- dynamic doubt/2. % Position | 'p' || 'e' || 'r'
:- dynamic danger/2. % Position | 'P' || 'd' || 'D' || 'T'



%%%%%			Facts			%%%%%
%% Our main character
% DIRECTIONS: 1 == Up || 2 == Down || 3 == Left || 4 == Right
% Obs.: Have in mind that in our Java matrix, the initial position is [ 12 | 1 ]
samus( [1 | 1], 1, 100, 5, 0 ). % Position | Facing Direction | Health | Ammo | Score

visited([1 | 1], '.').

toVisit([2 | 1]).
toVisit([0 | 1]).
toVisit([1 | 2]).
toVisit([1 | 0]).



%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
% RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead || 'G' == Grab object
action( A ) :- (grab(A1), passInformation( A, A1 ), ! ; move(A1), passInformation( A, A1 ) /*, ! ;  NO ENEMIES, NO HOLES, NO ENERGY NEEDED, */ ), !.


%% Grab object rule, if there's a gold or a healing item where she stands
grab( G ) :- samus([ I1 | J1 ],_,H,_,_), (visited([I1 | J1], 'O'), grabGold(I1, J1), G = 'G', ! ; visited([I1 | J1], 'U'), H =< 80, grabHealth(I1, J1), G = 'G', !).


%% Grab gold rule
grabGold(I, J) :- retract(visited([I | J], 'O')), assert(visited([I | J], '.')), statusChange('S', 999).


%% Grab health rule
grabHealth(I, J) :- retract(visited([I | J], 'U')), assert(visited([I | J], '.')), statusChange('H', 20), statusChange('S', -1).


%% Concatenation rule, first the action taken, then all of Samus' information
passInformation( L, A1 ) :- samus(P,D,H,A,S), append([A1], [P], L1), append(L1, [D], L2), append(L2, [H], L3), append(L3, [A], L4), append(L4, [S], L).


%% Movement rules, first checks if the direction is correct
% Move up
move( M ) :- samus([ I1 | J1 ],D,_,_,_), I1 < 12, (D == 1, I2 is I1 + 1, I2 =< 12, danger([I2|J1],_), statusChange('P', [ I2 | J1 ]), statusChange('S', -1), M = 'M', ! ; D \= 1, turnRight, M = 'D', !).
% Move right
move( M ) :- samus([ I1 | J1 ],D,_,_,_), J1 < 12, (D == 4, J2 is J1 + 1, J2 =< 12, checkDanger(I1, J2), statusChange('P', [ I1 | J2 ]), statusChange('S', -1), M = 'M', ! ; D \= 4, turnRight, M = 'D', !).
% Move down
move( M ) :- samus([ I1 | J1 ],D,_,_,_), I1 > 1, (D == 2, I2 is I1 - 1, I2 >= 1, checkDanger(I2, J1), statusChange('P', [ I2 | J1 ]), statusChange('S', -1), M = 'M', ! ; D \= 2, turnRight, M = 'D', !).
% Move left
move( M ) :- samus([ I1 | J1 ],D,_,_,_), J1 > 1, (D == 3, J2 is J1 - 1, J2 >= 1, checkDanger(I1, J2), statusChange('P', [ I1 | J2 ]), statusChange('S', -1), M = 'M', ! ; D \= 3, turnRight, M = 'D', !).


%% Vicinity danger check rule, for both dangers and doubts
checkDanger(I, J).


%% Right turn rule, turns one time only
turnRight :- samus(_,D,_,_,_), changeDirection(D, D1), statusChange('D', D1), statusChange('S', -1).


%% Direction changing rule, the turning logic
changeDirection( D, D1 ) :- D == 1, D1 = 4, ! ; D == 4, D1 = 2, ! ; D == 2, D1 = 3, ! ; D == 3, D1 = 1, !.


%% Samus' status change rule, check wich one to change and replace it
statusChange( Stat, V ) :- samus(P,D,H,A,S), retract(samus(_,_,_,_,_)), ((Stat == 'P', assert(samus(V,D,H,A,S))) ; (Stat == 'D', assert(samus(P,V,H,A,S))) ; (Stat == 'H', H2 is H + V, assert(samus(P,D,H2,A,S))) ; (Stat == 'A', A2 is A - V, assert(samus(P,D,H,A2,S))) ; (Stat == 'S', S2 is S + V, assert(samus(P,D,H,A,S2)))), !.