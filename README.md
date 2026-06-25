# FIFA World Cup 2026 Backend API

A robust Spring Boot REST API for managing FIFA World Cup group and knockout match results, team information, player statistics, and match goals.

## Features

✅ **Team Management** - Create and manage teams with country information
✅ **Group Management** - Organize teams into groups and track standings
✅ **Player Management** - Track player information and statistics
✅ **Match Management** - Create matches and update results
✅ **Goal Tracking** - Record goals and track top scorers
✅ **Match Statistics** - Detailed match analytics
✅ **API Documentation** - Interactive Swagger UI
✅ **Comprehensive Validation** - Input validation at all levels
✅ **Exception Handling** - Consistent error responses
✅ **Logging** - Full application logging

## Technology Stack

- **Framework**: Spring Boot 4.1.0
- **Language**: Java 17
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Jakarta Validation
- **API Docs**: Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Utilities**: Lombok
- **Testing**: JUnit 5, Mockito

## System Requirements

- Java 17 JDK or higher
- MySQL 8.0 or higher
- Maven 3.6.0 or higher
- 512MB RAM minimum
- Internet connection for Maven dependencies

## Installation & Setup

### 1. Clone the Project

```bash
git clone <repository-url>
cd world-cup-26
```

### 2. Create MySQL Database

```sql
CREATE DATABASE worldcup26;
CREATE USER 'worldcup'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON worldcup26.* TO 'worldcup'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Update Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/worldcup26
spring.datasource.username=worldcup
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=create  # Use 'create' for fresh install, 'update' for existing
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 6. Access API Documentation

**Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
**OpenAPI JSON**: `http://localhost:8080/api/v3/api-docs`

## API Usage Examples

### Create a Team

```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -d '{
    "name": "France",
    "countryCode": "FRA",
    "confederation": "UEFA"
  }'
```

### Create a Group

```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Group A",
    "stage": "GROUP_STAGE"
  }'
```

### Add Team to Group

```bash
curl -X POST http://localhost:8080/api/groups/1/teams/1 \
  -H "Content-Type: application/json" \
  -d '{
    "position": 1
  }'
```

### Create a Player

```bash
curl -X POST http://localhost:8080/api/players \
  -H "Content-Type: application/json" \
  -d '{
    "teamId": 1,
    "name": "Kylian Mbappé",
    "jerseyNumber": 10,
    "position": "FWD",
    "height": 178,
    "weight": 73
  }'
```

### Create a Match

```bash
curl -X POST http://localhost:8080/api/matches \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "groupId": 1,
    "matchType": "GROUP_MATCH",
    "matchDate": "2026-06-20T14:00:00",
    "venue": "Stadium Name",
    "status": "SCHEDULED"
  }'
```

### Update Match Result

```bash
curl -X PUT http://localhost:8080/api/matches/1/result \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamGoals": 2,
    "awayTeamGoals": 1,
    "status": "COMPLETED",
    "homeTeamShots": 15,
    "awayTeamShots": 8,
    "homeTeamPossessionPercentage": 65.5,
    "awayTeamPossessionPercentage": 34.5
  }'
```

### Record a Goal

```bash
curl -X POST http://localhost:8080/api/goals \
  -H "Content-Type: application/json" \
  -d '{
    "matchId": 1,
    "playerId": 1,
    "scoringTeamId": 1,
    "minute": 25,
    "goalType": "REGULAR",
    "isPenaltyGoal": false
  }'
```

### Get Group Standings

```bash
curl http://localhost:8080/api/groups/1/standings
```

## API Endpoints

### Teams
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teams` | Create team |
| GET | `/api/teams` | Get all teams |
| GET | `/api/teams/{id}` | Get team by ID |
| GET | `/api/teams/search?term=X` | Search teams |
| PUT | `/api/teams/{id}` | Update team |
| DELETE | `/api/teams/{id}` | Delete team |

### Groups
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groups` | Create group |
| GET | `/api/groups` | Get all groups |
| GET | `/api/groups/{id}` | Get group by ID |
| GET | `/api/groups/{id}/standings` | Get standings |
| PUT | `/api/groups/{id}` | Update group |
| DELETE | `/api/groups/{id}` | Delete group |

