(ns clj-swing.document
  (:require [clojure.contrib.string :as st])
  (:import [javax.swing.text AbstractDocument Position]
	   javax.swing.text.AbstractDocument$Content))

(defn- update-positions [positions offset change]
  (doall 
   (map 
    (fn [p]
      (if (>= @p offset)
	(swap! p + change)
	p))
    positions)))

(defn str-insert [s offset new-s]
  (str (st/take offset s) new-s (st/drop offset s)))

(defn str-remove [s offset cnt]
  (str (st/take offset s) (st/drop (+ offset cnt) s)))


(defn string-ref-content [str-ref]
  (let [positions (atom [])]
    (proxy [AbstractDocument$Content] []
      (createPosition [offset] 
		      (let [p (atom offset)]
			(swap! positions conj p)
			(proxy [Position] []
			  (getOffset [] @p))))

      (getChars [where len  txt] 
		(set! (.array txt ) (into-array (subs  @str-ref where len))))

      (getString [where len]
		(subs @str-ref where len))  

      (length []
	     (.length @str-ref))

      (insertString [where str]
		    (swap! positions update-positions where (.length str))
		    (dosync
		     (alter str-ref str-insert where str))
		    nil)

      (remove [where nitems] 
	      (swap! positions update-positions where (- 0 nitems))
	      (dosync 
	       (alter str-ref str-remove where nitems))
	      nil))))

(defn abstract-str-ref-document [str-ref]
  (javax.swing.text.AbstractDocument. #^AbstractDocument$Content (string-ref-content str-ref))) 