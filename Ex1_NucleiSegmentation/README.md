Example 1: Segmentation of nuclei in 3D from confocal stacks
---------------------------------

In this example we'll segment nuclei in 3D from a confocal image stack and measure some basic shape properties.

![Example output](./resources/example.gif)

_**Animation showing detection and measurement of nuclei in 3D.** (Left) Image stack with background subtraction applied.. Animation cycles through slices of the z-stack.  (Right) Detected nuclei visualised with white outlines and semi-transparent overlays, colour-coded by their diameter (longest chord) measurement. Unique ID for each nucleus shown as white text._

This example introduces the following concepts:
- Loading images into the MIA
- Performing basic image processing steps
- Segmentation of objects from binarised images
- Measuring detected objects and filtering based on these measurements
- Visualising detected objects with overlays
- Exporting object measurements to spreadsheet
