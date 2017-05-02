## WebSocket Server Java Bibliotek

Bibliotek for å sette opp WebSocket server i java. Kan kommunisere med en klient og send og motta korte meldinger (<126 byte).
WebSocketServer- og Frame-klassene er nødvendige. WebSocketServerImpl er et eksempel på en enkel ekko-server. index.html er en simpel testklient.

### Bruksmåte

Egen funksjonalitet implementeres ved å overstyre *onConnect*, *onMessage* og *onClose* metodene i WebSocketServer.
```java
    @Override
    public void onConnect() {
        System.out.println("Client connected");
    }

    @Override
    public void onMessage(String message){
        System.out.println(message);
        try {
            sendShortMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(){
        System.out.println("Client disconnected");
    }
```

Man kan da starte serveren ved å instantiere subklassen man har laget og kjøre *serve* metoden.
```java
    public static void main(String[] args) throws IOException {
        WebSocketServer server = new WebSocketServerImpl(80);
        server.serve();
    }
```

Det er også mulig å kjøre serveren i egen tråd:
```java
    public static void main(String[] args) throws IOException {
        WebSocketServer server = new WebSocketServerImpl(80);
        server.run();
    }
```

Hvis man skulle ha lyst til å sette opp ting manuelt:
```java
    public static void manualServer() throws IOException{
        WebSocketServer server = new WebSocketServer(80);

        // start listening on given port
        server.openServer();
        // wait for client to connect
        server.awaitConnection();
        // send message to client
        server.sendShortMessage("Heisann!");
        // wait for reply
        Frame received = server.awaitFrame();
        // assume its a message and decode it
        String reply = server.receiveMessage(received);
        System.out.println(reply);
        // thats all we wanted to do, close connection and close down the server
        server.closeConnection();
        server.closeServer();
    }
```




### Referanser:
  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers
  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_client_applications

---

### Oppgavetekst

Lag et WebSocket server bibliotek i et valgfritt programmeringsspråk. Se presentasjon (forelesning 8) og https://tools.ietf.org/html/rfc6455. Minstekrav:

- bruk av ferdige WebSocket biblioteker er ikke tillatt
- støtte kommunikasjon med 1 klient
  - kommunikasjon med flere klienter er ikke et krav
  - bruk av flere tråder er heller ikke et krav
- skal kunne kommunisere med en nettleser via JavaScript og støtte:
   - handshake
   - små tekstmeldinger begger veier (server til klient og klient til server)
      - større meldinger er ikke et krav
    - close
      - status og reason er ikke et krav
README.md fil for løsningen med eksempel bruk av biblioteket


