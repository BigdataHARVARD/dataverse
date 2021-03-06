package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.search.SearchFields;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;

@ViewScoped
@Named("AdvancedSearchPage")
public class AdvancedSearchPage implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(AdvancedSearchPage.class.getCanonicalName());

    @EJB
    DataverseServiceBean dataverseServiceBean;

    @EJB
    DatasetFieldServiceBean datasetFieldService;

    private Dataverse dataverse;
    private String dataverseIdentifier;
    private List<MetadataBlock> metadataBlocks;
    private Map<Long, List<DatasetFieldType>> metadataFieldMap = new HashMap();
    private List<DatasetFieldType> metadataFieldList;
    private String dvFieldName;
    private String dvFieldDescription;
    private String dvFieldAffiliation;
    private List<String> dvFieldSubject;
    private String dsPublicationDate;
    private String fileFieldName;
    private String fileFieldDescription;
    private String fileFieldFiletype;
    private String fileFieldVariableName;
    private String fileFieldVariableLabel;

    public void init() {

        if (dataverseIdentifier != null) {
            dataverse = dataverseServiceBean.findByAlias(dataverseIdentifier);
        }
        if (dataverse == null) {
            dataverse = dataverseServiceBean.findRootDataverse();
        }
        metadataBlocks = dataverse.getMetadataBlocks();
        this.metadataFieldList = datasetFieldService.findAllAdvancedSearchFieldTypes();

        for (MetadataBlock mdb : metadataBlocks) {

            List dsfTypes = new ArrayList();
            for (DatasetFieldType dsfType : metadataFieldList) {
                if (dsfType.getMetadataBlock().getId().equals(mdb.getId())) {
                    dsfTypes.add(dsfType);
                }
            }
            metadataFieldMap.put(mdb.getId(), dsfTypes);
        }

    }

    public String find() throws IOException {
        List<String> queryStrings = new ArrayList();
        queryStrings.add(constructDataverseQuery());
        queryStrings.add(constructDatasetQuery());
        queryStrings.add(constructFileQuery());
        String dataverseSubtree = "";
        if (dataverse != null) {
            if (!dataverse.equals(dataverseServiceBean.findRootDataverse())) {
                dataverseSubtree = "&id=" + dataverse.getId();
            }
        }

        String returnString = "/dataverse.xhtml?q=" + constructQuery(queryStrings, false, false) + dataverseSubtree + "&faces-redirect=true";
        logger.fine(returnString);
        return returnString;
    }

    private String constructDatasetQuery() {
        List<String> queryStrings = new ArrayList();
        for (DatasetFieldType dsfType : metadataFieldList) {
            if (dsfType.getSearchValue() != null && !dsfType.getSearchValue().equals("")) {
                queryStrings.add(constructQuery(dsfType.getSolrField().getNameSearchable(), dsfType.getSearchValue()));
            } else if (dsfType.getListValues() != null && !dsfType.getListValues().isEmpty()) {
                List<String> listQueryStrings = new ArrayList();
                for (String value : dsfType.getListValues()) {
                    listQueryStrings.add(dsfType.getSolrField().getNameSearchable() + ":" + "\"" + value + "\"");
                }
                queryStrings.add(constructQuery(listQueryStrings, false));
            }
        }
        if (StringUtils.isNotBlank(dsPublicationDate)) {
            queryStrings.add(constructQuery(SearchFields.DATASET_PUBLICATION_DATE, dsPublicationDate));
        }
        return constructQuery(queryStrings, true);

    }

    private String constructDataverseQuery() {
        List queryStrings = new ArrayList();
        if (StringUtils.isNotBlank(dvFieldName)) {
            queryStrings.add(constructQuery(SearchFields.DATAVERSE_NAME, dvFieldName));
        }

        if (StringUtils.isNotBlank(dvFieldAffiliation)) {
            queryStrings.add(constructQuery(SearchFields.DATAVERSE_AFFILIATION, dvFieldAffiliation));
        }

        if (StringUtils.isNotBlank(dvFieldDescription)) {
            queryStrings.add(constructQuery(SearchFields.DATAVERSE_DESCRIPTION, dvFieldDescription));
        }

        if (dvFieldSubject != null && !dvFieldSubject.isEmpty()) {
            List<String> listQueryStrings = new ArrayList();
            for (String value : dvFieldSubject) {
                listQueryStrings.add(SearchFields.DATAVERSE_SUBJECT + ":" + "\"" + value + "\"");
            }
            queryStrings.add(constructQuery(listQueryStrings, false));
        }

            return constructQuery(queryStrings, true);
        }

    private String constructFileQuery() {
        List queryStrings = new ArrayList();
        if (StringUtils.isNotBlank(fileFieldName)) {
            queryStrings.add(constructQuery(SearchFields.FILE_NAME, fileFieldName));
        }

        if (StringUtils.isNotBlank(fileFieldDescription)) {
            queryStrings.add(constructQuery(SearchFields.FILE_DESCRIPTION, fileFieldDescription));
        }

        if (StringUtils.isNotBlank(fileFieldFiletype)) {
            queryStrings.add(constructQuery(SearchFields.FILE_TYPE_SEARCHABLE, fileFieldFiletype));
        }

        if (StringUtils.isNotBlank(fileFieldVariableName)) {
            queryStrings.add(constructQuery(SearchFields.VARIABLE_NAME, fileFieldVariableName));
        }

        if (StringUtils.isNotBlank(fileFieldVariableLabel)) {
            queryStrings.add(constructQuery(SearchFields.VARIABLE_LABEL, fileFieldVariableLabel));
        }

        return constructQuery(queryStrings, true);
    }

    private String constructQuery(List<String> queryStrings, boolean isAnd) {
        return constructQuery(queryStrings, isAnd, true);
    }

    private String constructQuery(List<String> queryStrings, boolean isAnd, boolean surroundWithParens) {
        StringBuilder queryBuilder = new StringBuilder();

        int count = 0;
        for (String string : queryStrings) {
            if (!StringUtils.isBlank(string)) {
                if (++count > 1) {
                    queryBuilder.append(isAnd ? " AND " : " OR ");
                }
                queryBuilder.append(string);
            }
        }

        if (surroundWithParens && count > 1) {
            queryBuilder.insert(0, "(");
            queryBuilder.append(")");
        }

        return queryBuilder.toString().trim();
    }

    private String constructQuery(String solrField, String userSuppliedQuery) {

        StringBuilder queryBuilder = new StringBuilder();
        String delimiter = "[\"]+";

        List<String> queryStrings = new ArrayList();

        if (userSuppliedQuery != null && !userSuppliedQuery.equals("")) {
            if (userSuppliedQuery.contains("\"")) {
                String[] tempString = userSuppliedQuery.split(delimiter);
                for (int i = 1; i < tempString.length; i++) {
                    if (!tempString[i].equals(" ") && !tempString[i].isEmpty()) {
                        queryStrings.add(solrField + ":" + "\"" + tempString[i].trim() + "\"");
                    }
                }
            } else {
                StringTokenizer st = new StringTokenizer(userSuppliedQuery);
                while (st.hasMoreElements()) {
                    queryStrings.add(solrField + ":" + st.nextElement());
                }
            }
        }

        if (queryStrings.size() > 1) {
            queryBuilder.append("(");
        }

        for (int i = 0; i < queryStrings.size(); i++) {
            if (i > 0) {
                queryBuilder.append(" ");
            }
            queryBuilder.append(queryStrings.get(i));
        }

        if (queryStrings.size() > 1) {
            queryBuilder.append(")");
        }

        return queryBuilder.toString().trim();
    }

    public Dataverse getDataverse() {
        return dataverse;
    }

    public void setDataverse(Dataverse dataverse) {
        this.dataverse = dataverse;
    }

    public String getDataverseIdentifier() {
        return dataverseIdentifier;
    }

    public void setDataverseIdentifier(String dataverseIdentifier) {
        this.dataverseIdentifier = dataverseIdentifier;
    }

    public List<MetadataBlock> getMetadataBlocks() {
        return metadataBlocks;
    }

    public void setMetadataBlocks(List<MetadataBlock> metadataBlocks) {
        this.metadataBlocks = metadataBlocks;
    }

    public Map<Long, List<DatasetFieldType>> getMetadataFieldMap() {
        return metadataFieldMap;
    }

    public void setMetadataFieldMap(Map<Long, List<DatasetFieldType>> metadataFieldMap) {
        this.metadataFieldMap = metadataFieldMap;
    }

    public String getDvFieldName() {
        return dvFieldName;
    }

    public void setDvFieldName(String dvFieldName) {
        this.dvFieldName = dvFieldName;
    }

    public String getDvFieldDescription() {
        return dvFieldDescription;
    }

    public void setDvFieldDescription(String dvFieldDescription) {
        this.dvFieldDescription = dvFieldDescription;
    }

    public String getDvFieldAffiliation() {
        return dvFieldAffiliation;
    }

    public void setDvFieldAffiliation(String dvFieldAffiliation) {
        this.dvFieldAffiliation = dvFieldAffiliation;
    }

    public List<String> getDvFieldSubject() {
        return dvFieldSubject;
    }

    public void setDvFieldSubject(List<String> dvFieldSubject) {
        this.dvFieldSubject = dvFieldSubject;
    }

    public Collection<ControlledVocabularyValue> getDvFieldSubjectValues() {
        DatasetFieldType subjectType = datasetFieldService.findByName(DatasetFieldConstant.subject);
        return subjectType.getControlledVocabularyValues();
    }

    public String getDsPublicationDate() {
        return dsPublicationDate;
    }

    public void setDsPublicationDate(String dsPublicationDate) {
        this.dsPublicationDate = dsPublicationDate;
    }

    public String getFileFieldName() {
        return fileFieldName;
    }

    public void setFileFieldName(String fileFieldName) {
        this.fileFieldName = fileFieldName;
    }

    public String getFileFieldDescription() {
        return fileFieldDescription;
    }

    public void setFileFieldDescription(String fileFieldDescription) {
        this.fileFieldDescription = fileFieldDescription;
    }

    public String getFileFieldFiletype() {
        return fileFieldFiletype;
    }

    public void setFileFieldFiletype(String fileFieldFiletype) {
        this.fileFieldFiletype = fileFieldFiletype;
    }

    public String getFileFieldVariableName() {
        return fileFieldVariableName;
    }

    public void setFileFieldVariableName(String fileFieldVariableName) {
        this.fileFieldVariableName = fileFieldVariableName;
    }

    public String getFileFieldVariableLabel() {
        return fileFieldVariableLabel;
    }

    public void setFileFieldVariableLabel(String fileFieldVariableLabel) {
        this.fileFieldVariableLabel = fileFieldVariableLabel;
    }

}
