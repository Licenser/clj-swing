(ns clj-swing.tree
  (:use [clj-swing.core :only [add-action-listener add-listener icon-setters auto-setters]]
	[clj-swing panel]
	clojure.set)
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
	  (getIndexOfChild [n chi]
			   (let [ks (keys (get-in @map-ref (path n)))]
			     (index-of ks (node chi))))
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
		 (do-swing
		  (let [c (changed-path n o)
			k-n (set (keys (get-in n c)))
			k-o (set (keys (get-in o c)))
			only-n (difference k-n k-o)
			only-o (difference k-o k-n)
			_ (prn 1 only-n only-o)
			node-changed (and 
				      (= 1 (count only-n) (count only-o))
				      (= (get-in n (concat c only-n))
					 (get-in o (concat c only-o))))
;			c (if node-changed (concat c only-n) c)
			p (loop [ps [] c c]
			    (if (empty? c)
			      (reverse (conj ps root))
			      (recur (conj ps (node-wrapper (last c) (vec c))) (butlast c))))
			tp (TreePath. (to-array p))
			e (TreeModelEvent. model tp)]
		    (doseq [l @listeners]
		      (if node-changed
			(do
			  (.treeStructureChanged l e)
			  )
			(.treeStructureChanged l e)))))))
    model))

(defmacro tree [& {[[old-path new-path] & code] :action n :name :as opts }]
  (let [n (or n (gensym "tree"))]
    `(scroll-panel
      (let [~n (new JTree)]
	(doto ~n
	  ~@(if code
	      [`(add-listener .addTreeSelectionListener TreeSelectionListener
		   (valueChanged [e#]
				 (do-swing
				  (let [~new-path (if-let [p# (.getNewLeadSelectionPath e#)]
						    (path (.getLastPathComponent p#))
						    nil)
					~old-path (if-let [p# (.getOldLeadSelectionPath e#)]
						    (path (.getLastPathComponent p#))
						    nil)]
				    ~@code))))])
	  ~@(auto-setters JTree *tree-known-keys* opts))))))

{{:name "Netmask", :good true} "255.255.255.240", {:name "Network", :good true} "10.64.130.176", {:name "Router", :good false, :data {:SubnetMask "255.255.255.240", :SubnetIpAddress "10.64.130.176", :SubnetName "DMZ_ctrl(Cho)", :SubnetTypeName "DMZ_ctrl"}, :matcher :IpAddress, :cause true, :leaf true} nil}
{{:good true, :name "Router"} "123", {:name "Netmask", :good true} "255.255.255.240", {:name "Network", :good true} "10.64.130.176"}