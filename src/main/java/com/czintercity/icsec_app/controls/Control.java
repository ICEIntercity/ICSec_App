package com.czintercity.icsec_app.controls;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Control {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String code;
    private String description;

    @ManyToMany
    private Set<Topic> topics;
}
