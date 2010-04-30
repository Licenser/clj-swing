(ns clj-swing.core)

(import '(javax.swing JFrame JLabel JTextField JButton JComboBox JPanel Timer)
	'(java.awt.event ActionListener)
	'(java.awt GridBagLayout GridLayout GridBagConstraints))

(require '[clojure.contrib.java-utils :as java])

(defmulti add 
  "Adds to an object" 
  [target objs]
  type)

(defmacro combo-box [[& items] & actions]
  `(doto (JComboBox.)
     ~@(map #(list '.addItem %) items)
     ~@actions)))

(defn selected-item [obj]
  (.getSelectedItem obj))

(defmacro set-text! [component text]
  `(. ~component setText ~text))

(defmacro add-action-listener [obj [[event] & code]]
  `(doto ~obj
     (.addActionListener
      (proxy [ActionListener] []
	(actionPerformed [~event]
			 ~@code)))))

(defmacro button [n caption listener & actions]
  `(let [~n (JButton. ~caption)]
     (add-action-listener ~n ~listener)
     (doto ~n
       ~@actions)))

(defmacro panel [& args]
  `(JPanel. ~@args))
