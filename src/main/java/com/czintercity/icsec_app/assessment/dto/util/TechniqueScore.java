package com.czintercity.icsec_app.assessment.dto.util;

import com.czintercity.icsec_app.attack.entity.Technique;

public class TechniqueScore {
    public final Technique technique;
    public final Double score;

    public TechniqueScore(Technique technique, Double score) {
        this.technique = technique;
        this.score = score;
    }

    public Technique getTechnique() { return technique; }
    public Double getScore() { return score; }

    @Override
    public int hashCode() {
        return technique.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TechniqueScore) {
            return technique.equals(((TechniqueScore) other).technique);
        }
        else return false;
    }
}
