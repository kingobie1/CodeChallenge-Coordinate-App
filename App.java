//package viagogoGame;

/*
    By Obatola Seward-Evans

    Assumptions:
     1. The internal maps origin coordinate is (bottom left coordinate) 0,0
        In order to represent an offset map where the origin is -10,-10 the
        coordinate class translates each user displayed coordinate to the
        internal map's coordinate.

     2. An event with no ticket is free.

     3. An event cannot have a ticket removed.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Singleton object, App */
public class App {
    private static App app;
    private static ArrayList<Event> allEvents = new ArrayList<>();

    // App Rules:
    private static final int MAXNUMBEROFTICKETS = 10;
    private static final int MAXTICKETVALUE = 2000;
    static final double PROBABILITY_EVENT_TILE = 0.4;
    // determines the origin, bottom left coordinate, of the user displayed map:
    private static final Coordinate bottomLeftCoordinateDisplayedToUser = new Coordinate(-10, -10);


    public static void main(String args[]) throws IOException {
        Map map = new Map(21,21);
        startApp(map);
    }

    /**
     * start the app displayed in the terminal
     * @param map - the map used in the app
     */
    private static void startApp(Map map) {
        Scanner scanner = new Scanner(System.in);
        String input = "";

        clearConsoleDisplay();
        displayControls();

        while (!input.equals("q")) {
            System.out.print("> ");
            input = scanner.nextLine();
            clearConsoleDisplay();
            System.out.println();

            switch (input) {
                case "m":
                    map.printMap();
                    break;

                case "cm":
                    map.printCoordinateMap(bottomLeftCoordinateDisplayedToUser);
                    break;

                case "q":
                    clearConsoleDisplay();
                    return;

                default:
                    Pattern p = Pattern.compile("[-]?[0-9]+,[ ]*[-]?[0-9]+");
                    Matcher m = p.matcher(input);

                    if (m.find()) { // check if input is coordinates.
                        String[] stringCoordinate = input.split(",[ ]*");
                        int x = Integer.parseInt(stringCoordinate[0]);
                        int y = Integer.parseInt(stringCoordinate[1]);

                        // used when coordinates displayed to user are different than coordinates used by internal map (origin 0,0):
                        Coordinate origin = new Coordinate(x, y, bottomLeftCoordinateDisplayedToUser);

                        // used when coordinates displayed to user are the same as coordinates used by internal map (origin 0,0):
//                        Coordinate origin = new Coordinate(x, y);

                        if (map.isCoordinateInMap(origin)) {
                            MapTile[] closestEvents = map.getClosestNTilesWithEvents(origin, 5);

                            System.out.println("Closest Events to (" + x + "," + y + "):");
                            for (MapTile tile: closestEvents) {
                                displayTilesEventWithDistanceToCoord(tile, origin, map);
                            }
                        } else {
                            System.out.println("invalid coordinate.");
                        }

                    } else {
                        System.out.println("invalid input, type 'help' to see controls");
                    }
            }

            displayControls();
        }
    }

    private static void displayControls() {
        System.out.println(
                "\nControls\n" +
                        "   m: see map with cheapest event at each tile \n" +
                        "   cm: see map with coordinates \n" +
                        "   x,y: input coordinate \n" +
                        "   q: quit app \n"
        );
    }

    private static void displayTilesEventWithDistanceToCoord(MapTile tile, Coordinate coord, Map map) {
        if (tile != null) {
            System.out.println(
                    "Event " + tile.getEvent().getEventID() +
                            " - $" + tile.getEvent().getCheapestTicketPrice() +
                            ", Distance " + map.getManhattanDistance(coord, tile.getCoordinate())
            );
        }
    }

    private static void clearConsoleDisplay() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }

    static App getInstance() {
        if(app == null){
            app = new App();
        }
        return app;
    }

    /**
     * generate an event with a random number of tickets
     * @return newly generated Event
     */
    Event generateEvent() {
        Event e = new Event(allEvents.size());

        int numTickets = getRandomArbitrary(0, MAXNUMBEROFTICKETS);

        // create tickets for event:
        for (int t = 0; t < numTickets; t++) {
            int ticketValue = getRandomArbitrary(0, MAXTICKETVALUE);
            e.addTicket(new Ticket(ticketValue));
        }

        allEvents.add(e);
        return e;
    }

    /**
     * Get a random number between min (inclusive) and max (exclusive)
     * @param min inclusive minimum
     * @param max exclusive maximum
     * @return random int between max and min
     */
	static int getRandomArbitrary(int min, int max) {
		  return (int) (Math.random() * (max - min) + min);
	}

    Event[] getAllEvents() {
        return allEvents.toArray(new Event[allEvents.size()]);
    }
}

