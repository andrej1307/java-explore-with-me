package ru.practicum.evmsevice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "comments", schema = "public")
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @Column(name = "event_id", nullable = false)
    private Integer eventId;
    @Column(name = "text", nullable = false)
    private String text;
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "edited_on")
    private LocalDateTime editedOn;
}
