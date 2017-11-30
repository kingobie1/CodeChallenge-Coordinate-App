# Coding Challenge Map Coordinate App

## Getting Started
in order to run the app, go into terminal and type the following in the same folder where App.java is stored. 
`> javac App.java`  to compile
`> java App` to run the application
You’ll then be met with simple instructions on how to use the application in your terminal:

```
Controls
   m: see map with cheapest event at each tile 
   cm: see map with coordinates 
   x,y: input coordinate 
   q: quit app 
```

## Response To Questions
How might you change your program if you needed to support multiple events at the same location?
- if my app needed to support multiple events at the same location I would just have my `MapTile` have an array list of events instead of just one event. I would then create a simple findCheapestEvent function in the `MapTile` or `App` class that would loop through each and get the cheapest ticket.

How would you change your program if you were working with a much larger world size?
- I believe that my spiral method to search the map for cheap events is a smart efficient way to search the map on a large scale, especially if the map is heavily populated with events. If the map were gigantic, I would try to have a better way iterating through coordinates that are out of bounds on the map. If the map had a scarce amount of events, It might be smart to just iterate though each event and collect the closest 5 from there. For this function, I would need to re write a distance function as mine is being calculated in the spiraling loop.

## Prior Instructions
### Scenario
- Your program should randomly generate seed data.
- Your program should operate in a world that ranges from -10 to +10 (Y axis), and -10 to +10 (X axis).
- Your program should assume that each co-ordinate can hold a maximum of one event.
- Each event has a unique numeric identifier (e.g. 1, 2, 3).
- Each event has zero or more tickets.
- Each ticket has a non-zero price, expressed in US Dollars.
- The distance between two points should be computed as the Manhattan distance.

### Instructions
- You are required to write a program which accepts a user location as a pair of co-ordinates, and returns a list of the five closest events, along with the cheapest ticketprice for each event.
- Please detail any assumptions you have made.
- How might you change your program if you needed to support multiple events at the same location?
- How would you change your program if you were working with a much larger world size?


### Example Program Run
```
Please Input Coordinates:
\> 4,2
Closest Events to (-10,-10):
Event 155 - $100.0, Distance 0
Event 148 - $123.0, Distance 1
Event 156 - $209.0, Distance 5
Event 157 - $375.0, Distance 6
Event 141 - $54.0, Distance 6
```
