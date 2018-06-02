package com.example.messagingservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.LongStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JpaRepositoryTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    public List<User> userEntitySeeder(){
        List<User> userList = new ArrayList<>();
        LongStream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .forEach(userId -> userList.add(new User(Long.valueOf(userId))));
        return userList;
    }

    @Test
    public void testUserCreateAndReadOperations() {
        //given - no users

        //when  - tried to findAll
        Integer total = userRepository.findAll().size();
        //then  - size = 0
        assertThat(total).isEqualTo(0);

        //given - one user created
        entityManager.persist(userEntitySeeder().get(0));
        entityManager.flush();
        //when  - findOne
        User found = userRepository.findById(new Long(1)).get();
        //then  - it is the same user
        assertThat(found.getId()).isEqualTo(1);


    }

    @Test(expected = NoSuchElementException.class)
    public void testUserDeleteOperation() {
        //given - two users created
        List<User> users = userEntitySeeder();
        entityManager.persist(users.get(0));
        entityManager.persist(users.get(1));
        //when  - one user deleted
        userRepository.delete(users.get(1));
        User u2Found = userRepository.findById(Long.valueOf(2)).get();
    }

    @Test(expected = PersistenceException.class)
    public void testConnectionCreationWithoutUserCreated() {
        List<User> users = userEntitySeeder();
        Connection connection = new Connection(new ConnectionId(users.get(0), users.get(1)));
        entityManager.persist(connection);
        entityManager.flush();
    }

    @Test
    public void testConnectionCreateAndRead() {
        List<User> users = userEntitySeeder();
        //given - no connections

        //when  - tried to findAll
        Integer total = connectionRepository.findAll().size();
        //then  - size = 0
        assertThat(total).isEqualTo(0);

        //given - at least two users created - connection could be created
        entityManager.persist(users.get(0));
        entityManager.persist(users.get(1));
        entityManager.persist(users.get(2));

        ConnectionId ci12 = new ConnectionId(users.get(0), users.get(1));
        ConnectionId ci13 = new ConnectionId(users.get(0), users.get(2));

        Connection c1 = new Connection(ci12);
        Connection c2 = new Connection(ci13);
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
        //when  - findOne
        List<User> found = connectionRepository.findConnectionsByUserId(Long.valueOf(1));
        //then  - it is the same user
        assertThat(found.size()).isEqualTo(2);
        assertThat(found.get(0).getId()). isEqualTo(users.get(1).getId());
        assertThat(found.get(1).getId()). isEqualTo(users.get(2).getId());
    }

    @Test(expected = EntityExistsException.class)
    public void testDuplicateConnectionCreation() {
        List<User> users = userEntitySeeder();
        //given - at two users created - connection could be created
        entityManager.persist(users.get(0));
        entityManager.persist(users.get(1));

        Connection c1 = new Connection(new ConnectionId(users.get(0), users.get(1)));
        Connection c2 = new Connection(new ConnectionId(users.get(0), users.get(1)));
        entityManager.persist(c1);
        entityManager.flush();
        entityManager.persist(c2);
        entityManager.flush();
    }

    @Test
    public void testMultipleConnectionForUser() {
        //given - at two users created - connection could be created
        List<User> users = userEntitySeeder();
        entityManager.persist(users.get(0));
        entityManager.persist(users.get(1));
        entityManager.persist(users.get(2));

        Connection c1 = new Connection(new ConnectionId(users.get(0), users.get(1)));
        Connection c2 = new Connection(new ConnectionId(users.get(0), users.get(2)));
        entityManager.persist(c1);
        entityManager.flush();
        entityManager.persist(c2);
        entityManager.flush();
        List<Connection> connections = connectionRepository.findAll();
        assertThat(connections.size()).isEqualTo(2);
        //TODO: No accessors in Connection
    }

    @Test
    public void testConnectionDelete() {
        //given - at two users created - connection could be created
        List<User> users = userEntitySeeder();
        entityManager.persist(users.get(0));
        entityManager.persist(users.get(1));
        entityManager.persist(users.get(2));

        ConnectionId ci12 = new ConnectionId(users.get(0), users.get(1));
        ConnectionId ci13 = new ConnectionId(users.get(0), users.get(2));

        Connection c1 = new Connection(ci12);
        Connection c2 = new Connection(ci13);
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
        //when  - deleted and attempted to findOne
        connectionRepository.delete(c1);

        List<User> found = connectionRepository.findConnectionsByUserId(Long.valueOf(1));
        //then  - it is the same user
        assertThat(found.size()).isEqualTo(1);
        assertThat(found.get(0).getId()). isEqualTo(users.get(2).getId());
        //TODO: Think better way of unit testing this
    }

}
