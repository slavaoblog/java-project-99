package hexlet.code.app.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskStatusDTO {

    private Long id;

    @Size(min = 1)
    private String name;

    @Size(min = 1)
    private String slug;

    private LocalDate createdAt;
}
