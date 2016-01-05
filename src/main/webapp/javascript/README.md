AutomataTutor Automaton Interface
=========================

A Quick Overview of interface.js
---------------------------

The interface is made using  [d3.js](http://d3js.org) to generate and manipulate SVG elements.


The automaton is described by the following:

`alphabet` is an array of strings denoting the alphabet of the automaton

`nodes` is an array of records with the following fields (each denoting a state):
`id` --  an integer that is both the id and label displayed for the node
`initial` -- a boolean with value `true` if the state is the initial states
`accepting` -- a boolean with value `true` if the state is a final state
`reflexiveNum` -- an integer with value of how many reflexive transitions the state has associated with it
`flip` -- a boolean with value `true` if the reflexive transitions are displayed below (instead of above) the state
`menuVisible` -- a boolean with value `true` if the hover menu is being displayed (only used for NFAs)

`links` is an array of records with the following fields (each denoting a transition):
`source` -- node record denoting the source of the transition
`target` -- node record denoting the target of the tranisiton
`reflexive` -- boolean with value `true` if the transition is reflexive
`trans` -- string array denoting what subset of the alphabet the transition is over


Two functions control the majority of the interface's behavior:

`tick` controls how the automaton is displayed, updating the location of the various SVG elements

`restart` controls what automaton is displayed. Thus it handles most of the user interaction with the
interface and updates the variables accordingly. This includes adding/removing state and transitions, etc...
