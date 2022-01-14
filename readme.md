Studio for kdb+
=========

Studio for kdb+ is a rapid development environment for the **ultra-fast** database kdb+ from [Kx Systems]. In the style of commonly used SQL Clients, it allows you to

  - Connect to kdb+ processes
  - Execute selected text from the editor window
  - View results as tables, charts, or classic console style 

The editor component is based on the NetBeans editor component, and includes the following features
  - find/search/replace
  - cut/copy/paste
  - undo/redo
  - syntax highlighting for the q language
  - most recent files menu

Additionally the application features
  - export to Excel
  - drag and drop
  - immediate charting of grid data

Screenshot
---------
![alt tag](https://raw.githubusercontent.com/dzmipt/studio/master/meta/ssthumb.png)

Current Version
----

3.34 build date 2018.02.28

Credits
-----------

Studio for kdb+ uses the following open source projects:

* [RSyntaxTextArea] - syntax highlighting text component
* [JFreeChart] - charting component
* [Kx Systems] - kdb+ driver c.java
* [Log4j 2] - Apache logging
* [Gradle] - Build tool
* [Txtmark] - Java markdown processor - to convert notes.md to HTML during build
* [Apache] - POI for Excel export
* [JNA] - Java Native Access

Installation
--------------
Download the latest release from

https://github.com/dzmipt/kdbStudio

Gradle command is used to start

    gradle run


Background
----------
Studio for kdb+ has been developed since October 2002, and the source was released to the kdb+ community in September 2008 as the primary developer wanted to allow the community to develop the application further.

Studio is written 100% in Java. The primary motivation for its development was to be able to comfortably access remote kdb+ processes. In time, it has become clear that it is not an IDE as such, but is better described as a rapid execution environment. One can edit text in the "scratch" window, highlight a selection and execute it against a remote kdb+ process via tcp/ip, with the results displayed as a grid or as in the classic kdb+ console.

In 2020, the [git repo] was forked. And now it is developed in a separate [dzmipt git repo].  

License
-------
Apache 2 , see LICENSE file in repository root.

N.B. JFreeChart and c.java components have their own respective licenses.

Icon Experience Collection

Selected icons within the lib/images directory are part of the Icon Experience
collection (http://www.iconexperience.com) and may be freely used with Studio for kdb+
without charge, but may not be used separately from Studio for kdb+ without a purchase
of a license from Icon Experience.

[Kx Systems]:http://www.kx.com
[license]:https://github.com/CharlesSkelton/studio/blob/master/license.md
[git repo]:https://github.com/CharlesSkelton/studio
[dzmipt git repo]:https://github.com/dzmipt/studio
[JFreeChart]:http://www.jfree.org/jfreechart/
[Log4j 2]:https://logging.apache.org/log4j/2.x/index.html
[Gradle]:https://gradle.org/
[Txtmark]:https://github.com/rjeschke/txtmark
[Apache]:https://www.apache.org/
[RSyntaxTextArea]:http://bobbylight.github.io/RSyntaxTextArea/
[JNA]:https://github.com/java-native-access/jna