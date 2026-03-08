package com.czintercity.icsec_app.controls;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToMany
    @JoinTable(
            name="topic_controls",
            joinColumns = @JoinColumn(name="topic_id"),
            inverseJoinColumns = @JoinColumn(name="control_id")
    )
    Set<Control> controls;
}