class Map {
	/** The width in grid cells of the map */
	private static int WIDTH = 20;

	/** The height in grid cells of the map */
	private static int HEIGHT = 20;

	/** The actual data for the map */
	private MapTile[] data;

	/**
	 * Generate WIDTH x HEIGHT size Map with evens and tickets
	 */
    Map(int width, int height) {
        if (width > 1 && height > 1) {
            WIDTH = width;
            HEIGHT = height;
            data = new MapTile[WIDTH * HEIGHT];
        }

        App g = App.getInstance();

		for (int r = 0; r < HEIGHT; r++) {
			for (int c = 0; c < WIDTH; c++) {
			    Coordinate coordinate = new Coordinate(c, (HEIGHT - 1) - r);

			    // create a new tile with a random event from all events:
			    MapTile t = new MapTile( coordinate );

                // PROBABILITY_EVENT_TILE% chance there's an event at tile:
			    if (Math.random() < g.PROBABILITY_EVENT_TILE) {
                    t.giveEvent(g.generateEvent());
                }

				data[ coordinateToIndex( coordinate ) ] = t;
			}
		}
	}

	void printMap() {
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
                printCheapestEventAtMapTile(this.getTileAtCoordinate(new Coordinate(col, (HEIGHT - 1) - row)));
			}
			System.out.println("\n");
		}
	}

    /**
     * helper for print map:
     */
	private static void printCheapestEventAtMapTile(MapTile tile) {
        Event eventAtCoordinate = tile.getEvent();
        if (eventAtCoordinate != null) { // event exists at coordinate.
            if (eventAtCoordinate.hasTickets()) {
                System.out.print("[$" + eventAtCoordinate.getCheapestTicketPrice() + "] ");
            } else {
                System.out.print("[$0.00]"); // Assumption: An event with no ticket is free.
            }
        } else { // no event at coordinate.
            System.out.print("[----] ");
        }
    }

    /**
     * Convert given coordinate to the corresponding index in MapTile[] data
     * @param coord given coordinate to convert
     * @return int - representing the corresponding index in MapTile[] data
     */
	private static int coordinateToIndex(Coordinate coord) {
	    int x = coord.getX();
	    int y = (HEIGHT - 1) - coord.getY();

        return ((y * WIDTH) + x);
	}

    MapTile getTileAtCoordinate(Coordinate coord) {
        if (isCoordinateInMap(coord)) {
            return data[coordinateToIndex(coord)];
        } else {
            return null;
        }
	}

	int getManhattanDistance(Coordinate coord1, Coordinate coord2) {
        return Math.abs(coord1.getX()-coord2.getX()) + Math.abs(coord1.getY()-coord2.getY());
    }

	boolean isCoordinateInMap(Coordinate coord) {
	    return 0 <= coord.getY() && coord.getY() < HEIGHT && 0 <= coord.getX() && coord.getX() < WIDTH;
    }

    MapTile[] getClosestNTilesWithEvents(Coordinate originCoordinate, int n) {
        int xs = originCoordinate.getX();
        int ys = originCoordinate.getY();

        int maxDistance = 999;
        int numEventsFound = 0;

        MapTile[] closestEvents = new MapTile[n];

        // check origin coordinate:
        if (isCoordinateInMap(originCoordinate)) {
            if (this.getTileAtCoordinate(originCoordinate).hasEvent()) {
                closestEvents[numEventsFound] = this.getTileAtCoordinate(originCoordinate);
                numEventsFound += 1;
                if (numEventsFound == n) { return closestEvents; }
            }
        }

        for (int d = 1; d < maxDistance; d++) {
            for (int i = 0; i < d + 1; i++) {
                // check coordinate (x1, y1):
                Coordinate coord1 = new Coordinate(xs - d + i, ys - i);
                if (isCoordinateInMap(coord1)) {
                    if (this.getTileAtCoordinate(coord1).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord1);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }

                // check coordinate (x2, y2):
                Coordinate coord2 = new Coordinate(xs + d - i, ys + i);
                if (isCoordinateInMap(coord2)) {
                    if (this.getTileAtCoordinate(coord2).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord2);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }
            }

            for (int i = 1; i < d; i++) {
                // Check coordinate (x1, y1):
                Coordinate coord1 = new Coordinate(xs - i, ys + d - i);
                if (isCoordinateInMap(coord1)) {
                    if (this.getTileAtCoordinate(coord1).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord1);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }

                // Check coordinate (x2, y2):
                Coordinate coord2 = new Coordinate(xs + d - i, ys - i);
                if (isCoordinateInMap(coord2)) {
                    if (this.getTileAtCoordinate(coord2).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord2);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }
            }
        }

        return closestEvents;
    }

    void printCoordinateMap(Coordinate bottomLeftCoordinate) {
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                Coordinate c = new Coordinate(col, (HEIGHT - 1) - row);
                Coordinate userDisplayedCoordinate = c.getCoordinateDisplayedToUser(bottomLeftCoordinate);
                printCoordinateForMap(userDisplayedCoordinate);
            }
            System.out.println("\n");
        }
    }

    /**
     * helper for print map:
     */
    private static void printCoordinateForMap(Coordinate coordinate) {
        System.out.print("["+ coordinate.getX() +","+ coordinate.getY() +"] ");
    }
}

