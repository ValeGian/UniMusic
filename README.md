# LSMDB_UniMusic
Project for the <i>'Large-Scale and Multi-Structured Databases'</i> course. Please, check the [Documentation](Documentation.pdf) for a complete view. 

## Introduction
<b>UniMusic</b> is an application whose main purpose is to provide a song search and management service; songs that can be found on the platform are associated with links to official sources on the Internet, where users can listen to them (for example, official YouTube / Spotify source).
The application provides general information about each stored song (e.g. name, genre, artist) and offers users the ability to search and browse them, possibly applying specific
filters to narrow the search scope. Users can then express their thoughts on the songs through a "like" system and possibly add them to favorites.
In order to simplify and improve user experience, it's possible to organize songs into playlists or follow other people's playlists. Users can also follow other users to get suggestions on songs and playlists you might like.

## Software Architecture
The application has been divided into two-tiers according to the Client-Server paradigm, exploiting Java as the core programming language.

### Client Side
The client side is divided into:
- A Front-End module, in charge of
  - providing a GUI based on JavaFX for users to interact with the application.
  - communicating with the underlying middleware to retrieve information obtained by processing data stored on server side.
- A Middleware module, which communicates with the server side. It includes the logic used to communicate with the MongoDB cluster and the Neo4j database on the server, plus all the logic needed to handle and process data retrieved so that it can be used by the front end to present it to users.

### Server Side
Server composed of 3 virtual machines which hosts a MongoDB cluster and a single instance of the
Neo4j database.

[](imgs/architecture-diagram.png)
