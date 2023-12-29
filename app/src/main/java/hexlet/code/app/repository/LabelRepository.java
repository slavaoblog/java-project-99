package hexlet.code.app.repository;

import hexlet.code.app.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    default void deleteLabelWithCheck(Label label) {
        if (label.getTasks().isEmpty()) {
            delete(label);
        } else {
            throw new RuntimeException("Cannot delete Label with existing Task associations");
        }
    }

    Optional<Label> findByName(String newName);
}
