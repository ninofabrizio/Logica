## Known issues and other needed changes:

- Marks doubts in the same zone as dangers (danger is supposed to be a fact she's sure, so she shouldn't mark a doubt in it).
- Sometimes she marks a zone as danger automathicaly even when there are neighbors that are still not visited (in that case everyone should be marked as doubt).
- There are some cases where she leaves a zone as danger without having feeling neighbors to confirm it, she should always check if that danger has neighbor with feeling to correct it if it's wrong.
- The cases below result in her marking doubts and dangers wrongly, so sometimes she might end up dying because she thought a certain zone was free from a whole, thinking another neighbor has that hole.
- We let her "go inside" walls to learn about bump feelings, in some cases that let's her correct doubts of neighbor zones with walls. In that case, we should only let her correct doubt of the zone she went (the first wall itself).
- Not every case was tested, so there's probably more erros. Principally involving cases where she's out of valid options (there's still some options missing in Prolog), or when she enters the same zone as a teleport enemy and gets sent to zones we are not yet treating 100% correctly in Java.
- The order of the rules in Move makes her almost always try to call of the AStar algorithm and stay in a loop like that (common when 3 out of 4 neighbors are danger or doubt).
- We should let AStar include toVisit zones as possible zones for the best path because technically they are safe for Samus to walk to.