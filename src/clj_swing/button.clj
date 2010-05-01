(ns clj-swing.button
  (:use [clj-swing.core :only [add-action-listener icon-setters auto-setters]])
  (:import (javax.swing JButton ImageIcon)))


(def *button-icon-keys* 
     [:icon :disabled-icon :selected-icon :pressed-icon :disabled-selected-icon :rollover-icon :rollover-selected-icon ])
(def *button-known-keys*
     (concat [:action] *button-icon-keys*))
   

(defmacro button [title & {action :action :as opts}]
  (let [b (or name (gensym "btn"))]
    `(let [~b  (JButton.)]
       (doto ~b
	 ~@(if title  
	     [`(.setText ~title)])
	 ~@(if action  
	     [`(add-action-listener ~action)])
	 ~@(icon-setters *button-icon-keys*  opts)
	 ~@(auto-setters JButton *button-known-keys* opts)))))