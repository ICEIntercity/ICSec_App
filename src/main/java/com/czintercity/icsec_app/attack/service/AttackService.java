package com.czintercity.icsec_app.attack.service;

import com.czintercity.icsec_app.attack.entity.Tactic;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TacticRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttackService {
    private final TacticRepository tacticRepository;

    public AttackService(TacticRepository tacticRepository) {
        this.tacticRepository = tacticRepository;
    }

    /**
     * Returns all tactics sorted by MITRE ID, each paired with its techniques sorted
     * by MITRE ID. Used to populate the technique relevance assessment and coverage heatmap views.
     */
    @Transactional
    public LinkedHashMap<Tactic, List<Technique>> getTacticsWithTechniques() {
        List<Tactic> tactics = tacticRepository.findAll(Sort.by("mitreId"));
        LinkedHashMap<Tactic, List<Technique>> result = new LinkedHashMap<>();
        for (Tactic tactic : tactics) {
            List<Technique> sorted = new ArrayList<>(tactic.getTechniques());
            sorted.sort(Comparator.comparing(Technique::getMitreId));
            result.put(tactic, sorted);
        }
        return result;
    }
}