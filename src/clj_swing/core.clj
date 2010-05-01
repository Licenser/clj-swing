(ns clj-swing.core
  (:import (java.awt.event ActionListener))
  (:require [clojure.contrib.string :as st]))

(defn kw-to-setter [kw]
  (symbol (apply str "set" (map st/capitalize (st/split #"-" (name kw))))))

(defn remove-known-keys [m ks]
  (reduce dissoc m ks))

(defn icon-setters [names opts]
  (remove 
   nil?
   (map
    (fn [name] 
      (when-let [icon (opts name)] 
	`(.  ~(kw-to-setter name) (.getImage (ImageIcon. ~icon))))) names)))

(defn auto-setters [cl known-kws opts]
  (map (fn [[a v]] (list 
		    '. 
		    (kw-to-setter a) 
		    (if (keyword? v)
		      `(. ~cl ~(symbol (st/upper-case (name v))))
		      v)))
       (remove-known-keys opts known-kws)))

(defmacro add-action-listener [obj [[event] & code]]
  `(doto ~obj
     (.addActionListener
      (proxy [ActionListener] []
	(actionPerformed [~event]
			 ~@code)))))
(comment
(import '(javax.swing JFrame JLabel JTextField JButton JComboBox JPanel Timer)
	'(java.awt.event ActionListener)
	'(java.awt GridBagLayout GridLayout GridBagConstraints))



(defmacro set-text! [component text]
  `(. ~component setText ~text))




(defn selected-item [obj]
  (.getSelectedItem obj))

(defmacro combo-box [[& items] & actions]
  `(doto (JComboBox.)
     ~@(map #(list '.addItem %) items)
     ~@actions))

(defmacro panel [& args]
  `(JPanel. ~@args))
)