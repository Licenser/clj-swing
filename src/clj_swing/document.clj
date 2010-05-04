(ns clj-swing.document
  (:use [clojure.contrib.swing-utils :only [do-swing]])
  (:require [clojure.contrib.string :as st])
  (:import [javax.swing.text AbstractDocument Position Element PlainDocument]
	   [javax.swing.event DocumentEvent DocumentListener]
	   javax.swing.event.DocumentEvent$EventType
	   javax.swing.event.DocumentEvent$ElementChange
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

      (getChars [where len txt]
		(let [s (max 0 (min where len))
		      e (max 0 (min where len (.length @str-ref)))]
		  (println "A:" @str-ref where len)
		  (set! (. txt array) (into-array Character/TYPE (seq (subs @str-ref s e))))
		  (println "B")
		  (prn (seq (. txt array)))))

      (getString [where len]
		 (let [s (max 0 (min where len))
		      e (max 0 (min where len (.length @str-ref)))]
		   (subs @str-ref s e)))

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


(defn plain-str-ref-document [str-ref]
  (PlainDocument. (string-ref-content str-ref)))

(comment defn abstract-str-ref-document [str-ref]
  (let [d (proxy [AbstractDocument]  [(string-ref-content str-ref)])]
    (add-watch str-ref (gensym "abstract-str-ref-document-watch")
	       (fn [_ _ _ state]
		 (.fireChangedUpdate d (proxy [DocumentEvent] []
					 (getChange [elem] (proxy [DocumentEvent$ElementChange] []
							     (getChildrenAdded [] (into-array Element[]))
							     (getChildrenRemoved [] (into-array Element[]))
							     (getElement[] elem) 
							     (getIndex[] 0)))
						    

					 (getDocument [] d)
					 (getLength [] (.length state))
					 (getOffset [] 0)
					 (getType [] (DocumentEvent$EventType/CHANGE))))))
    (.fireChangedUpdate d (proxy [DocumentEvent] []
			    (getChange [elem] (proxy [DocumentEvent$ElementChange] []
						(getChildrenAdded [] (into-array Element[]))
						(getChildrenRemoved [] (into-array Element[]))
						(getElement[] elem) 
						(getIndex[] 0)))
			    
			    
			    (getDocument [] d)
			    (getLength [] (.length state))
			    (getOffset [] 0)
			    (getType [] (DocumentEvent$EventType/CHANGE))))
    d))

(defn add-str-ref-doc-listener [doc-owner str-ref]
  (let [doc (.getDocument doc-owner)
	watch-key (gensym "str-ref-doc-listener-watch")
	watch-fn (fn [l]
		   (fn [a b c state]
		     (do-swing
		      (.removeDocumentListener doc l)
		      (.remove doc 0 (.getLength doc))
		      (.insertString doc 0 state nil)
		      (.addDocumentListener doc l))))
	l (proxy [DocumentListener] []
	    (insertUpdate [event]
			  (let [offset (.getOffset event)]
			     (remove-watch str-ref watch-key)
			     (dosync (alter str-ref str-insert offset (.getText doc offset (.getLength event))))
			     (add-watch str-ref watch-key (watch-fn this))))
	    (removeUpdate [event]
			   (remove-watch  str-ref watch-key)
			   (dosync (alter str-ref str-remove (.getOffset event) (.getLength event)))
			   (add-watch str-ref watch-key (watch-fn this)))
	    (changedUpdate [event]))]
    (if (< 0 (.getLength doc))
      (.remove doc 0 (.getOffset (.getEndPosition doc))))
    (prn str-ref)
    (.insertString doc 0 @str-ref nil)
    (.addDocumentListener doc l)
    (add-watch str-ref watch-key (watch-fn l))))
