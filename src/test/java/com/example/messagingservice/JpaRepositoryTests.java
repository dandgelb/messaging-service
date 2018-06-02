package com.example.messagingservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JpaRepositoryTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

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
}
