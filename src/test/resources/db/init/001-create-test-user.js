// Create test user with readWrite and dbAdmin roles
// This script runs automatically when MongoDB test container is created for the first time

db = db.getSiblingDB('cpwarehouse_test');

// Check if user already exists
var existingUser = db.getUser('cpwarehouse_test_user');

if (existingUser) {
  print('User cpwarehouse_test_user already exists, skipping creation');
} else {
  // Create new user with necessary roles for testing
  db.createUser({
    user: 'cpwarehouse_test_user',
    pwd: 'cpwarehouse_test_pass',
    roles: [
      {
        role: 'readWrite',
        db: 'cpwarehouse_test'
      },
      {
        role: 'dbAdmin',
        db: 'cpwarehouse_test'
      }
    ]
  });

  print('✓ Test user cpwarehouse_test_user created successfully');
  print('✓ Roles: readWrite, dbAdmin on cpwarehouse_test database');
  print('✓ Connection: mongodb://cpwarehouse_test_user:cpwarehouse_test_pass@localhost:27018/cpwarehouse_test?authSource=cpwarehouse_test');
}
