This wiki page contains informations, email exchanges and useful links regarding the possibility to mix and distribute code licensed under the GPL and the EPL.



# Open letter for request of clarification to the Free Software Foundation Europe regarding the coexistence of GPL and EPL licensed code #

To gain better understanding of the GPL-EPL compatibility problem, we wrote the following open letter to the Eclipse Foundationd and the FSF.

## Introduction ##

My name is Andrea Antonello and I am an active member of the free and open source software (from now on FOSS) community for years now. I am member of the Open Source Geospatial Foundation (www.osgeo.org) and active developer and steering comittee member in a couple of FOSS GIS projects based on the eclipse rcp (from now on RCP).

For better understanding of the usecases I will ask you clarification of, let me give some introductory explanation.

[Eclipse RCP](http://www.eclipse.org) doesn't need much presentation. It is the Rich Client Platform, released under the Eclipse Public License (from now on EPL) and the source of many doubts in licensing issues.
[uDig](http://udig.refractions.net) is a FOSS GIS and a GIS development kit released under lesser general public license (from now on LGPL). uDig is heavily based on the RCP.
[JGrass](http://www.jgrass.org) is a set of plugins for uDig and RCP released under LGPL. JGrass is heavily based on the RCP.

It is clear that this situation has no issues, since EPL and LGPL are compatible licenses.

I should mention that in the following cases GPL refers to the GPL version 3.


## Motivations ##

JGrass is a highly scientific project, and as such it has difficulties in exploiting the real power of FOSS, since many scientific project that JGrass could use or connect to are released under general public license (from now on GPL).

This is the reason for which the JGrass team would like to release its plugins under the GPL license.
Given the incompatibility of EPL and GPL, many discussions were raised which lead to lot of confusion and potential border cases that may harm both the project's community and the involved commercial partners.

This letter is meant to request some clarification once and for all by proposing a couple of use cases of development and deployment of the above mentioned applications. I apologize in advance for  having inserted some obviously incompatible cases. The reason for this is that I would like to create a document for the above communities (and not only) to give more clearness and to assure many community members that have had problems with freeing their code to the above RCP based projects.


## Use cases ##

Of the following 5 use cases, the first 4 all assume the following:
I write a simple rcp plugin (from now on called MYPLUGIN) to be used for uDig (for example like explained [here](http://udig.refractions.net/confluence/display/DEV/2+Plugin+Tutorial)). Developed in such a way the plugin contains imports of libraries from both the RCP:
  * org.eclipse.ui
  * org.eclipse.core.runtime
and uDig:
  * net.refrations.udig.project.ui

It is important to note that the MYPLUGIN contains only plugin specific code, which relies on implementations that reside in the referenced packages
  * org.eclipse.ui
  * org.eclipse.core.runtime
  * net.refrations.udig.project.ui
and are present in the target environment (uDig or RCP).

To be clear, in this case import is meant really as the java statement to declare which packages of external libraries shall be used by MYPLUGIN.

### Use case one: writing a RCP based plugin and releasing it under GPL ###

I license MYPLUGIN under GPL, create a small jar archive of MYPLUGIN, put it on a website with installation instructions for putting it into uDig.

Can this be done?

### Use case two: writing a RCP based plugin, releasing it under GPL and deploy it as an application ###

I license MYPLUGIN under GPL, package it with the uDig application, which I brand and bundle in one ready-to-use installer. I then put it on a website where users can download and use it.

Can this be done?

### Use case three: writing a RCP based plugin, releasing it under GPL and install it as an application for a customer ###

I license MYPLUGIN under GPL and put it on a website as in use case one.

Assuming I wrote MYPLUGIN for a customer,  which of the following options are legal?
  * subcase 1: make a cd with both the uDig application and MYPLUGIN (but separate), and put on the cd installation instructions.
  * subcase 2: make a cd with uDig incorporating MYPLUGIN within a single installation file.
  * subcase 3: go to the customer site and install uDig and MYPLUGIN, creating an application ready for the customer's use.


### Use case four: writing a RCP based plugin, releasing it under GPL and making it available via the eclipse rcp update site engine ###

I license MYPLUGIN under GPL.

I want to use the RCP's own [installation manager](http://help.eclipse.org/galileo/index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-34.htm) to make MYPLUGIN available to both community and customers.

Can this be done?


### Use case five: writing plugins using the Osgi framework ###

The following use case is a bit different from the above ones.

I want to develop a library based on the [Osgi](http://www.osgi.org/About/HowOSGi) framework. Since through the uDig and RCP development I am very bound to the eclipse RCP, I decide to use the [Equinox](http://www.eclipse.org/equinox/) framework to create the plugins that compose my library (instead of for example [Apache Felix](http://felix.apache.org/site/index.html), which would have compatible license).

The library I produce will therefore contain imports of the equinox framework.

Which of the below can I do?
  * subcase 1: I deliver my library and the user will have to download the osgi framework on his own.
  * subcase 2: I deliver my library packaged together with the osgi framework libraries.


## Conclusions ##

I feel that there is much confusion on the user's part when it comes to certain licenses. The EPL and GPL mixture is a real headache and still I'm not able to find someone that would assure me regarding this topic.

This letter is born of the opinion that a FOSS project comittee has to be able to give clear information about what can and cannot be done to potential developers and investors of the project.


Thanks in advance for the clarifications that the Free Software Foundation can give us.

Warmest regards
Andrea Antonello

on behalf of at least:
  * the uDig community
  * the JGrass community
  * the BeeGIS community
  * the Java User Group Trentino-Altoadige
  * and several commercial FOSS based companies




# EPL-GPL: the verdict - UPDATE 2010/04 #

Through the below letter we got several email exchanges directly with the Executive Director of the Eclipse Foundation, Mike Milinkovich, who was very helpful in getting a good picture of what the problems are.

At the time of sending my email, Eclipse Foundation and FSF were discussing the GPL/EPL issue, so I didn't get a direct answer to the questions in the open letter, but now there is a public answer about the issues available:

[Here the point of view of the Eclipse Foundation can be found.](http://dev.eclipse.org/blogs/mike/2010/04/06/epl-gpl-commentary/)

[And here the public statement of the FSF.](http://www.fsf.org/blogs/licensing/using-the-gpl-for-eclipse-plug-ins)


# Comments and conclusions #

After long discussions we think that the following applies:

  * it is not possible to create a pure GPL eclipse plugin. That makes one wonder why so many exist in the eclipse marketplace...
  * since an eclipse plugin is compatible only in the case in which an [exception](http://en.wikipedia.org/wiki/GPL_linking_exception) is added to the license (remember the [classpath exception](http://en.wikipedia.org/wiki/GPL_linking_exception#The_classpath_exception)?)
  * and to create such an exception **every** author involved in the linked code has to agree on the exception
  * also because derived works inherit the license together with the exception
  * which basically is what the FSF states in the last part of its blog post: _"...can address this issue by providing an additional permission with their license that grants users permission to combine their work with Eclipse in this way..."_



