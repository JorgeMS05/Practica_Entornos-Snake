package es.codeurjc.em.snake;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Snake extends SnakeGame{

	private static final int DEFAULT_LENGTH = 5;

	private final int id;

	private Location head;
	private final Deque<Location> tail = new ArrayDeque<>();
	private int length = DEFAULT_LENGTH;
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	private SnakeGame snakeGame = new SnakeGame();

	private final String hexColor;
	private Direction direction;

	private final WebSocketSession session;

	public Snake(int id, WebSocketSession session) {
		this.id = id;
		this.session = session;
		this.hexColor = SnakeUtils.getRandomHexColor();
		resetState();
	}

	private void resetState() {
		this.direction = Direction.NONE;
		this.head = SnakeUtils.getRandomLocation();
		this.tail.clear();
		this.length = DEFAULT_LENGTH;
	}

	private synchronized void kill() throws Exception {
		resetState();
		sendMessage("{\"type\": \"dead\"}");
	}

	private synchronized void reward() throws Exception {
		this.length++;
		sendMessage("{\"type\": \"kill\"}");
	}

	protected void sendMessage(String msg) throws Exception {
		this.session.sendMessage(new TextMessage(msg));
	}

	public synchronized void update(Collection<Snake> snakes, Collection<Comida> comidas) throws Exception {

		Location nextLocation = this.head.getAdjacentLocation(this.direction);

		if (nextLocation.x >= Location.PLAYFIELD_WIDTH) {
			nextLocation.x = 0;
		}
		if (nextLocation.y >= Location.PLAYFIELD_HEIGHT) {
			nextLocation.y = 0;
		}
		if (nextLocation.x < 0) {
			nextLocation.x = Location.PLAYFIELD_WIDTH;
		}
		if (nextLocation.y < 0) {
			nextLocation.y = Location.PLAYFIELD_HEIGHT;
		}

		if (this.direction != Direction.NONE) {
			this.tail.addFirst(this.head);
			if (this.tail.size() > this.length) {
				this.tail.removeLast();
			}
			this.head = nextLocation;
		}

		handleCollisions(snakes);
		handleComida(comidas);
	}

	private void handleCollisions(Collection<Snake> snakes) throws Exception {

		for (Snake snake : snakes) {

			boolean headCollision = this.id != snake.id && snake.getHead().equals(this.head);

			boolean tailCollision = snake.getTail().contains(this.head);

			if (headCollision || tailCollision) {
				kill();
				if (this.id != snake.id) {
					snake.reward();
				}
			}
		}
	}
	
	private void handleComida(Collection<Comida> c) {
		Iterator<Comida> iterador = c.iterator();
		while(iterador.hasNext()) {
			Comida com = iterador.next();
			if(this.head.equals(com.comidaLoc)) {
					iterador.remove();
					this.length++;
			}
		}
	}

	public synchronized Location getHead() {
		return this.head;
	}

	public synchronized Collection<Location> getTail() {
		return this.tail;
	}

	public synchronized void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getId() {
		return this.id;
	}

	public String getHexColor() {
		return this.hexColor;
	}
}
