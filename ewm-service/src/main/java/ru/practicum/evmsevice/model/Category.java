package ru.practicum.evmsevice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Setter
@Getter
@Table(name = "category", schema = "public")
@EqualsAndHashCode(of = {"name"})
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Название категории не может быть пустым")
    private String name;
}
