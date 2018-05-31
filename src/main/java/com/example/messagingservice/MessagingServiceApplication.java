package com.example.messagingservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
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
        //Add users
        LongStream.of(1, 2, 3, 4, 5)
        .forEach(userId -> userRepository.save(new User(userId)));

        //Add connections
        HashSet set = new HashSet<>();
        LongStream.of(2, 3)
                .forEach(connectionId -> set.add(new User(connectionId)));
        userRepository.save(new User(new Long(1), set));

        HashSet s2 = new HashSet<>();
        s2.add(new User(new Long(2)));
        userRepository.save(new User(new Long(3), s2));

        //print
        userRepository.findAll().forEach(System.out::println);
    }
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

    @JoinTable(name="connections")
    @ManyToMany(fetch = FetchType.EAGER)
    Set<User> connection;

    User(Long userId) {
        id = userId;
    }
}
