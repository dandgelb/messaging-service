package com.example.messagingservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@SpringBootApplication
public class MessagingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessagingServiceApplication.class, args);
	}
}

class CustomErrorType {

    private String errorMessage;

    public CustomErrorType(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}


@RestController
class ConnectionController {
    @Autowired
    ConnectionRepository connectionRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/users/{id}/connections")
    public ResponseEntity<List<User>> getConnectionsByUserId(@PathVariable(value = "id") Long userId) {
        List<User> connections = connectionRepository.findConnectionsByUserId(userId);
        return new ResponseEntity<List<User>>(connections, HttpStatus.OK);
    }



    @PostMapping("/users/{id}/connections")
    public ResponseEntity<?> createConnection(@PathVariable(value = "id") Long userId, @RequestBody User connection) {
        try {
            userRepository.findById(userId).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity(new CustomErrorType("Unable to create connection. User with id " + userId + " is not found."),
                    HttpStatus.NOT_FOUND);
        }

        try {
            userRepository.findById(connection.getId()).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity(new CustomErrorType("Unable to create connection. User with id " + connection.getId() + " is not found."),
                    HttpStatus.NOT_FOUND);
        }

        Connection c1 = new Connection(new ConnectionId(new User(userId), connection));
        int size = connectionRepository.findConnection(userId, connection.getId());
        if(size > 0) {
            return new ResponseEntity(new CustomErrorType("Connection(" + userId + ", " + connection.getId() + ") already exists."),
                    HttpStatus.CONFLICT);
        }

        connectionRepository.save(c1);
        return new ResponseEntity<Connection>(HttpStatus.CREATED);

    }

    @DeleteMapping("/users/{id}/connections")
    public ResponseEntity<?> deleteConnection(@PathVariable(value = "id") Long userId, @RequestBody User connection) {
        Connection c1 = new Connection(new ConnectionId(new User(userId), connection));
        int size = connectionRepository.findConnection(userId, connection.getId());
        if(size > 0) {
            connectionRepository.delete(c1);
        }
        return new ResponseEntity<Connection>(HttpStatus.OK);
    }
}

@Repository
interface ConnectionRepository extends JpaRepository<Connection, Long> {
    @Query("select c.id.connection from Connection c where c.id.user.id = ?1")
    List<User> findConnectionsByUserId(Long userId);

    @Query("select count(c) from Connection c where c.id.user.id = ?1 and c.id.connection.id = ?2")
    int findConnection(Long userId, Long connectionId);
}

@RepositoryRestResource
interface UserRepository extends JpaRepository<User, Long> {

}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    @Id
    private Long id;
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class ConnectionId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "user_id",insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "connection_id",insertable = false, updatable = false)
    private User connection;
}

@Entity
@Table
@NoArgsConstructor
class Connection {

    @EmbeddedId
    private ConnectionId id;

    Connection(ConnectionId connection) {
        this.id = connection;
    }
}

@Entity
@Data
@NoArgsConstructor
class Message {
    @Id
    @GeneratedValue
    private Long id;

    private String subject;

    private String messageBody;

    @ManyToOne
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

}

@Entity
@Data
@NoArgsConstructor
class MessageRecipient {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id", insertable = false, updatable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    private User recipient;
}