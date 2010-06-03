(ns clj-swing.tree
  (:use [clj-swing.core :only [add-action-listener icon-setters auto-setters]]
	[clj-swing panel])
  (:use [clojure.contrib.swing-utils :only [do-swing]])
  (:import (javax.swing JTree)
	   (javax.swing.event TreeSelectionListener TreeSelectionEvent TreeModelEvent)
           (javax.swing.tree TreePath TreeModel)))

(def *tree-known-keys* [:action :name])

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
  [map-ref root-node &
   {node-wrapper :node-wrapper leaf? :leaf?
    :or {node-wrapper (fn [node path] (Pathed. node (str node) path))
	 leaf? (fn [node map-ref] 
		  (not (map? (get-in @map-ref (path node)))))}}]
  (let [listeners (atom [])
	root (node-wrapper root-node [])
	model
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
		   root)
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
		       p (loop [ps [] c c]
			   (if (empty? c)
			     (reverse (conj ps root))
			     (recur (conj ps (node-wrapper (last c) (vec c))) (butlast c))))
		       _ (prn 1 p (map path p))
		       tp (TreePath. (to-array p))
		       _ (prn 2 tp)
		       e (TreeModelEvent. model tp)]
		   (doseq [l @listeners]
		     (prn l)
		     (.treeStructureChanged l e)))))
    model))

(defmacro tree [& {[[old-path new-path] & code] :action n :name :as opts }]
  (let [n (or n (gensym "tree"))]
    `(scroll-panel
      (let [~n (new JTree)]
	(doto ~n
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
	  ~@(auto-setters JTree *tree-known-keys* opts))))))