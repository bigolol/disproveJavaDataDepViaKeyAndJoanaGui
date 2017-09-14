This is a project which uses both [JOANA](https://pp.ipd.kit.edu/projects/joana/) and [KeY](https://key-project.org/) to try and analyze
security risks (i.e., unwanted information flow) in Java programs.
The way it works is explained in more depth in this paper: https://formal.iti.kit.edu/biblio/?lang=en&key=BeckertBischofEA2017

The tool provides a GUI which gives the user access to all internal functionality. It supports two file formats: ".joak"
(JOana And Key) and ".dispro" (DISproving PROgress). The former describes where to find the Java project and how to annotate
sources and sinks. It is recommended that files of this type are generated using the
joanakeygui: https://github.com/bigolol/disproveJavaDataDepViaKeyAndJoanaGui/tree/master/src/joanakeygui
(disclaimer: it is very rough around the edges).

Before, you need to create a jar file from your java sources, e.g., for a main class M in the package pkg, a manifest-file "manifest.mf"
(reading "Main-Class: pkg.M"), do the following:
```
jar cfm M.jar manifest.mf pkg/*
```
You can now load the file M.jar and the folder pkg with joanakeygui.

The latter (.dispro) saves the information regarding the progress of the disproving process. Whenever a ".joak" file is loaded
(using "file -> load .joak" in the main menu), it is immediately transformed into a ".dispro" file. The ".dispro" file
also holds all information necessary to continue disproving information flows in the program. 

Whenever one is not in the process of disproving a summary edge, it is possible to save the progress using "File -> Save Progress".
Be advised that every time the progress is saved, a new instance of the system dependence graph (SDG) is also saved. This is
necessary since the SDG changed with every summary edge removed from it. It is possible to delete the old instances if one is
sure they are not needed any more.

After loading either a ".joak" or a ".dispro" file, the buttons at the bottom of the GUI window become accessible. The various lists
are being filled with information about the safety violations found by JOANA. The leftmost one shows which summary edges exist in
the current chop and which actual method they correspond to. By clicking on one of them, it is possible to view the method code
in the topmost codearea. The other lists show which formal nodetuples correspond to the summary edge and which line positions
(beginning at the top of the method) contain loops. By clicking on one of these line positions, one can view the 
loop invariant currently used by the system in the bottom left codearea. 
This loop invariant can be changed and saved by clicking the button "Save Loop Invariant".
It is also possible to reset the loop invariant by clicking the corresponding button.
In the bottom right codearea it shows the currently used contract for this summary edge. This is what KeY would use when trying
to disprove the information flow. It is currently not possible to change this in the program.

To disprove a specific edge, select it and click the button "Try Disprove Selected". The system will generate a Java project
containing only the necessary classes and methods, where all called methods are annotated with their most general contract.
If KeY manages to automatically disprove the summary edge, it will be removed.
Otherwise, it is also possible to manually try and disprove a summary edge. To do so, select it and then click on 
"Open Selected in KeY". This will open the project in KeY. If one is succesful in manually disproving the information flow,
remove it by clicking on "Mark as Disproved".

The last possible choice is the auto pilot (option "Run Auto"). It traverses through all edges one by one (in an order which
tries to disprove the easiest edges first) and tries to disprove them. If it is not successful, it moves on to the next one.

This software was mainly designed and implemented by the following students:

* Holger Klein
* Marko Kleine Büning
* [Simon Bischof](https://pp.ipd.kit.edu/person.php?id=148)

For more information, please contact [Mihai Herda](https://formal.iti.kit.edu/~herda/?lang=en)
or [Michael Kirsten](https://formal.iti.kit.edu/~kirsten/?lang=en).
