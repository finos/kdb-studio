* Monitoring file changes on disk to reloaded into editor

`dz2.0` 2021.12.01
-----
* Double-click in result grid:
    * beside system double-click, added emulated double-click with configurable speed
    * ALT + mouse click also copies the cell
* (Windows only) Icon grouping in taskbar
    * grouping are different to other application started with the same Java machine
    * grouping for editor and charting frames are different
* Number of fractional digits in output is now configurable 
* Adding mnemonic shortcuts in confirmation dialogs
* Support of different line ending styles
* Adding SaveAll action
* Adding (*) to the title if the tab has modified content
* Adding tooltip into output tab with server where the query was executed

`dz1.11` 2021.09.24
-----
* Output grid:
    * add vertical lines and make header compact
    * Settings: adding right padding and max width for columns
    * trying to keep old order during sorting
    * sorting row header as well
* Added shortcut to select next and previous editor tab
* Reworked charts:
    * added shapes, strokes and bars
    * legends with colors and char types for individual charts
    * zoom with mouse wheel
    * chart frame title is derived from chart title

`dz1.10` 2021.06.30
-----
* Open - changed behaviour to open in a new tab
* Settings: auto save modified files
* Settings: exit without asking to save anything
* Upload result to a server
* Drag and Drop for editor and result tabs

`dz1.9` 2021.04.28
-----
* Manually closure of tabs
    * Middle-click to close tab or right-click for a popup menu  
* Pop up menu in the result table can include actions to open servers
    * The decision is made by values in current row and selected cell 
* Restoring windows and tabs from previous application run
    * The state is also persisted every minute  
* Show an About dialog on start up if release notes are changed
* Options to execute all script when nothing is selected. The default option is to ask
    * The option is added to Settings
* Add notes into About dialog
* Add support for multiple tabs in a StudioPanel:
    * Ctrl + N (Command + N) opens a new tab
    * Ctrl + Shift + N (Command + Shift + N) opens a new window

`dz1.8` 2021.03.30
-----
* Fix memory leak related to keeping previously loaded results
* Fix formatting of Composition type in the result output
* Import servers from QPad (http://www.qinsightpad.com/)

`dz1.7` 2021.01.19
-----
* Add history of servers opened in StudioPanel
* Fix syntax highlighting for communication handle symbol (like `:server:port )
* Add Log4j 2 for logging application and queries to $HOME/.studioforkdb/log folder
* Set new syntax highlighting in Console result pane

`dz1.6` 2020.12.22
-----
* Rework q syntax highlighting
* Update versioning and About Dialog. Added release notes into notes.md 

`dz1.5` 2020.12.04
-----
* Customization of output format with comma thousands separator
* Bugfix for not starting Studio without config file
* Copy and cut action adds syntax highlighting into clipboard

`dz1.4` 2020.10.19
-----
* Double click in result table cells copies content into clipboard
* Remove zero char when copying into clipboard
* Fix formatting of projections

`dz1.3` 2020.06.04
-----
* Syntax highlighting in output result
* Multiple tabs in the result pane
* Selection and Look and Feel

`dz1.2` 2020.05.01
-----
* Hide drop down servers option
* Tree view for Server list
* Add/remove line in charts
* Copy as HTML

`dz1.1` 2020.04.10
-----
* Text field with connection details
* Added Settings menu
* Bugfix for loading custom authentication plugin
* Dictionary and list are displayed as table
* Added server list
* Added formatting for `binr`, `cov`, `cor` (BinaryPrimitive) and `var`, `dev`, `hopen` (UnaryPrimitive)


`3.35` 2020.01.24
-----
The version which was forked from