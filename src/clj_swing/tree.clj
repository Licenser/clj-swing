(ns clj-swing.tree
  (:use [clj-swing.core :only [add-action-listener icon-setters auto-setters]]
	[clj-swing panel])
  (:use [clojure.contrib.swing-utils :only [do-swing]])
  (:import (javax.swing JTree)
	   (javax.swing.event TreeSelectionListener TreeSelectionEvent TreeModelEvent)
           (javax.swing.tree TreePath TreeModel)))

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
  (node [t])
  (toString [t]))
 
(deftype Pathed [node name path]
  pPathed
  (toString [this] name)
  (node [this] node)
  (path [this] path))

(defn changed-path [map-a map-b]
  (if (or (= map-a map-b)
	  (not (map? map-a))
	  (not (map? map-b))
	  (not= (keys map-a) (keys map-b)))
    []
    (let [c (filter #(not= (get map-a %) (get map-b %)) (keys map-a))]
      (if (= 1 (count c))
	(let [c (first c)]
	  (cons c (changed-path (get map-a c) (get map-b c))))
	[]))))
	

(defn mapref-tree-model 
  [map-ref root-name &
   {node-wrapper :node-wrapper leaf? :leaf?
    :or {node-wrapper (fn [node path] (Pathed. node (str node) path))
	 leaf? (fn [node map-ref] 
		  (not (map? (get-in @map-ref (path node)))))}}]
  (let [listeners (atom [])
	p
	(proxy [TreeModel] []
	  (addTreeModelListener [l] (swap! listeners conj l))
	  (getChild [node idx]
		    (let [par (get-in @map-ref (path node))
			  ks (keys par)
			  chi (nth ks idx)]
		      (node-wrapper chi (conj (path node) chi))))
	  (getChildCount [node]
			 (count (keys (get-in @map-ref (path node)))))
	  (getIndexOfChild [node chi]
			   (let [c (str chi)
				 ks (keys (get-in @map-ref (path node)))]
			     (index-of ks c)))
	  (getRoot [] 
		   (node-wrapper root-name []))
	  (isLeaf [node]
		  (leaf? node map-ref))
	  (removeTreeModelListener [l] (swap! listeners #(remove (partial = l) %)))
	  (valueForPathChanged 
	   [path value]
	   (dosync
	    (alter map-ref update-in (.getLastPathComponent path) (constantly value)))))]
    (add-watch map-ref (gensym)
	       (fn [k r o n]
		 (let [c (changed-path o n)
		       e (TreeModelEvent. p (TreePath. (node-wrapper(get-in @map-ref c) c)))]
		   (doseq [l @listeners]
		     (.treeStructureChanged l e)))))
	       
    p))

(defmacro tree [& data]
  (let [{[[old-path new-path] & code] :action :as opts} (apply hash-map data)]
  `(scroll-panel 
    (doto (JTree.)
      ~@(if code
	  [`(.addTreeSelectionListener
	     (proxy [TreeSelectionListener] []
	       (valueChanged [e#]
			     (do-swing
			     (let [~new-path (if-let [p# (.getNewLeadSelectionPath e#)]
					       (path (.getLastPathComponent p#))
					       nil)
				   ~old-path (if-let [p# (.getOldLeadSelectionPath e#)]
					       (path (.getLastPathComponent p#))
					       nil)]
				 ~@code)))))])
    ~@(auto-setters JTree *tree-known-keys* opts)))))