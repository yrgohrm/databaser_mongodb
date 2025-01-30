# A simple MongoDB example

This is a super basic example to get started with MongoDB and their Java Sync
Driver.

## Running

First of all you need a database. This simple example has been tested
with MongoDB 8 and get one up and running quickly with Docker.

```SH
docker run -d --name mongodb \
	-e MONGO_INITDB_ROOT_USERNAME=mongoadmin \
	-e MONGO_INITDB_ROOT_PASSWORD=secretP4ssword \
	mongo:latest
```

Then you need to create a database and user by running the 
`init.mongodb.js` script in a mongoshell.

Finally you need to build the application and run it. To do this you need to
set up three environment variables. `DB_USERNAME`, `DB_PASSWORD` and `DB_HOST`.

The following will probably do it for cmd:
```
set DB_USERNAME=shopUser
set DB_PASSWORD=secretP4ssword
set DB_HOST=localhost
```

The equivalent for PowerShell:
```PowerShell
$env:DB_USERNAME="shopUser"
$env:DB_PASSWORD="secretP4ssword"
$env:DB_HOST="localhost"
```

The equivalent for bash:
```SH
export DB_USERNAME="shopUser"
export DB_PASSWORD="secretP4ssword"
export DB_HOST="localhost"
```

Now run (in the same shell that you set up the environment variables in):
```
mvn package
mvn exec:exec
```
