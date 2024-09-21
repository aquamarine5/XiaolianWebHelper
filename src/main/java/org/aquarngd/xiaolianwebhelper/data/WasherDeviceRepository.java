package org.aquarngd.xiaolianwebhelper.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WasherDeviceRepository extends JpaRepository<WasherDevice, Integer> {
    @NonNull Optional<WasherDevice> findById(@NonNull Integer id);
}
