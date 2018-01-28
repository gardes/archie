package com.nedap.archie.rminfo;

import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.CPrimitiveObject;
import com.nedap.archie.aom.profile.AomProfile;
import com.nedap.archie.aom.profile.AomProfiles;
import com.nedap.archie.base.MultiplicityInterval;
import com.nedap.archie.rminfo.ModelInfoLookup;
import com.nedap.archie.rminfo.RMAttributeInfo;
import com.nedap.archie.rminfo.ReferenceModels;
import org.openehr.bmm.core.BmmClass;
import org.openehr.bmm.core.BmmContainerProperty;
import org.openehr.bmm.core.BmmModel;
import org.openehr.bmm.core.BmmProperty;
import org.openehr.bmm.core.BmmType;
import org.openehr.bmm.persistence.validation.BmmDefinitions;
import org.openehr.bmm.rmaccess.ReferenceModelAccess;

import java.text.MessageFormat;
import java.util.List;

import static org.openehr.bmm.persistence.validation.BmmDefinitions.typeNameToClassKey;

/**
 * MetaModel class that provides some opertaions for archetype validation and flattener that is either based on
 * an implementation-derived model (ModelInfoLookup) or BMM
 */
public class MetaModels implements MetaModelInterface{

    private final ReferenceModels models;
    private final ReferenceModelAccess bmmModels;
    private AomProfiles aomProfiles;

    private MetaModel selectedModel;
    private AomProfile selectedAomProfile;


    public MetaModels(ReferenceModels models, ReferenceModelAccess bmmModels) {
        this.models = models;
        this.bmmModels = bmmModels;
        aomProfiles = new AomProfiles();
    }

    /**
     * Select a meta model based on an archetype
     * @param archetype the archetype to find the model for
     * @throws NoModelSelectedException when no BMM and no ModelInfoLookup model has been found matching the archetype
     */
    public void selectModel(Archetype archetype) throws NoModelSelectedException {
        ModelInfoLookup selectedModel = null;
        BmmModel selectedBmmModel = null;
        if(models != null) {
             selectedModel = models.getModel(archetype);
        }
        if(bmmModels != null) {
             selectedBmmModel = bmmModels.getReferenceModelForClosure(BmmDefinitions.publisherQualifiedRmClosureName(archetype.getArchetypeId().getRmPublisher(), archetype.getArchetypeId().getRmPackage()));
        }

        for(AomProfile profile:aomProfiles.getProfiles()) {
            if(profile.getProfileName().equalsIgnoreCase(archetype.getArchetypeId().getRmPublisher())) {
                this.selectedAomProfile = profile;
                break;
            }
        }

        if(selectedModel == null && selectedBmmModel == null) {
            throw new ModelNotFoundException(String.format("model for %s not found", archetype.getArchetypeId().toString()));
        }
        this.selectedModel = new MetaModel(selectedModel, selectedBmmModel, selectedAomProfile);



    }

    public ModelInfoLookup getSelectedModelInfoLookup() {
        return selectedModel == null ? null : selectedModel.getSelectedModel();
    }

    public BmmModel getSelectedBmmModel() {
        return selectedModel == null ? null : selectedModel.getSelectedBmmModel();
    }

    public MetaModel getSelectedModel() {
        return selectedModel;
    }

    public ReferenceModels getReferenceModels() {
        return models;
    }

    public ReferenceModelAccess getReferenceModelAccess() {
        return bmmModels;
    }

    public boolean isMultiple(String typeName, String attributeName) {
        checkThatModelHasBeenSelected();
        return selectedModel.isMultiple(typeName, attributeName);
    }

    public boolean rmTypesConformant(String childTypeName, String parentTypeName) {
        checkThatModelHasBeenSelected();
        return selectedModel.rmTypesConformant(childTypeName, parentTypeName);
    }

    public boolean typeNameExists(String typeName) {
        checkThatModelHasBeenSelected();
        return selectedModel.typeNameExists(typeName);
    }

    public boolean attributeExists(String rmTypeName, String propertyName) {
        checkThatModelHasBeenSelected();
        return selectedModel.attributeExists(rmTypeName, propertyName);
   }

    @Override
    public boolean isNullable(String typeId, String attributeName) {
        checkThatModelHasBeenSelected();
        return selectedModel.isNullable(typeId, attributeName);
    }


    public boolean typeConformant(String rmTypeName, String rmAttributeName, String childConstraintTypeName) {
        checkThatModelHasBeenSelected();
        return selectedModel.typeConformant(rmTypeName, rmAttributeName, childConstraintTypeName);

    }

    public boolean hasReferenceModelPath(String rmTypeName, String path) {
        checkThatModelHasBeenSelected();
        return selectedModel.hasReferenceModelPath(rmTypeName, path);
    }

    private void checkThatModelHasBeenSelected() throws NoModelSelectedException {
        if(selectedModel == null) {
            throw new NoModelSelectedException("Please call the selectModel() method before trying to use MetaModels");
        }

    }

    public MultiplicityInterval referenceModelPropMultiplicity(String rmTypeName, String rmAttributeName) {
        checkThatModelHasBeenSelected();
        return selectedModel.referenceModelPropMultiplicity(rmTypeName, rmAttributeName);
    }

    public boolean validatePrimitiveType(String rmTypeName, String rmAttributeName, CPrimitiveObject cObject) {
        checkThatModelHasBeenSelected();
        return selectedModel.validatePrimitiveType(rmTypeName, rmAttributeName, cObject);
    }

    public AomProfiles getAomProfiles() {
        return aomProfiles;
    }

    public AomProfile getSelectedAomProfile() {
        return selectedAomProfile;
    }
}
