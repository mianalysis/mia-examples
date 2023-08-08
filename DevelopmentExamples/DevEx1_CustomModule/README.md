Development example 1: Creation of a custom MIA module
---------------------------------

This example contains the code required to create a custom MIA module.  

Two example modules are provided:
- "ExampleModule.java" is a full, working example module which performs a variety of operations that a module may use.  These include loading images and objects from the workspace, creating new images and objects, assigning relationships between objects and creating new measurements.
- "TemplateModule.java" contains the minimum content required to create a Module, but itself doesn't perform any operations.  This file can be used as a basis for any new modules.

For the purpose of testing, a MIA workflow file ("DevEx1_CustomModules.mia") and example image ("DevEx1_ImageAndNuclei.tif") are provided which work with the example module.  To launch a copy of MIA with access to "ExampleModule", the main method in ExampleModule.java can be run.  

Under normal operation, custom modules can be added to MIA via two different routes:
- Via a pull request on the MIA repository (https://github.com/mianalysis/mia).  This will add the module to the main MIA distribution available from the ImageJ Updater.
- As a standalone .jar file added to the "plugins" folder of a copy of Fiji.  This will be automatically detected when MIA launches and included in the list of available modules.
