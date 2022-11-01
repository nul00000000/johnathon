# johnathon
It is named Johnathon because I needed a name and that's what my girlfriend said ¯\\_(ツ)_/¯

Anyway this is a project I decided to make because I felt that modern security systems were lacking in general
<br>
Flood lights? Lame
<br>
Spotlight that follows you across the lawn as you put out the trash? Yes please

The code is designed in a way that leaves everything extremely open to development, for example mounting the camera or light on a moving rail system (hint hint I want to do this)

Need a belt to pull the carriage to a certain position?
Just add a ```/set``` hook for ```beltX``` on both ends and boom instant communication

It definitely needs a bit of a redesign in some places since it is extremely sensitive to WiFi strength, but thats for future me to worry about

But currently, the design is essentially that both the ESP32 and ESP32CAM are servers that accept GET requests as commands, while the central control program is a client that interfaces with both

Also, this code is completely open source and is ready to be adapted to other projects with it's flexible architecture, so fork away
