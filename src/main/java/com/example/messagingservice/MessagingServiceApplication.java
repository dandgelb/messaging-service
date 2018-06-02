package com.example.messagingservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.LongStream;

@SpringBootApplication
public class MessagingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessagingServiceApplication.class, args);
	}
}

@Component
class SampleUserDataCLR implements CommandLineRunner {
    private final UserRepository userRepository;

    SampleUserDataCLR(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        LongStream.of(1, 2, 3, 4, 5)
        .forEach(userId -> userRepository.save(new User(userId)));
        userRepository.findAll().forEach(System.out::println);
    }
}
@RestController
class ConnectionController {
    @Autowired
    ConnectionRepository connectionRepository;

    @GetMapping("/users/{id}/connections")
    public ResponseEntity<List<User>> getConnectionsByUserId(@PathVariable(value = "id") Long userId) {
        List<User> connections = connectionRepository.findConnectionsByUserId(userId);
        return new ResponseEntity<List<User>>(connections, HttpStatus.OK);
    }
}

@Repository
interface ConnectionRepository extends JpaRepository<Connection, Long> {
    @Query("select c.id.connection from Connection c where c.id.user.id = ?1")
    List<User> findConnectionsByUserId(Long userId);

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
    @GeneratedValue
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
class Connection {

    @EmbeddedId
    private ConnectionId id;
}
