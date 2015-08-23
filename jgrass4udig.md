# Introduction #

It is since the joining of the uDig community that we wanted to slowly migrate all of JGrass as smooth as possible into uDig. This process has been slow and effort consuming, but is now at a good point. See for example [this](http://jgrasstechtips.blogspot.com/2010/10/first-small-jgrass-tools-migrate-to.html) and [this](http://jgrasstechtips.blogspot.com/2010/12/another-bit-of-grace-of-udig-to.html).

Lately we broke our head against the walls of licenses and against the wall of missing fundings. [HydroloGIS](http://www.hydrologis.com) is now putting an effort to do the necessary to make the final great steps.

# JGrasstool & OMS3 #

Since the moment we started to adopt the [OMS3 modelling framework](http://www.javaforge.com/project/oms), we decided to bring JGrass at a library level. The [jgrasstools](http://www.jgrasstools.org) project got born, creating  perfect spot to only think about the processing level of GIS, without having to think about the heavy weight of the whole eclipse framework of uDig. That experience went quite well and a first GUI was born for the project, which was never relased because of missing funds to create the project structure. That was the [JConsole](http://code.google.com/p/jgrasstools/wiki/JConsole), which anyway is going to be the scripting console of JGrass and hopefully uDig.

# JGrass for uDig #

While the JConsole is a nice project and funny to use, we really want to work with a full powered GIS, so the objective here is to get back into uDig stronger than before. To do so we had to handle a couple of problems, one of which is the non compatible license of JGrasstools ([GPL](http://en.wikipedia.org/wiki/GNU_General_Public_License)) and uDig ([LGPL](http://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License)). But first things first.

## The omsbox ##

**Here comes the proposal.** After all this intro, here is the proposal we have for a generic processing box for uDig. We are building a small plugin, that is ablt to generate guis, launch and handle processing of modules that are OMS3 annotated. This is handly, since OMS3 annotations are lightweight, they do not mess up your code. And they are usually (we are working on it with the USDA guys) enough to do what is needed.

How this would look like?

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/jgrass2000/01_jgrass2000.png' /></p>

Handy thing, since the modules are annotated with descriptions, field docs, measurement units and ranges, these can all appear in the gui that is generated.

If something doesn't work, you do not need a new uDig, you solve the problem at library level and load the new libs into uDig again.

## Runtime loading of libraries ##

Since ther idea is to support everything OMS3 based, we need to be able to load libraries whenever we want. There is a settings page that gives the opportunity to load jars at any time. Once ok is pressed, the libraries are scanned for modules and the list of available modules is updated.

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/jgrass2000/02_jgrass2000.png' /></p>

Note that all of the geospatial libraries of uDig are made available to the processes. So if you just want to write any script, that will be possible. Actually our aim is to include the [geoscript project here](http://geoscript.org/).

## Control over your processes ##

One thing important to us was the possibility to prepare your job on a nice desktop gis but then be able to run the same script/thing on serverside. That is why every script is run in its own process. This gives the possibility to define the memory to be used for each process.

It also helps to educate users about what they are doing. For example it is possible to get the command to launch on commandline at serverside to run the same process again:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/jgrass2000/03_jgrass2000.png' /></p>

It also gives the possibility to stop a process easily whenever needed:

<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/jgrass2000/04_jgrass2000.png' /></p>


## Licensing ##

Having things separated in this way helps also with the licensing. The JGrasstools project can stay on GPL without getting into trouble. The only code that touches both worlds is the OMS3 code, which is released under a LGPL like license.

Any user will be able and responsible of loading whichever library they want.

## Extending ##

With this tool it will be easy to create libraries of small tools to be loaded and used when needed. Create your OMS3 based module. Use without problem FeatureCollections and GridCoverages2D in it and load it into the omsbox. They will appear in the gui.


# Conclusions #

Sure, the thing is not finished yet, but we are planning to work heavily on it in every moment that permits it. But we also need the PSC committee of uDig to accept this new uDig plugin, which is why we already present this work in progress.

