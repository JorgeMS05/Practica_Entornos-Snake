var Console = {};

Console.log = (function(message) {
	var console = document.getElementById('console');
	var p = document.createElement('p');
	p.style.wordWrap = 'break-word';
	p.innerHTML = message;
	console.appendChild(p);
	while (console.childNodes.length > 25) {
		console.removeChild(console.firstChild);
	}
	console.scrollTop = console.scrollHeight;
});

var auxPunt = [];
var nombresPuntuacion = [];

//Inserción de nombre Æ
var nombre;
var color;

getRandomColor();

function getRandomColor() {
	nombre = prompt("Inserta tu nombre","Nombre");
	var letters = '0123456789ABCDEF';
	color = '#';
	for (var i = 0; i < 6; i++) {
		color += letters[Math.floor(Math.random() * 16)];
	}
	return color;
}

class Snake {

	constructor() {
		this.snakeBody = [];
		this.color = null;
	}

	draw(context) {
		for (var pos of this.snakeBody) {
			context.fillStyle = this.color;
			context.fillRect(pos.x, pos.y,
				game.gridSize, game.gridSize);
		}
	}
}

class Comida {
	
	constructor(x,y) {
		this.color = "#66FF66";
		this.x = x;
		this.y = y;
	}
	
	draw(context) {
		context.fillStyle = this.color;
		context.fillRect(this.x, this.y, game.gridSize, game.gridSize);
	}
}

class Game {

	constructor(){
	
		this.fps = 30;
		this.socket = null;
		this.nextFrame = null;
		this.interval = null;
		this.direction = 'none';
		this.gridSize = 10;
		
		this.skipTicks = 1000 / this.fps;
		this.nextGameTick = (new Date).getTime();
	}

	initialize() {	
	
		this.snakes = [];
		this.comidas = [];
		this.puntuaciones = [];
		this.nombresUpdate = [];
		let canvas = document.getElementById('playground');
		if (!canvas.getContext) {
			Console.log('Error: 2d canvas not supported by this browser.');
			return;
		}
		
		this.context = canvas.getContext('2d');
		window.addEventListener('keydown', e => {
			
			var code = e.keyCode;
			if (code > 36 && code < 41) {
				switch (code) {
				case 37:
					if (this.direction != 'east')
						this.setDirection('west');
					break;
				case 38:
					if (this.direction != 'south')
						this.setDirection('north');
					break;
				case 39:
					if (this.direction != 'west')
						this.setDirection('east');
					break;
				case 40:
					if (this.direction != 'north')
						this.setDirection('south');
					break;
				}
			}
		}, false);
		
		this.connect();
	}


	setDirection(direction) {
		this.direction = direction;
		this.socket.send(direction);
		Console.log('Sent: Direction ' + direction);
	}

	startGameLoop() {
	
		this.nextFrame = () => {
			requestAnimationFrame(() => this.run());
		}
		
		this.nextFrame();		
	}

	stopGameLoop() {
		this.nextFrame = null;
		if (this.interval != null) {
			clearInterval(this.interval);
		}
	}

	draw() {
		this.context.clearRect(0, 0, 640, 480);
		
		for (var id in this.snakes) {			
			this.snakes[id].draw(this.context);
		}
		
		// Pintamos las comidas
		for (var c in this.comidas){
			this.comidas[c].draw(this.context);
		}
	}

	addSnake(id, color) {
		this.snakes[id] = new Snake();
		this.snakes[id].color = color;
	}

	updateSnake(id, snakeBody) {
		if (this.snakes[id]) {
			this.snakes[id].snakeBody = snakeBody;
		}
	}

	removeSnake(id) {
		this.snakes[id] = null;
		// Force GC.
		delete this.snakes[id];
	}

	run() {
	
		while ((new Date).getTime() > this.nextGameTick) {
			this.nextGameTick += this.skipTicks;
		}
		this.draw();
		if (this.nextFrame != null) {
			this.nextFrame();
		}
}

