package othellocrown.account.repository;

import org.springframework.data.repository.CrudRepository;
import othellocrown.account.entity.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    List<Account> findAll();
}
