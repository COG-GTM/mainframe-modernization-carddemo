package com.carddemo.repository.memory;

import com.carddemo.domain.User;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** USRSEC backed by a map seeded from the DUSRSECJ.jcl mock users. */
@Repository
public class InMemoryUserRepository implements UserRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, User> users = new LinkedHashMap<>();

    public InMemoryUserRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        users.clear();
        dataSet.users().forEach(user -> users.put(user.getUserId(), User.parse(user.format())));
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(users.get(RecordKeys.userId(userId)));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User save(User user) {
        users.put(RecordKeys.userId(user.getUserId()), user);
        return user;
    }

    @Override
    public boolean deleteById(String userId) {
        return users.remove(RecordKeys.userId(userId)) != null;
    }

    @Override
    public boolean existsById(String userId) {
        return users.containsKey(RecordKeys.userId(userId));
    }
}
