package ru.practicum.evmsevice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "eventss", schema = "public")
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "annotation", nullable = false)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "confirmedrequests")
    private Integer confirmedRequests;
    @Column(name = "createdon", nullable = false)
    private LocalDateTime createdon;
    @Column(name = "description")
    private String description;
    @Column(name = "eventdate", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User initiator;
    @Column(name = "lat")
    private Float lat;
    @Column(name = "lon")
    private Float lon;
    @Column(name = "paid")
    private Boolean paid;
    @Column(name = "participantlimit")
    private Integer participantLimit;
    @Column(name = "publishedon")
    private LocalDateTime publishedOn;
    @Column(name = "requestmoderation")
    private Boolean requestModeration;
    @Column(name = "state")
    private String state;
    @Column(name = "title")
    private String title;
}
