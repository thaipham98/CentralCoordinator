#!/bin/bash

 #./start_application.sh --appconfig.nodePorts=1000,1001 --appconfig.nodeHostnames=localhost,localhost
# Get the path of the current directory
current_dir=$(pwd)

# Get the port numbers from the command line arguments
port_args=$(echo "$@" | grep -Eo '(--appconfig.nodePorts=)[0-9,]*' | cut -d= -f2)

# Split the port numbers into an array
IFS=',' read -ra node_ports <<< "$port_args"

# Get the hostnames from the command line arguments
hostname_args=$(echo "$@" | grep -Eo '(--appconfig.nodeHostnames=)[^ ]+' | cut -d= -f2)

# Split the hostnames into an array
IFS=',' read -ra node_hostnames <<< "$hostname_args"

# Set up variables
MYSQL_USER="mysql"
MYSQL_GROUP="mysql"
MYSQL_INSTALL_DIR="/usr/local/mysql"
SERVER_ID=1
MASTER_PORT=11111
MASTER_HOST=127.0.0.1
REPL_USER="replication"
REPL_PASS="password"
CENTRAL_APP_PROPERTIES="application.properties"

## Install MySQL dependencies
#brew install cmake bison ncurses
#
## Download MySQL source code
#cd /tmp
#curl -LO https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.28.tar.gz
#tar zxvf mysql-8.0.28.tar.gz
#
## Configure, build, and install MySQL
#cd mysql-8.0.28
#cmake . -DCMAKE_INSTALL_PREFIX=$MYSQL_INSTALL_DIR -DDEFAULT_CHARSET=utf8mb4 -DDEFAULT_COLLATION=utf8mb4_general_ci
#make
#make install


# Create master MySQL instance
MASTER_DATA_DIR="/usr/local/var/mysql_master"
MASTER_CONF_FILE="/usr/local/etc/my_master.cnf"

# Create the data directory and set permissions
sudo mkdir -p $MASTER_DATA_DIR
sudo chown -R $MYSQL_USER:$MYSQL_GROUP $MASTER_DATA_DIR

# Initialize the MySQL data directory
sudo $MYSQL_INSTALL_DIR/bin/mysqld --initialize-insecure --user=$MYSQL_USER --basedir=$MYSQL_INSTALL_DIR --datadir=$MASTER_DATA_DIR

# Create custom MySQL configuration file for master
echo "[mysqld]" > $MASTER_CONF_FILE
echo "port=$MASTER_PORT" >> $MASTER_CONF_FILE
echo "datadir=$MASTER_DATA_DIR" >> $MASTER_CONF_FILE
echo "socket=/tmp/mysql_master.sock" >> $MASTER_CONF_FILE
echo "server-id=999" >> $MASTER_CONF_FILE

# Start the master MySQL server with custom configuration file
sudo $MYSQL_INSTALL_DIR/bin/mysqld_safe --defaults-file=$MASTER_CONF_FILE --user=$MYSQL_USER --datadir=$MASTER_DATA_DIR &


# Generate the properties file for the current port number
properties_file="application-replica11111.properties"
echo "server.port=9999" > $properties_file
echo "spring.datasource.url=jdbc:mysql://localhost:11111/store?user=root&createDatabaseIfNotExist=true" >> $properties_file
echo "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect" >> $properties_file
echo "spring.jpa.hibernate.ddl-auto=none" >> $properties_file
echo "spring.sql.init.mode=always" >> $properties_file

# Move the properties file to the target folder
mv $properties_file ../backend-ecommerce/Ecommerce-back-end/src/main/resources
# Start a new terminal window
osascript -e "tell app \"Terminal\"
    do script \"cd '${current_dir}/../backend-ecommerce/Ecommerce-back-end' && mvn spring-boot:run -Dspring-boot.run.profiles=replica11111\"
end tell"
sleep 10

# Create replication user on master
mysql -h$MASTER_HOST -P$MASTER_PORT -u root -e "CREATE USER '$REPL_USER'@'%' IDENTIFIED WITH mysql_native_password BY '$REPL_PASS';"
mysql -h$MASTER_HOST -P$MASTER_PORT -u root -e "GRANT REPLICATION SLAVE ON *.* TO '$REPL_USER'@'%';"
mysql -h$MASTER_HOST -P$MASTER_PORT -u root -e "FLUSH PRIVILEGES;"

sleep 5

