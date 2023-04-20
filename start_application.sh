#!/bin/bash

# Get the path of the current directory
current_dir=$(pwd)

# Get the port numbers from the command line arguments
port_args=$(echo "$@" | grep -Eo '(--appconfig.nodePorts=)[0-9,]*' | cut -d= -f2)

# Split the port numbers into an array
IFS=',' read -ra node_ports <<< "$port_args"

# Get the hostnames from the command line arguments
hostname_args=$(echo "$@" | grep -Eo '(--appconfig.nodeHostnames=)[^,]*' | cut -d= -f2)

# Split the hostnames into an array
IFS=',' read -ra node_hostnames <<< "$hostname_args"

# Loop through the array of port numbers
for (( i=0; i<${#node_ports[@]}; i++ )); do

  # Generate the properties file for the current port number
  properties_file="application-replica$((i+1)).properties"
  echo "server.port=${node_ports[$i]}" > $properties_file
  echo "spring.datasource.url=jdbc:mysql://${node_hostnames[$i]}:3306/store$((i+1))?createDatabaseIfNotExist=true" >> $properties_file
  echo "spring.datasource.username=root" >> $properties_file
  echo "spring.datasource.password=gus101997" >> $properties_file
  echo "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect" >> $properties_file
  echo "spring.jpa.hibernate.ddl-auto=none" >> $properties_file
  echo "spring.sql.init.mode=always" >> $properties_file

  # Move the properties file to the target folder
  mv $properties_file ../backend-ecommerce/Ecommerce-back-end/src/main/resources
  # Start a new terminal window
  osascript -e "tell app \"Terminal\"
      do script \"cd '${current_dir}/../backend-ecommerce/Ecommerce-back-end' && mvn spring-boot:run -Dspring-boot.run.profiles=replica${i}\"
  end tell"
done

# Wait for the application to start
sleep 15

# Start Spring Boot application at localhost:8080
mvn spring-boot:run



