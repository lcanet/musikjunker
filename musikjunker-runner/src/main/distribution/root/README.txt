Musikjunker - yet another jukebox
---------------------------------

run.
	
	java -jar musikjunker.jar [-port N]
	use -port to specify HTTP port
	
log.
	java -Dlog4j.properties=file:somewhere-you-place-your-log4j.properties
	by default use console logging
	
configure.
	read musikjunker.properties
	configure music path and DB connection settings