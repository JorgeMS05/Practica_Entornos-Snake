package es.codeurjc.em.snake;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Timer;


public class SnakeGame {

	private final static long TICK_DELAY = 100;

	private ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<>();
	private AtomicInteger numSnakes = new AtomicInteger();
	private List<Comida> comidas = new ArrayList<Comida>();
	private long elapseTime = 0;
	private long currentTime = System.currentTimeMillis();
	
	static CopyOnWriteArrayList<String> nombresTemp = new CopyOnWriteArrayList();

	private ScheduledExecutorService scheduler;

	public void addSnake(Snake snake) {
		
		snakes.put(snake.getId(), snake);

		int count = numSnakes.getAndIncrement();

		if (count == 0) {
			startTimer();
		}
	}

	public Collection<Snake> getSnakes() {
		return snakes.values();
	}

	public void removeSnake(Snake snake) {

		snakes.remove(Integer.valueOf(snake.getId()));

		int count = numSnakes.decrementAndGet();

		if (count == 0) {
			stopTimer();
		}
	}

	private void tick() {
		Timer timer;
		try {
			
			elapseTime = System.currentTimeMillis();
			System.out.println(elapseTime - currentTime);
			
			nombresTemp = SnakeHandler.getNombres();
			
			
			for (Snake snake : getSnakes()) {
				snake.update(getSnakes(), comidas);
			}

			if(comidas.size() == 0) {
				Comida c = new Comida();
                comidas.add(c);
			}
			
			if (elapseTime - currentTime > 10000) {
				currentTime = System.currentTimeMillis();
                elapseTime = 0;
                if (comidas.size() < 20) {
                    Comida c = new Comida();
                    comidas.add(c);
                }
            }
			
			StringBuilder sb = new StringBuilder();
			for (Snake snake : getSnakes()) {
				sb.append(getLocationsJson(snake));
				sb.append(',');
			}
			
			StringBuilder c = new StringBuilder();
			for (Comida comida : comidas) {
				c.append(String.format("{\"x\": %d, \"y\": %d}", comida.getComidaLoc().x, comida.getComidaLoc().y));
                c.append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			c.deleteCharAt(c.length()-1);
			
			StringBuilder punt = new StringBuilder();
			for (Snake snake : getSnakes()) {
				punt.append(String.format("{\"puntuacion\": %d}", snake.getLength()));
				punt.append(',');
			}
			punt.deleteCharAt(punt.length()-1);
			
			StringBuilder nomb = new StringBuilder();
			for (int i=0; i<nombresTemp.size(); i++) {
				nomb.append(String.format("{\"nombre\": %s}", "\"" + nombresTemp.get(i) + "\""));
				nomb.append(',');
			}
			nomb.deleteCharAt(nomb.length()-1);
			
			String msg = String.format("{\"type\": \"update\", \"data\" : [%s] , \"comidas\" : [%s], \"puntuaciones\" : [%s], \"nombres\" : [%s]}", sb.toString(), c.toString(), punt.toString(), nomb.toString());
			System.out.println(msg);

			broadcast(msg);

		} catch (Throwable ex) {
			System.err.println("Exception processing tick()");
			ex.printStackTrace(System.err);
		}
	}

	private String getLocationsJson(Snake snake) {

		synchronized (snake) {

			StringBuilder sb = new StringBuilder();
			sb.append(String.format("{\"x\": %d, \"y\": %d}", snake.getHead().x, snake.getHead().y));
			for (Location location : snake.getTail()) {
				sb.append(",");
				sb.append(String.format("{\"x\": %d, \"y\": %d}", location.x, location.y));
			}

			return String.format("{\"id\":%d,\"body\":[%s]}", snake.getId(), sb.toString());
		}
	}

	public void broadcast(String message) throws Exception {

		for (Snake snake : getSnakes()) {
			try {

				System.out.println("Sending message " + message + " to " + snake.getId());
				snake.sendMessage(message);

			} catch (Throwable ex) {
				System.err.println("Execption sending message to snake " + snake.getId());
				ex.printStackTrace(System.err);
				removeSnake(snake);
			}
		}
	}

	public void startTimer() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopTimer() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}
}
