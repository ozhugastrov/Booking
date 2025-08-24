# Simple Booking service
This is a simple booking service that prevents double bookings. When a conflict occurs, it suggests alternative dates and records the conflict information via Kafka into PostgreSQL for further analysis.
## You could run it in two possible way:
- locally if you have postgres and kafka services
- or inside a container 

### To run it locally:
1. Create Kafka topic `booking`
2. Update Postgres and Kaka connections in [application.json](https://github.com/ozhugastrov/Booking/blob/1973c13f8161bbc049f0ab416e23799271acf4e7/src/main/resources/application.json)
3. Use `sbt run` command to start service 

### To run inside a container:
1. Check if all ports used in [docker-compose.yml](https://github.com/ozhugastrov/Booking/blob/1973c13f8161bbc049f0ab416e23799271acf4e7/docker-compose.yml) are free and update if some of them are in used
2. Use `docker compose up` to run service

### Query examples

```
curl -X POST "http://localhost:8080/api/v1/bookings/book" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": 123,
    "startDate": "2025-09-04",
    "endDate": "2025-09-08"
  }'
```

```
curl -X GET "http://localhost:8080/api/v1/bookings/123"
```