package io.github.mianalysis.example;

import java.awt.Color;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureObjectIntensity;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectCentroid;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectFill;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline; 
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.mathfunc.CumStat;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.VolumeType;
import net.imagej.ImageJ;

/**
 * An example of a MIA Module.
 * 
 * This module performs the following steps which are designed to demonstrate as many core concepts as possible:
 * 1. Takes an existing image and object set from the workspace
 * 2. For each object, it extracts the central coordinate and stores this as a new object in a new object set
 * 3. Applies a parent-child relationship between the input and created objects
 * 4. Measures the intensity of the created objects for the input image
 * 5. Displays the new old and new objects as either outlines or semi-transparent fill on the input image
 * 6. Outputs the new object set and image with displayed outlines to the workspace
 * 
 * In a normal workflow, the steps above would be better achieved using existing modules, but here this modules serves the purpose of demonstrating the various concepts a module may use.
 * 
 * The code here is all that's required to have this module be recognised by MIA and included in the available module list menu and module search.
 * 
 * All functions shown are required, but in many cases can simply return "null" if not needed (e.g. the {@link updateAndGetPartnerRefs} function for a module where no partner relationships are created).
 *
 * For a module to be automatically added to MIA, it must include the following line. This registers it with the SciJava Plugin service.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)

/**
 * The module must also extend the Module class.
 */
public class ExampleModule extends Module {
    /**
     * (Optional) If we wish to put our new module into a different category to the default categories, we need to create it here. Leaving it till the {@link getCategory} method is called is too late.  Here, we create a new module category called "Examples" which will be in the first level of the available modules list.
     */
    private static final Category category = new Category("Examples", "Modules used in examples", Categories.ROOT,
            true);

    /**
     * The names of parameters for this module.  The convention in MIA is that any public static Strings are parameter names.  Each of the parameters is described in detail in the {@link initialiseParameters} module where they are created.
     */
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_SEPARATOR = "Image and object output";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String OVERLAY_SEPARATOR = "Overlay controls";
    public static final String OVERLAY_MODE = "Overlay mode";
    public static final String LINE_WIDTH = "Line width";
    public static final String OPACITY = "Opacity";

    /**
     * Parameter choices are defined as interfaces containing each choice as a String as well as a String array containing all values.
     */
    public interface OverlayModes {
        String FILL = "Fill";
        String OUTLINES = "Outlines";

        String[] ALL = new String[] { FILL, OUTLINES };

    }

