(ns clj-swing.text-field
  (:use [clj-swing.core])
  (:import (javax.swing JTextField ListModel)
	   (javax.swing.event ListDataEvent ListDataListener ListSelectionListener)))

(defmacro text-field [[& items] & {action :action :as opts}]
  `(doto (JTextField.)
     ~@(if action  
	 [`(add-action-listener ~action)])
     ~@(auto-setters JComboBox *cb-known-keys* opts)
     ~@(map #(list '.addItem %) items)))


