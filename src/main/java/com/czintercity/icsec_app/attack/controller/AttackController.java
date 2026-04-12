package com.czintercity.icsec_app.attack.controller;

import com.czintercity.icsec_app.attack.entity.Tactic;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TacticRepository;
import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.HashSet;
import java.util.List;

@Controller
public class AttackController {
    private static final Logger log = LoggerFactory.getLogger(AttackController.class);

    @Autowired
    private TechniqueRepository techniqueRepository;

    @Autowired
    private TacticRepository tacticRepository;

    @GetMapping("/attack/reload")
    public ResponseEntity<String> reloadAttack(){

        log.info("Reloading MITRE ATT&CK Tactics from hardcoded file...");
        log.debug("Clearing DB...");

        techniqueRepository.deleteAll();
        tacticRepository.deleteAll();

        log.warn("MITRE database cleared.");
        log.info("Loading tactics...");

        // Set up classpath resources
        Resource tacticsResource = new ClassPathResource("mitre_attack/ics-attack-tactics.csv");
        Resource techniquesResource = new ClassPathResource("mitre_attack/ics-attack-techniques.csv");

        // Set up CSV parser
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(';')
                .withQuoteChar('"')
                .build();

        try {
            InputStreamReader tacticsInput = new InputStreamReader(tacticsResource.getInputStream());
            BufferedReader reader = new BufferedReader(tacticsInput);
            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .withCSVParser(csvParser)
                    .build()) {

                String[] record;
                while ((record = csvReader.readNext()) != null) {
                    // Create new tactic for each row
                    Tactic tactic = new Tactic();
                    tactic.setMitreId(record[0]);
                    tactic.setName(record[2]);
                    tactic.setDescription(record[3]);
                    tactic.setMitreLink(record[4]);

                    tacticRepository.save(tactic);
                }

            } catch (IOException e) {
                log.error ("Failed to read tactic CSV: {}", e.getMessage());
                return new ResponseEntity<>("Failed to read tactics file.", HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (CsvValidationException e) {
                log.error ("Invalid tactic CSV: {}", e.getMessage());
                return new ResponseEntity<>("Invalid tactics CSV file.", HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                log.error ("Error while handling tactics: {}", e.getMessage());
                return new ResponseEntity<>("Error while handling tactics.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            tacticsInput.close();
        } catch (IOException e) {
            log.error("IO Error while processing tactics: {}", e.getMessage());
            return new ResponseEntity<>("IO Error while processing tactics.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            InputStreamReader techniquesInput = new InputStreamReader(techniquesResource.getInputStream());
            BufferedReader reader = new BufferedReader(techniquesInput);

            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(csvParser)
                    .withSkipLines(1)
                    .build()){

                String[] record;
                while((record = csvReader.readNext()) != null){
                    Technique technique = new Technique();

                    // Set base fields
                    technique.setMitreId(record[0]);
                    technique.setName(record[2]);
                    technique.setDescription(record[3]);
                    technique.setMitreLink(record[4]);

                    // Set tactics
                    HashSet<Tactic> tactics = new HashSet<>();
                    for (String tacticName : record[9].split(", ")){
                        List<Tactic> tac = tacticRepository.findByName(tacticName);
                        if(!tac.isEmpty()){
                            tactics.addAll(tac);
                        }
                        else{
                            log.warn("Tactic {} not found.", tacticName);
                        }
                    }
                    technique.setTactics(tactics);
                    techniqueRepository.save(technique);
                }
            } catch (IOException e) {
                log.error("Failed to read technique CSV: {}", e.getMessage());
                return new ResponseEntity<>("Failed to read technique CSV.", HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (CsvValidationException e) {
                log.error("Invalid technique CSV: {}", e.getMessage());
                return new ResponseEntity<>("Invalid technique CSV file.", HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                log.error ("Error while handling techniques: {}", e.getMessage());
                return new ResponseEntity<>("Error while handling techniques.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            techniquesInput.close();
        }
        catch(IOException e){
            log.error ("IO Error while processing techniques: {}", e.getMessage());
            return new ResponseEntity<>("IO Error while processing techniques.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("Successfully reloaded MITRE ATT&CK.");
        return new ResponseEntity<>("MITRE ATT&CK successfully reloaded.", HttpStatus.OK);
    }
}
