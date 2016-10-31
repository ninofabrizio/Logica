%%%%%	Mutable Facts and Rules	%%%%%
:- dynamic samus/5.



%%%%%			Facts			%%%%%
%% Our main character
% POSITIONS: 1 == Up || 2 == Down || 3 == Left || 4 == Right
% Obs.: Have in mind that in our Java matrix, the initial position is [ 12 | 1 ]
samus( [1 | 1], 1, 100, 5, 0 ). % Position | Facing Direction | Health | Ammo | Score



%%%%%		Main Rules			%%%%%
%% Actions rule, this is the one Java calls to perform an action
% RETURNED ACTIONS: 'D' == Direction changed || 'M' == Moved ahead
action( A ) :- /* NO ENEMIES, NO HOLES, NO ENERGY NEEDED, NO GOLD FOUND, */ move(A1), passInformation( A, A1 ).


%% Concatenation rule, first the action taken, then all of Samus' information
passInformation( L, A1 ) :- samus(P,D,H,A,S), append([A1], [P], L1), append(L1, [D], L2), append(L2, [H], L3), append(L3, [A], L4), append(L4, [S], L).


%% Movement rules, first checks if the direction is correct
% Move up
move( M ) :- samus([ I1 | J1 ],D,_,_,_), I1 < 12, (D == 1, I2 is I1 + 1, I2 =< 12, statusChange('P', [ I2 | J1 ]), M = 'M', ! ; D \= 1, turnRight, M = 'D', !).
% Move right
move( M ) :- samus([ I1 | J1 ],D,_,_,_), J1 < 12, (D == 4, J2 is J1 + 1, J2 =< 12, statusChange('P', [ I1 | J2 ]), M = 'M', ! ; D \= 4, turnRight, M = 'D', !).
% Move down
move( M ) :- samus([ I1 | J1 ],D,_,_,_), I1 > 1, (D == 2, I2 is I1 - 1, I2 >= 1, statusChange('P', [ I2 | J1 ]), M = 'M', ! ; D \= 2, turnRight, M = 'D', !).
% Move left
move( M ) :- samus([ I1 | J1 ],D,_,_,_), I1 > 2, (D == 3, J2 is J1 - 1, J2 >= 1, statusChange('P', [ I1 | J2 ]), M = 'M', ! ; D \= 3, turnRight, M = 'D', !).


%% Right turn rule, turns one time only
turnRight :- samus(_,D,_,_,_), changeDirection(D, D1), statusChange('D', D1).


%% Direction changing rule, the turning logic
changeDirection( D, D1 ) :- D == 1, D1 = 4, ! ; D == 4, D1 = 2, ! ; D == 2, D1 = 3, ! ; D == 3, D1 = 1, !.


%% Samus' status change rule, check wich one to change and replace it
statusChange( Stat, V ) :- samus(P,D,H,A,S), retract(samus(_,_,_,_,_)), ( (Stat == 'P', assert(samus(V,D,H,A,S))) ; (Stat == 'D', assert(samus(P,V,H,A,S))) ; (Stat == 'H', assert(samus(P,D,V,A,S))) ; (Stat == 'A', assert(samus(P,D,H,V,S))) ; (Stat == 'S', assert(samus(P,D,H,A,V)))), !.






/*:- dynamic homem/1.
:- dynamic mulher/2.

homem(ismael).

ola :- assert(homem(joao)).
tchau :- assert(homem(pedro)).*/