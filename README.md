# Pom Graph

This Java tool recursively searches a directory for Maven POM files 
and builds a dependency graph. The tool can write either a adjacency list
style CSV (which can be read by at least [Gephi](https://gephi.org/)) or a GraphML file (which
can be read by most graph analysis tools). 

# Prerequisites
* A Java 8 development kit 
* A copy of [Maven](https://maven.apache.org/) installed and on the `Path` environment variable

# Compiling and running
Clone this repository or download a zip. To compile, run `mvn package` in the directory 
you just cloned or downloaded. To run, run `java -jar target/pom-graph-1.0-SNAPSHOT.jar`
or on Windows, simply double clicking the jar file in the `target` directory should 
work as well.

# Usage
 -d,--directory <arg>   Directory that will be scanned recursively for POM
                        files, required
                        
 -f,--format <arg>      Output format, can be "csv" (adjacency list) or
                        "graphml", required
                        
 -h,--help              Prints this message
 
 -i,--include-version   Whether to create a separate node for each version
                        of an artifact, or merge them all into one node
                        
 -o,--output <arg>      Output file, required

## Known issues

* The tool does not fully resolve POM files, so artifact IDs,
group IDs and version numbers can appear in the graph in an unresolved form. 
Fully resolving has a large overhead but might be investigated in the future. 
