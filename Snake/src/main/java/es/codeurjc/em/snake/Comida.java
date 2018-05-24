package es.codeurjc.em.snake;

import org.springframework.web.socket.WebSocketSession;

public class Comida {
	
	Location comidaLoc;
	private final String color;
	
	
	public Comida() {
		this.color = "#66ff66";
		this.comidaLoc = SnakeUtils.getRandomLocation();
	}

	public Location getComidaLoc() {
		return comidaLoc;
	}


	public String getColor() {
		return color;
	}
}