    /**
     * Measurements output by each module are, by convention, also listed in an interface as Strings.
     */
    public interface Measurements {
        String INTENSITY = "Intensity";

    }

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
        AvailableModules.addModuleName(ExampleModule.class);

    }

    /**
     * The module constructor requires us to provide the name of this module. In this case, we're just calling it "Example module".
     * 
     * @param modules The module constructor, when called from within MIA, provides
     *                all the modules currently in the workflow as an argument.
     */
    public ExampleModule(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Example module", modules);
    }

    /**
     * The module category within MIA in which this module will be placed. We can choose any of the default categories available in io.github.mianalysis.mia.module.Categories or use one created along with this module. In this case, we're using a custom category called "Examples", which was assigned to the "category" variable.
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
        return "An example module demonstrating some of the key concepts of a MIA Module.  This particular module will (1) take an image and object set, (2) calculate the centroid of each object, (3) apply parent-child relationships between the input and centroid objects, (4) measure the input image intensity of the centroid objects, (5) create an overlay showing the input and centroid objects and (6) export the centroid objects and overlay image to the MIA workspace.";
    }

    /**
     * The method which is run as part of a workflow.  This method contains all the code for loading items from the MIA workspace, performing the action of this module and exporting any new items to the workspace.
     * 
     * @param workspace The current workspace containing all available images and objects (i.e. those previously output by earlier modules in the workflow).
     */
    @Override
    public Status process(Workspace workspace) {
        // Getting parameter values
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String overlayMode = parameters.getValue(OVERLAY_MODE, workspace);
        int lineWidth = parameters.getValue(LINE_WIDTH, workspace);
        int opacity = parameters.getValue(OPACITY, workspace);

        // Getting the input image from the MIA workspace.  Images in MIA are stored as the "Image" class, which itself acts as a wrapper for other image storage classes.  The main image types in MIA are "ImagePlusImage", which stores image data as an ImageJ ImagePlus format and "ImgPlusImage", which uses the ImgLib2 image format.  Future development of MIA will see a shift from ImagePlusImage to ImgPlusImage.  Both types are capable of returning ImagePlus and ImgPlus images.  The Image class also holds information such as measurements that can be accessed by downstream modules.
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Getting the input objects from the MIA workspace.  Object collections are stored in the Objs class, whereby each individual Obj can be accessed by a unique ID number.  Objs also hold information on the spatiotemporal region in which objects exist - this is typically defined by the image from which they were initially detected.  
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Creating the output object set. Every Objs object set contains spatial and temporal information about the available space. Since this information is likely the same as that stored in the input object set, we can provide that as a reference.
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // Iterating over each Obj object in the input Objs object collection.
        for (Obj inputObject : inputObjects.values()) {
            // Each individual Obj stores the coordinates for that region at a single timepoint.  It can also store measurements for that object.

            // For each input object, an output centroid Obj is created.  New objects can be created from the output Objs object collection.  The coordinates in each Obj can be stored as a pointlist, quadtree or octree depending on the shape of that object.  Pointlist works well for very thin (e.g. pixel wide) objects and isolated points (such as the centroids here), while quadtree is better for larger, solid objects.  Octree is best for large solid 3D objects specifically with isotropic spatial resolution in XY and Z.  For fluorescence images where resolution in XY is typically much higher than Z, quadtree storage is usually better.
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);

            // Getting the centroid location of the input object as a double-precision "Point" object.
            Point<Double> centroid = inputObject.getMeanCentroid();

            // Coordinates in MIA are stored as integers, so the centroid is added to the output centroid object after rounding of XY and Z coordinates to integer coordinates.  The full (double) precision coordinates could be stored as Measurements for the output (or input) object, but these would only be accessible by modules specifically designed to accept measurements for single coordinates (for example, the AddFromPositionMeasurement image overlay module) and in the output Excel file.
            try {
                outputObject.add((int) Math.round(centroid.x), (int) Math.round(centroid.y),
                        (int) Math.round(centroid.z));
            } catch (PointOutOfRangeException e) {
                // Only coordinates within the predefined spatial limits of an Objs object collection can be added.  These limits typically correspond to the dimensions of the image from which objects were initially detected.
                MIA.log.writeError(e);
            }

            // Each Obj exists in a single timepoint.  This sets the timepoint of the output centroid object to the same as the input object.  Although not required here, hor physical objects that persist over multiple timepoints, we can create parent "track" objects, which link all indiviudal timepoint instances of a single physical object together.
            outputObject.setT(inputObject.getT());

            // The centroid object is assigned as a child of the input parent object.  This allows them to be assigned the same colour in the overlays later on.
            inputObject.addChild(outputObject);
            outputObject.addParent(inputObject);

        }

        // Measuring intensity for each centroid object.  Iterating over each output centroid object.
        for (Obj outputObject : outputObjects.values()) {
            // The MeasureObjectIntensity module has a public method that allows the intensity of a single object to be measured for a specified image.  This returns a cumulative statistic (CumStat) object, from which we can extract statistics about the intensities of all coordinates in the specified object.  Here, the object only has a single point, so mean, min, max and sum will all have the same value.
            CumStat cs = MeasureObjectIntensity.measureIntensity(outputObject, inputImage, false);

            // Creating a new "Measurement" object to store the intensity of the centroid object.  As noted above, all statistics (bar the standard deviation) will have the same value, so the "mean" is used.
            Measurement intensity = new Measurement(Measurements.INTENSITY, cs.getMean());

            // Adding the measurement to the output object.
            outputObject.addMeasurement(intensity);

        }

        // Before creating the overlays, we must calculate the colours for each overlay object. Here, we will use random colours assigned by the input object ID. The output (centroid) objects will also be colour-coded by their parent (input) object ID. This way both overlay components should have the same colour.  First, we determine the hues for each object as these are the same, irrespective of opacity.  Hues are stored in a HashMap for which the Integer key is the ID of each object.
        HashMap<Integer, Float> inputHues = ColourFactory.getIDHues(inputObjects, true);

        // Next, generating colours for each of the input hues.  As with hues, these are stored in a HashMap, where each Integer key is the ID of each object.  The colour will depend on the opacity, so may be different depending on whether the overlay mode is "Fill" or "Outline".
        HashMap<Integer, Color> inputColours;
        switch (overlayMode) {
            case OverlayModes.FILL:
            default:
                inputColours = ColourFactory.getColours(inputHues, opacity);
                break;
            case OverlayModes.OUTLINES:
                inputColours = ColourFactory.getColours(inputHues);
                break;
        }

        // The output centroid objects are always displayed a single points, so the colour generation will always be the same.
        HashMap<Integer, Color> outputColours = ColourFactory
                .getColours(ColourFactory.getParentIDHues(outputObjects, inputObjectsName, true));

        // Creating the output image using the ImageFactory class.  The ImageFactory will create a MIA "Image" class object.  The ImageFactory will determine if the "Image" should be an "ImagePlusImage" or "ImgPlusImage" depending on what type of image type it is given.  Here, an ImagePlus is passed as a argument, so it will be an ImagePlusImage.  By using the abstract Image class, which can output both ImagePlus or ImgPlus, modules should work irrespective of how the image data is stored.
        Image outputImage = ImageFactory.createImage(outputImageName, inputIpl.duplicate());

        // Getting the ImagePlus for the output image.  The ImagePlus is currently used by many overlay modules.
        ImagePlus outputIpl = outputImage.getImagePlus();

        // Running the relevant method from the separate AddObjectFill or AddObjectOutline modules depending on what parameter was selected in the GUI.
        switch (overlayMode) {
            case OverlayModes.FILL:
                AddObjectFill.addOverlay(outputIpl, inputObjects, inputColours, false, true);
                break;
            case OverlayModes.OUTLINES:
                AddObjectOutline.addOverlay(outputIpl, inputObjects, 1, lineWidth, inputColours,
                        false, true);
                break;
        }

        // Adding the output centroid object's single coordinate as a spot to the overlay
        String size = AddObjectCentroid.PointSizes.MEDIUM;
        String type = AddObjectCentroid.PointTypes.DOT;
        AddObjectCentroid.addOverlay(outputIpl, outputObjects, outputColours, size, type, false, true);

        // Adding the output image and Objs object collection to the current workspace, so they will be available to all downstream modules.
        workspace.addImage(outputImage);
        workspace.addObjects(outputObjects);

        // If "Show output" (eyeball button to the left of a module in "Editing view") is enabled, the output image and an ImageJ results table showing the measurements will be displayed when the module is run.
        if (showOutput) {
            outputImage.show();
            outputObjects.showMeasurements(this, modules);
        }

        // The module returns a "PASS" value to confirm it completed successfully
        return Status.PASS;

    }

    /**
     * Creates an instance of each parameter, each of which is stored in the "parameters" variable of the module.  Each new instance of the module will have a new set of parameters.  This method runs once, when the module is first created.
     */
    @Override
    protected void initialiseParameters() {
        // Parameters in this method can be defined in any order, but by convention are initialised in order of appearance.  At a minimum, each parameter requires the name of the parameter and a reference to the module in which it's placed (typically just "this").  Some parameters will also require default values to be specified.  It's also possible to include descriptions of each parameter which will be included in the GUI help panel (accessible by going to Help > Show help panel) as well as the automatically-generated documentation at // https://mianalysis.github.io/modules.

        // Here, the first parameter is the top separator which will appear in the parameters panel of MIA.  This simply shows a blue line across the parameter panel with the name of the separator.  This parameter can't be edited by the user.
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));

        // This parameter allows an image already in the workspace to be selected.  When rendered, it will show a drop-down menu showing all the available image names.
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        // Likewise, this parameter will show a list of all available object names (i.e. those that have been output by previous modules in the workflow)
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        // The second separator defines the output section of the parameter panel
        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));

        // This parameter allows a name for the output image (in this case, the overlay image) to be specified.  This parameter is rendered as a text input box.  Subsequent modules will be able to access this image by selecting the specified name as an input.
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        // Likewise, this parameter specifies the name of the output (here, object centroid) objects.
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        // The final parameter separator is for parameters that control the output overlay image.
        parameters.add(new SeparatorP(OVERLAY_SEPARATOR, this));

        // This parameter lets the user select what type of overlay will be applied to the overlay image.  This is a fixed choice-type parameter and as such, we specify both the default option from the {@OverlayModes} interface (here, the "Fill" option) as well as the String array containing all possible choices (also from the {@value OverlayModes} interface).
        parameters.add(new ChoiceP(OVERLAY_MODE, this, OverlayModes.FILL, OverlayModes.ALL));

        // If the overlay mode is set to "Outline", the user will be able to specify the line width as an integer.  Although the visibility of this parameter in the GUI is dependent on which overlay mode is selected, we still need to initialise it.  Numeric-type parameters require a default value to be specified (here, "1").
        parameters.add(new IntegerP(LINE_WIDTH, this, 1));

        // Similarly, if the overlay mode is set to "Fill", the user can specify the opacity of the fill layer.  The default value here is "50".
        parameters.add(new IntegerP(OPACITY, this, 50));

    }

    /**
     * Returns the currently-active parameters for this module.  The returned parameters will change depending on what other parameters are set to.  The output of this module determines the parameters that are displayed in the GUI.
     */
    @Override
    public Parameters updateAndGetParameters() {
        // A new Parameters object is created for the returned parameters.  The parameters added to this should be the original copies held in the main "parameters" object for this module, so that any changes to the parameters are retained (i.e. copies of parameters shouldn't be added to "returnedParameters").  Note: If all parameters are always returned (i.e. none depend on what other parameters are set to), the "parameters" object can be returned.
        Parameters returnedParameters = new Parameters();

        // Any parameters which are always shown, irrespective of how other parameters are set, can be directly added to "returnedParameters"
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OVERLAY_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OVERLAY_MODE));

        // Depending on how "Overlay mode" is set, either the "Opacity" or "Line width" parameters will be added to "returnedParameters"
        switch ((String) parameters.getValue(OVERLAY_MODE, null)) {
            case OverlayModes.FILL:
                returnedParameters.add(parameters.getParameter(OPACITY));
                break;
            case OverlayModes.OUTLINES:
                returnedParameters.add(parameters.getParameter(LINE_WIDTH));
                break;
        }

        return returnedParameters;

    }

    /**
     * Measurements added to any images by this module are reported by adding their reference to an ImageMeasurementRefs collection.  When no measurements are added by this module, this method can simply return "null".  See {"updateAndGetObjectMeasurementRefs"} for an equivalent example which does return measurement references.  These references tell downstream modules what measurements are available for each image.  Returned references should be the original copies stored in the local "imageMeasurementRefs" object.
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
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        // For each measurement added to an image, an ObjMeasurementRef object is created and added to the returned reference collection.  As with parameters, the ObjMeasurementRef is stored in a collection within the module called "objectMeasurementRefs".  The original copy of the ObjMeasurementRef should be taken from "objectMeasurementRefs" to ensure 
        ObjMeasurementRef measurementRef = objectMeasurementRefs.getOrPut(Measurements.INTENSITY);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, null);
        measurementRef.setObjectsName(outputObjectsName);
        returnedRefs.add(measurementRef);

        return returnedRefs;

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
        ParentChildRefs returnedRefs = new ParentChildRefs();

        // Getting the current names of parent and child objects
        String parentObjectsName = parameters.getValue(INPUT_OBJECTS, null);
        String childObjectsName = parameters.getValue(OUTPUT_OBJECTS, null);
        
        // Creating a new parent child reference in the "parentChildRefs" object and immediately adding this to "returnedRefs"
        returnedRefs.add(parentChildRefs.getOrPut(parentObjectsName,childObjectsName));
        
        return returnedRefs;

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