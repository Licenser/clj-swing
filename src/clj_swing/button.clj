(ns clj-swing.button
  (:use [clj-swing.core :only [add-action-listener icon-setters auto-setters]])
  (:import (javax.swing JButton JCheckBox ImageIcon JCheckBoxMenuItem JRadioButton JRadioButtonMenuItem JToggleButton ButtonGroup)))


(def *button-icon-keys* 
     [:icon :disabled-icon :selected-icon :pressed-icon :disabled-selected-icon :rollover-icon :rollover-selected-icon ])
(def *button-known-keys*
     (concat [:action :caption] *button-icon-keys*))


(defmacro general-button [cl {caption :caption action :action :as opts}]
  (let [b (gensym "btn")]
    `(let [~b  (new ~cl)]
       (doto ~b
	 ~@(if caption  
	     [`(.setText ~caption)])
	 ~@(if action  
	     [`(add-action-listener ~action)])
	 ~@(icon-setters *button-icon-keys*  opts)
	 ~@(auto-setters JButton *button-known-keys* opts)))))

(defmacro button [caption & {:as opts}]
  `(general-button JButton ~(assoc opts :caption caption)))

(defmacro check-box [ & {:as opts}]
  `(general-button JCheckBox ~opts))

(defmacro check-box-menu-item [ & {:as opts}]
  `(general-button JCheckBoxMenuItem ~opts))

(defmacro radio-button [ & {:as opts}]
  `(general-button JRadioButton ~opts))

(defmacro radio-button-menu-item [ & {:as opts}]
  `(general-button JRadioButtonMenuItem ~opts))

(defmacro toggle-button [ & {:as opts}]
  `(general-button JToggleButton ~opts))

(defmacro button-group [& buttons]
  `(doto (ButtonGroup.)
     ~@(map (fn [btn]
	      `(.add ~btn)) buttons)))