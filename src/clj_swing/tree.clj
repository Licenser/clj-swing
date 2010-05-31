(ns clj-swing.tree
  (:use [clj-swing.core :only [add-action-listener icon-setters auto-setters]]
	[clj-swing panel])
  (:import (javax.swing JTree)
           (javax.swing.tree TreeModel)))

(def *tree-known-keys* [:action])

(defn- index-of [coll item]
  (loop [coll coll idx  (int 0)]
    (if (empty? coll)
      -1
      (if (= (first coll) item)
        idx
        (recur (rest coll) (inc idx))))))

(defprotocol pPathed
  (path [t])
  (toString [t]))

(deftype Pathed [node path]
  pPathed
  (toString [this] (str node))
  (path [this] path))

(defn mapref-tree-model [map-ref root-name] 
  (let [listeners (atom [])]
    (proxy [TreeModel] []
      (addTreeModelListener [l] (swap! listeners conj l))
      (getChild [node idx]
		(prn 'getChild [node idx])
		(let [par (get-in @map-ref (path node))
		      ks (keys par)
		      chi (nth ks idx)]
		  (Pathed. chi (conj (path node) chi))))
      (getChildCount [node]
		     (prn 'getChildCount [node])
        (count (keys (get-in @map-ref (path node)))))
      (getIndexOfChild [node chi]
		       (let [c (str chi)
			     ks (keys (get-in @map-ref (path node)))]
			 (index-of ks c)))
      (getRoot [] 
	       (prn 'getRoot [])
	       (Pathed. root-name []))
      (isLeaf [node]
	      (prn 'isLeaf [node])
	      (not (map? (get-in @map-ref (path node)))))
      (removeTreeModelListener [l] (swap! listeners #(remove (partial = l) %)))
      (valueForPathChanged [path value]
			   (prn 'valueForPathChanged [path value])
        (dosync
          (alter map-ref update-in (.getLastPathComponent path) (constantly value)))))))

(defmacro tree [& data]
  (let [{action :action :as opts} (apply hash-map data)]
  `(scroll-panel 
    (doto (JTree.)
      ~@(if action
	  [`(add-action-listener ~action)])
      ~@(auto-setters JTree *tree-known-keys* opts)))))