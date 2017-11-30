//package viagogoGame;

/*
    Assumptions:
     1. Code was written assuming that coordinate 0,0 is the bottom left of the map.
        Coordinate class was altered to represent the map where the bottom left of
        the map is -10,-10. In order for the code to be reusable for a dynamic sized
        map where the bottom left is 0,0, remove the coordinate conversion code in the
        coordinate constructor.

        1.1

     2. An event with no ticket is free.

     3. An event cannot have a ticket removed.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Singleton object, Game */
public class Game {
    private static Game game;
    private static ArrayList<Event> allEvents = new ArrayList<>();

    // Game Rules
    private static final int MAXNUMBEROFTICKETS = 10;
    private static final int MAXTICKETVALUE = 2000;
    private static final Coordinate bottomLeftCoordinate = new Coordinate(-10, -10);


    public static void main(String args[]) throws IOException {
        Map map = new Map(21,21);
//		map.printMap();
//        printAllEvents();
        startGame(map);
    }

    private static void startGame(Map map) {
        Scanner scanner = new Scanner(System.in);
        String input = "";

        clearConsole();

        System.out.println(
                "Main Menu \n" +
                "   m: see map with cheapest event at each tile \n" +
                "   cm: see map with coordinates \n" +
                "   x,y: input coordinate \n" +
                "   q: quit game \n"
        );

        while (!input.equals("q")) {
            System.out.print("> ");
            input = scanner.nextLine();
            clearConsole();
            System.out.println();

            switch (input) {
                case "m":
                    map.printMap();
                    break;

                case "cm":
                    map.printCoordinateMap(bottomLeftCoordinate);
                    break;

                case "q":
                    clearConsole();
                    return;

                default:
                    Pattern p = Pattern.compile("[-]?[0-9]+,[ ]*[-]?[0-9]+");
                    Matcher m = p.matcher(input);

                    if (m.find()) { // check if input is coordinates
                        String[] stringCoordinate = input.split(",[ ]*");
                        Coordinate origin = new Coordinate(Integer.parseInt(stringCoordinate[0]), Integer.parseInt(stringCoordinate[1]));

                        // Only used to convert user coordinate to standard coordinate system, where the top left tile is represented as 0,0
                        origin.convertUserCoordinateToStandard(bottomLeftCoordinate);

                        if (map.isCoordInMap(origin)) {
                            Tile[] closestEvents = map.getClosestNTilesWithEvents(
                                    origin,
                                    5
                            );

                            for (Tile tile: closestEvents) {
                                System.out.println(
                                        "Event " + tile.getEvent().getEventID() +
                                                " - $" + tile.getEvent().getCheapestTicketPrice() +
                                                ", Distance " + map.getManhattanDistance(origin, tile.getCoordinate())
                                );
                            }
                        } else {
                            System.out.println("invalid coordinate.");
                        }

                    } else {
                        System.out.println("invalid input, type 'help' to see controls");
                    }
            }

            System.out.println(
                "\nControls: \n" +
                "   m: see map \n" +
                "   x,y: input coordinate \n" +
                "   q: quit game \n"
            );
        }
    }

