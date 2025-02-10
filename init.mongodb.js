// Setup script for mongo shell

use('shop');

// replace the password with a better one
db.createUser({
    user: 'shopUser',
    pwd: 'secretP4ssword',
    roles: [
        { role: 'readWrite', db: 'shop' }
    ]
});
