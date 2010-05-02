(ns clj-swing.text-field
  (:use [clj-swing.core])
  (:import (javax.swing JTextField ListModel)
	   (javax.swing.event ListDataEvent ListDataListener ListSelectionListener)))

(def *text-field-known-keys* [:action])

(defmacro text-field [& {action :action :as opts}]
  `(doto (JTextField.)
     ~@(if action  
	 [`(add-action-listener ~action)])
     ~@(auto-setters JTextField *text-field-known-keys* opts)))


