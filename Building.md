

<br><br><br>

<h1>Setup uDig Development Environment</h1>

Before starting with the JGrass development environment, you will have to go through the setup of uDig's development environment. The uDig community has setup a great guide to lead you through every single step, from the installation of the IDE to the code checkout and the execution of uDig.<br>
<br>
Find it here: <a href='http://udig.refractions.net/confluence/display/ADMIN/02+Development+Environment'>uDig Development Environment</a>

<h1>Checking out the JGrass code</h1>

Exactly as you did for the uDig code, you now have to checkout the jgrass code from the <a href='http://gitorious.org/udig/jgrass'>main code repository</a>. Since you have already installed git during the uDig devel setup, you can simply issue the command:<br>
<br>
<pre><code>git clone git://gitorious.org/udig/jgrass.git jgrass<br>
</code></pre>

which will create a folder named jgrass containing three folders:<br>
<ul><li><b>plugins</b>, which containes the JGrass plugins.<br>
</li><li><b>dbplugins</b>, which containes the libs and database plugins. Those are kept separate from the other JGrass plugins only because they are shared with the <a href='http://code.google.com/p/beegis/'>BeeGIS</a> project and having things clearly separated helps those that need only BeeGIS.<br>
</li><li><b>eclipse-extras</b>, which contains the extra eclipse plugins that are needed for JGrass to work inside uDig.</li></ul>


<h1>Importing the plugin projects</h1>

To import the JGrass plugins into the eclipse workspace, just select the <b>import</b> command from the the <b>File</b> menu and then select: <b>Existing Projects into Workspace</b>:<br>
<br>
<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/build_jgrass/build_01.png' /></p>

In the following tab a root folder containing the plugins will be required. Just enter the folder into which you checked the JGrass code out and the list of plugins will appear in the central panel.<br>
<br>
Follow the guide and at the end you should have the following plugins in your eclipse workspace:<br>
<br>
<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/build_jgrass/build_02.png' /></p>

<a href='Hidden comment: 
Downloading the necessary third party libraries

In order to make things compile properly you need the third party libraries that are used by the JGrass plugins. Those are not kept inside the code repository and can be retrieved the same way as it is done with the uDig libs plugin through the ant maven task.

As you can see in the following picture, the pugin is built almost the same way an you just need to right-click on the *refresh.xml* plugin and do a *Run As* -> *Ant Build*.
This will trigger the download of the needed libraries. As soon as the download has finished, *refresh* the plugin and everything should build fine.

<p align="center"><img src="http://wiki.jgrass.googlecode.com/hg/images/build_jgrass/build_04.png" />

Unknown end tag for </p>


'></a><br>
<br>
<br>
<h1>Adding the plugins to your run configuration</h1>

Once the plugins are in the workspace, you have to add them to your run configuration.<br>
<br>
Open the run configuration you created to run uDig, switch to the <b>Plug-ins</b> tab (it might be worth to duplicate the <b>udig</b> entry and rename it to <b>jgrass</b> to keep the original clean). You should find something like this:<br>
<br>
<p align='center'><img src='http://wiki.jgrass.googlecode.com/hg/images/build_jgrass/build_03.png' width='600px' /></p>

As you might note, the jgrass plugins are not selected. You should go through the list of plugins and check them so that they are picked during startup. Note that you will have to check all the plugins that you imported before and are visible in the image above.<br>
<br>
<h1>Conclusions</h1>

You should now be able to run jgrass and develop on it. If you find issues or want to propose enhancements to this page, please send them to us.