    private static void clearConsole() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }

    private Game() { }

    static Game getInstance(){
        if(game == null){
            game = new Game();
        }
        return game;
    }

    /**
     * generate an event with a random number of tickets
     * @return newly generated Event
     */
    Event generateEvent() {
        Event e = new Event(allEvents.size());

        int numTickets = getRandomArbitrary(0, MAXNUMBEROFTICKETS);

        // create tickets for event
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

	private static final double PROBABILITY_EVENT_TILE = 0.6;

	/** The actual data for the map */
	private Tile[] data;

	/**
	 * Generate WIDTH x HEIGHT size Map with evens and tickets
	 */
    Map(int width, int height) {
        if (width > 1 && height > 1) {
            WIDTH = width;
            HEIGHT = height;
            data = new Tile[WIDTH * HEIGHT];
        }

        Game g = Game.getInstance();

		for (int r = 0; r < HEIGHT; r++) {
			for (int c = 0; c < WIDTH; c++) {
			    Coordinate coord = new Coordinate(c, (HEIGHT - 1) - r);

			    // create a new tile with a random event from all events
			    Tile t = new Tile( coord );

			    if (Math.random() < PROBABILITY_EVENT_TILE) { // PROBABILITY_EVENT_TILE% chance there's an event at tile
                    t.giveEvent(g.generateEvent());
                }

				data[ coordToIndex( coord ) ] = t;
			}
		}
	}

	void printMap() {
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
			    Event eventAtCoordinate = this.getTileAtCoordinate(new Coordinate(col, (HEIGHT - 1) - row)).getEvent();
			    if (eventAtCoordinate != null && eventAtCoordinate.hasTickets()) {
                    System.out.print("[$" + eventAtCoordinate.getCheapestTicketPrice() + "] ");
                } else {
                    System.out.print("[----] ");
                }

			}
			System.out.println("\n");
		}
	}

    /**
     * Convert given coordinate to the corresponding index in Tile[] data
     * @param coord given coordinate to convert
     * @return int - representing the corresponding index in Tile[] data
     */
	private static int coordToIndex(Coordinate coord) {
	    int x = coord.getX();
	    int y = (HEIGHT - 1) - coord.getY();

        return ((y * WIDTH) + x);
	}

    Tile getTileAtCoordinate(Coordinate coord) {
        if (isCoordInMap(coord)) {
            return data[coordToIndex(coord)];
        } else {
            return null;
        }
	}

	int getManhattanDistance(Coordinate coord1, Coordinate coord2) {
        return Math.abs(coord1.getX()-coord2.getX()) + Math.abs(coord1.getY()-coord2.getY());
    }

	boolean isCoordInMap(Coordinate coord) {
	    return 0 <= coord.getY() && coord.getY() < HEIGHT && 0 <= coord.getX() && coord.getX() < WIDTH;
    }

    Tile[] getClosestNTilesWithEvents(Coordinate originCoordinate, int n) {
        int xs = originCoordinate.getX();
        int ys = originCoordinate.getY();

        int maxDistance = 999;
        int numEventsFound = 0;

        Tile[] closestEvents = new Tile[n];

        // check origin coordinate
        if (isCoordInMap(originCoordinate)) {
            if (this.getTileAtCoordinate(originCoordinate).hasEvent()) {
                closestEvents[numEventsFound] = this.getTileAtCoordinate(originCoordinate);
                numEventsFound += 1;
                if (numEventsFound == n) { return closestEvents; }
            }
        }

        for (int d = 1; d < maxDistance; d++) {
            for (int i = 0; i < d + 1; i++) {
                Coordinate coord1 = new Coordinate(xs - d + i, ys - i);

                // check coordinate (x1, y1)
                if (isCoordInMap(coord1)) {
                    if (this.getTileAtCoordinate(coord1).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord1);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }

                Coordinate coord2 = new Coordinate(xs + d - i, ys + i);

                // check coordinate (x2, y2)
                if (isCoordInMap(coord2)) {
                    if (this.getTileAtCoordinate(coord2).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord2);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }
            }

            for (int i = 1; i < d; i++) {
                Coordinate coord1 = new Coordinate(xs - i, ys + d - i);

                // Check coordinate (x1, y1)
                if (isCoordInMap(coord1)) {
                    if (this.getTileAtCoordinate(coord1).hasEvent()) {
                        closestEvents[numEventsFound] = this.getTileAtCoordinate(coord1);
                        numEventsFound += 1;
                        if (numEventsFound == n) { return closestEvents; }
                    }
                }

                Coordinate coord2 = new Coordinate(xs + d - i, ys - i);

                // Check coordinate (x2, y2)
                if (isCoordInMap(coord2)) {
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
//        for (int row = 0; row < HEIGHT; row++) {
//            for (int col = 0; col < WIDTH; col++) {
//                    Coordinate c = new Coordinate(col, (HEIGHT - 1) - row);
////                    c.convertUserCoordinateToStandard(bottomLeftCoordinate);
//                    System.out.print("[" + c.getX() + ", " + c.getY() + " ]");
//            }
//            System.out.println("\n");
//        }
    }
}

class Tile {
	private Event event;
	private Coordinate coordinate;

    Tile(Coordinate coordinate) { this.coordinate = coordinate; }

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
	    if (cheapestTicket == null) { // an event with no ticket is free
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
     * Creates a standard coordinate used by the internal map based off of the given x,y coordinate displayed to the
     * user.
     *
     * Translates the x,y coordinate displayed to the user to the corresponding coordinate used by the internal map, with
     * an origin (bottom left coordinate) of 0,0. This is used when the map presented to the user is offset from the
     * internal map.
     *
     * Function should only be used when the coordinate system displayed to the user is offset from the internal map
     * coordinate system.
     */
    void convertUserCoordinateToStandard(Coordinate bottomLeftCoordinate) {
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
        return null;
    }

    int getX() { return x; }
    int getY() { return y; }
}