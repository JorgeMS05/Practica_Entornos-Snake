package es.codeurjc.em.snake;

import java.awt.Color;
import java.util.Random;

public class SnakeUtils {

	public static final int GRID_SIZE = 10;

	private static final Random random = new Random();

	public static String getRandomHexColor() {
		/*float hue = random.nextFloat();
		// sat between 0.1 and 0.3
		float saturation = (random.nextInt(2000) + 1000) / 10000f;
		float luminance = 0.9f;
		Color color = Color.getHSBColor(hue, saturation, luminance);
		return '#' + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);*/
		
			String [] letters = {"0","1","2","3","4","5","7","8","9","A","B","C","D","E","F"};
			String color = "#";
			for (int i = 0; i < 6; i++) {
				color += letters[(int) Math.floor(Math.random() * 16)];
			}
			return color;
		}
	

	public static Location getRandomLocation() {
		int x = roundByGridSize(random.nextInt(Location.PLAYFIELD_WIDTH));
		int y = roundByGridSize(random.nextInt(Location.PLAYFIELD_HEIGHT));
		return new Location(x, y);
	}

	private static int roundByGridSize(int value) {
		value = value + (GRID_SIZE / 2);
		value = value / GRID_SIZE;
		value = value * GRID_SIZE;
		return value;
	}

}
