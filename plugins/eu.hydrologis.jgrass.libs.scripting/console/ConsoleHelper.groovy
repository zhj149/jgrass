/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



    /*
     * from the console engine we get some variables added that will be
     * available:
     * 
     * String mapsetPath
     * String remotedbUrl
     * 
     * the variables should be added to:
     * eu.hydrologis.jgrass.libs.scripting.VariablesAndCommands
     * in order to get some nice color in the console.
     */

    File mapsetFile = new File(mapsetPath);

    locationPath = mapsetFile.parent;
    mapsetName = mapsetFile.name;

    if (out == null) {
    	out = System.out;
    } 
    if (err == null) {
    	err = System.err;
    }
    monitor =	new PrintStreamProgressMonitor(out);
	
	def echo = { text ->
        out.println(text);
	}
	
	def echon = { text ->
	    out.print(text);
	}
    
    def errecho = { text ->
        err.println(text);
    }
	
	commands = [];
	def getHelp () {
	    commands.each{ command ->
			out.println( command);
	    }
	}
	
	windPath = mapsetPath + File.separator + JGrassConstants.WIND;
	
	commands << "Available variables:"
	commands << "\tACTIVEREGION: the processed map region."
	def getACTIVEREGION(){
		new JGrassRegion(windPath);
	}
	commands << "\tNORTH: the active's region north."
	def getNORTH(){
		ACTIVEREGION.getNorth();
	}
	commands << "\tSOUTH: the active's region south."
	def getSOUTH(){
		ACTIVEREGION.getSouth();
	}
	commands << "\tEAST: the active's region east."
    double getEAST(){
		ACTIVEREGION.getEast();
	}
	commands << "\tWEST: the active's region west."
    double getWEST(){
		ACTIVEREGION.getWest();
	}
	commands << "\tWERES: the active's region resolution along the X axis."
    double getWERES(){
		ACTIVEREGION.getWEResolution();
	}
	commands << "\tNSRES: the active's region resolution along the Y axis."
    double getNSRES(){
		ACTIVEREGION.getNSResolution();
	}
	commands << "\tROWS: the active's region rows."
    int getROWS(){
		ACTIVEREGION.getRows();
	}
	commands << "\tCOLS: the active's region cols."
    int getCOLS(){
		ACTIVEREGION.getCols();
	}
	
	commands << "\tDB: the database instance."
	
	
	///////////////////////// commands
	
	commands << ""
	commands << "Available commands:"
	
	commands << "\tfullMapPath(mapName)\n\t\tget the absolute path of a map.\n"
	def fullMapPath = { mapName ->
		mapsetPath + File.separator + JGrassConstants.CELL + File.separator + mapName;
	}
	
	commands << "\tcoordinateFromRowCol(row, col)\n\t\tget the coordinate at a given row and col of the active region.\n"
	def coordinateFromRowCol = { row, col ->
		JGrassUtilities.rowColToCenterCoordinates(ACTIVEREGION, row, col);
	}
	
	commands << "\trowColFromCoordinate(easting, northing)\n\t\tget the nearest row and col of a given coordinate in the active region.\n"
	def rowColFromCoordinate = { easting, northing ->
		JGrassUtilities.coordinateToNearestRowCol(ACTIVEREGION, new Coordinate(easting, northing));
	}
	
	commands << "\tprintMap\n\t\tprints the contents of a map as returned by grassMapToMatrix.\n"
    def printMap = { map ->
    	for(double[] row: map){
    		for(double value: row){
    			echon value + " ";
    		}   
    		echo ""
    	}
    }

	
	dbConnection = null;
	def getDB () {
        if (!dbConnection) {
            /*
             * db connection if there is one
             * postgresql:host:port:database:user:passwd
             */
            if (remotedbUrl && remotedbUrl.indexOf(':') != -1) {
                String[] urlSplit = remotedbUrl.split(":");
                StringBuilder jdbcUrlBuilder = new StringBuilder();
                jdbcUrlBuilder.append("jdbc:");
                jdbcUrlBuilder.append(urlSplit[0]);
                jdbcUrlBuilder.append("://");
                jdbcUrlBuilder.append(urlSplit[1]);
                jdbcUrlBuilder.append(":");
                jdbcUrlBuilder.append(urlSplit[2]);
                jdbcUrlBuilder.append("/");
                jdbcUrlBuilder.append(urlSplit[3]);
    
                String driverString = "";
                if (urlSplit[0].equals("postgresql")) {
                    driverString = "org.postgresql.Driver";
                }
    
                Sql.newInstance(jdbcUrlBuilder.toString(), urlSplit[4], urlSplit[5], driverString);
            }else{
                errecho "No database connection info available, check your settings."
            }
        }else{
            dbConnection;
		}
    }
	
	commands << "\tdataset(tableName)\n\t\tgets a browsable dataset for a given table name.\n"
	def dataset = { table ->
	    DB.dataSet(table);
	}
	
	commands << "\tselect(query)\n\t\texecutes a query on the database. The query is assumed to start after the select statement.\n"
	def select = { table ->
		def rows = [];
		def colCount = -1;
		def metadata;
		DB.eachRow("select " + table){
			if(colCount == -1){
			    def resultSet = it.getResultSet();
				metadata = resultSet.getMetaData();
			    colCount = metadata.getColumnCount();
			}
			def tmp = [:] 
			for (i in 1..colCount){
			    tmp.put(metadata.getColumnName(i), it[i-1])
			}
			rows << tmp;
		}
		rows;
	}
	
	commands << "\tcount(query)\n\t\texecutes a count on the database. The query is assumed to start after the from statement.\n"
	def count = {table ->
		def num;
    	DB.eachRow("select count(*) as c from " + table){
    	    num = it.c
    	}
		num;
	}
	
	commands << "\tmatrixToGrassMap(mapName, matrix, novalue)\n\t\twrites a matrix as GRASS map named mapName into the current mapset, considering novalue.\n";
	def matrixToGrassMap = { mapName, matrix, novalue ->
        if(!novalue) novalue = JGrassConstants.doubleNovalue;
		if(!mapName || !matrix){
		    errecho "For the matrixToGrassMap method the mapname and matrix params are mandatory.";
		}else{
            echo "Writing data to map: ${mapName}";
            try {
                RasterData data = new JGrassRasterData(matrix);
                JGrassRasterMapWriter mw = new JGrassRasterMapWriter(ACTIVEREGION, 
                        mapName, mapsetName, locationPath, monitor);
                if (mw.open()) {
                    mw.write(data);
                }
                mw.close();
            } catch (RasterWritingFailureException e) {
                e.printStackTrace();
                errecho "An error occurred while writing: ${mapName} to disk.";
                return;
            }
            echo "The map was successfully to disk";
		}
    }
	
	commands << "\tgrassMapToMatrix(mapName)\n\t\treads the GRASS raster map named mapName and returns it as a matrix of doubles.\n";
    def grassMapToMatrix = { mapName ->
        JGrassRasterMapReader jgrassMapReader = new JGrassRasterMapReader.BuilderFromPathAndNames(ACTIVEREGION, mapName,
                mapsetName, locationPath).maptype(JGrassConstants.GRASSBINARYRASTERMAP).monitor(
                monitor).build();
        if (!jgrassMapReader.open()) {
            errecho "An error occurred while reading the map: ${mapName}";
            return null;
        }
        double[][] data = null;
        try {
            if (jgrassMapReader.hasMoreData()) {
                RasterData rasterData = jgrassMapReader.getNextData();
                data = rasterData.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jgrassMapReader.close();
        return data;
    }
	
    commands << "\tvalueOfGrassMapInCoordinate(mapName, easting, northing)\n\t\treads the value of a GRASS raster map named mapName in the position defined by easting and northing.\n";
	def valueOfGrassMapInCoordinate = { mapName, easting, northing ->
		def rowCol = rowColFromCoordinate(easting, northing);
		def bb = JGrassUtilities.rowColToNodeboundCoordinates( ACTIVEREGION, rowCol[0], rowCol[1] );
		//n, s, e, w
		def region = new JGrassRegion(bb[3], bb[2], bb[1], bb[0], 1, 1);
		JGrassRasterMapReader jgrassMapReader = new JGrassRasterMapReader.BuilderFromPathAndNames(region, mapName,
				mapsetName, locationPath).maptype(JGrassConstants.GRASSBINARYRASTERMAP).monitor(
				monitor).build();
		if (!jgrassMapReader.open()) {
			errecho "An error occurred while reading the map: ${mapName}";
			return null;
		}
		double[][] data = null;
		try {
			if (jgrassMapReader.hasMoreData()) {
				RasterData rasterData = jgrassMapReader.getNextData();
				data = rasterData.getData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		jgrassMapReader.close();
		data[0][0];
	}
	
    commands << "\tgetFeaturesFromLayer(mapName)\n\t\treads the features from the layer named mapName and returns it as list of features.\n";
    def getFeaturesFromLayer = { mapName ->
		echo "Reading features from ${mapName}";
		
		IMap activeMap = ApplicationGIS.getActiveMap();
		String activeMapName = activeMap.getName();
		ILayer selectedLayer;
		List<ILayer> layersList = activeMap.getMapLayers();
		for( ILayer iLayer : layersList ) {
			if (iLayer.getName().equals(mapName)) {
				selectedLayer = iLayer;
				break;
			}
		}
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = selectedLayer.getResource(FeatureSource.class,
                new NullProgressMonitor());
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        Filter filter = selectedLayer.getFilter();
        if (filter.equals(Filter.EXCLUDE)) {
            featureCollection = featureSource.getFeatures();
        } else {
            featureCollection = featureSource.getFeatures(filter);
        }

        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            featuresList.add(feature);
        }
        featureCollection.close(featureIterator);

        echo "Read ${featuresList.size} features";
        return featuresList;
    }
	
	commands << "\tgetFilteredFeaturesFromLayer(mapName, cqlFilter)\n\t\treads the features from the layer named mapName and returns it as list of features applying the cqlFilter.\n";
    def getFilteredFeaturesFromLayer = { mapName, filter ->
        echo "Reading features from ${mapName}";
		Filter cqlFilter = CQL.toFilter(filter);
		
        IMap activeMap = ApplicationGIS.getActiveMap();
        String activeMapName = activeMap.getName();
        ILayer selectedLayer;
        List<ILayer> layersList = activeMap.getMapLayers();
        for( ILayer iLayer : layersList ) {
            if (iLayer.getName().equals(mapName)) {
                selectedLayer = iLayer;
                break;
            }
        }
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = selectedLayer.getResource(FeatureSource.class,
                new NullProgressMonitor());
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        featureCollection = featureSource.getFeatures(cqlFilter);
        
        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            featuresList.add(feature);
        }
        featureCollection.close(featureIterator);
        
        echo "Read ${featuresList.size} features";
        return featuresList;
    }
	
	commands << "\tgetFeaturesFromShapefile(shapePath)\n\t\treads the features from the shapefile at the path shapePath and returns it as list of features.\n";
    def getFeaturesFromShapefile = { shapePath ->
        echo "Reading features from ${shapePath}";

        FileDataStore store = FileDataStoreFinder.getDataStore(new File(shapePath));
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store.getFeatureSource();
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            featuresList.add(feature);
        }
        featureCollection.close(featureIterator);

        echo "Read ${featuresList.size} features";
        return featuresList;
    }
	
	commands << "\tgetFilteredFeaturesFromShapefile(mapName, cqlFilter)\n\t\treads the features from a shapefile and returns it as list of features applying the cqlFilter.\n";
	def getFilteredFeaturesFromShapefile = { shapePath, filter ->
		Filter cqlFilter = CQL.toFilter(filter);
		echo "Reading features from ${shapePath}";
		
		FileDataStore store = FileDataStoreFinder.getDataStore(new File(shapePath));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures(cqlFilter);
		
		List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
		FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
		while( featureIterator.hasNext() ) {
			SimpleFeature feature = featureIterator.next();
			featuresList.add(feature);
		}
		featureCollection.close(featureIterator);
		
		echo "Read ${featuresList.size} features";
		return featuresList;
	}
	
	
	
	commands << "\tloadGrassMap(mapName)\n\t\tloads the map named mapName to be rendered.\n";
	def loadGrassMap = { mapname ->
        if (JGrassPlugin.getDefault() != null) {
            JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(locationPath, mapsetName, mapname,
                    JGrassConstants.GRASSBINARYRASTERMAP);
            if (addedMap == null)
                errecho "An error occurred while trying to add the map to the catalog.";

            IMap activeMap = ApplicationGIS.getActiveMap();
            ApplicationGIS.addLayersToMap(activeMap, Collections.singletonList((IGeoResource) addedMap), activeMap.getMapLayers()
                    .size());
        }
    }
	
	commands << "\tgrassMapAvg(mapName)\n\t\tcalculates the average of a raster map.\n";
	def grassMapAvg = { mapName ->
		def map = grassMapToMatrix(mapName);
	    def avg = 0;
		monitor.beginTask("Processing avg...", map.length);
		int activeCells = 0;
		for(double[] row: map){
			for(double value: row){
				if(!JGrassConstants.isNovalue(value)){
				    avg = avg + value;
					activeCells++;
				}
			}
			monitor.worked(1);
		}
		monitor.done();
    	avg = avg / activeCells;
	}
	
	commands << "\tgrassMapMin(mapName)\n\t\tcalculates the min of a raster map.\n";
	def grassMapMin = { mapName ->
    	def map = grassMapToMatrix(mapName);
    	def min = Double.MAX_VALUE;
    	monitor.beginTask("Processing min...", map.length);
    	for(double[] row: map){
    	    for(double value: row){
    	        if(!JGrassConstants.isNovalue(value) && value < min){
    	            min = value;
    	        }
    	    }
    	    monitor.worked(1);
    	}
    	monitor.done();
    	min;
	}
	
	commands << "\tgrassMapMax(mapName)\n\t\tcalculates the max of a raster map.\n";
	def grassMapMax = { mapName ->
    	def map = grassMapToMatrix(mapName);
    	def max = Double.MIN_VALUE;
    	monitor.beginTask("Processing max...", map.length);
    	for(double[] row: map){
    	    for(double value: row){
    	        if(!JGrassConstants.isNovalue(value) && value > max){
    	            max = value;
    	        }
    	    }
    	    monitor.worked(1);
    	}
    	monitor.done();
    	max;
	}
	
	commands << "\tgrassMapModa(mapName)\n\t\tcalculates the most probable value of a raster map.\n";
	def grassMapModa = { mapName ->
		def map = grassMapToMatrix(mapName);
		
		def valueMap = [:];
		
		monitor.beginTask("Processing moda (1/2)...", map.length);
		for(double[] row: map){
			for(double value: row){
				if(!JGrassConstants.isNovalue(value)){
				    def num = valueMap.get(value);
				    if (num) {
				        valueMap.put(value, ++num);
				    } else {
				        valueMap.put(value, 1);
				    }
				}
			}
			monitor.worked(1);
		}
		monitor.done();
		
		monitor.beginTask("Processing moda (2/2)...", valueMap.size());
		def maxNum = 0;
		def maxNumValue = 0;
		valueMap.each{ entry ->
			if(entry.value && entry.value > maxNum){
				maxNum = entry.value;
				maxNumValue = entry.key;
			}
			monitor.worked(1);
		}
		monitor.done();
		
		maxNumValue > 1 ? maxNumValue : Double.NaN;
	}
	
	commands << "\tfeaturesLayerAvg(mapName, fieldName)\n\t\tcalculates the average of a numeric field from the layer named mapName.\n";
	def featuresLayerAvg = { mapName, fieldName ->
		echo "Processing avg..."
		def featureList = getFeaturesFromLayer(mapName);
		
		def avg = 0;
		double i = 0;
		featureList.each{ feature ->
		    def attribute = feature.getAttribute(fieldName);
			if (attribute instanceof Number){
			    avg = avg + attribute;
			}else{
			    errecho "featuresLayerAvg: The average operator can be executed only on numeric fields.";
			}
			i++;
		}
		echo "Completed avg."
		avg = avg / i;
	}
	
	commands << "\tshapefileAvg(shapePath, fieldName)\n\t\tcalculates the average of a numeric field from a shapefile.\n";
	def shapefileAvg = { shapePath, fieldName ->
		echo "Processing avg..."
    	def featureList = getFeaturesFromShapefile(shapePath);
    	
    	def avg = 0;
    	double i = 0;
    	featureList.each{ feature ->
        	def attribute = feature.getAttribute(fieldName);
        	if (attribute instanceof Number){
        	    avg = avg + attribute;
        	}else{
        	    errecho "shapefileAvg: The average operator can be executed only on numeric fields.";
        	}
        	i++;
    	}
    	echo "Completed avg."
    	avg = avg / i;
	}
	
	
	commands << "\tfeaturesLayerMin(mapName, fieldName)\n\t\tcalculates the min of a numeric field from the layer named mapName.\n";
	def featuresLayerMin = { mapName, fieldName ->
		echo "Processing min..."
    	def featureList = getFeaturesFromLayer(mapName);
    	
    	def min = Double.MAX_VALUE;
    	featureList.each{ feature ->
    	    def attribute = feature.getAttribute(fieldName);
        	if (attribute instanceof Number){
				if(attribute < min){
				    min = attribute;
				}
        	}else{
        	    errecho "featuresLayerMin: The min operator can be executed only on numeric fields.";
        	}
    	}
		echo "Completed min."
    	min;
	}
	
	commands << "\tshapefileMin(shapePath, fieldName)\n\t\tcalculates the min of a numeric field from a shapefile.\n";
	def shapefileMin = { shapePath, fieldName ->
		echo "Processing min..."
		def featureList = getFeaturesFromShapefile(shapePath);
		
		def min = Double.MAX_VALUE;
		featureList.each{ feature ->
			def attribute = feature.getAttribute(fieldName);
			if (attribute instanceof Number){
				if(attribute < min){
					min = attribute;
				}
			}else{
				errecho "shapefileMin: The min operator can be executed only on numeric fields.";
			}
		}
		echo "Completed min."
		min;
	}
	
	commands << "\tfeaturesLayerMax(mapName, fieldName)\n\t\tcalculates the max of a numeric field from the layer named mapName.\n";
	def featuresLayerMax = { mapName, fieldName ->
		echo "Processing max..."
    	def featureList = getFeaturesFromLayer(mapName);
    	
    	def max = Double.MIN_VALUE;
    	featureList.each{ feature ->
    	def attribute = feature.getAttribute(fieldName);
        	if (attribute instanceof Number){
        	    if(attribute > max){
        	        max = attribute;
        	    }
        	}else{
        	    errecho "featuresLayerMax: The max operator can be executed only on numeric fields.";
        	}
    	}
		echo "Completed max."
    	max;
	}
	
	commands << "\tshapefileMax(shapePath, fieldName)\n\t\tcalculates the max of a numeric field from a shapefile.\n";
	def shapefileMax = { shapePath, fieldName ->
		echo "Processing max..."
    	def featureList = getFeaturesFromShapefile(shapePath);
    	
    	def max = Double.MIN_VALUE;
    	featureList.each{ feature ->
        	def attribute = feature.getAttribute(fieldName);
        	if (attribute instanceof Number){
        	    if(attribute > max){
        	        max = attribute;
        	    }
        	}else{
        	    errecho "shapefileMax: The max operator can be executed only on numeric fields.";
        	}
    	}
		echo "Completed max."
    	max;
	}
	
	commands << "\tfeaturesLayerModa(mapName, fieldName)\n\t\tcalculates the most probable value of a numeric field from a featuresLayer.\n";
	def featuresLayerModa = { mapName, fieldName ->
		echo "Processing moda..."
    	def featureList = getFeaturesFromLayer(mapName);
    	
    	def valueMap = [:];
    	
    	featureList.each{ feature ->
        	def attribute = feature.getAttribute(fieldName);
        	if (attribute instanceof Number){
        	    Double value = attribute.doubleValue();
        	    def num = valueMap.get(value);
        	    if (num) {
        	        valueMap.put(value, ++num);
        	    } else {
        	        valueMap.put(value, 1);
        	    }
        	}else{
        	    errecho "featuresLayerModa: The moda operator can be executed only on numeric fields.";
        	}
    	}
    	def maxNum = 0;
    	def maxNumValue = 0;
    	valueMap.each{ entry ->
        	if(entry.value && entry.value > maxNum){
        	    maxNum = entry.value;
        	    maxNumValue = entry.key;
        	}
    	}
		echo "Completed moda."
		maxNumValue > 1 ? maxNumValue : Double.NaN;
	}
	
	commands << "\tshapefileModa(shapePath, fieldName)\n\t\tcalculates the most probable value of a numeric field from a shapefile.\n";
	def shapefileModa = { shapePath, fieldName ->
		echo "Processing moda..."
		def featureList = getFeaturesFromShapefile(shapePath);
		
		def valueMap = [:];
		
		featureList.each{ feature ->
			def attribute = feature.getAttribute(fieldName);
			if (attribute instanceof Number){
				Double value = attribute.doubleValue();
				def num = valueMap.get(value);
				if (num) {
				    valueMap.put(value, ++num);
				} else {
				    valueMap.put(value, 1);
				}
			}else{
				errecho "shapefileModa: The moda operator can be executed only on numeric fields.";
			}
		}
		def maxNum = 0;
		def maxNumValue = 0;
		valueMap.each{ entry ->
			if(entry.value && entry.value > maxNum){
				maxNum = entry.value;
				maxNumValue = entry.key;
			}
		}
		echo "Completed moda."
		maxNumValue;
	}
	

	commands << "\tcopyToNewMapset(newMapsetName, maps...)\n\t\tcopies supplied maps to a new mapset.\n";
    def copyToNewMapset( String newMapsetName, String... maps ) {
        String originalMapsetPath = locationPath + File.separator + mapsetName;
        String newMapsetPath = locationPath + File.separator + newMapsetName;

        File f = new File(newMapsetPath);
        if (f.exists()) {
            errecho "The mapset already exists. Can't export to an existing mapset. Choose a different name.";
            return;
        }

        boolean createdMapset = JGrassCatalogUtilities.createMapset(locationPath, newMapsetName, null, null);
        if (!createdMapset) {
			errecho "An error occurred while creating the new mapset structure. Check your permissions.";
        }

        StringBuilder warnings = new StringBuilder();
        monitor.beginTask("Copy maps...", maps.length);
        for( String mapName : maps ) {
            monitor.worked(1);
            String[] originalMaps = JGrassUtilities.filesOfRasterMap(originalMapsetPath, mapName);
            String[] copiedMaps = JGrassUtilities.filesOfRasterMap(newMapsetPath, mapName);

            for( int i = 0; i < originalMaps.length; i++ ) {
                File orig = new File(originalMaps[i]);
                if (!orig.exists()) {
                    warnings.append("\nWarning: The following file didn't exist: " + originalMaps[i]);
                    continue;
                }
                if (orig.isDirectory()) {
                    continue;
                }
                File copiedParent = new File(copiedMaps[i]).getParentFile();
                if (!copiedParent.exists()) {
                    copiedParent.mkdirs();
                }
                FileUtilities.copyFile(originalMaps[i], copiedMaps[i]);
            }
        }
        monitor.done();
        out.println(warnings.toString());
    }

	commands << "\tdeleteGrassMap(mapName)\n\t\tremoves a GRASS raster map from the mapset.\n";
    def deleteGrassMap = { map ->
        echo "Removing map: ${map}";
        if (!JGrassUtilities.removeGrassRasterMap(mapsetPath, map)) {
            errecho "Map ${map} could not be removed";
        }
    }
	
	
    commands << "\texportEsriAscii(mapName, path)\n\t\texports a GRASS raster map to an esri ascii grid.\n";
	def exportEsriAscii = { mapName, path ->
		GrassCoverageReader tmp = new GrassCoverageReader(null, null, true, false,
					monitor);
		tmp.setInput(new File(fullMapPath(mapName)));
		GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(ACTIVEREGION);
		def gridCoverage2D = tmp.read(gcReadParam, false);
		
		final ArcGridFormat format = new ArcGridFormat();
		final ArcGridWriteParams wp = new ArcGridWriteParams();
		final ParameterValueGroup paramWrite = format.getWriteParameters();
		paramWrite.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
		File dumpFile = new File(path + ".asc");
		ArcGridWriter gtw = (ArcGridWriter) format.getWriter(dumpFile);
		gtw.write(gridCoverage2D, (GeneralParameterValue[]) paramWrite.values().toArray(new GeneralParameterValue[1]));
	}
	
	commands << "\texportTiff(mapName, path)\n\t\texports a GRASS raster map to a tiff image.\n";
	def exportTiff = { mapName, path ->
    	GrassCoverageReader tmp = new GrassCoverageReader(null, null, true, false,
    	        monitor);
    	tmp.setInput(new File(fullMapPath(mapName)));
    	GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(ACTIVEREGION);
    	def gridCoverage2D = tmp.read(gcReadParam, false);
		
		final GeoTiffFormat format = new GeoTiffFormat();
		final GeoTiffWriteParams wp = new GeoTiffWriteParams();
		wp.setCompressionMode(GeoTiffWriteParams.MODE_DEFAULT);
		wp.setTilingMode(GeoToolsWriteParams.MODE_DEFAULT);
		final ParameterValueGroup paramWrite = format.getWriteParameters();
		paramWrite.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
		File dumpFile = new File(path + ".tif");
		GeoTiffWriter gtw = (GeoTiffWriter) format.getWriter(dumpFile);
		gtw.write(gridCoverage2D, (GeneralParameterValue[]) paramWrite.values().toArray(new GeneralParameterValue[1]));
	}
	
	commands << "\timportTiff(path, mapName)\n\t\timports a tiff to GRASS raster map.\n";
	def importTiff = { path, mapName ->
		GeoTiffReader reader = new GeoTiffReader(new File(path));
		def gridCoverage2D = (GridCoverage2D) reader.read(null);
		
		GrassBinaryImageWriterSpi writerSpi = new GrassBinaryImageWriterSpi();
		GrassBinaryImageWriter writer = (GrassBinaryImageWriter) writerSpi.createWriterInstance();
		RenderedImage renderedImage = gridCoverage2D.view(ViewType.GEOPHYSICS).getRenderedImage();
		File file = new File(fullMapPath(mapName));
		writer.setOutput(file);
		writer.write(renderedImage);
	}
	
	commands << "\timportEsriAscii(path, mapName)\n\t\timports an esri grid to GRASS raster map.\n";
	def importEsriAscii = { path, mapName ->
	    echo "Reading esri grid..."
	    ArcGridReader reader = new ArcGridReader(new File(path));
    	def gridCoverage2D = (GridCoverage2D) reader.read(null);
    	
    	File file = new File(fullMapPath(mapName));
    	GrassCoverageWriter gCW = new GrassCoverageWriter(file, monitor);
    	gCW.write(gridCoverage2D, JGrassUtilities.getJGrassRegionFromGridCoverage(gridCoverage2D));
	}
	
	class MapCalculator {
	    def result;
		def function;
		def mapsetPath;
		def activeRegion;
		def monitor;
		
		def missings = [:];
		def exec(){
			String cellPath = mapsetPath + File.separator + "cell";
			File cellFile = new File(cellPath);
			File[] filesList = cellFile.listFiles();
			List<String> mapNames = new ArrayList<String>();
			for( File file : filesList ) {
				if (file.isFile()) {
					mapNames.add(file.getName());
				}
			}
			def mapsArray = (String[]) mapNames.toArray(new String[mapNames.size()]);
			
			def tmp = missings.result.toString();
			result = tmp.substring(1, tmp.length() - 1);
			tmp = missings.function.toString();
			function = tmp.substring(1, tmp.length() - 1);
			MapcalcJifflerHeadless mJiffler = new MapcalcJifflerHeadless(function, result, mapsArray, activeRegion,
			        cellPath, monitor);
			mJiffler.exec();
		}
		
		def methodMissing(String name, args){
			missings[name] = args;
		}
	}
	
	def mapcalc (closure) {
		MapCalculator mapCalculator = new MapCalculator();
		closure.delegate = mapCalculator;
		closure();
		mapCalculator.setMapsetPath mapsetPath;
		mapCalculator.setActiveRegion ACTIVEREGION;
		mapCalculator.setMonitor monitor;
		
		mapCalculator.exec();
	}
	

	/*
	 * metaexpandos
	 */
	SimpleFeatureImpl.metaClass.getArea = { ->
		delegate.defaultGeometry.area;
	}	
    SimpleFeatureImpl.metaClass.getCoordinates = { ->
        delegate.defaultGeometry.coordinates;
    }	
    SimpleFeatureImpl.metaClass.getLength = { ->
        delegate.defaultGeometry.length;
    }	
    SimpleFeatureImpl.metaClass.getCentroid = { ->
        delegate.defaultGeometry.centroid;
    }	
	
	
	
