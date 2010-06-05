(ns clj-swing.core
  (:use [clojure.contrib.swing-utils :only [do-swing]])
  (:import (java.awt.event ActionListener)
	   (javax.swing ImageIcon)))

(try
  (require '[clojure.contrib.string :as st])
   (catch Exception e (require '[clojure.contrib.str-utils2 :as st])))

(defn kw-to-setter [kw]
  (symbol (apply str "set" (map st/capitalize (st/split #"-" (name kw))))))

(defn group-container-args [args]
  (reduce 
   (fn [{options :options kw :kw state :state :as r} arg]
     (cond
      (= state :forms)
      (update-in r [:forms] conj arg)
      kw
      (assoc r :options (assoc options kw arg) :kw nil)
      (keyword? arg)
      (assoc r :kw arg)
      (vector? arg)
      (assoc r :bindings arg :state :forms)))
   {:options {} :kw nil :state :options :forms []} args))

(defn remove-known-keys [m ks]
  (reduce dissoc m ks))

(defn has-index? [seq idx]
  (and (>= idx 0) (< idx (count seq))))

(defn icon-setters [names opts]
  (if opts
    (remove 
     nil?
     (map
      (fn [name] 
	(when-let [icon (opts name)] 
	  `(.  ~(kw-to-setter name) (.getImage (ImageIcon. ~icon))))) names))))

(defn auto-setters [cl known-kws opts]
  (if opts
    (map (fn [[a v]] (list 
		      '. 
		      (kw-to-setter a) 
		      (if (keyword? v)
			`(. ~cl ~(symbol (st/upper-case (name v))))
			v)))
	 (remove-known-keys opts known-kws))))

(defn insert-at [seq idx item]
  (concat (take idx seq) [item] (drop idx seq)))

(defn drop-nth [seq idx]
  (concat (take idx seq) (drop (inc idx) seq)))

(defmacro add-listener [obj add-fn listener-class & events]
  `(doto ~obj
     (~add-fn
      (proxy [~listener-class] []
	~@events))))

(defmacro add-action-listener [obj [[event] & code]]
  `(add-listener ~obj .addActionListener ActionListener
	(actionPerformed [~event]
			  ~@code)))


(defn <3 [love & loves] 
  (loop [l (str "I love " love) loves loves]
    (let [[love & loves] loves]
      (if (nil? love)
	(str l ".")
	(if (empty? loves)
	  (str l " and " love ".")
	  (recur (str l ", " love) loves))))))
