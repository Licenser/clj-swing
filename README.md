# clj-swing

clj-swing is supposed to be an easy way to create swing GUIs in clojure.

## Usage

Genreal guidelines:
 * classes can be configured during creation time. If a java class has a setter setHorizontalAlignment the attribute can be given to the constructor as :horizontal-alignment.
 * if a class has fields for a setter, the field name can be given in lower case letters so (label ... :horizontal-alignment :left) on a JLabel is equivalent to: (.setHorizontalAlignment label (JLabel/LEFT))
 * the form macro works as a kind of let with an implicit doto so you can write (form <configuration> [c1 (component1) c2 (component2 doing something with 1)] the components will be inserted in this order.
 * The form macro also takes optional constraints for each component (this is an idea I took from Stuart Sierras blog post http://stuartsierra.com/2010/01/05/taming-the-gridbaglayout thousand thanks here!) They can but do not have to be added, if they are you need to add the :constraints optin holding the constraints object.

## Installation

clj-swing on clojars :).

## License

Copyright (c) 2010 Heinz N. Gies.

This code is published under the EPL, have fun! See LICENSE.html for the booring stuff.