class MapTile {
	private Event event;
	private Coordinate coordinate;

    MapTile(Coordinate coordinate) { this.coordinate = coordinate; }

	void giveEvent(Event event) {
        this.event = event;
    }

    Event getEvent() {
        return event;
    }

    Coordinate getCoordinate() { return coordinate; }

    boolean hasEvent() {
        return event != null;
    }
}

class Event {
	private ArrayList<Ticket> tickets = new ArrayList<Ticket>();
	private Ticket cheapestTicket;
	private int eventID;

	Event(int id) { this.eventID = id; }

    void addTicket(Ticket ticket) {
	    if (cheapestTicket == null) {
	        cheapestTicket = ticket;
        } else {
            if (ticket.getPrice() < cheapestTicket.getPrice()) {
                cheapestTicket = ticket;
            }
        }

	    this.tickets.add(ticket);
    }

    Ticket[] getTickets() {
        return tickets.toArray(new Ticket[tickets.size()]);
    }

    boolean hasTickets() {
        return tickets.size() > 0;
    }

    double getCheapestTicketPrice() {
	    if (cheapestTicket == null) { // an event with no ticket is free.
	        return 0;
        } else {
            return cheapestTicket.getPrice();
        }
    }

    int getEventID() { return eventID; }
}

class Ticket {
	private double price;

	Ticket(int price) {
		this.price = price;
	}

    double getPrice() {
        return price;
    }
}

class Coordinate {
    private int x;
    private int y;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a standard coordinate used by the internal map based off of the given x,y coordinate displayed to the
     * user.
     *
     * Translates the x,y coordinate displayed to the user to the corresponding coordinate used by the internal map, with
     * an origin (bottom left coordinate) of 0,0. This is used when the map presented to the user is offset from the
     * internal map.
     *
     * Function should only be used when the coordinate system displayed to the user is offset from the internal map
     * coordinate system.
     *
     * @param x - the x coordinate in coordinate system displayed to the user.
     * @param y - the x coordinate in coordinate system displayed to the user.
     * @param bottomLeftCoordinate - the bottom left coordinate, origin, that is displayed to the user.
     */
    Coordinate(int x, int y, Coordinate bottomLeftCoordinate) {
        this.x = x - bottomLeftCoordinate.getX();
        this.y = y - bottomLeftCoordinate.getY();
    }

    /**
     * Returns the user displayed coordinate of this coordinate.
     *
     * The internal map uses a coordinate system where the origin is 0,0. The coordinate system displayed to the user
     * might be different than the internal map system.
     *
     * Function should only be used when the coordinate system displayed to the user is offset from the internal map
     * coordinate system.
     *
     * @param bottomLeftCoordinate - the bottom left coordinate, origin, that is displayed to the user.
     */
    Coordinate getCoordinateDisplayedToUser(Coordinate bottomLeftCoordinate) {
        int x =  this.x + bottomLeftCoordinate.getX();
        int y =  this.y + bottomLeftCoordinate.getY();
        return new Coordinate(x, y);
    }

    int getX() { return x; }
    int getY() { return y; }
}