package io.github.mianalysis.example;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImageJ;

/**
 * A template MIA module.
 */ 
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)

public class TemplateModule extends Module {
    /**
     * (Optional) If we wish to put our new module into a different category to the default categories, we need to create it here. Leaving it till the {@link getCategory} method is called is too late.  Here, we create a new module category called "Examples" which will be in the first level of the available modules list.
     */
    private static final Category category = new Category("Examples", "Modules used in examples", Categories.ROOT,
            true);

    /**
     * (Optional) This main function allows us to launch a new copy of Image, start MIA and add the current module to MIA.  These steps are only necessary when running the MIA and the new module from a main function.  When run as part of  distribution, the module will be automatically included in MIA.
     * 
     * @param args Can be left blank
     */
    public static void main(String[] args) {
        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(TemplateModule.class);

    }

    /**
     * The module constructor requires us to provide the name of this module.
     * 
     * @param modules The module constructor, when called from within MIA, provides
     *                all the modules currently in the workflow as an argument.
     */
    public TemplateModule(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Template module", modules);
    }

    /**
     * The module category within MIA in which this module will be placed. We can choose any of the default categories available in io.github.mianalysis.mia.module.Categories or use one created along with this module. This template uses the "Example" category created at the top of this class.
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * Each module should include a description which will be included in the GUI (accessible by going to View / Show help panel) as well as in the automatically-generated online documentation at https://mianalysis.github.io/modules.
     */
    @Override
    public String getDescription() {
        return "";
    }

    /**
     * The method which is run as part of a workflow.  This method contains all the code for loading items from the MIA workspace, performing the action of this module and exporting any new items to the workspace.
     * 
     * @param workspace The current workspace containing all available images and objects (i.e. those previously output by earlier modules in the workflow).
     */
    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;

    }

    /**
     * Creates an instance of each parameter, each of which is stored in the "parameters" variable of the module.  Each new instance of the module will have a new set of parameters.  This method runs once, when the module is first created.
     */
    @Override
    protected void initialiseParameters() {

    }

    /**
     * Returns the currently-active parameters for this module.  The returned parameters will change depending on what other parameters are set to.  The output of this module determines the parameters that are displayed in the GUI.
     */
    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    /**
     * Measurements added to any images by this module are reported by adding their reference to an ImageMeasurementRefs collection.  When no measurements are added by this module, this method can simply return "null".  These references tell downstream modules what measurements are available for each image.  Returned references should be the original copies stored in the local "imageMeasurementRefs" object.
     */
    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    /**
     * Measurements added to any objects by this module are reported by adding their reference to an ObjMeasurementRefs collection.  When no measurements are added by this module, this method can simply return "null".  These references tell downstream modules what measurements are available for each object of a specific object collection.  Returned references should be the original copies stored in the local "objectMeasurementRefs" object.
     */
    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
       return null;

    }

    /**
     * Values added to the workspace's metadata collection by this module are reported by adding their reference to a MetadataRefs collection.  When no metadata values are added by this module, this method can simply return "null".  Metadata values are single values within a workspace that specify information such as the root filename or series number.  These references tell downstream modules what metadata is available.  Returned references should be the original copies stored in the local "metadataRefs" object.
     */
    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    /**
     * Any parent-child relationships established between objects by this module are reported by adding their reference to a ParentChildRefs collection.  When no parent-child relationships are added by this module, this method can simply return "null".  These references tell downstream modules what parent-child relationships are available.  Returned references should be the original copies stored in the local "parentChildRefs" object.
     */
    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;

    }

    /**
     * Any partner-partner relationships established between objects by this module are reported by adding their reference to a PartnerRefs collection.  When no partner-partner relationships are added by this module, this method can simply return "null".  These references tell downstream modules what partner-partner relationships are available.  Returned references should be the original copies stored in the local "partnerRefs" object.
     */    
    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    /**
     * Can be used to perform checks on parameters or other conditions to ensure the module is configured correctly.  This runs whenever a workflow is updated (e.g. a parameter in any module is changed).
     */
    @Override
    public boolean verify() {
        return true;
    }
}