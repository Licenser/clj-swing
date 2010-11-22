(ns clj-swing.combo-box
  (:use [clj-swing.core]
	[clojure.contrib.swing-utils :only [do-swing]])
  (:import (javax.swing JComboBox ComboBoxModel MutableComboBoxModel)
	   (javax.swing.event ListDataEvent ListDataListener)))

(def *cb-known-keys* [:action])

(defmacro commbo-box-model [& {mutable :mutable
			       [[] & size-code] :size
			       [[get-idx] & get-code] :get
			       [[a-l-l] & add-listener-code] :add-listener
			       [[r-l-l] & remove-listener-code] :remove-listener
			       
			       [[] & get-selected-item-code] :get-selected-item
			       [[s-s-i-item] & set-selected-item-code] :set-selected-item

			       [[add-item] & add-code] :add 
			       [[add-at-item add-at-idx] & add-at-code] :add-at 
			       [[remove-item] & remove-code] :remove 
			       [[remove-idx] & remove-at-code] :remove-at 

}]
  `(proxy [~(if mutable 'MutableComboBoxModel 'ComboBoxModel)] []

;; Code for List model
     (getSize []
	      ~@size-code)
     (getElementAt [~get-idx]
		   ~@get-code)
     (addListDataListener [~a-l-l]
			  ~@add-listener-code)
     (removeListDataListener [~r-l-l]
			  ~@remove-listener-code)

;; Code for Combobox Model
     (getSelectedItem []
		      ~@get-selected-item-code)
     (setSelectedItem [~s-s-i-item]
		      ~@set-selected-item-code)

;; Code for mutable combobox model
     ~@(if mutable
       [
	`(addElement [~add-item] 
		     ~@add-code)
	`(insertElementAt [~add-at-item ~add-at-idx] 
		     ~@add-at-code)
	`(removeElement [~remove-item] 
		     ~@remove-code)
	`(removeElementAt [~remove-idx] 
		     ~@remove-at-code)])))

(defn seq-ref-combobox-model [seq-ref & [selected]]
  (let [selected (or selected (atom nil))
	listeners (atom #{})
	key (gensym "seq-ref-combobox-model-watch")
	m (commbo-box-model
	   :mutable true
	   :size ([] (count @seq-ref))
	   :get-selected-item ([] (dosync (if (and @selected (some #(= @selected %) @seq-ref)) @selected (swap! selected (constantly nil)))))
	   :set-selected-item ([i] (dosync (if (and i (some #(= i %) @seq-ref)) (swap! selected (constantly i)) (swap! selected (constantly nil)))))
	   :add-listener ([l] (swap! listeners conj l))
	   :remove-listener ([l] (swap! listeners disj l))
	   :get ([i] (if (has-index? @seq-ref i) (nth @seq-ref i) nil))
	   :add ([itm] (dosync (alter seq-ref conj itm)))
	   :add-at ([itm idx] (dosync 
			       (if (vector? @seq-ref)
				 (alter seq-ref #(vec (insert-at % idx itm)))
				 (alter seq-ref insert-at idx itm))))
	   :remove ([itm] (dosync 
			       (if (vector? @seq-ref)
				 (alter seq-ref #(vec (remove (partial = itm) %)))
				 (alter seq-ref #(remove (partial = itm) %)))))
	   :remove-at ([idx] (dosync 
			       (if (vector? @seq-ref)
				 (alter seq-ref #(vec (drop-nth % idx)))
				 (alter seq-ref drop-nth idx)))))]
    (add-watch seq-ref key 
		    (fn [_ _ _ state]
		      (do-swing
		       (let [m (ListDataEvent. m (ListDataEvent/CONTENTS_CHANGED) 0 (count state))]
			(doseq [l @listeners]
			  (.contentsChanged l m))))))
    m))
    
     


(defmacro combo-box [items & {action :action :as opts}]
  `(let [cb# (JComboBox.)]
     (doto cb#
       ~@(if action  
	   [`(add-action-listener ~action)])
       ~@(auto-setters JComboBox *cb-known-keys* opts))
     (doseq [item# ~items] (.addItem cb# item#))
     cb#))

(defn selected-item [obj]
  (.getSelectedItem obj))


;; TODO Add list cell renderer proxy stuff