	connect() {
		
		this.socket = new WebSocket("ws://127.0.0.1:8080/snake");

		this.socket.onopen = () => {
			
			// Socket open.. start the game loop.
			Console.log('Info: WebSocket connection opened.');
			Console.log('Info: Press an arrow key to begin.');

			this.startGameLoop();
			
			setInterval(() => this.socket.send('ping'), 5000);
			
			var enviarNombre= {
		            type: "nombre",
					name : nombre
			}
			alert(JSON.stringify(enviarNombre));

	        game.socket.send(JSON.stringify(enviarNombre));
	        
	        var colorMessage= {
	        		type: "colorMessage",
	        		color: color
	        }
	        
	        game.socket.send(JSON.stringify(colorMessage));
		}

		this.socket.onclose = () => {
			Console.log('Info: WebSocket closed.');
			this.stopGameLoop();
		}

		this.socket.onmessage = (message) => {
			var auxColor;
			var packet = JSON.parse(message.data);
			
			switch (packet.type) {
			case 'update':
				for (var i = 0; i < packet.data.length; i++) {
					this.updateSnake(packet.data[i].id, packet.data[i].body);
				}
				this.comidas = new Array();
				for(var i = 0; i < packet.comidas.length; i++) {
					this.comidas[i] = new Comida(packet.comidas[i].x, packet.comidas[i].y);
				}
				this.nombresUpdate = new Array();
				for(var i = 0; i < packet.nombres.length; i++) {
					this.nombresUpdate.push(packet.nombres[i].nombre);
					console.log("El tamaño de mi polla es: " + this.nombresUpdate.length);
				}
				if(this.nombresUpdate !== undefined || this.nombresUpdate.length != 0){
				this.puntuaciones = new Array();
				for(var i = 0; i < packet.nombres.length; i++) {
					var msg = this.nombresUpdate[i] + " :" + JSON.stringify(packet.puntuaciones[i].puntuacion);
					if(auxPunt[i] == undefined || auxPunt[i]!=parseInt(JSON.stringify(packet.puntuaciones[i].puntuacion))){
						if(auxPunt[i]!=parseInt(JSON.stringify(packet.puntuaciones[i].puntuacion)) && auxPunt[i]!=undefined){
							document.getElementById("leaderboardFin").deleteRow(i);
						}
						auxPunt[i] = parseInt(JSON.stringify(packet.puntuaciones[i].puntuacion));
						var aux = "<tr id=\"" + i + "\">" + msg + "</tr>";
						var table = document.getElementById("leaderboardFin")/*.innerHTML += "<tr id=\"" + i + "\">" + msg + "</tr>"*/;
						var row = table.insertRow(i);
						var cell = row.insertCell(0);
						cell.innerHTML = msg;
					}
				}}
				
				break;
			case 'join':
				for (var j = 0; j < packet.data.length; j++) {
					this.addSnake(packet.data[j].id, packet.data[j].color);
				}
				 if (packet.name == nombre){
					 auxColor = color;
				 }else{
					 auxColor = packet.color;
					 alert("Voy a pintar de color: " + auxColor);
				 }
				break;
			case 'leave':
				this.removeSnake(packet.id);
				break;
			case 'dead':
				Console.log('Info: Your snake is dead, bad luck!');
				this.direction = 'none';
				break;
			case 'kill':
				Console.log('Info: Head shot!');
				break;
			case 'chat':
				 /*var auxColor;
				 if (packet.name == nombre){
					 auxColor = color;
				 }else{
					 auxColor = packet.color;
					 alert("Voy a pintar de color: " + auxColor);
				 }*/
				 var msg = packet.name + " : " + packet.mensaje;
				 Console.log(msg.fontcolor(auxColor));
				 break;
			case 'nombres':
				
				//console.log("Entro en nombre " + JSON.stringify(packet.data[0].nombre));
				nombresPuntuacion=[];
				for(var i = 0; i < packet.data.length; i++) {
					nombresPuntuacion.push(JSON.stringify(packet.data[i].nombre));
				}
				console.log(nombresPuntuacion);
				/*for(var i = 0; i < packet.nombre.length; i++) {
					nombresPuntuacion.push(nombre[i].nombrePunt);
				}*/
			}
		}
	}
}

$(document).ready(function(){
    $('#clickMe').click(function() {
		alert("Color colorcito abajo: " + color);
        var object = {
            type: "chat",
			name : nombre,
			color: color,
            message : document.getElementById("myTextArea").value
		}
		alert(JSON.stringify(object));

        game.socket.send(JSON.stringify(object));
        $('#myTextArea').val('');
    });
})

game = new Game();

game.initialize()
