# Docker Compose Setup with Nginx Load Balancer

This setup creates a Docker Compose environment with two instances of the Spring Boot application and an Nginx load balancer.

## Components

1. **API Services (api1 and api2)**
   - Two identical instances of the Spring Boot application
   - Each instance runs in its own container
   - They are configured to use each other as fallback servers

2. **Nginx Load Balancer**
   - Distributes incoming traffic between the two API instances
   - Provides a single entry point for clients (port 8080)
   - Includes a health check endpoint at `/health`

3. **Docker Network**
   - All services are connected through a bridge network
   - Services can communicate with each other using their service names

## Configuration Files

### docker-compose.yml
- Defines the services, networks, and resource limits
- Maps port 8080 on the host to the Nginx load balancer
- Sets environment variables for the API services

### nginx.conf
- Configures Nginx as a load balancer
- Sets up round-robin load balancing between api1 and api2
- Includes proxy settings and timeout configurations

## How to Run

1. Make sure Docker and Docker Compose are installed on your system
2. Navigate to the project directory
3. Run the following command to start all services:

```bash
docker-compose up -d
```

4. To check the status of the services:

```bash
docker-compose ps
```

5. To view logs from all services:

```bash
docker-compose logs
```

6. To view logs from a specific service (e.g., nginx):

```bash
docker-compose logs nginx
```

7. To stop all services:

```bash
docker-compose down
```

## Accessing the Application

- The application is accessible at `http://localhost:8080`
- Available endpoints:
  - `GET /payments-summary` - Returns a summary of payment statistics
  - `POST /payments` - Processes a payment request
  - `GET /health` - Health check endpoint (returns 200 OK)

## Scaling

If you need to scale the application beyond the two instances defined in the docker-compose.yml file, you can use the following command:

```bash
docker-compose up -d --scale api1=2 --scale api2=2
```

Note: If you scale the services, you'll need to update the nginx.conf file to include the additional instances.

## Troubleshooting

- If you encounter issues with the services not starting, check the logs using `docker-compose logs`
- Ensure that port 8080 is not already in use on your host machine
- If the API services are not responding, check if they're healthy using `docker-compose ps`