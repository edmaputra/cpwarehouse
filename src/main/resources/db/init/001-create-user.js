// Create application user with readWrite and dbAdmin roles
// This script runs automatically when MongoDB container is created for the first time

db = db.getSiblingDB('cpwarehouse');

// Check if user already exists
var existingUser = db.getUser('cpwarehouse_user');

if (existingUser) {
  print('User cpwarehouse_user already exists, skipping creation');
} else {
  // Create new user with necessary roles
  db.createUser({
    user: 'cpwarehouse_user',
    pwd: 'cpwarehouse_pass',
    roles: [
      {
        role: 'readWrite',
        db: 'cpwarehouse'
      },
      {
        role: 'dbAdmin',
        db: 'cpwarehouse'
      }
    ]
  });

  print('✓ User cpwarehouse_user created successfully');
  print('✓ Roles: readWrite, dbAdmin on cpwarehouse database');
  print('✓ Connection: mongodb://cpwarehouse_user:cpwarehouse_pass@localhost:27017/cpwarehouse?authSource=cpwarehouse');
}
