package com.czintercity.icsec_app.attack;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Technique {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // MITRE ATT&CK Properties
    private String name;
    private String mitre_id;
    private String description;

    public String toString(){
        return String.format("Technique [id='%d';mitre_id='%s',name='%s';description='%s';]", id, mitre_id, name, description);
    }

    public String getName() {
        return(this.name);
    }

    public String getMitreId(){
        return(this.mitre_id);
    }

    public String getDescription(){
        return(this.description);
    }
}
