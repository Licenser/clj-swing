(ns clj-swing.core
  (:import (java.awt.event ActionListener)
	   (javax.swing ImageIcon))
  (:require [clojure.contrib.string :as st]))

(defn kw-to-setter [kw]
  (symbol (apply str "set" (map st/capitalize (st/split #"-" (name kw))))))

(defn remove-known-keys [m ks]
  (reduce dissoc m ks))

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

(defmacro add-action-listener [obj [[event] & code]]
  `(doto ~obj
     (.addActionListener
      (proxy [ActionListener] []
	(actionPerformed [~event]
			 ~@code)))))
(comment

(defmacro panel [& args]
  `(JPanel. ~@args))
)