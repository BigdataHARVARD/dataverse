/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author ellenk
 */
@Entity
public class ControlledVocabAlternate implements Serializable {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(columnDefinition="TEXT") 
    private String strValue;

    public String getStrValue() {
        return strValue;
    }
    public void setStrValue(String strValue) {
        this.strValue = strValue;
        
    }
    
    @ManyToOne
    private DatasetFieldType datasetFieldType;
    public DatasetFieldType getDatasetFieldType() {
        return datasetFieldType;
    }
    public void setDatasetFieldType(DatasetFieldType datasetFieldType) {
        this.datasetFieldType = datasetFieldType;
    }
    
    @ManyToOne
    private ControlledVocabularyValue controlledVocabularyValue;

    public ControlledVocabularyValue getControlledVocabularyValue() {
        return controlledVocabularyValue;
    }

    public void setControlledVocabularyValue(ControlledVocabularyValue controlledVocabularyValue) {
        this.controlledVocabularyValue = controlledVocabularyValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ControlledVocabAlternate other = (ControlledVocabAlternate) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}
