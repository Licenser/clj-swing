(ns clj-swing.list
  (:use [clj-swing core panel])
  (:import (javax.swing JList ListModel)
	   (javax.swing.event ListDataEvent ListDataListener ListSelectionListener)))

(def *list-known-keys* [:action :on-selection-change])


(defmacro add-list-selection-listener [obj [[event] & code]]
  `(doto ~obj
     (.addListSelectionListener
      (proxy [ListSelectionListener] []
	(valueChanged [~event]
			 ~@code)))))

(defmacro list-model [& {[[] & size-code] :size
			 [[get-idx] & get-code] :get
			 [[a-l-l] & add-listener-code] :add-listener
			 [[r-l-l] & remove-listener-code] :remove-listener}]
  `(proxy [ListModel] []
     (getSize []
	      ~@size-code)
     (getElementAt [~get-idx]
		   ~@get-code)
     (addListDataListener [~a-l-l]
			  ~@add-listener-code)
     (removeListDataListener [~r-l-l]
			     ~@remove-listener-code)))


(defn seq-ref-list-model [seq-ref]
  (let [listeners (atom #{})
	key (gensym "seq-ref-list-model-watch")
	m (list-model
	   :size ([] (count @seq-ref))
	   :add-listener ([l] (swap! listeners conj l))
	   :remove-listener ([l] (swap! listeners disj l))
	   :get ([i] (if (has-index? @seq-ref i) (nth @seq-ref i) nil))
	   )]
    (add-watch seq-ref key 
	       (fn [_ _ _ state]
		 (let [m (ListDataEvent. m (ListDataEvent/CONTENTS_CHANGED) 0 (count state))]
		   (doseq [l @listeners]
		     (.contentsChanged l m)))))
    m))

(defmacro jlist [& {action :action on-selection-change :on-selection-change items :items scrolling :scrolling :as opts}]
  (let [l (gensym "jlist")]
  `(let [~l (doto (JList.)
	      ~@(if action  
		  [`(add-action-listener ~action)])
	      ~@(if on-selection-change  
		  [`(add-list-selection-listener ~on-selection-change)])    
	      ~@(auto-setters JList *list-known-keys* opts)
	      ~@(map #(list '.addItem %) items))]
     
     ~@(if scrolling 
	`[(scroll-panel ~l)]
	`[~l]))))

;; TODO Add list cell renderer proxy stuff