# The Vector Import and Export Plugin #

This plugin enables uDig and derivates to some particular formats described below. They are licensed under GPL and not LGPL and therefore can't be packaged with udig.

## What you get with it ##

At the moment it supports import of

  * import of dxf files to feature layer. This plugin is borrowed by the great OpenJump plugin by Michaël Michaud. To see how it is used see [this post](http://jgrasstechtips.blogspot.com/2009/10/dxf-and-dwg-in-jgrass.html) and [this post](http://jgrasstechtips.blogspot.com/2009/10/some-dxfdwg-screens.html).
  * import of dwg files to feature layer. This was borrowed by the gvSig community and doesn't work a lot. But yeah, dwg is proprietary and it is hard to maintain such a plugin.
  * import of kml files to feature layer.

and export of
  * shapefiles to the JOSM openstreetmap format **.osm. To see how it is used see [this post](http://jgrasstechtips.blogspot.com/2009/12/moving-little-step-closer-to-osm.html).**

## Installation of the plugin ##

The plugin works with the uDig 1.2.x series that can be downloaded [here](http://udig.refractions.net/files/downloads/).

Its installation is manual but fast and easy:

  1. [Download the latest plugin version](http://jgrass.googlecode.com/files/eu.hydrologis.jgrass.vectorimportexport_1.0.0.201006211502.jar)
  1. Open the folder in which you installed uDig/JGrass/BeeGIS, then enter the folder named '''plugins'''. Copy the downloaded jar into the plugins folder.
  1. Restart the application.