### Players
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/players` | Create player |
| GET | `/api/players` | Get all players |
| GET | `/api/players/team/{teamId}` | Get team players |
| GET | `/api/players/top-scorers` | Get top scorers |
| PUT | `/api/players/{id}` | Update player |
| DELETE | `/api/players/{id}` | Delete player |

### Matches
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/matches` | Create match |
| GET | `/api/matches` | Get all matches |
| GET | `/api/matches/{id}` | Get match by ID |
| GET | `/api/matches/upcoming` | Get upcoming matches |
| PUT | `/api/matches/{id}/result` | Update result |
| DELETE | `/api/matches/{id}` | Cancel match |

### Goals
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/goals` | Record goal |
| GET | `/api/goals/match/{matchId}` | Get match goals |
| GET | `/api/goals/player/{playerId}` | Get player goals |
| DELETE | `/api/goals/{id}` | Delete goal |

## Database Schema

### Key Tables
- **teams** - Team information
- **groups** - Tournament groups
- **group_teams** - Teams in groups with standings
- **players** - Player information
- **matches** - Match details
- **goals** - Goal records
- **match_statistics** - Match analytics

## Error Handling

All errors follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "code": 404
}
```

### Common Error Codes
- `400` - Bad Request (validation error)
- `404` - Not Found
- `409` - Conflict (duplicate resource)
- `500` - Internal Server Error

## Data Validation

### Team Validation
- Name: 2-100 characters, unique
- Country Code: 2-3 characters, unique
- Confederation: Optional, max 50 characters

### Player Validation
- Name: 2-100 characters
- Jersey Number: 1-99, unique per team
- Height: 100-250 cm
- Weight: 30-150 kg
- Position: One of GK, DEF, MID, FWD

### Match Validation
- Teams: Cannot be the same
- Match Date: Cannot be in the past (for scheduled matches)
- Status: SCHEDULED, ONGOING, COMPLETED, CANCELLED
- Type: GROUP_MATCH, KNOCKOUT, SEMI_FINAL, FINAL, THIRD_PLACE

### Goal Validation
- Minute: 1-150
- Player must belong to scoring team
- Goal Type: REGULAR, PENALTY, OWN_GOAL, HEADER, FREE_KICK

## Configuration Options

### Logging Levels
Modify `application.properties`:

```properties
logging.level.root=INFO
logging.level.org.springframework.web=DEBUG
logging.level.org.sgd.worldcup=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Database Connection Pool
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

## Development

### Running Tests

```bash
mvn test
```

### Code Quality

```bash
mvn clean verify
```

### Build Production JAR

```bash
mvn clean package
```

## Frontend Integration (Angular)

The backend API is designed to be consumed by an Angular frontend:

1. **Base URL**: `http://localhost:8080/api`
2. **CORS**: Ready for configuration (see SecurityConfig)
3. **API Documentation**: Available at `/swagger-ui.html`
4. **Response Format**: Consistent JSON with success and error handling

### Angular Integration Example

```typescript
// app.service.ts
constructor(private http: HttpClient) {}

getTeams() {
  return this.http.get('/api/teams');
}

createTeam(team: TeamDTO) {
  return this.http.post('/api/teams', team);
}
```

## Performance Optimization

- **Indexes**: Strategic database indexes for fast queries
- **Connection Pooling**: HikariCP for efficient connections
- **Batch Processing**: Configured for inserts/updates
- **Lazy Loading**: JPA associations optimized
- **Query Optimization**: Custom query methods

## Security Considerations

- Input validation prevents SQL injection
- Exception handling prevents information leakage
- Transaction management ensures data consistency
- CORS configuration ready for frontend

## Troubleshooting

### Database Connection Error
```
Check MySQL is running
Verify credentials in application.properties
Ensure database exists: CREATE DATABASE worldcup26;
```

### Port Already in Use
```bash
# Change port in application.properties
server.port=8081
```

### Migration Issues
```bash
# Recreate database
DROP DATABASE worldcup26;
CREATE DATABASE worldcup26;
mvn clean install
```

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Swagger/OpenAPI](https://swagger.io/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

## Contributing

When adding new features:
1. Create entity classes with validation
2. Create DTOs for API contracts
3. Create repositories with custom queries
4. Create services with business logic
5. Create controllers with endpoints
6. Add unit and integration tests

## License

This project is part of the World Cup 2026 management system.

## Support

For issues or questions, please contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: 2026-06-20  
**Status**: In Development

