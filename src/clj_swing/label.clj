(ns clj-swing.label
 ; (:use clj-swing.core)
  (:import (javax.swing JLabel ImageIcon)))

(defn label 
  [caption & {icon :icon alignment :alignment nmeonic :nmeonic obj :for}]
  (let [a-map {
	       :LEFT (JLabel/LEFT)
	       :CENTER (JLabel/CENTER)
	       :RIGHT (JLabel/RIGHT)
	       :LEADING (JLabel/LEADING)
	       :TRAILING (JLabel/TRAILING)}
	l (cond
	   (and icon (not alignment))
	   (JLabel. caption (ImageIcon. icon))
	   (and icon alignment)
	   (JLabel. caption (.getImage (ImageIcon. icon)) (get a-map alignment (JLabel/LEFT)))
	   (and (not icon) alignment)
	   (JLabel. caption (get a-map alignment (JLabel/LEFT)))
	   :else
	   (JLabel. caption))]
    (if obj (.setLabelFor l obj))
    (if nmeonic 
      (cond
       (instance? nmeonic Number)
       (.setDisplayedMnemonicIndex (int nmeonic))
       (char? nmeonic)
       (.setDisplayedMnemonic nmeonic)
       (instance? nmeonic String)
       (.setDisplayedMnemonicIndex (first nmeonic)))
      l)))
