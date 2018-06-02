package com.example.messagingservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Test
    public void testUserCreateAndReadOperations() {
        //given - no users

        //when  - tried to findAll
        Integer total = userRepository.findAll().size();
        //then  - size = 0
        assertThat(total).isEqualTo(0);

        //given - one user created
        User user = new User(new Long(1));
        entityManager.persist(user);
        entityManager.flush();
        //when  - findOne
        User found = userRepository.findById(new Long(1)).get();
        //then  - it is the same user
        assertThat(found.getId()).isEqualTo(1);


    }

    @Test(expected = NoSuchElementException.class)
    public void testUserDeleteOperation() {
        //given - two users created
        User u2 = new User(Long.valueOf(2));
        User u3 = new User(Long.valueOf(3));
        entityManager.persist(u2);
        entityManager.persist(u3);
        //when  - one user deleted
        userRepository.delete(u3);
        User u2Found = userRepository.findById(Long.valueOf(3)).get();
    }

    @Test(expected = PersistenceException.class)
    public void testConnectionCreationWithoutUserCreated() {
        Connection connection = new Connection(new ConnectionId(new User(Long.valueOf(1)), new User(Long.valueOf(2))));
        entityManager.persist(connection);
        entityManager.flush();
    }

    @Test
    public void testConnectionCreateAndRead() {
        //given - no connections

        //when  - tried to findAll
        Integer total = connectionRepository.findAll().size();
        //then  - size = 0
        assertThat(total).isEqualTo(0);

        //given - at least two users created - connection could be created
        User u1 = new User(Long.valueOf(1));
        User u2 = new User(Long.valueOf(2));
        User u3 = new User(Long.valueOf(3));
        entityManager.persist(u1);
        entityManager.persist(u2);
        entityManager.persist(u3);

        ConnectionId ci12 = new ConnectionId(u1, u2);
        ConnectionId ci13 = new ConnectionId(u1, u3);

        Connection c1 = new Connection(ci12);
        Connection c2 = new Connection(ci13);
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
        //when  - findOne
        List<User> found = connectionRepository.findConnectionsByUserId(Long.valueOf(1));
        //then  - it is the same user
        assertThat(found.size()).isEqualTo(2);
        assertThat(found.get(0).getId()). isEqualTo(u2.getId());
        assertThat(found.get(1).getId()). isEqualTo(u3.getId());
    }

    @Test(expected = EntityExistsException.class)
    public void testDuplicateConnectionCreation() {
        //given - at two users created - connection could be created
        entityManager.persist(new User(Long.valueOf(1)));
        entityManager.persist(new User(Long.valueOf(2)));

        Connection c1 = new Connection(new ConnectionId(new User(Long.valueOf(1)), new User(Long.valueOf(2))));
        Connection c2 = new Connection(new ConnectionId(new User(Long.valueOf(1)), new User(Long.valueOf(2))));
        entityManager.persist(c1);
        entityManager.flush();
        entityManager.persist(c2);
        entityManager.flush();
    }

    @Test
    public void testMultipleConnectionForUser() {
        //given - at two users created - connection could be created
        entityManager.persist(new User(Long.valueOf(1)));
        entityManager.persist(new User(Long.valueOf(2)));
        entityManager.persist(new User(Long.valueOf(3)));

        Connection c1 = new Connection(new ConnectionId(new User(Long.valueOf(1)), new User(Long.valueOf(2))));
        Connection c2 = new Connection(new ConnectionId(new User(Long.valueOf(1)), new User(Long.valueOf(3))));
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
        User u1 = new User(Long.valueOf(1));
        User u2 = new User(Long.valueOf(2));
        User u3 = new User(Long.valueOf(3));
        entityManager.persist(u1);
        entityManager.persist(u2);
        entityManager.persist(u3);

        ConnectionId ci12 = new ConnectionId(u1, u2);
        ConnectionId ci13 = new ConnectionId(u1, u3);

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
        assertThat(found.get(0).getId()). isEqualTo(u3.getId());
        //TODO: Think better way of unit testing this
    }

}