mysqldump --all-databases --single-transaction --master-data > backup.sql
# Create MySQL slave instances
for (( i=0; i<${#node_ports[@]}; i++ ))
do
    MYSQL_PORT=$((3307+$i))
    MYSQL_DATA_DIR="/usr/local/var/mysql_instance$i"
    MYSQL_CONF_FILE="/usr/local/etc/my$i.cnf"
    MYSQL_SERVER_ID=$((SERVER_ID+i))

    # Create the data directory and set permissions
    sudo mkdir -p $MYSQL_DATA_DIR
    sudo chown -R $MYSQL_USER:$MYSQL_GROUP $MYSQL_DATA_DIR

    # Initialize the MySQL data directory
    sudo $MYSQL_INSTALL_DIR/bin/mysqld --initialize-insecure --user=$MYSQL_USER --basedir=$MYSQL_INSTALL_DIR --datadir=$MYSQL_DATA_DIR

    # Create custom MySQL configuration file
    echo "[mysqld]" > $MYSQL_CONF_FILE
    echo "port=$MYSQL_PORT" >> $MYSQL_CONF_FILE
    echo "datadir=$MYSQL_DATA_DIR" >> $MYSQL_CONF_FILE
    echo "socket=/tmp/mysql$i.sock" >> $MYSQL_CONF_FILE
    echo "server-id=$MYSQL_SERVER_ID" >> $MYSQL_CONF_FILE

    # Start the MySQL server on the new port with custom configuration file
    sudo $MYSQL_INSTALL_DIR/bin/mysqld_safe --defaults-file=$MYSQL_CONF_FILE --user=$MYSQL_USER --datadir=$MYSQL_DATA_DIR &


    # Add Kafka properties to the properties file
    topics="healthcheck-${node_hostnames[$i]}-${MYSQL_PORT}, $topics"
    replicas="${node_hostnames[$i]}-${MYSQL_PORT}, $replicas"


    # Generate the properties file for the current port number
    properties_file="application-replica${MYSQL_PORT}.properties"
    echo "server.port=${node_ports[$i]}" > $properties_file
    echo "spring.datasource.url=jdbc:mysql://${node_hostnames[$i]}:${MYSQL_PORT}/store?user=root&createDatabaseIfNotExist=true" >> $properties_file
    echo "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect" >> $properties_file
    echo "spring.jpa.hibernate.ddl-auto=none" >> $properties_file
    echo "spring.sql.init.mode=always" >> $properties_file
    echo "replica.id=${node_hostnames[$i]}-${MYSQL_PORT}" >> $properties_file
    echo "healthcheck.topic=health-check-${node_hostnames[$i]}-${MYSQL_PORT}" >> $properties_file
    echo "replica.url=http://${node_hostnames[$i]}:${MYSQL_PORT}" >> $properties_file
    echo "spring.kafka.bootstrap-servers=localhost:9092" >> $properties_file
    echo "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer" >> $properties_file
    echo "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer" >> $properties_file

    # Move the properties file to the target folder
    mv $properties_file ../backend-ecommerce/Ecommerce-back-end/src/main/resources
    # Start a new terminal window
    osascript -e "tell app \"Terminal\"
        do script \"cd '${current_dir}/../backend-ecommerce/Ecommerce-back-end' && mvn spring-boot:run -Dspring-boot.run.profiles=replica${MYSQL_PORT}\"
    end tell"

    # Start the slave MySQL instances
    mysql -P$MYSQL_PORT < backup.sql
    mysql -P$MYSQL_PORT -u root -e "STOP SLAVE;"
    mysql -P$MYSQL_PORT -u root -e "CHANGE MASTER TO MASTER_HOST='$MASTER_HOST', MASTER_PORT=$MASTER_PORT, MASTER_USER='$REPL_USER', MASTER_PASSWORD='$REPL_PASS';"
    mysql -P$MYSQL_PORT -u root -e "START SLAVE;"
    mysql -P$MYSQL_PORT -u root -e "SHOW SLAVE STATUS\G"
done

echo "healthcheck.topics=${topics::-1}" > $CENTRAL_APP_PROPERTIES
echo "healthcheck.replicas=${replicas::-1}" >> $CENTRAL_APP_PROPERTIES
mv $CENTRAL_APP_PROPERTIES src/main/resources

# Download and run the Kafka server
#!/bin/bash

# Define Kafka version
KAFKA_VERSION=3.4.0

# Define download URL
KAFKA_DOWNLOAD_URL=https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_2.13-${KAFKA_VERSION}.tgz

# Define installation directory
KAFKA_INSTALL_DIR=/usr/local/kafka

# Create installation directory if it doesn't exist
if [ ! -d "${KAFKA_INSTALL_DIR}" ]; then
  sudo mkdir -p ${KAFKA_INSTALL_DIR}
fi

## Download Kafka
#sudo curl ${KAFKA_DOWNLOAD_URL} -o ${KAFKA_INSTALL_DIR}/kafka_${KAFKA_VERSION}.tgz
#
## Extract Kafka
#sudo tar -xzf ${KAFKA_INSTALL_DIR}/kafka_${KAFKA_VERSION}.tgz -C ${KAFKA_INSTALL_DIR}
#
## Delete downloaded file
#sudo rm ${KAFKA_INSTALL_DIR}/kafka_${KAFKA_VERSION}.tgz

# Set environment variables
export KAFKA_HOME=${KAFKA_INSTALL_DIR}
export PATH="$PATH:$KAFKA_HOME"

# Start ZooKeeper
#osascript -e "tell app \"Terminal\"
#  do script \"cd ${KAFKA_HOME} && bin/zookeeper-server-start.sh config/zookeeper.properties\"
#end tell"

osascript -e "tell app \"Terminal\"
  do script \"${KAFKA_HOME}/bin/zookeeper-server-start.sh ${KAFKA_HOME}/config/zookeeper.properties\"

end tell"

sleep 10

# Start Kafka
#osascript -e "tell app \"Terminal\"
#  do script \"cd ${KAFKA_HOME} && bin/kafka-server-start.sh config/server.properties\"
#end tell"

osascript -e "tell app \"Terminal\"
  do script \"${KAFKA_HOME}/bin/kafka-server-start.sh ${KAFKA_HOME}/config/server.properties\"
end tell"

# Print success message
echo "Apache Kafka ${KAFKA_VERSION} has been downloaded and started"


# Wait for the application to start
sleep 20

# Start Spring Boot application at localhost:8080
mvn spring-boot:run
