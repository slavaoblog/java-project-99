package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {

    private Long id;

    private JsonNullable<Integer> index;

    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> title;

    private JsonNullable<String> content;

    private JsonNullable<String> status;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    private JsonNullable<Set<Long>> taskLabelIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}
