(ns clj-swing.label
  (:use [clj-swing.core :only [add-action-listener icon-setters auto-setters]])
  (:import (javax.swing JLabel ImageIcon)))

(defmacro label 
  [caption & {obj :for :as opts}]
  `(let [l# (JLabel. ~caption)]
     (doto l#
       ~@(if obj 
	 [`(.setLabelFor ~obj)])
       ~@(icon-setters [:icon :disabled-icon]  opts)
       ~@(auto-setters JLabel [:for] opts))))
