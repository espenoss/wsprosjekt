Oppgavetekst

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

Referanser:
  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers
  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_client_applications
