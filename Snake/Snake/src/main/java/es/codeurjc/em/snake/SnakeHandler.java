package es.codeurjc.em.snake;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SnakeHandler extends TextWebSocketHandler {

	private static final String SNAKE_ATT = "snake";

	private AtomicInteger snakeIds = new AtomicInteger(0);

	private SnakeGame snakeGame = new SnakeGame();
	
	private ConcurrentHashMap<WebSocketSession, Snake> sesiones = new ConcurrentHashMap<>();
	public ObjectMapper mapper = new ObjectMapper();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		int id = snakeIds.getAndIncrement();

		Snake s = new Snake(id, session);
		
		sesiones.put(session, s);

		session.getAttributes().put(SNAKE_ATT, s);

		snakeGame.addSnake(s);

		StringBuilder sb = new StringBuilder();
		for (Snake snake : snakeGame.getSnakes()) {			
			sb.append(String.format("{\"id\": %d, \"color\": \"%s\"}", snake.getId(), snake.getHexColor()));
			sb.append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		String msg = String.format("{\"type\": \"join\",\"data\":[%s]}", sb.toString());
		
		snakeGame.broadcast(msg);
	}
	
    private void chatHandler(WebSocketSession session, TextMessage message) throws Exception{
        
        try{
            
            JsonNode mens = mapper.readTree(message.getPayload());
            ObjectNode difusion = mapper.createObjectNode();
            difusion.put("name",mens.get("name").asText());
            difusion.put("mensaje",mens.get("message").asText());
            difusion.put("type","chat");
            difusion.put("color", mens.get("color").asText());
            
            for(Entry<WebSocketSession, Snake> s : sesiones.entrySet()){

                    s.getKey().sendMessage(new TextMessage(difusion.toString()));
            
            }
        }catch(IOException e){
    
            System.out.println("Error: " + e.getMessage());
    
        }
        
    }
	

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		try {
			
			String payload = message.getPayload();

			if (payload.equals("ping")) {
				return;
			}
			if(payload.contains("chat")){
                 
                chatHandler(session,message);
            
             }

			Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);

			Direction d = Direction.valueOf(payload.toUpperCase());
			s.setDirection(d);

		} catch (Exception e) {
			System.err.println("Exception processing message " + message.getPayload());
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		System.out.println("Connection closed. Session " + session.getId());

		Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);

		snakeGame.removeSnake(s);

		String msg = String.format("{\"type\": \"leave\", \"id\": %d}", s.getId());
		
		snakeGame.broadcast(msg);
	}

}
