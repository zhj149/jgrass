

## Last week report ##

I finally completed the last part of my project, which is a proper navigation view for udig. The navigation view takes care of navigating scale, time and a vertical axis (if available). Once a map that has timesteps is loaded, the navigation view gets aware of that and proposes the available timesteps.

To give a better feeling about what I am talking, I had to create a screencast, in which I show how to connect to a online netcdf file from a dods server, then navigate the temporal layers of the dataset.

I also added an world overview panel and a geonames browser, to be able to see always where I am and to be able to navigate the world by places of names. These are not related in any way to netcdf, but it was a while that I wanted to see them in uDig :)

Enjoy!

[uDig navigation view and netcdf support video](http://www.youtube.com/v/5kpiJDWtZzc&hl=en&fs=1&)

## Friday, 7th of August ##

Finished up the first part of the internals of the navigation engine for the whole of udig. Now the viewportmodel is aware of time and Z values, apart of resolution, scale and CRS as it had been before. What that means is that any datastore that is able to supply different data for different timesteps and elevation values, can do that now. So that will apply to any type of data capable of that, so probably WMS and service like that may be able to exploit it. For now, I know netcdf, so let me show you some screen of how it should work.

For example I loaded two layer, and you might now already have noted that one of the two has both time and elevation information, while the other one just time:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/navigation_01.png' /></p>


For now I implemented just the possibility to select a layer and from there set the map window to a time or elevation value available to that layer. So rightclicking on it gives me:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/navigation_02.png' /></p>

and in the case of both time and elevation layer I get:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/navigation_03.png' /></p>


The first combo shows timesteps where the data are available, whereas the second shows what we (actually I) til now called elevation. Now we see that in this case it is really depth (the minus):

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/navigation_04.png' /></p>


whereas in the case of time only:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/navigation_05.png' /></p>


Once I push ok, the viewportmodel is notified about the change in the current time and elevation and the renderers behave to adapt to that:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/navigation_06.png' /></p>



Next step is to work on performance and to implement an cool gui for the navigation...


## Wednesday, 29th of July ##

Finally I finished the export mechanism of netcdf files from JGrass. Yes, JGrass, this feature doesn't work for uDig, since uDig doesn't now that much the concept of scientific usage of data, which is why it has JGrass.
What I wanted, was to create an export of gridded dataset (speak GRASS rasters) to netcdf datasets. It should have also the possibility to add all possible metadata needed to be CF compliant.

To get and overview about what came out, let me take you through an export tour:

Let's assume you have 6 maps of groundwater surface representing the level of the surface in different timesteps, that you want to export to netcdf file:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_01.png' /></p>

Rightclick in the layer's view and select export:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_02.png' /></p>

The first panel that appears, is the panel of the global parameters for the netcdf file, such as:
**grass mapset from which to take the maps to export** path for the output file to create
**definition of the time frame** definition of the levels
**definition of additional metadata**

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_03.png' /></p>

So for example you can choose the mapset from the catalog, since you already loaded your map:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_04.png' /></p>

and fill tou whatever needed:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_05.png' /></p>

Note that I decided to define the timesteps and levels at the begin, which means basically that the user has to know what he is doing at the begin of everything. But this is really something necessary in the case of netcdf in general.

Once completed, the second panel gives the possibility to add the actual variables and layers to the netcdf dataset.

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_06.png' /></p>


So first a variable has to be defined. Since I supplied a temporal frame, I am asked if the new variable will have knowledge of time. As you will see later, this is necessary for adding layers to the variable.
If I had supplied also levels, here they would have asked me if the variable is composed of different levels.

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_07.png' /></p>

After pushing ok, the new (and empty) variable appears in my variables list. I am now allowed to add layers to the variable by selecting the variable and pushing the plus button under the layers list. That will open a dialog that asks me to select the timestep (remember, I added time) and the raster map that will give the data to that variable for that particular timestep.

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_08.png' /></p>

I then add all the layers for the proper timestep. By selecting a layer in the layer's list, in the last part of the panel some info about the layer and the variable appear.

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_09.png' /></p>

Just to show a time-independent variable, I add another variable:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_10.png' /></p>

As you can see, the new layer dialog in this case doesn't ask for the timestep.

After selecting the raster map and having defined the new layer for the new variable, I finally push the Finish button of the export wizard and the data get dumped to the netcdf file.

To view the result, I drag the netcdf file back into uDig:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdfexp_11.png' /></p>

The proper variables appear and as you can see in the layer view, the water\_surface layer icon shows that the layer has timesteps available, which is right.




## Thursday, 10th of July ##

  1. **What did I get done this week?**

> This week I finished the bigger part of the netcdf writing engine. It still misses a proper gui and some metadata handling, which I hope to finish up in the next days.


> Now it is possible to export rasters from a GRASS workspace and "bundle" them as netcdf dataset, defining for each raster the timestep and elevation (or just one of the two or none, whichever applies). Since the only coordinate reference system that is equally dealth with in both netcdf and geotools is the EPSG:4326, the rasters are reprojected to lat/lon before exporting them to the new netcdf.

> So for example I can take this elevation model,



<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdf_write_orig.png' /></p>

> assign it a date and an elevation level (doesn't make that much sense right now, it is just to test the driver) and create a netcdf dataset from it. Here is how the resulting dataset is then visualized in [ncBrowse](http://www.epic.noaa.gov/java/ncBrowse/) a visualization tool by the [NOAA](http://www.esdim.noaa.gov/).




<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdf_write_ncbrowse.png' /></p>

> One thing that you note for sure, is that the date wasn't parsed properly. I chose 2009-01-01 00:00, which should have been 2008-12-31 23:00, but instead ncBrowse fails to parse it for some reason and makes it a nice 16th of May of the year 40971. You might ask why I think ncBrowse parses that wrong. I visualized the same output with other two tools. The first was [Panolpy](http://www.giss.nasa.gov/tools/panoply), a very handy tool, but I wasn't able to figure out how to zoom to the whole dataset yet and seing this small portion of territory on the whole world doesn't work. Anyway Panolpy shows up the date properly. And the same applies for the data browser that is supplied with the netcdf java library.


  * side the use of external visualization tools was just a check, since uDig at this point already has the netcdf visualization capabilities. And here we go:




<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/netcdf_write.png' /></p>

> Oh, I love GIS... :-)

  1. #2 **What do I plan on doing next week?**


> Next week I will partecipate fulltime at the [udig code grind](http://udig-news.blogspot.com/2009/06/udig-code-grind-2009-coming-up.html).

> Anyway, next things to do are:
    * first create a structure from the standard variable names that can then be used to define the exported data in a better way
    * second create the wizard for the export to netcdf (the design is already there).

  1. #3 **Are there any blocking issues?**

> Nothing.

## Friday, 3rd of July ##

  1. **What did I get done this week?**

> Not that much. Decided exchange the tasks of gui implementation with the writing drivers, as it was in my initial plan. Took the time to dive into netcdf data creation.

  1. #2 **What do I plan on doing next week?**

> Write the midterm report and design the framework/rules for netcdf export in JGrass. This will affect JGrass extentions more than uDig, since uDig doesn't have a clear raster format for geophysical data. The quantity of metadata mandatory for the creation of a netcdf dataset leads me to think that it is better base the export on the well defined GRASS grid data instead of permitting to export any any-banded image of any type. All this will lead to the creation of a new plugin, since the ones existing up to now work in uDig without the need of JGrass extentions, and I want it to stay like that. Instead the netcdf export plugin will need also the JGrass extentions.

  1. #3 **Are there any blocking issues?**

> Nothing.


## Thursday, 25th of June ##

  1. **What did I get done this week?**

> Implemented the netcdf service and renderer in uDig. It is now able to load lacal netcdf files and remote netcdf data from opendap servers. Currently the first timestep and first elevation level is visualized. So following the udig logic now I can drag netcdf files into the map or catalog and also drag opendap urls the same way to view those datasets.

> I started with putting time and elevation near the variable/layer names, but as you can see from the following image (which uses [this remote data](http://mersea.dmi.dk/thredds/dodsC/ecoop/BalticBestEstimate.html)), that started to be way too crowded in the catalog and layer view:


<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/time_elevation_near_varname.png' /></p>



> Therefore I decided to use the icons to show those infos:


<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/time_elevation_in_icon.png' /></p>




> As you can see the icons change in the cases if a layer has time and elevation or just one of them. For example here the icons tell that the layer elev despite of its name has different timesteps defined, but not different elevation layers. Instead sea\_water\_velocity has both elevation and timesteps defined.



> If in the dataset two variables represent the X and Y components of a vectorial quantity, it is possible to show them as vector layer, as you can see here in the case of the **sea\_water\_velocity** layer, which in the dataset doesn't exist as such, but is created from the **layers uvel and vvel**, which are the layers of components. The arrows show direction while the raster in the background shows the instensity:


<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/gsoc2009/vectors.png' /></p>


  1. #2 **What do I plan on doing next week?**

> Solve existing issues and review the rendering system that needs some love. Probably the best way to go is to plug it into the JGrass rendering mechanism, which has already a complete system to deal with colortables.

  1. #3 **Are there any blocking issues?**

> There are some problems on the drag and drop mechanics of the udig service. Already in talk with the community. Since the problem is not critical I will leave it behind and solve it along the way.

## Friday, 19th of June ##

  1. **What did I get done this week?**

> Basically my week is summarized in the page: ["the choice to go with ncWMS"]

> 2. **What do I plan on doing next week?**

> Implement the migrated ncWMS code to the uDig plugins and implement the uDig netcdf service.

> 3. **Are there any blocking issues?**

> Nope



## Friday, 12th of June ##

  1. **What did I get done this week?**

> Still writing prototypes for reading different data types and parsing their metadata. I got datasets from the netcdf-cf community that threw me into trouble. Most of all knowing that a lat or a lon axis can be a 2 dimensional grid made me feel really ignorant. Been very interesting/important to see datasets I never played with. Still have to decide whether to support also vector data or not. The initial aim was grids, but we have the framework to support also the vector layers. Well, I think this will be seattled this week. It much depends on what I will choose to integrate for the reading part. I am saying this because I in touch with the [ncWMS](http://www.resc.rdg.ac.uk/trac/ncWMS/) developer that adviced me to have a look at his code and I have to say that he was damn right. A lot of nice stuff to study and reuse in there. So I will for now prototype around with this second option and then finally decide for the reading part.

> 2. **What do I plan on doing next week?**

> Finish with the ncWMS prototyping and hopefully then be able to start with the implementation of the uDig netcdf service.

> 3. **Are there any blocking issues?**

> Again not blocking, but stalling. The netcdf format is a dataset nirvana, a dataformat addicted's heaven. Understand the format and get in touch with its community is slow but fruitful, so more time than I guessed is needed for this first step.



## Friday, 5th of June ##

  1. **What did I get done this week?**

  * Setting up [code repo](https://svn.dev.cocos.bz/svnroot/jgrass/jgrass3.0/community/moovida/gsoc2009_netcdf/).
  * Creating udig plugins for netcdf libs.
  * Prototyping and exploring data structures.
  * Found out there is too much possible. Need to limit or better think about how to integrate in the udig catalog. Put thoughts here ["thoughts and tests"].

> 2. **What do I plan on doing next week?**

> Begin implementing the uDig netcdf service.

> 3. **Are there any blocking issues?**

> Not blocking but stalling. Netcdf format is complex and open to almost everything. Needed to scramble the timeline and anticipate some parts in order to have a good visible result in the next two weeks, so that there is something to "talk" about with the community and the mentor. Anyway things are proceding as expected.


## Friday, 29th of May ##

  1. **What did I get done this week?**

> Had to finish up the last piece of starting documentation about [NetCDF Climate and Forecast (CF) Metadata Convention](http://cf-pcmdi.llnl.gov/). Nothing amazing to show yet. :)

> 2. **What do I plan on doing next week?**

> Quickly setup the udig plugins with the necessary libs in it to finally start prototyping along. After that I will first switch to the raster styling issues, mandatory to get on with the netcdf task.

> 3. **Are there any blocking issues?**

> Nothing.